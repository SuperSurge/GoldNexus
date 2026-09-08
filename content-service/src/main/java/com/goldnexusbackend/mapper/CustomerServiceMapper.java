package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.ConversationSummary;
import com.goldnexusbackend.entity.CustomerServiceMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CustomerServiceMapper {

    /** 插入消息 */
    @Insert("INSERT INTO customer_service_message (user_id, admin_id, sender_role, content, is_read, created_time) " +
            "VALUES (#{user_id}, #{admin_id}, #{sender_role}, #{content}, #{is_read}, #{created_time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(CustomerServiceMessage message);

    /** 查询某用户的所有消息（按时间升序） */
    @Select("SELECT * FROM customer_service_message WHERE user_id = #{user_id} ORDER BY created_time ASC")
    List<CustomerServiceMessage> selectMessagesByUserId(Integer user_id);

    /** 管理员：查询所有发起过对话的用户摘要 */
    @Select("SELECT DISTINCT m.user_id, u.username, u.name, " +
            "  (SELECT content FROM customer_service_message WHERE user_id = m.user_id ORDER BY created_time DESC LIMIT 1) AS last_message, " +
            "  (SELECT created_time FROM customer_service_message WHERE user_id = m.user_id ORDER BY created_time DESC LIMIT 1) AS last_time, " +
            "  (SELECT COUNT(*) FROM customer_service_message WHERE user_id = m.user_id AND is_read = 0 AND sender_role = 'USER') AS unread_count " +
            "FROM customer_service_message m " +
            "JOIN user u ON m.user_id = u.id " +
            "ORDER BY last_time DESC")
    List<ConversationSummary> selectConversationSummaries();

    /** 将某用户的未读消息标为已读 */
    @Update("UPDATE customer_service_message SET is_read = 1 " +
            "WHERE user_id = #{user_id} AND sender_role = 'USER' AND is_read = 0")
    int markMessagesAsRead(Integer user_id);
}
