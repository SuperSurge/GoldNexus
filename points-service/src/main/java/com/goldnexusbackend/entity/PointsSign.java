package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsSign {
    private Integer id;
    private Integer userId;
    private LocalDate signDate;//签到日期
    private Integer consecutive;//连续签到天数
    private Integer points;//本次获得积分
    private LocalDateTime createTime;
}
