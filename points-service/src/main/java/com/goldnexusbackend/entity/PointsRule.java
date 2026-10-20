package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsRule {
    private Integer id;
    private String eventType;//REGISTER/REAL_NAME/SIGN_IN/REPAY_SETTLE
    private String name;
    private Integer points;//基础分值
    private Integer requireVerified;//是否需实名 1是 0否
    private Integer dailyCap;//单日发放次数上限(NULL=不限)
    private Integer totalCap;//终身次数上限(NULL=不限)
    private Integer enabled;//1启用 0停用
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
