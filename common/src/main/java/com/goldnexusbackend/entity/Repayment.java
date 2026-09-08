package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repayment {
    private Integer planId;
    private Integer applicationId;//关联的申请id
    private Integer periodNumber;//第几期
    private LocalDate dueDate;//这一期的预计还款日期
    private BigDecimal amount;//这一期的还款金额（本金加利息）
    private BigDecimal principal;//这一期的本金
    private Integer status;//状态：0未还款，1已还款，2逾期
    private LocalDate actualPaymentDate;//实际还款日期
    private BigDecimal paidAmount;//已还款金额
    private BigDecimal penaltyAmount;
}
