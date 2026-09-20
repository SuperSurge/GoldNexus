"""主流程编排。

完整链路（对应文档 §3）：
  用户消息
    → ① 加载记忆：会话历史 + 用户画像
    → ② LLM 判断是否调工具(bind_tools)：需要 → 执行(查产品/算息/查单/转人工)
    → ③ RAG 检索：召回 top-k 候选
    → ③.5 Reranker 精排：取 top-n 进 Prompt
    → ④ 组装 Prompt：系统约束 + 画像 + 历史 + 检索知识 + 工具结果
    → ⑤ 生成回复（SSE 流式）
    → ⑥ 写入记忆：追加本轮对话 / 更新画像

两个入口：
  run_core(...)  —— 非流式，返回完整轨迹 dict，供评估/测试（scripts/evaluate.py）
  run(...)       —— 流式 async generator，供 /chat SSE 接口
"""
import json
import re

from langchain_core.messages import HumanMessage, SystemMessage

from app.config import settings
from app.core.llm import get_llm, llm
from app.core.prompt import (
    SYSTEM_PROMPT,
    build_final_prompt,
    build_tool_decision_messages,
)
from app.memory import memory as mem
from app.rag.rerank import rerank
from app.rag.store import retrieve
from app.tools import TOOLS, execute_tool

# 绑定工具后的 LLM：模型会返回 tool_calls 而不是直接作答
llm_with_tools = llm.bind_tools(TOOLS)

_PROFILE_EXTRACT_PROMPT = """从下面的对话里抽取用户画像信息，输出 JSON。

字段（没有的填 null，不要编造）：借款金额、期限偏好、征信等级、联系方式、职业收入

【当前已知画像】
{current}

【本轮对话】
用户：{user_msg}
客服：{assistant_msg}

只输出 JSON，不要多余文字。"""


def _parse_json(text: str) -> dict:
    """尽量把模型输出剥成 JSON（去掉 ```json 围栏 / 前后废话）。"""
    text = (text or "").strip()
    text = re.sub(r"^```(json)?\s*", "", text, flags=re.IGNORECASE)
    text = re.sub(r"\s*```$", "", text)
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        m = re.search(r"\{.*\}", text, re.DOTALL)
        if m:
            try:
                return json.loads(m.group(0))
            except json.JSONDecodeError:
                pass
    return {}


def _extract_profile(current_profile: dict, user_msg: str, assistant_msg: str) -> dict:
    """用 LLM 从本轮对话抽取画像字段，合并进已有画像；失败则原样返回。"""
    try:
        prompt = _PROFILE_EXTRACT_PROMPT.format(
            current=json.dumps(current_profile, ensure_ascii=False),
            user_msg=user_msg,
            assistant_msg=assistant_msg,
        )
        resp = llm.invoke([HumanMessage(content=prompt)])
        data = _parse_json(resp.content)
        merged = dict(current_profile)
        for k, v in data.items():
            if v:
                merged[k] = str(v)
        return merged
    except Exception:
        return current_profile


def _decide_and_execute_tools(profile: dict, history: list[dict], message: str):
    """② 工具判断 + 执行。返回 (tool_calls, tool_results, transfer_human)。"""
    tool_calls = []
    tool_results = []
    transfer_human = False
    try:
        ai = llm_with_tools.invoke(build_tool_decision_messages(profile, history, message))
        for tc in ai.tool_calls or []:
            tool_calls.append({"name": tc.get("name"), "args": tc.get("args", {})})
            if tc.get("name") == "transfer_to_human":
                transfer_human = True
            tool_results.append(execute_tool(tc))
    except Exception as e:
        tool_results.append(f"工具调用异常：{e}")
    return tool_calls, tool_results, transfer_human


def _retrieve_kb(message: str) -> list:
    """③ + ③.5 RAG：多召回 + reranker 精排。未建库/模型不可用时降级为空。"""
    try:
        candidates = retrieve(message, k=settings.rag_top_k)
        return rerank(message, candidates, top_n=settings.rag_rerank_top_n)
    except Exception:
        return []


def _remember(session_id: str, user_id: str, message: str, answer: str, profile: dict) -> None:
    """⑥ 写记忆：追加短期历史 + 抽取更新长期画像。"""
    mem.save_turn(session_id, message, answer)
    try:
        mem.save_profile(user_id, _extract_profile(profile, message, answer))
    except Exception:
        pass


def run_core(session_id: str, user_id: str, message: str, remember: bool = True) -> dict:
    """非流式完整执行一次，返回轨迹 dict（供评估/测试）。

    remember=False 时跳过画像抽取（评估时省 token/时间）。
    """
    history = mem.load_history(session_id)
    profile = mem.load_profile(user_id)

    tool_calls, tool_results, transfer_human = _decide_and_execute_tools(profile, history, message)

    if transfer_human:
        answer = "已为您转接人工客服，人工坐席将尽快接入，请稍候。"
        mem.save_turn(session_id, message, answer)
        return {
            "transfer_human": True, "tool_calls": tool_calls,
            "tool_results": tool_results, "kb": [], "answer": answer,
        }

    kb = _retrieve_kb(message)
    final_prompt = build_final_prompt(profile, history, kb, tool_results, message)
    try:
        resp = llm.invoke(
            [SystemMessage(content=SYSTEM_PROMPT), HumanMessage(content=final_prompt)]
        )
        answer = resp.content or ""
    except Exception as e:
        answer = f"[生成异常：{e}]"

    if remember:
        _remember(session_id, user_id, message, answer, profile)

    return {
        "transfer_human": False, "tool_calls": tool_calls,
        "tool_results": tool_results, "kb": kb, "answer": answer,
    }


async def run(session_id: str, user_id: str, message: str):
    """流式主流程（async generator），供 /chat SSE 接口。

    逐条 yield 事件：
      {"event": "token",         "data": "文本片段"}   # 流式回复
      {"event": "transfer_human","data": "转人工提示"}  # 触发转人工
      {"event": "done",          "data": ""}            # 结束
    """
    # ① 加载记忆
    history = mem.load_history(session_id)
    profile = mem.load_profile(user_id)

    # ② 工具判断 + 执行
    tool_calls, tool_results, transfer_human = _decide_and_execute_tools(profile, history, message)

    # 触发转人工：直接返回，不再生成
    if transfer_human:
        msg = "已为您转接人工客服，人工坐席将尽快接入，请稍候。"
        mem.save_turn(session_id, message, msg)
        yield {"event": "transfer_human", "data": msg}
        yield {"event": "done", "data": ""}
        return

    # ③ + ③.5 RAG
    kb = _retrieve_kb(message)

    # ④ 组装 Prompt + ⑤ 流式生成
    final_prompt = build_final_prompt(profile, history, kb, tool_results, message)
    stream_llm = get_llm(streaming=True)
    answer_parts = []
    try:
        async for chunk in stream_llm.astream(
            [SystemMessage(content=SYSTEM_PROMPT), HumanMessage(content=final_prompt)]
        ):
            token = chunk.content or ""
            if token:
                answer_parts.append(token)
                yield {"event": "token", "data": token}
    except Exception as e:
        yield {"event": "token", "data": f"\n[生成异常：{e}]"}
    answer = "".join(answer_parts)

    # ⑥ 写记忆
    _remember(session_id, user_id, message, answer, profile)

    yield {"event": "done", "data": ""}
