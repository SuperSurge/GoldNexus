package com.goldnexusbackend.entity;

import lombok.Data;

@Data
public class LoanApplicant {

    /** 年龄 */
    private Integer age;

    /** 是否实名认证 */
    private Boolean verified;

    /** 信用分 */
    private Integer creditScore;

    /** 月收入 */
    private Double monthlyIncome;

    /** 是否存在逾期 */
    private Boolean overdue;

    /** 是否失信人员 */
    private Boolean blackList;

    /** 是否有稳定工作 */
    private Boolean employed;

    /** 贷款金额 */
    private Double loanAmount;

    /** 是否允许贷款 */
    private Boolean approved = true;

    /** 拒绝原因 */
    private String rejectReason;

}