package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsAdjust {
    private Integer id;
    private Integer adminId;//发起调账的管理员
    private Integer userId;
    private String changeType;//ADD/SUB 增/减
    private Integer amount;//调账积分(正数)
    private String reason;//调账原因(必填)
    private String status;//PENDING/APPROVED/REJECTED
    private Integer approveAdminId;//审核管理员
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
