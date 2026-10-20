package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsDailyQuota {
    private Integer id;
    private Integer userId;
    private LocalDate quotaDate;
    private Integer used;//当日已发
    private Integer dailyLimit;//单日上限(可配)
    private LocalDateTime createTime;
}
