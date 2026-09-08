package com.goldnexusbackend.service;
import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.mapper.UserLoanMapper;
import com.goldnexusbackend.mapper.UserPenaltyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPenaltyService {
    private final UserLoanMapper userLoanMapper;
    private final UserPenaltyMapper userPenaltyMapper;

    //检查是否有逾期，计算并更新罚息
    @Scheduled(cron = "0 0 0 * * ?")//每天零点自动执行这个方法检查逾期并计算、更新罚息
    public void check_overdue(){
        List<Repayment> repaymentList = userPenaltyMapper.selectUnpaidPlan();
        for(Repayment single_repayment : repaymentList){
            if(!single_repayment.getDueDate().isBefore(LocalDate.now())) {//已还款或没有到还款日期
                continue;
            }
            LoanApplication loanApplication = userPenaltyMapper.selectLoanApplication(single_repayment.getApplicationId());
            LoanProduct loanProduct = userLoanMapper.selectLoanProductById(loanApplication.getProductId());
            BigDecimal penalty_rate = loanProduct.getPenalty();//获取对应贷款产品的罚息，该罚息是日利率
            single_repayment.setStatus(2);//状态设置为逾期
            long overdue_days = ChronoUnit.DAYS.between(single_repayment.getDueDate(), LocalDate.now());//计算当前日期与原定还款日期之间的天数差
            BigDecimal unpaid_amount = single_repayment.getAmount().subtract(single_repayment.getPaidAmount());//未还金额
            single_repayment.setPenaltyAmount(unpaid_amount.multiply(BigDecimal.valueOf(overdue_days)).multiply(penalty_rate));//计算并更新罚息金额
            userPenaltyMapper.check_overdue(single_repayment);//更新数据库
        }
    }

}
