package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsMessageMapper {

    @Insert("insert into points_message(userId, type, content, isRead) values(#{userId}, #{type}, #{content}, #{isRead})")
    int insert(PointsMessage message);

    @Select("select * from points_message where userId = #{userId} order by id desc")
    List<PointsMessage> selectByUser(@Param("userId") Integer userId);

    @Update("update points_message set isRead = 1 where id = #{id}")
    int markRead(@Param("id") Integer id);

    // 未读过期提醒条数(提醒去重用)
    @Select("select count(*) from points_message where userId = #{userId} and type = 'POINT_EXPIRING' and isRead = 0")
    int countUnreadExpiring(@Param("userId") Integer userId);
}
