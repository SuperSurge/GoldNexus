package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsAdjust;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsAdjustMapper {

    @Insert("insert into points_adjust(adminId, userId, changeType, amount, reason, status) " +
            "values(#{adminId}, #{userId}, #{changeType}, #{amount}, #{reason}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointsAdjust adjust);

    @Select("select * from points_adjust where userId = #{userId} order by id desc")
    List<PointsAdjust> selectByUser(@Param("userId") Integer userId);

    @Update("update points_adjust set status = #{status}, approveAdminId = #{approveAdminId} where id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("approveAdminId") Integer approveAdminId);
}
