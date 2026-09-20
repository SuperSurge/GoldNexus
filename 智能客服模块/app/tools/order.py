"""查借款进度 / 账户，以及转人工。"""
from langchain_core.tools import tool


@tool
def query_loan_status(order_id: str) -> str:
    """查询借款申请进度 / 放款状态。

    Args:
        order_id: 借款订单号
    """
    order_id = (order_id or "").strip()
    if not order_id:
        return "请提供订单号后再查询。"

    # 模拟：真实环境应调用后端 /order/{order_id} 接口。
    # 用订单号字符和取模，保证同一订单号每次结果一致（可复现演示）。
    statuses = ["已提交待审核", "审核中", "审核通过待放款", "已放款", "还款中", "已结清"]
    idx = sum(ord(c) for c in order_id) % len(statuses)
    return f"订单 {order_id} 当前状态：{statuses[idx]}。（模拟数据，实际以平台订单系统为准）"


@tool
def transfer_to_human(reason: str) -> str:
    """用户明确要求转人工、或投诉/情绪激动时，转接人工客服。

    Args:
        reason: 转人工原因
    """
    return f"已为您提交转人工申请（原因：{reason}）。人工坐席将尽快接入，请稍候。"
