package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsAnnouncement {
    private Integer id;
    private Integer productId;
    private Integer oldPrice;
    private Integer newPrice;
    private LocalDateTime effectiveTime;//生效时间
    private Integer status;//0待生效 1已生效 2已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
