package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.Repayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserRepayMapper {
    @Select("select * from repayment_plan where applicationId = #{applicationId}")
    List<Repayment> selectRepaymentByApplicationId(Integer applicationId);

    //全部还完
    @Update("update repayment_plan set paidAmount=#{paidAmount},actualPaymentDate=#{actualPaymentDate},status=1 where planId=#{planId}")
    int repayAllOnce(BigDecimal paidAmount, LocalDate actualPaymentDate, Integer planId);

    //整笔申请全部还清后更新申请状态为已还完
    @Update("update loan_application set repayStatus = 1 where applicationId = #{applicationId}")
    int updateRepayStatus(Integer applicationId);
}
