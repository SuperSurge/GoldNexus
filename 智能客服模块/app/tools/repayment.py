"""还款 / 利息计算。

等额本息、等额本金两种标准公式，模型只负责把用户口述的「本金/期限/利率/方式」
解析成参数传进来，计算由本函数完成，杜绝模型心算出错。
"""
from langchain_core.tools import tool


@tool
def calculate_repayment(principal: float, annual_rate: float, months: int, method: str) -> str:
    """计算贷款还款计划（月供、总利息、还款总额）。

    Args:
        principal: 贷款本金（元）
        annual_rate: 年化利率（百分数，如 12 表示 12%）
        months: 期数（月）
        method: 还款方式，equal_installment(等额本息) / equal_principal(等额本金)
    """
    try:
        principal = float(principal)
        annual_rate = float(annual_rate)
        months = int(months)
    except (TypeError, ValueError):
        return "计算失败：请确认本金/利率/期数为有效数字。"

    if principal <= 0 or annual_rate < 0 or months <= 0:
        return "计算失败：本金、利率、期数必须为正数。"

    rate = annual_rate / 100 / 12  # 月利率

    if method == "equal_installment":
        # 等额本息：每月还款额固定
        payment = principal * rate * (1 + rate) ** months / ((1 + rate) ** months - 1)
        total = payment * months
        interest = total - principal
        return (
            f"【等额本息】本金 {principal:.2f} 元，{months} 期，年化 {annual_rate}%\n"
            f"每月月供：{payment:.2f} 元\n"
            f"总利息：{interest:.2f} 元\n"
            f"还款总额：{total:.2f} 元"
        )

    if method == "equal_principal":
        # 等额本金：每月还本金相同，利息逐月递减
        monthly_principal = principal / months
        first_payment = monthly_principal + principal * rate
        last_payment = monthly_principal + monthly_principal * rate
        interest = principal * rate * (months + 1) / 2
        total = principal + interest
        return (
            f"【等额本金】本金 {principal:.2f} 元，{months} 期，年化 {annual_rate}%\n"
            f"首月月供：{first_payment:.2f} 元，末月月供：{last_payment:.2f} 元（逐月递减）\n"
            f"总利息：{interest:.2f} 元\n"
            f"还款总额：{total:.2f} 元"
        )

    return "计算失败：method 只支持 equal_installment(等额本息) 或 equal_principal(等额本金)。"
