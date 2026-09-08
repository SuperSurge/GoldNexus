package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客服会话摘要 — 管理员端用户列表用
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummary {
    private Integer user_id;
    private String username;
    /** 用户真实姓名 */
    private String name;
    /** 最新一条消息内容 */
    private String last_message;
    /** 最新消息时间 */
    private LocalDateTime last_time;
    /** 未读用户消息数 */
    private Integer unread_count;
}
