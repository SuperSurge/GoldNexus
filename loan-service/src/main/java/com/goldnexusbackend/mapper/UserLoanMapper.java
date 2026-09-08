package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.LoanProduct;
import com.goldnexusbackend.entity.Repayment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserLoanMapper {

    @Select("select * from loan_product where isRelease = 1")
    List<LoanProduct> selectAllProducts();

    @Select("select * from loan_product where productId=#{id}")
    LoanProduct selectLoanProductById(Integer id);

    @Insert("insert into loan_application " +
            "    (productId, name, userId, amount,interestWay, applyTime, useWay, applicationStatus,repayStatus,message)" +
            "    values \n" +
            "    (#{productId}, #{name}, #{userId}, #{amount}, #{interestWay}, #{applyTime}, #{useWay}, #{applicationStatus},#{repayStatus},#{message})")
    @Options(useGeneratedKeys = true,keyProperty = "applicationId",keyColumn = "applicationId")
    int insertLoanApplication(LoanApplication loanApplication);

    @Select("select * from loan_application where userId=#{userId}")
    List<LoanApplication> recordApplication(int userId);

    //初次生成
    @Insert("<script>" +
            "insert into repayment_plan (applicationId, periodNumber, dueDate, amount, principal) " +
            "values " +
            "<foreach collection='repaymentList' item='item' separator=','>" +
            "   (#{item.applicationId}, #{item.periodNumber}, #{item.dueDate}, #{item.amount}, #{item.principal})" +
            "</foreach>" +
            "</script>")
    int insertRepaymentPlan(@Param("repaymentList") List<Repayment> repaymentList);

    // 查询还款计划
    @Select("select * from repayment_plan where applicationId = #{applicationId}")
    List<Repayment> selectRepaymentPlanByApplicationId(Integer applicationId);

    @Select("select count(*) from loan_application where userId = #{userId}")
    int loanApplicationCountByUserId(Integer userId);
}
