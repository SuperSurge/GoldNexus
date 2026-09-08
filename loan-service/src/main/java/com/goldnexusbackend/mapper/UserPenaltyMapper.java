package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.Repayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserPenaltyMapper {
    //罚息计算需要得到贷款产品的信息，根据贷款申请ID查贷款记录表
    @Select("select * from loan_application where applicationId = #{applicationId}")
    LoanApplication selectLoanApplication(Integer applicationId);
    //查找所有的未还款的还款计划
    @Select("select * from repayment_plan where status = 0 or status = 2")//筛选状态为未偿还或逾期的还款计划
    List<Repayment> selectUnpaidPlan();
    //检查逾期，计算罚息
    @Update("update repayment_plan set status=#{status},penaltyAmount=#{penaltyAmount} where planId=#{planId} ")
    void check_overdue(Repayment repayment);
}
