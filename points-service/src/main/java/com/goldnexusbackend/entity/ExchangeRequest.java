package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRequest {
    private String requestId;//幂等键,同一requestId重复提交只成一单
    private Integer productId;
    private String addressInfo;//实体商品收货信息JSON
}
