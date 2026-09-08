package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.Check;
import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.LoanProduct;
import com.goldnexusbackend.entity.Repayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminLoanMapper {
    @Select("select * from loan_application")
    List<LoanApplication> allLoanApplication();

    @Select("select * from loan_application where applicationId = #{applicationId}")
    LoanApplication selectLoanApplicationById(int applicationId);

    @Select("select * from loan_application where userId=#{userId}")
    List<LoanApplication> selectLoanApplicationByUserId(Integer userId);

    @Select("select * from loan_application where applicationStatus = 1")
    List<LoanApplication> checkAllApplication();

    @Select("select * from loan_application where applicationId=#{applicationId} and applicationStatus=1")
    LoanApplication checkApplication(Integer applicationId);

    @Update("update loan_application set applicationStatus=#{status},message=#{message} where applicationId=#{applicationId}")
    int updateLoanApplicationStatus(Check check);

    @Select("select * from repayment_plan where applicationId = #{applicationId}")
    List<Repayment> adminSelectRepaymentPlanByApplicationId(Integer applicationId);
}
