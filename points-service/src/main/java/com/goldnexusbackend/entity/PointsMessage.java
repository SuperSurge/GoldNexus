package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsMessage {
    private Integer id;
    private Integer userId;
    private String type;//POINT_EXPIRING/POINT_ANNOUNCE 等
    private String content;
    private Integer isRead;//0未读 1已读
    private LocalDateTime createTime;
}
