package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PointsAccountMapper {

    @Select("select * from points_account where userId = #{userId}")
    PointsAccount selectByUserId(@Param("userId") Integer userId);

    @Insert("insert into points_account(userId, status, balance, totalEarned, totalSpent) " +
            "values(#{userId}, #{status}, #{balance}, #{totalEarned}, #{totalSpent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointsAccount account);

    @Update("update points_account set balance = balance + #{points}, totalEarned = totalEarned + #{points} " +
            "where userId = #{userId}")
    int addEarned(@Param("userId") Integer userId, @Param("points") Integer points);

    // 加积分(条件 UPDATE: 余额不超过上限才加, 影响行数=0 表示已达上限)
    @Update("update points_account set balance = balance + #{points}, totalEarned = totalEarned + #{points} " +
            "where userId = #{userId} and balance + #{points} <= #{cap}")
    int addEarnedWithCap(@Param("userId") Integer userId, @Param("points") Integer points, @Param("cap") Integer cap);

    @Update("update points_account set balance = balance - #{points}, totalSpent = totalSpent + #{points} " +
            "where userId = #{userId} and balance >= #{points}")
    int deductSpent(@Param("userId") Integer userId, @Param("points") Integer points);

    // 减积分(仅减余额, 不计入消费; 用于过期/管理员扣减)
    @Update("update points_account set balance = balance - #{points} " +
            "where userId = #{userId} and balance >= #{points}")
    int deductBalance(@Param("userId") Integer userId, @Param("points") Integer points);

    @Update("update points_account set status = #{status} where userId = #{userId}")
    int updateStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    @Update("update points_account set balance = 0, status = 3 where userId = #{userId}")
    int clear(@Param("userId") Integer userId);
}
