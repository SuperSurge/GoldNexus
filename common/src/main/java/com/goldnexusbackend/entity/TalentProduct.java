package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TalentProduct {
    /** 主键，自增 */
    private Integer id;
    /** 产品名称 */
    private String name;
    /** 产品描述/说明 */
    private String description;
    /** 贷款金额下限（元） */
    private BigDecimal amountMin;
    /** 贷款金额上限（元） */
    private BigDecimal amountMax;
    /** 贷款期限下限（月） */
    private Integer termMin;
    /** 贷款期限上限（月） */
    private Integer termMax;
    /** 年化利率（小数，如 0.05 表示 5%） */
    private BigDecimal interestRate;
    /** 日罚息率（逾期罚息用，如 0.01 表示日 1%） */
    private BigDecimal penalty;
    /** 是否需要应届生认证（1=需要，0=不需要） */
    private Integer requireVerification;
    /** 产品状态（1=上架，0=下架） */
    private Integer status;
}
