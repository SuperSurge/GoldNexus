package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsOrderMapper {

    @Insert("insert into points_order(orderNo, userId, productId, productName, pointsCost, itemType, status, addressInfo, requestId) " +
            "values(#{orderNo}, #{userId}, #{productId}, #{productName}, #{pointsCost}, #{itemType}, #{status}, #{addressInfo}, #{requestId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointsOrder order);

    @Select("select * from points_order where orderNo = #{orderNo}")
    PointsOrder selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("select * from points_order where userId = #{userId} and requestId = #{requestId}")
    PointsOrder selectByRequestId(@Param("userId") Integer userId, @Param("requestId") String requestId);

    @Select("select * from points_order where userId = #{userId} order by id desc limit #{offset}, #{limit}")
    List<PointsOrder> selectByUserPage(@Param("userId") Integer userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from points_order where userId = #{userId}")
    int countByUser(@Param("userId") Integer userId);

    @Select("select * from points_order where userId = #{userId} order by id desc")
    List<PointsOrder> selectByUser(@Param("userId") Integer userId);

    @Update("update points_order set status = #{status} where orderNo = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") String status);

    @Update("update points_order set logisticsNo = #{logisticsNo}, status = 'COMPLETED' where orderNo = #{orderNo}")
    int ship(@Param("orderNo") String orderNo, @Param("logisticsNo") String logisticsNo);
}
