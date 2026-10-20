package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TalentProductApplication {
    /** 主键，自增 */
    private Integer id;
    /** 申请用户ID（关联 user 表） */
    private Integer userId;
    /** 申请的产品ID（关联 talent_product 表） */
    private Integer productId;
    /** 关联的认证记录ID（关联 talent_verification_record 表，未认证则为空） */
    private Integer verificationId;
    /** 申请金额（元） */
    private BigDecimal amount;
    /** 申请期限（月） */
    private Integer term;
    /** 申请状态（PENDING=待审核） */
    private String status;
    /** 申请时间 */
    private LocalDateTime createdAt;
}
