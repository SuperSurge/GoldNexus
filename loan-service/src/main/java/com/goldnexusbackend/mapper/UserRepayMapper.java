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
}
