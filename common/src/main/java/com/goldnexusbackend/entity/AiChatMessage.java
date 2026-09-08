package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI智能客服消息实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatMessage {
    private Integer id;
    private Integer user_id;
    /** 角色: user / assistant */
    private String role;
    private String content;
    private LocalDateTime created_time;
}
