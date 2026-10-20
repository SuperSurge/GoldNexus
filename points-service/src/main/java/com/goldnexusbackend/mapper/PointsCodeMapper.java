package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PointsCodeMapper {

    @Insert("insert into points_code(productId, codeValue, status) values(#{productId}, #{codeValue}, #{status})")
    int insert(PointsCode code);

    // 锁一张未用码: FOR UPDATE 防并发重复领取
    @Select("select * from points_code where productId = #{productId} and status = 0 order by id limit 1 for update")
    PointsCode selectAvailableForUpdate(@Param("productId") Integer productId);

    @Update("update points_code set status = 1, userId = #{userId}, orderNo = #{orderNo} " +
            "where codeValue = #{codeValue} and status = 0")
    int lockCode(@Param("codeValue") String codeValue, @Param("userId") Integer userId, @Param("orderNo") String orderNo);

    @Update("update points_code set status = 2 where codeValue = #{codeValue} and status = 1")
    int verify(@Param("codeValue") String codeValue);

    @Select("select * from points_code where codeValue = #{codeValue}")
    PointsCode selectByCode(@Param("codeValue") String codeValue);
}
