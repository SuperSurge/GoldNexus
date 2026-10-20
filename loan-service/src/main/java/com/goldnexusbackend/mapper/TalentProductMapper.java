package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.TalentProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

//应届生产品管理
@Mapper
public interface TalentProductMapper {

    @Insert("insert into talent_product (name, description, amountMin, amountMax, termMin, termMax, interestRate, penalty, requireVerification, status) " +
            "values (#{name}, #{description}, #{amountMin}, #{amountMax}, #{termMin}, #{termMax}, #{interestRate}, #{penalty}, #{requireVerification}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addProduct(TalentProduct product);

    @Update("update talent_product set name = #{name}, description = #{description}, amountMin = #{amountMin}, amountMax = #{amountMax}, " +
            "termMin = #{termMin}, termMax = #{termMax}, interestRate = #{interestRate}, penalty = #{penalty}, requireVerification = #{requireVerification}, status = #{status} " +
            "where id = #{id}")
    int updateProduct(TalentProduct product);

    @Select("select * from talent_product where id = #{id}")
    TalentProduct selectById(Integer id);

    @Select("select * from talent_product")
    List<TalentProduct> selectAll();

    @Select("select * from talent_product where status = 1")
    List<TalentProduct> selectOnShelf();

    @Delete("delete from talent_product where id = #{id}")
    int deleteById(Integer id);
}
