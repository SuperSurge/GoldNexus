package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsAccount {
    private Integer id;
    private Integer userId;//用户ID(与user.id一致)
    private Integer status;//1正常 2冻结 3注销清零
    private Integer balance;//可用积分(镜像,对账依据是流水)
    private Integer totalEarned;//累计获得
    private Integer totalSpent;//累计消耗
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
