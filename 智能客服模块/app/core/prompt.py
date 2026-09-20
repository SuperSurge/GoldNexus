"""Prompt 模板与组装。

系统提示词是「金融防幻觉」的核心落点：把「关键数据只能来自工具/知识库」这条
硬约束写死在系统提示里，模型才不敢凭空编利率、额度、放款时间。
"""
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage

SYSTEM_PROMPT = """你是【金纽贷（GoldNexus）多数据源融合第三方贷款平台】的智能客服，只负责贷款相关咨询。

【核心约束：金融合规 / 防幻觉】
1. 额度、利率、期限、费用、放款时间、月供、利息等关键数据，只能来自两个来源：
   - 工具调用的返回结果（查产品 / 算息 / 查单）；
   - 知识库检索到的内容（【参考资料】）。
2. 严禁凭空编造任何数字；没有可靠来源时，明确告知「请以平台实际审批为准」，或建议转人工。
3. 不承诺「百分百通过」，不夸大宣传，不编造规则。

【服务边界】
4. 只回答贷款相关问题；遇到无关话题（写代码、聊天气等）礼貌拒绝，并引导回贷款话题。
5. 涉及违法用途（赌博、炒股、购房投资、虚拟币等）时明确拒绝。

【回答规范】
6. 专业、简洁、礼貌、合规；涉及计算时以工具结果为准，不要自己心算。
7. 引用知识库内容时，可说明「根据平台规则」，保持来源可溯。

【转人工】
8. 用户明确要求转人工、或表达强烈不满 / 投诉 / 情绪激动时，调用 transfer_to_human 工具。
"""


def _profile_text(profile: dict) -> str:
    """把画像 dict 转成可读文本，避免把 None 也拼进去。"""
    items = [f"{k}：{v}" for k, v in profile.items() if v]
    return "\n".join(items)


def _history_text(history: list[dict]) -> str:
    lines = []
    for turn in history:
        lines.append(f"用户：{turn.get('user', '')}")
        lines.append(f"客服：{turn.get('assistant', '')}")
    return "\n".join(lines)


def build_tool_decision_messages(profile: dict, history: list[dict], message: str) -> list:
    """第一轮：让模型决定「要不要调工具、传什么参数」。

    拼入系统提示 + 画像 + 历史 + 当前问题；模型若判断需要工具，会返回 tool_calls。
    """
    msgs = [SystemMessage(content=SYSTEM_PROMPT)]
    if profile:
        msgs.append(HumanMessage(content=f"【用户画像】\n{_profile_text(profile)}"))
    for turn in history:
        msgs.append(HumanMessage(content=turn.get("user", "")))
        msgs.append(AIMessage(content=turn.get("assistant", "")))
    msgs.append(HumanMessage(content=message))
    return msgs


def build_final_prompt(
    profile: dict,
    history: list[dict],
    kb: list,
    tool_results: list[str],
    message: str,
) -> str:
    """最终生成：拼接 画像 + 历史 + 工具结果 + 知识库 + 当前问题，返回一段完整 prompt。

    工具结果和知识库都是「可信来源」，模型据此作答，避免幻觉。
    """
    parts = []
    if profile:
        parts.append(f"【用户画像】\n{_profile_text(profile)}")
    if history:
        parts.append(f"【对话历史】\n{_history_text(history)}")
    if tool_results:
        parts.append("【工具返回结果】\n" + "\n\n".join(tool_results))
    if kb:
        kb_text = "\n\n".join(f"[资料{i+1}] {d.page_content.strip()}" for i, d in enumerate(kb))
        parts.append(f"【参考资料（平台知识库检索）】\n{kb_text}")
    parts.append(f"【用户当前问题】\n{message}")
    return "\n\n".join(parts)
