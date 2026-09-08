package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 智能客服消息实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerServiceMessage {
    private Integer id;
    private Integer user_id;
    private Integer admin_id;
    /** 发送者角色: USER / ADMIN */
    private String sender_role;
    private String content;
    /** 0-未读 1-已读 */
    private Integer is_read;
    private LocalDateTime created_time;
}
