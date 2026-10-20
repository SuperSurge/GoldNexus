package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsFlow {
    private Integer id;
    private Integer userId;
    private Integer batchId;//关联批次(过期/消费时非空)
    private String flowType;//EARN/SPEND/EXPIRE/REVOKE/ADJUST_ADD/ADJUST_SUB
    private String bizType;//业务类型(幂等键1)
    private String bizNo;//业务单号(幂等键2)
    private Integer amount;//正数=获得,负数=消耗/过期/撤销
    private String remark;
    private Integer operatorId;//管理员操作时记录
    private LocalDateTime createTime;
}
