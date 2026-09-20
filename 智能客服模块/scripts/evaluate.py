# -*- coding: utf-8 -*-
"""
evaluate.py —— 评估智能客服模块（P6 评估）

定一批测试题 → 跑 run_core 抓完整轨迹（工具/参数/答案）→ 两层打分：
  ① 规则打分（客观、免费）：工具调没调对、参数对不对、答案里有没有该出现的数字/关键词
  ② LLM 裁判（主观、多花 token）：有没有编造、是否切题、整体质量

为什么这么分（面试必讲）：
  「工具调对了吗 / 参数对不对 / 数字对不对」有标准答案，规则就能判，零成本；
  「有没有编造」「有没有跑题」没有固定答案，只能再拉一个模型当裁判（LLM-as-Judge）。

运行（在项目根目录 智能客服模块/ 下，用项目 venv 的 python）：
    python scripts/evaluate.py
结果存 scripts/eval_results.json。
"""
import json
import os
import re
import sys
import time

# 把项目根目录加进 import 路径，确保能 import app
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, PROJECT_ROOT)

from app.core.chain import run_core  # noqa: E402
from app.core.llm import get_llm  # noqa: E402


# ====================== 等额本息计算器（和 repayment.py 里的公式一致） ======================
def loan_payment(principal, months, year_rate=14.8):
    """算期望月供，用来核对答案里的数字对不对。"""
    rate = year_rate / 100 / 12
    payment = principal * rate * (1 + rate) ** months / ((1 + rate) ** months - 1)
    return round(payment, 2)


# ====================== 测试集 ======================
# expected_tool   : 期望调用的工具名；None 表示「不该调任何工具」
# expected_args   : 期望的关键参数（数值比对；字符串模糊包含）
# expected_answer : 答案里该出现的 关键词(contains) / 数字(numbers，2位小数核对)
TESTS = [
    {
        "id": 1, "name": "查产品",
        "question": "我想借 30000 元，分 12 期，征信良好，有什么产品？",
        "expected_tool": "search_loan_products",
        "expected_args": {"amount": 30000, "term_months": 12},
        "expected_answer": {"contains": ["产品"]},
    },
    {
        "id": 2, "name": "算月供（等额本息）",
        "question": "借 30000 元，分 12 期，年利率 12%，等额本息，帮我算每月还多少",
        "expected_tool": "calculate_repayment",
        "expected_args": {"principal": 30000, "months": 12, "annual_rate": 12},
        "expected_answer": {"contains": ["月供"], "numbers": [loan_payment(30000, 12, 12)]},
    },
    {
        "id": 3, "name": "查订单进度",
        "question": "帮我查一下订单 GX20240901001 的进度",
        "expected_tool": "query_loan_status",
        "expected_args": {"order_id": "GX20240901001"},
        "expected_answer": {"contains": ["状态"]},
    },
    {
        "id": 4, "name": "政策类 RAG",
        "question": "如果逾期了会怎么样？",
        "expected_tool": None,
        "expected_args": {},
        "expected_answer": {"contains": ["征信"]},
    },
    {
        "id": 5, "name": "情绪投诉转人工",
        "question": "我要投诉你们，马上给我转人工！",
        "expected_tool": "transfer_to_human",
        "expected_args": {},
        "expected_answer": {"contains": ["人工"]},
    },
    {
        "id": 6, "name": "兜底聊天",
        "question": "你好，你是谁？",
        "expected_tool": None,
        "expected_args": {},
        "expected_answer": {"contains": ["贷款"]},
    },
    {
        "id": 7, "name": "越界请求",
        "question": "帮我写一段 Python 爬虫代码",
        "expected_tool": None,
        "expected_args": {},
        "expected_answer": {"contains": ["贷款"]},
    },
]


# ====================== 规则打分（免费、客观） ======================
def args_match(expected, actual):
    """只比对期望里列出的字段；数值用 1e-6 容差，字符串模糊包含。"""
    for k, v in expected.items():
        if k not in actual:
            return False
        av = actual[k]
        if isinstance(v, (int, float)):
            try:
                if abs(float(av) - float(v)) > 1e-6:
                    return False
            except (TypeError, ValueError):
                return False
        else:
            sv, sa = str(v).lower(), str(av).lower()
            if sv not in sa and sa not in sv:
                return False
    return True


def norm(s):
    """去掉千分位逗号，让 '2,665.52' 和 '2665.52' 能匹配上。"""
    return s.replace(",", "")


def rule_score(test, trace):
    called_names = [c["name"] for c in trace["tool_calls"]]

    # 1) 工具：期望 None → 不该调任何工具；期望某工具 → 关键工具必须调到
    if test["expected_tool"] is None:
        tool_ok = len(called_names) == 0
        tool_note = f"应不调工具，实际调了 {called_names}" if called_names else "未调工具 ✓"
    else:
        tool_ok = test["expected_tool"] in called_names
        tool_note = f"期望 {test['expected_tool']}，实际调了 {called_names}"

    # 2) 参数：找到期望工具那次调用，比对参数
    args_ok = None
    args_note = "—"
    if test["expected_tool"] and test["expected_args"]:
        for c in trace["tool_calls"]:
            if c["name"] == test["expected_tool"]:
                args_ok = args_match(test["expected_args"], c.get("args", {}))
                args_note = f"参数 {c.get('args')}"
                break
        if args_ok is None:
            args_ok = False
            args_note = "没找到该工具调用"

    # 3) 答案规则：归一化千分位，比对 contains 关键词 + numbers 数字
    ans = norm(trace["answer"])
    contains_ok = all(s in ans for s in test["expected_answer"].get("contains", []))
    nums_ok = all(f"{n:.2f}" in ans for n in test["expected_answer"].get("numbers", []))
    answer_ok = contains_ok and nums_ok
    answer_note = "关键词/数字齐全 ✓" if answer_ok else (
        f"缺关键词 {[s for s in test['expected_answer'].get('contains', []) if s not in ans]}"
        + (f"；缺数字 {[f'{n:.2f}' for n in test['expected_answer'].get('numbers', []) if f'{n:.2f}' not in ans]}"
           if not nums_ok else "")
    )

    return tool_ok, args_ok, answer_ok, {"tool": tool_note, "args": args_note, "answer": answer_note}


# ====================== LLM 裁判（付费、主观） ======================
_judge_llm = None


def llm_judge(question, tool_calls, tool_results, answer):
    global _judge_llm
    if _judge_llm is None:
        _judge_llm = get_llm()  # 复用 DeepSeek，temperature 0.3；裁判可用更低的，这里直接复用

    tool_desc = "\n".join(f"- {c['name']}({c.get('args')})" for c in tool_calls) or "（未调用任何工具）"
    result_desc = "\n".join(tool_results) or "（无工具返回）"
    prompt = f"""你是严格的评估裁判，评估一个「贷款智能客服」的一次回答。

【用户问题】{question}
【Agent 调用的工具】{tool_desc}
【工具返回的真实结果】{result_desc}
【Agent 的最终回答】{answer}

从三个角度打分：
1. hallucination（编造）：回答里是否出现「工具返回/知识库里没有」的数字或事实？尤其编造月供/额度/利率。
2. relevant（切题）：是否遵守「只回答贷款相关问题」？对越界请求是否礼貌拒绝而非照做？
3. score：整体质量 1~5 分（5 最好）。

只输出 JSON，不要多余文字，格式：
{{"hallucination": true或false, "hallucination_reason": "一句话", "relevant": true或false, "score": 1到5的整数, "comment": "一句话总评"}}
"""
    try:
        from langchain_core.messages import HumanMessage
        resp = _judge_llm.invoke([HumanMessage(content=prompt)])
        text = resp.content or ""
        text = re.sub(r"^```(json)?\s*", "", text.strip(), flags=re.IGNORECASE)
        text = re.sub(r"\s*```$", "", text)
        return json.loads(text)
    except Exception as e:
        m = re.search(r"\{.*\}", text, re.DOTALL) if 'text' in locals() else None
        if m:
            try:
                return json.loads(m.group(0))
            except Exception:
                pass
        return {"parse_error": str(e)}


# ====================== 主流程 ======================
def main():
    rows = []
    total_latency = 0.0

    print("=" * 70)
    print("评估对象：智能客服模块（run_core，含工具调用 + RAG + 记忆）")
    print(f"测试题：{len(TESTS)} 条   |   打分：规则（工具/参数/答案） + LLM 裁判（编造/切题）")
    print("=" * 70)

    for test in TESTS:
        q = test["question"]
        session_id = f"eval_{test['id']}"

        t0 = time.perf_counter()
        # remember=False：评估时不写画像（省 token/时间），也避免画像影响后续题目
        trace = run_core(session_id, "eval_user", q, remember=False)
        latency = time.perf_counter() - t0
        total_latency += latency

        tool_ok, args_ok, answer_ok, notes = rule_score(test, trace)
        judge = llm_judge(q, trace["tool_calls"], trace["tool_results"], trace["answer"])

        print(f"\n【{test['id']}】{test['name']}")
        print(f"  问：{q}")
        print(f"  工具：{notes['tool']}   {'✓' if tool_ok else '✗'}")
        if args_ok is not None:
            print(f"  参数：{notes['args']}   {'✓' if args_ok else '✗'}")
        print(f"  答案(规则)：{notes['answer']}   {'✓' if answer_ok else '✗'}")
        print(f"  答案(截断)：{trace['answer'][:80]}{'…' if len(trace['answer']) > 80 else ''}")
        if judge and "parse_error" not in judge:
            h = "编造" if judge.get("hallucination") else "无编造"
            r = "切题" if judge.get("relevant") else "跑题"
            print(f"  裁判：{h} | {r} | 质量 {judge.get('score')}/5")
            print(f"        评语：{judge.get('comment', '')}")
        else:
            print(f"  裁判：解析失败 {judge}")

        rows.append({
            "id": test["id"], "name": test["name"], "question": q,
            "tool_calls": trace["tool_calls"],
            "answer": trace["answer"],
            "tool_ok": tool_ok, "args_ok": args_ok, "answer_ok": answer_ok,
            "judge": judge,
            "latency": round(latency, 2),
        })

    # 汇总
    n = len(rows)
    tool_acc = sum(1 for r in rows if r["tool_ok"]) / n
    ans_acc = sum(1 for r in rows if r["answer_ok"]) / n
    args_rows = [r for r in rows if r["args_ok"] is not None]
    args_acc = sum(1 for r in args_rows if r["args_ok"]) / len(args_rows) if args_rows else 0
    scores = [r["judge"].get("score") for r in rows if r["judge"] and "parse_error" not in r["judge"]]
    valid_scores = [s for s in scores if isinstance(s, (int, float))]  # 裁判偶尔漏给 score，过滤 None
    hallucination_count = sum(1 for r in rows if r["judge"] and r["judge"].get("hallucination"))
    avg_score = sum(valid_scores) / len(valid_scores) if valid_scores else 0

    print("\n" + "=" * 70)
    print("汇总报告")
    print("=" * 70)
    print(f"  工具调用正确率   ：{tool_acc:.0%}  ({sum(1 for r in rows if r['tool_ok'])}/{n})")
    print(f"  参数解析正确率   ：{args_acc:.0%}  ({sum(1 for r in args_rows if r['args_ok'])}/{len(args_rows)})")
    print(f"  答案规则通过率   ：{ans_acc:.0%}  ({sum(1 for r in rows if r['answer_ok'])}/{n})")
    print(f"  裁判平均质量分   ：{avg_score:.1f}/5")
    print(f"  编造案例数       ：{hallucination_count}")
    print(f"  总耗时           ：{total_latency:.1f}s（平均 {total_latency / n:.1f}s/题）")

    out_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "eval_results.json")
    report = {
        "summary": {
            "tool_accuracy": round(tool_acc, 4),
            "args_accuracy": round(args_acc, 4),
            "answer_accuracy": round(ans_acc, 4),
            "avg_judge_score": round(avg_score, 2),
            "hallucination_count": hallucination_count,
            "total_latency_s": round(total_latency, 2),
        },
        "cases": rows,
    }
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    print(f"\n详细结果已存：{out_path}")


if __name__ == "__main__":
    main()
