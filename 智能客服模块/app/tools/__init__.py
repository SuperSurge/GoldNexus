"""函数工具统一注册与执行。

4 个工具对应验收清单：
  search_loan_products  -> 借款金额/期限 -> 命中真实产品
  calculate_repayment   -> 借 X 元 Y 期利率 Z -> 数值正确
  query_loan_status     -> 查订单进度
  transfer_to_human     -> 情绪投诉/明确要求 -> 转人工
"""
from app.tools.products import search_loan_products
from app.tools.repayment import calculate_repayment
from app.tools.order import query_loan_status, transfer_to_human

# 传给 llm.bind_tools(TOOLS) 的工具列表
TOOLS = [search_loan_products, calculate_repayment, query_loan_status, transfer_to_human]


def execute_tool(tool_call: dict) -> str:
    """执行一次工具调用，返回字符串结果（给模型看的自然语言/JSON）。

    tool_call 是 langchain 返回的 {"name": ..., "args": {...}, "id": ...}，
    这里按 name 找到真实函数，用 t.invoke(args) 解包参数执行。
    """
    name = tool_call.get("name", "")
    args = tool_call.get("args", {}) or {}
    for t in TOOLS:
        if t.name == name:
            try:
                return str(t.invoke(args))
            except Exception as e:  # 参数没填对时别让整个流程崩，回错误给模型纠正
                return f"工具 {name} 调用失败：{e}"
    return f"未知工具：{name}"
