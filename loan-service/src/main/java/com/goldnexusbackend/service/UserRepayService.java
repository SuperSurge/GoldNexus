package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.Repayment;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.UserPenaltyMapper;
import com.goldnexusbackend.mapper.UserRepayMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepayService {
    private final UserRepayMapper  userRepayMapper;
    private final UserPenaltyMapper userPenaltyMapper;
    private final RestTemplate restTemplate;
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
            Repayment repayment = repaymentList.get(periodNumber-1);
            Integer planId = repayment.getPlanId();
            Integer wasOverdue = repayment.getStatus();// 还款前状态: 0未还 2逾期
            LocalDate actualPaymentDate = LocalDate.now();
            userRepayMapper.repayAllOnce(amount,actualPaymentDate,planId);

            // 整笔申请全部还清后, 更新申请状态为已还完
            boolean allPaid = userRepayMapper.selectRepaymentByApplicationId(applicationId).stream()
                    .allMatch(r -> r.getStatus() != null && r.getStatus() == 1);
            if (allPaid) {
                userRepayMapper.updateRepayStatus(applicationId);
            }

            // 结清当期且非逾期 → 触发积分(REPAY_SETTLE, 幂等键=repay-{planId}, 分值=floor(本金×0.001))
            if (wasOverdue == null || wasOverdue != 2) {
                try {
                    LoanApplication application = userPenaltyMapper.selectLoanApplication(applicationId);
                    if (application != null && application.getUserId() != null && repayment.getPrincipal() != null) {
                        int points = repayment.getPrincipal().multiply(new BigDecimal("0.001"))
                                .setScale(0, RoundingMode.DOWN).intValue();
                        grantPoints("REPAY_SETTLE", "repay-" + planId, application.getUserId(), points);
                    }
                } catch (Exception e) {
                    log.warn("结清积分发放失败(忽略): {}", e.getMessage());
                }
            }

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

    // 调用积分服务发放(服务间直连, 失败不影响主流程)
    private void grantPoints(String eventType, String bizNo, Integer userId, Integer points) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventType", eventType);
            body.put("bizNo", bizNo);
            body.put("userId", userId);
            body.put("points", points);
            restTemplate.postForObject("http://points-service/goldnexus/internal/grant", body, String.class);
        } catch (Exception e) {
            log.warn("积分发放调用失败(忽略): {}", e.getMessage());
        }
    }
}
