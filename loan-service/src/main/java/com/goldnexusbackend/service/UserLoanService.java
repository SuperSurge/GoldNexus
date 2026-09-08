package com.goldnexusbackend.service;


import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.mapper.UserLoanMapper;
import com.goldnexusbackend.mapper.UserMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoanService {

    private final UserLoanMapper userLoanMapper;
    private final UserMapper userMapper;
    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode DECIMAL_ROUNDING = RoundingMode.HALF_UP;

    Res res=new Res();

    @Transactional
    public Res selectAllProducts(){
        log.info("查询所有贷款产品");

        List<LoanProduct> products=userLoanMapper.selectAllProducts();
        res.setCode(200);
        res.setMsg("查询所有贷款产品成功");
        res.setData(products);
        log.info("查询所有贷款产品成功");
        return res;
    }

    //生成订单->判断审批阈值->储存订单/提供给管理员审核    通过后生成还款计划，扫描逾期，金额计算逻辑

    @Transactional
    public Res loanApply(LoanApplication loanApplication){
        log.info("进行贷款申请请求");

        CurrentUser currentUser= SecurityContextHelper.getCurrentUser();
        if (currentUser==null){
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }


        User applyUser = userMapper.selectUserById(currentUser.getId());

        //实名认证判断
        if(applyUser.getAuthentication()!=1){
            res.setCode(500);
            res.setMsg("申请失败，该用户未进行实名认证");
            res.setData(null);
            return res;
        }

        //年龄判断
        if (Period.between(applyUser.getAge(),LocalDate.now()).getYears()<18){
            res.setCode(500);
            res.setMsg("用户未成年，无法借贷");
            log.info("用户未成年，无法借贷");
            res.setData(null);
            return res;
        }

        String name = applyUser.getName();
        int userId = applyUser.getId();
        loanApplication.setName(name);
        loanApplication.setUserId(userId);
        loanApplication.setApplyTime(LocalDateTime.now());
        loanApplication.setRepayStatus(0);

        LoanProduct loanProduct = userLoanMapper.selectLoanProductById(loanApplication.getProductId());
        if (loanProduct==null){
            res.setCode(500);
            res.setMsg("未找到该产品");
            log.info("未找到该产品");
            res.setData(null);
        }

        //信用分判断
        if(applyUser.getScore()<loanProduct.getMinThreshold()){
            loanApplication.setApplicationStatus(0);
            loanApplication.setMessage("申请未通过，信用分过低");
            try{
                int i = userLoanMapper.insertLoanApplication(loanApplication);
                if (i>0){
                    res.setCode(200);
                    res.setMsg("申请未通过，信用分过低");
                    log.info("申请未通过，信用分过低");
                    res.setData(null);
                    return res;
                }else{
                    res.setCode(500);
                    res.setMsg("内部错误1");
                    log.info("内部错误1");
                    res.setData(null);
                    return res;
                }
            }catch (Exception e){
                res.setCode(500);
                res.setMsg("内部错误2");
                log.info("内部错误2");
                log.info(e.getMessage());
                res.setData(null);
                return res;
            }
        }

        if(applyUser.getScore()>=loanProduct.getMaxThreshold()){
            loanApplication.setApplicationStatus(2);
            try{
                int i = userLoanMapper.insertLoanApplication(loanApplication);
                if (i>0){
                    int i1 = generateRepayment(loanApplication);

                    if (i1==1){
                        res.setCode(200);
                        res.setMsg("申请通过，还款计划已生成");
                        log.info("申请通过，还款计划已生成");
                        res.setData(null);
                        return res;
                    }
                    else{
                        res.setCode(200);
                        res.setMsg("申请通过，还款计划生成异常");
                        log.info("申请通过，还款计划生成异常");
                        res.setData(null);
                        return res;
                    }

                }else{
                    res.setCode(500);
                    res.setMsg("内部错误1");
                    log.info("内部错误1");
                    res.setData(null);
                    return res;
                }
            }catch (Exception e){
                res.setCode(500);
                res.setMsg("内部错误2");
                log.info("内部错误2");
                log.info(e.getMessage());
                res.setData(null);
                return res;
            }
        }

        loanApplication.setApplicationStatus(1);
        try{
            int i = userLoanMapper.insertLoanApplication(loanApplication);
            if (i>0){
                res.setCode(200);
                res.setMsg("已申请，等待审核");
                log.info("已申请，等待审核");
                res.setData(null);
                return res;
            }else{
                res.setCode(500);
                res.setMsg("内部错误1");
                log.info("内部错误1");
                res.setData(null);
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("内部错误2");
            log.info("内部错误2");
            log.info(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res recordApplication(){
        log.info("进行个人历史申请查询请求");

        CurrentUser currentUser= SecurityContextHelper.getCurrentUser();
        if (currentUser==null){
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }


        log.info("历史申请记录查询成功");
        res.setCode(200);
        res.setMsg("历史申请记录查询成功");
        res.setData(userLoanMapper.recordApplication(currentUser.getId()));
        return res;
    }

    @Transactional
    public Res repaymentPlan(Integer applicationId) {
        log.info("进行查询还款计划申请");

        List<Repayment> repaymentList = userLoanMapper.selectRepaymentPlanByApplicationId(applicationId);
        if (repaymentList == null) {
            res.setCode(500);
            res.setMsg("查询失败");
            log.info("查询失败");
            res.setData(null);
            return res;
        } else {
            System.out.println(repaymentList);
            res.setCode(200);
            res.setData(repaymentList);
            res.setMsg("查询还款计划成功");
            log.info("查询还款计划成功");
            return res;
        }
    }

    @Transactional
    public Res loanApplicationCountByUserId(){
        log.info("查询用户申请总数请求");
        CurrentUser currentUser= SecurityContextHelper.getCurrentUser();
        if (currentUser==null){
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }
        int i = userLoanMapper.loanApplicationCountByUserId(currentUser.getId());
        res.setCode(200);
        res.setData(i);
        res.setMsg("查询成功");
        log.info("查询成功");
        return res;
    }


    public int generateRepayment(LoanApplication loanApplication){
        log.info("生成还款计划请求");
        LoanProduct product = userLoanMapper.selectLoanProductById(loanApplication.getProductId());

        if(loanApplication.getApplicationStatus()!=2){
            log.info("该请求审核未通过");
            return 0;
        };

        Integer applicationId = loanApplication.getApplicationId();
        BigDecimal interest = product.getInterest();
        BigDecimal amount = loanApplication.getAmount();
        Integer period = product.getPeriod();
        LocalDate startDate = loanApplication.getApplyTime().toLocalDate();
        BigDecimal penalty = product.getPenalty();

        if(loanApplication.getInterestWay()==1){
            List<Repayment> plans = calculateEqualInstallment(applicationId,interest,amount,period,startDate);
            try {
                int i = userLoanMapper.insertRepaymentPlan(plans);
                if (i>0){
                    log.info("添加还款计划成功");
                    return 1;
                }
                else {
                    log.info("添加还款计划失败1");
                    return 0;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                log.info("添加还款计划失败");
                return 0;
            }
        }
        else {
            List<Repayment> plans = calculateEqualPrincipal(applicationId,interest,amount,period,startDate);
            try {
                int i = userLoanMapper.insertRepaymentPlan(plans);
                if (i>0){
                    log.info("添加还款计划成功");
                    return 1;
                }
                else {
                    log.info("添加还款计划失败2");
                    return 0;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                log.info("添加还款计划失败");
                return 0;
            }
        }
    }


    //等额本息
    private List<Repayment> calculateEqualInstallment(
            Integer applicationId,
            BigDecimal interest,
            BigDecimal amount,
            Integer period,
            LocalDate startDate
    )
    {
        List<Repayment> repaymentList = new ArrayList<>();

        try {

            //转化为月利率
            BigDecimal monthlyInterestRate=interest.divide(BigDecimal.valueOf(12),10,DECIMAL_SCALE);

            // 等额本息计算公式：每月还款额 = [本金 × 月利率 × (1+月利率)^期数] ÷ [(1+月利率)^期数 - 1]

            // 计算 (1+月利率)^期数
            BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyInterestRate);
            BigDecimal power = BigDecimal.ONE;
            for (int i = 0; i < period; i++) {
                power = power.multiply(onePlusRate);
            }

            // 计算每月还款额
            BigDecimal monthlyPayment = amount
                    .multiply(monthlyInterestRate)
                    .multiply(power)
                    .divide(power.subtract(BigDecimal.ONE), DECIMAL_SCALE, DECIMAL_ROUNDING);

            // 剩余本金
            BigDecimal remainingPrincipal = amount;

            for (int i = 1; i <= period; i++) {
                // 计算本月利息 = 剩余本金 × 月利率
                BigDecimal monthlyInterest = remainingPrincipal
                        .multiply(monthlyInterestRate)
                        .setScale(DECIMAL_SCALE, DECIMAL_ROUNDING);

                // 计算本月本金 = 月还款额 - 本月利息
                BigDecimal monthlyPrincipal = monthlyPayment.subtract(monthlyInterest);

                // 最后一期调整，确保本金总额准确
                if (i == period) {
                    monthlyPrincipal = remainingPrincipal;
                    monthlyPayment = monthlyPrincipal.add(monthlyInterest);
                }

                // 计算本期还款日期（每月一期）
                LocalDate dueDate = startDate.plusMonths(i - 1);

                // 创建还款计划
                Repayment repayment = new Repayment();
                repayment.setApplicationId(applicationId);
                repayment.setPeriodNumber(i);
                repayment.setDueDate(dueDate);
                repayment.setAmount(monthlyPayment);
                repayment.setPrincipal(monthlyPrincipal);
                repayment.setStatus(0); // 0-未还款
                // 实际还款日期和已还款金额初始为null和0
                repayment.setPaidAmount(BigDecimal.ZERO);

                repaymentList.add(repayment);

                // 更新剩余本金
                remainingPrincipal = remainingPrincipal.subtract(monthlyPrincipal);
            }
        } catch (Exception e) {
            log.error("计算等额本息还款计划失败", e);
        }
        return  repaymentList;
    }

    //等额本金
    private List<Repayment> calculateEqualPrincipal(
            Integer applicationId,
            BigDecimal interest,
            BigDecimal amount,
            Integer period,
            LocalDate startDate
    ) {

        List<Repayment> repaymentList = new ArrayList<>();

        try {
            // 参数校验
            if (applicationId == null || interest == null || amount == null ||
                    period == null || period <= 0 || startDate == null) {
                log.error("参数校验失败，参数不能为空且期数必须大于0");
                return repaymentList;
            }

            // 将年利率转换为月利率
            BigDecimal monthlyInterestRate = interest
                    .divide(BigDecimal.valueOf(12), 10, DECIMAL_ROUNDING); // 年利率转月利率

            // 每月固定本金
            BigDecimal monthlyPrincipal = amount
                    .divide(BigDecimal.valueOf(period), DECIMAL_SCALE, DECIMAL_ROUNDING);

            // 剩余本金
            BigDecimal remainingPrincipal = amount;

            for (int i = 1; i <= period; i++) {
                // 计算本月利息 = 剩余本金 × 月利率
                BigDecimal monthlyInterest = remainingPrincipal
                        .multiply(monthlyInterestRate)
                        .setScale(DECIMAL_SCALE, DECIMAL_ROUNDING);

                // 计算本期还款总额 = 固定本金 + 本月利息
                BigDecimal monthlyPayment = monthlyPrincipal.add(monthlyInterest);

                // 最后一期调整，确保本金总额准确
                if (i == period) {
                    monthlyPrincipal = remainingPrincipal;
                    monthlyPayment = monthlyPrincipal.add(monthlyInterest);
                }

                // 计算本期还款日期（每月一期）
                LocalDate dueDate = startDate.plusMonths(i - 1);

                // 创建还款计划
                Repayment repayment = new Repayment();
                repayment.setApplicationId(applicationId);
                repayment.setPeriodNumber(i);
                repayment.setDueDate(dueDate);
                repayment.setAmount(monthlyPayment);
                repayment.setPrincipal(monthlyPrincipal);
                repayment.setStatus(0); // 0-未还款
                // 实际还款日期和已还款金额初始为null和0
                repayment.setPaidAmount(BigDecimal.ZERO);

                repaymentList.add(repayment);

                // 更新剩余本金
                remainingPrincipal = remainingPrincipal.subtract(monthlyPrincipal);
            }

        } catch (Exception e) {
            log.error("计算等额本金还款计划失败", e);
        }

        return repaymentList;
    }


    //计算罚息
    public BigDecimal calculatePenalty(Integer overdueDays, BigDecimal overdueAmount, BigDecimal penaltyRate) {
        if (overdueDays <= 0 || overdueAmount == null || penaltyRate == null) {
            return BigDecimal.ZERO;
        }

        // 假设罚息率是日罚息率
        BigDecimal dailyPenaltyRate = penaltyRate
                .divide(BigDecimal.valueOf(100), 10, DECIMAL_ROUNDING);

        return overdueAmount
                .multiply(dailyPenaltyRate)
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(DECIMAL_SCALE, DECIMAL_ROUNDING);
    }

    //更新还款状态
    public void updateRepaymentStatus(Repayment repayment, BigDecimal paymentAmount, LocalDate actualPaymentDate) {
        if (repayment == null || paymentAmount == null) {
            return;
        }

        // 更新已还款金额
        BigDecimal newPaidAmount = repayment.getPaidAmount().add(paymentAmount);
        repayment.setPaidAmount(newPaidAmount);

        // 如果已还金额 >= 应还金额，标记为已还款
        if (newPaidAmount.compareTo(repayment.getAmount()) >= 0) {
            repayment.setStatus(1); // 1-已还款
            repayment.setActualPaymentDate(actualPaymentDate);

            // 如果有超额还款，可以在这里处理（例如转到下一期或作为预付款）
        } else if (actualPaymentDate != null && actualPaymentDate.isAfter(repayment.getDueDate())) {
            // 还款日期晚于应还日期，标记为逾期
            repayment.setStatus(2); // 2-逾期
        }
    }
}
