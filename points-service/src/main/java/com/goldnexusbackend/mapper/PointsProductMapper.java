package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsProduct;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsProductMapper {

    @Select("select * from points_product where status = 1 order by id limit #{offset}, #{limit}")
    List<PointsProduct> selectOnShelfPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from points_product where status = 1")
    int countOnShelf();

    @Select("select * from points_product where status = 1 order by id")
    List<PointsProduct> selectOnShelf();

    @Select("select * from points_product order by id")
    List<PointsProduct> selectAll();

    @Select("select * from points_product where id = #{id}")
    PointsProduct selectById(@Param("id") Integer id);

    @Insert("insert into points_product(name, itemType, pointsPrice, stock, coverUrl, detail, status) " +
            "values(#{name}, #{itemType}, #{pointsPrice}, #{stock}, #{coverUrl}, #{detail}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PointsProduct product);

    @Update("update points_product set name = #{name}, pointsPrice = #{pointsPrice}, stock = #{stock}, " +
            "coverUrl = #{coverUrl}, detail = #{detail} where id = #{id}")
    int update(PointsProduct product);

    @Update("update points_product set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 扣库存: 条件 UPDATE, 影响行数=0 表示库存不足
    @Update("update points_product set stock = stock - 1 where id = #{id} and stock > 0")
    int deductStock(@Param("id") Integer id);
}
