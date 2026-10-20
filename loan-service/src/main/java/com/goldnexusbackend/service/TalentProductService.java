package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.TalentProduct;
import com.goldnexusbackend.entity.TalentProductApplication;
import com.goldnexusbackend.entity.TalentVerificationRecord;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.mapper.TalentProductMapper;
import com.goldnexusbackend.mapper.TalentVerificationMapper;
import com.goldnexusbackend.mapper.UserLoanMapper;
import com.goldnexusbackend.mapper.UserMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TalentProductService {

    private final TalentProductMapper talentProductMapper;
    private final TalentVerificationMapper talentVerificationMapper;
    private final UserLoanMapper userLoanMapper;
    private final UserMapper userMapper;

    @Value("${talent.graduate-years:2}")
    private int graduateYears;

    //列出上架的应届生产品
    public Res listProducts() {
        Res res = new Res();
        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(talentProductMapper.selectOnShelf());
        return res;
    }

    //申请应届生产品：校验产品状态、认证状态、应届年限、金额/期限范围，通过后写入 loan_application 走统一审批+还款流程
    @Transactional
    public Res apply(TalentProductApplication application) {
        Res res = new Res();

        CurrentUser user = SecurityContextHelper.getCurrentUser();
        if (user == null) {
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        TalentProduct product = talentProductMapper.selectById(application.getProductId());
        if (product == null) {
            res.setCode(500);
            res.setMsg("产品不存在");
            res.setData(null);
            return res;
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            res.setCode(500);
            res.setMsg("产品已下架");
            res.setData(null);
            return res;
        }

        //要求应届生认证的产品，校验认证状态 + 应届年限
        Integer verificationId = null;
        if (product.getRequireVerification() != null && product.getRequireVerification() == 1) {
            TalentVerificationRecord record = talentVerificationMapper.selectLatestSuccessByUserId(user.getId());
            if (record == null) {
                res.setCode(500);
                res.setMsg("请先完成应届生认证");
                res.setData(null);
                return res;
            }
            LocalDate graduateDate = record.getGraduateDate();
            if (graduateDate == null || graduateDate.isBefore(LocalDate.now().minusYears(graduateYears))) {
                res.setCode(500);
                res.setMsg("您的毕业时间已超过应届范围");
                res.setData(null);
                return res;
            }
            verificationId = record.getId();
        }

        //金额/期限范围校验
        if (application.getAmount() == null
                || (product.getAmountMin() != null && application.getAmount().compareTo(product.getAmountMin()) < 0)
                || (product.getAmountMax() != null && application.getAmount().compareTo(product.getAmountMax()) > 0)) {
            res.setCode(500);
            res.setMsg("申请金额超出产品范围");
            res.setData(null);
            return res;
        }
        if (application.getTerm() == null
                || (product.getTermMin() != null && application.getTerm() < product.getTermMin())
                || (product.getTermMax() != null && application.getTerm() > product.getTermMax())) {
            res.setCode(500);
            res.setMsg("申请期限超出产品范围");
            res.setData(null);
            return res;
        }

        //写入 loan_application，复用已有审批(applicationStatus)+还款计划+还款+罚息全流程
        User applyUser = userMapper.selectUserById(user.getId());
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setProductId(application.getProductId());
        loanApplication.setName(applyUser.getName());
        loanApplication.setUserId(user.getId());
        loanApplication.setAmount(application.getAmount());
        loanApplication.setInterestWay(1); // 优才默认等额本息
        loanApplication.setApplyTime(LocalDateTime.now());
        loanApplication.setApplicationStatus(1); // 待审核
        loanApplication.setRepayStatus(0);
        loanApplication.setProductType(1); // 优才
        loanApplication.setTerm(application.getTerm());
        loanApplication.setVerificationId(verificationId);

        try {
            int i = userLoanMapper.insertLoanApplication(loanApplication);
            if (i > 0) {
                res.setCode(200);
                res.setMsg("申请已提交，等待审核");
                res.setData(null);
            } else {
                res.setCode(500);
                res.setMsg("申请提交失败");
                res.setData(null);
            }
        } catch (Exception e) {
            log.error("应届生产品申请失败", e);
            res.setCode(500);
            res.setMsg("申请提交失败");
            res.setData(null);
        }
        return res;
    }
}
