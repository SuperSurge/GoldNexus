package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplication {
    private Integer applicationId;
    private Integer productId;
    private String name;
    private Integer userId;
    private BigDecimal amount;
    private Integer interestWay;
    private String useWay;
    private LocalDateTime applyTime;
    private Integer applicationStatus;
    private Integer repayStatus;
    private String message;
    /** 产品类型：0普通，1优才 */
    private Integer productType;
    /** 期数（优才用户自选） */
    private Integer term;
    /** 应届生认证记录ID */
    private Integer verificationId;
}
