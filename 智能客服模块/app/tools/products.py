"""查贷款产品（多数据源融合）。

真实环境：这里应调用平台「多数据源融合层」的接口（聚合奇富科技、乐信、度小满等
第三方合规机构的在售产品），返回按金额/期限/征信过滤后的产品列表。
本模块为独立演示，用 Mock 数据占位，并在返回里明确标注「模拟数据」，
符合金融合规：不编造真实机构的实时利率。
"""
import json

from langchain_core.tools import tool

# ===== Mock 多数据源融合产品表 =====
# 字段：platform(来源平台) / product(产品名) / min_amount, max_amount(额度区间，元)
#       rate_range(年化区间) / terms(可选期数) / credit(接受的征信等级)
_MOCK_PRODUCTS = [
    {
        "platform": "平台自营·金纽贷",
        "product": "金纽贷",
        "min_amount": 1000, "max_amount": 100000,
        "rate_range": "6.0%~14.8%", "terms": [3, 6, 9, 12, 24],
        "credit": ["good", "fair"],
    },
    {
        "platform": "奇富科技",
        "product": "周转贷",
        "min_amount": 1000, "max_amount": 50000,
        "rate_range": "7.2%~14.8%", "terms": [3, 6, 9, 12, 24],
        "credit": ["good", "fair"],
    },
    {
        "platform": "乐信",
        "product": "分期乐",
        "min_amount": 1000, "max_amount": 80000,
        "rate_range": "6.0%~14.8%", "terms": [3, 6, 12, 24],
        "credit": ["good", "fair", "poor"],
    },
    {
        "platform": "度小满",
        "product": "有钱花",
        "min_amount": 5000, "max_amount": 200000,
        "rate_range": "6.5%~12.0%", "terms": [6, 12, 24],
        "credit": ["good"],
    },
]


@tool
def search_loan_products(amount: int, term_months: int, credit_level: str) -> str:
    """查询符合金额、期限、征信等级的贷款产品（聚合多家第三方平台）。

    Args:
        amount: 期望借款金额（元）
        term_months: 借款期限（月），支持 3/6/9/12/24
        credit_level: 征信等级，good(良好)/fair(一般)/poor(较差)
    """
    credit_level = (credit_level or "").strip().lower()
    matched = []
    for p in _MOCK_PRODUCTS:
        ok_amount = p["min_amount"] <= amount <= p["max_amount"]
        ok_term = term_months in p["terms"]
        ok_credit = credit_level in p["credit"]
        if ok_amount and ok_term and ok_credit:
            matched.append(p)

    if not matched:
        return (
            f"暂无同时满足「金额 {amount} 元、期限 {term_months} 期、征信 {credit_level}」的产品。"
            "请以平台实际审批为准。"
        )

    lines = [
        f"为您找到 {len(matched)} 款符合条件的产品（模拟数据，实际以平台审批为准）：",
    ]
    for p in matched:
        lines.append(
            f"- {p['platform']}「{p['product']}」：额度 {p['min_amount']}~{p['max_amount']} 元，"
            f"年化 {p['rate_range']}，可选期数 {p['terms']}"
        )
    return "\n".join(lines)
