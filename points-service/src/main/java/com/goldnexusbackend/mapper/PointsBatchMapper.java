package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsBatch;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsBatchMapper {

    @Insert("insert into points_batch(userId, sourceType, sourceBizNo, pointsEarned, pointsRemaining, earnedAt, expiresAt, status) " +
            "values(#{userId}, #{sourceType}, #{sourceBizNo}, #{pointsEarned}, #{pointsRemaining}, #{earnedAt}, #{expiresAt}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointsBatch batch);

    // 临期优先扣减(先到期先用), FOR UPDATE 行锁防并发重复扣
    @Select("select * from points_batch where userId = #{userId} and status = 1 and pointsRemaining > 0 " +
            "and expiresAt >= now() order by expiresAt asc, id asc for update")
    List<PointsBatch> selectAvailableForUpdate(@Param("userId") Integer userId);

    @Update("update points_batch set pointsRemaining = pointsRemaining - #{points} " +
            "where id = #{id} and pointsRemaining >= #{points}")
    int deductRemaining(@Param("id") Integer id, @Param("points") Integer points);

    // 批量过期: 一条 UPDATE, 不逐行循环
    @Update("update points_batch set status = 2 where status = 1 and pointsRemaining > 0 and expiresAt < now()")
    int batchExpire();

    // 已过期但状态仍为可用的批次(过期任务先取出)
    @Select("select * from points_batch where status = 1 and pointsRemaining > 0 and expiresAt < now()")
    List<PointsBatch> selectExpired();

    // 某用户 days 天内到期的可用批次
    @Select("select * from points_batch where userId = #{userId} and status = 1 " +
            "and expiresAt between now() and date_add(now(), interval #{days} day)")
    List<PointsBatch> selectExpiring(@Param("userId") Integer userId, @Param("days") Integer days);

    // 全部 days 天内到期的可用批次(提醒任务用)
    @Select("select * from points_batch where status = 1 " +
            "and expiresAt between now() and date_add(now(), interval #{days} day)")
    List<PointsBatch> selectAllExpiring(@Param("days") Integer days);

    // 用户全部批次(管理端查询)
    @Select("select * from points_batch where userId = #{userId} order by id desc")
    List<PointsBatch> selectByUser(@Param("userId") Integer userId);

    // 对账: 该用户可用批次剩余积分之和
    @Select("select ifnull(sum(pointsRemaining), 0) from points_batch where userId = #{userId} and status = 1")
    Integer sumRemaining(@Param("userId") Integer userId);

    // 即将过期积分(days 天内到期)之和
    @Select("select ifnull(sum(pointsRemaining), 0) from points_batch where userId = #{userId} and status = 1 " +
            "and expiresAt between now() and date_add(now(), interval #{days} day)")
    Integer sumExpiring(@Param("userId") Integer userId, @Param("days") Integer days);

    // 清零: 撤销该用户所有可用批次(置3=已撤销, 剩余归0, 保证余额=Σ批次)
    @Update("update points_batch set status = 3, pointsRemaining = 0 where userId = #{userId} and status = 1")
    int revokeAllByUser(@Param("userId") Integer userId);
}
