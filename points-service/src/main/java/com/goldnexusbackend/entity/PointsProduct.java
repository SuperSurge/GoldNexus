package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsProduct {
    private Integer id;
    private String name;
    private String itemType;//PHYSICAL实体 / VIRTUAL_CODE兑换码 / VIP权益
    private Integer pointsPrice;//所需积分
    private Integer stock;//实体库存;-1=不限
    private String coverUrl;
    private String detail;
    private Integer status;//1上架 0下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
