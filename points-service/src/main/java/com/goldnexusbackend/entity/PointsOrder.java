package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsOrder {
    private Integer id;
    private String orderNo;//对外订单号
    private Integer userId;
    private Integer productId;
    private String productName;//下单时快照
    private Integer pointsCost;//下单时积分快照
    private String itemType;
    private String status;//CREATED/PAID/CANCELLED/COMPLETED/REFUNDED
    private String addressInfo;//实体:收货人/电话/地址JSON
    private String logisticsNo;//实体:物流单号
    private String codeValue;//虚拟:兑换码
    private String requestId;//幂等键
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
