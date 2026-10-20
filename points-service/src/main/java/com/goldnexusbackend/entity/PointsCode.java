package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsCode {
    private Integer id;
    private Integer productId;
    private String codeValue;//随机码,不可枚举
    private Integer status;//0未用 1已锁定 2已核销
    private String orderNo;
    private Integer userId;
    private LocalDateTime expireTime;
}
