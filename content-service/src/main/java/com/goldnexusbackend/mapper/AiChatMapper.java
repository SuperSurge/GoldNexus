package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.AiChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiChatMapper {

    /** 插入消息 */
    @Insert("INSERT INTO ai_chat_message (user_id, role, content, created_time) " +
            "VALUES (#{user_id}, #{role}, #{content}, #{created_time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(AiChatMessage message);

    /** 查询某用户的AI聊天记录（按时间升序） */
    @Select("SELECT * FROM ai_chat_message WHERE user_id = #{user_id} ORDER BY created_time ASC")
    List<AiChatMessage> selectMessagesByUserId(Integer user_id);

    /** 查询某用户最近的N条消息，用于构建DeepSeek上下文 */
    @Select("SELECT * FROM ai_chat_message WHERE user_id = #{user_id} " +
            "ORDER BY created_time DESC LIMIT #{limit}")
    List<AiChatMessage> selectRecentMessages(Integer user_id, int limit);
}
