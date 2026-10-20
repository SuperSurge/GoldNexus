package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsFlow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PointsFlowMapper {

    @Insert("insert into points_flow(userId, batchId, flowType, bizType, bizNo, amount, remark, operatorId) " +
            "values(#{userId}, #{batchId}, #{flowType}, #{bizType}, #{bizNo}, #{amount}, #{remark}, #{operatorId})")
    int insert(PointsFlow flow);

    @Select("select * from points_flow where userId = #{userId} order by createTime desc")
    List<PointsFlow> selectByUser(@Param("userId") Integer userId);

    @Select("select * from points_flow where userId = #{userId} and createTime between #{start} and #{end} " +
            "order by createTime desc limit #{offset}, #{limit}")
    List<PointsFlow> selectByUserAndTimePage(@Param("userId") Integer userId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    @Select("select count(*) from points_flow where userId = #{userId} and createTime between #{start} and #{end}")
    int countByUserAndTime(@Param("userId") Integer userId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    @Select("select * from points_flow where userId = #{userId} and createTime between #{start} and #{end} " +
            "order by createTime desc")
    List<PointsFlow> selectByUserAndTime(@Param("userId") Integer userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    // 对账: 余额镜像 = 流水之和
    @Select("select ifnull(sum(amount), 0) from points_flow where userId = #{userId}")
    Integer sumAmount(@Param("userId") Integer userId);
}
