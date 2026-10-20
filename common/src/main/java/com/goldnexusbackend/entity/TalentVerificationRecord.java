package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TalentVerificationRecord {
    /** 主键，自增 */
    private Integer id;
    /** 用户ID（关联 user 表） */
    private Integer userId;
    /** 学信网认证报告PDF的存储地址（MinIO） */
    private String reportFileUrl;
    /** 在线验证码（从PDF中提取） */
    private String verifyCode;
    /** 真实姓名 */
    private String realName;
    /** 毕业院校 */
    private String school;
    /** 专业 */
    private String major;
    /** 学历层次（如 本科/硕士/博士） */
    private String educationLevel;
    /** 毕业日期 */
    private LocalDate graduateDate;
    /** 认证状态（PENDING=待认证，SUCCESS=认证成功，FAILED=认证失败） */
    private String status;
    /** 认证结果说明 */
    private String verifyResult;
    /** 失败原因 */
    private String failReason;
    /** 提交时间 */
    private LocalDateTime createdAt;
    /** 最后更新时间 */
    private LocalDateTime updatedAt;
    /** 认证完成时间 */
    private LocalDateTime verifiedAt;
}
