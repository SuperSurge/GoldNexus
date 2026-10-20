package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsBatch {
    private Integer id;
    private Integer userId;
    private String sourceType;//来源事件,同points_rule.eventType
    private String sourceBizNo;//业务单号(幂等键)
    private Integer pointsEarned;//本批获得积分
    private Integer pointsRemaining;//本批剩余可用
    private LocalDateTime earnedAt;
    private LocalDateTime expiresAt;//= earnedAt + 365天
    private Integer status;//1可用 2已过期 3已撤销
    private LocalDateTime createTime;
}
