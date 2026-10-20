package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsSign;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PointsSignMapper {

    @Insert("insert into points_sign(userId, signDate, consecutive, points) " +
            "values(#{userId}, #{signDate}, #{consecutive}, #{points})")
    int insert(PointsSign sign);

    @Select("select * from points_sign where userId = #{userId} and signDate = #{signDate}")
    PointsSign selectByUserAndDate(@Param("userId") Integer userId, @Param("signDate") LocalDate signDate);

    // 最近一次签到, 用于计算连续签到天数
    @Select("select * from points_sign where userId = #{userId} order by signDate desc limit 1")
    PointsSign selectLatest(@Param("userId") Integer userId);

    // 用户全部签到记录(管理端查询)
    @Select("select * from points_sign where userId = #{userId} order by signDate desc")
    List<PointsSign> selectByUser(@Param("userId") Integer userId);
}
