package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Repayment;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.UserRepayMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepayService {
    private final UserRepayMapper  userRepayMapper;
    Res res = new Res();

    @Transactional
    public Res repay(Integer applicationId,Integer periodNumber, BigDecimal amount){
        log.info("用户还款请求");

        List<Repayment> repaymentList = userRepayMapper.selectRepaymentByApplicationId(applicationId);

        if(periodNumber>1&&repaymentList.get(periodNumber-2).getStatus()!=1){
            res.setCode(500);
            res.setMsg("还款失败，上一期还未还完");
            log.info("还款失败，上一期还未还完");
            res.setData(null);
            return res;
        }
        //如果还款金额恰好等于应还金额+罚息，则还款成功
        if (amount != null && amount.compareTo(
                repaymentList.get(periodNumber-1).getAmount()
                        .add(repaymentList.get(periodNumber-1).getPenaltyAmount() != null ?
                                repaymentList.get(periodNumber-1).getPenaltyAmount() : BigDecimal.ZERO)
        ) == 0) {
            Integer planId = repaymentList.get(periodNumber-1).getPlanId();
            LocalDate actualPaymentDate = LocalDate.now();
            userRepayMapper.repayAllOnce(amount,actualPaymentDate,planId);

            res.setCode(200);
            res.setMsg("还款成功,本期已还完");
            log.info("还款成功,本期已还完");
            res.setData(null);
            return res;
        }

        res.setCode(500);
        res.setMsg("还款失败");
        log.info("还款失败");
        res.setData(null);
        return res;
    }
}
