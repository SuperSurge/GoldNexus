package com.goldnexusbackend.mapper;


import com.goldnexusbackend.entity.LoanProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

//管理员产品管理
@Mapper
public interface AdminProductMapper {

    //新增产品
    @Insert("insert into loan_product value (#{productId},#{productName},#{isRelease},#{maxLimit},#{minLimit},#{maxThreshold},#{minThreshold},#{penalty},#{period},#{interest})")
    int addProduct(LoanProduct loanProduct);

    //修改
    @Update("update loan_product set productName= #{productName}, isRelease= #{isRelease}, maxLimit= #{maxLimit}, minLimit= #{minLimit},maxThreshold= #{maxThreshold},minThreshold= #{minThreshold},penalty= #{penalty}, period= #{period}, interest= #{interest} where productId=#{productId}")
    int updateProduct(LoanProduct loanProduct);

    //查询
    @Select("select * from loan_product where productId=#{productId}")
    LoanProduct selectProductByProductId(Integer productId);

    //删除
    @Delete("delete from loan_product where productId =#{productId}")
    int deleteProductByProductId(Integer productId);

    //查询所有产品
    @Select("select *from loan_product")
    List<LoanProduct> selectAllProducts();
}
