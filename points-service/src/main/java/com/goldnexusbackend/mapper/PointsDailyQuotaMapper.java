package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsDailyQuota;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface PointsDailyQuotaMapper {

    @Select("select * from points_daily_quota where userId = #{userId} and quotaDate = #{quotaDate}")
    PointsDailyQuota selectByUserAndDate(@Param("userId") Integer userId, @Param("quotaDate") LocalDate quotaDate);

    @Insert("insert into points_daily_quota(userId, quotaDate, used, dailyLimit) " +
            "values(#{userId}, #{quotaDate}, #{used}, #{dailyLimit})")
    int insert(PointsDailyQuota quota);

    // 原子扣日额度: 条件 UPDATE, 影响行数=0 表示已达上限
    @Update("update points_daily_quota set used = used + #{points} " +
            "where userId = #{userId} and quotaDate = #{quotaDate} and used + #{points} <= dailyLimit")
    int addUsed(@Param("userId") Integer userId, @Param("quotaDate") LocalDate quotaDate, @Param("points") Integer points);
}
