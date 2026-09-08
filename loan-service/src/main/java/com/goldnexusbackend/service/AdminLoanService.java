package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.mapper.AdminLoanMapper;
import com.goldnexusbackend.mapper.AdminUserMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLoanService {
    private final AdminLoanMapper adminLoanMapper;
    private final UserLoanService userLoanService;
    private final AdminUserMapper adminUserMapper;

    Res res=new Res();

    @Transactional
    public Res CheckAllApplication(){
        log.info("进行所有待审核申请查询请求");

        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        List<LoanApplication> loanApplications = adminLoanMapper.checkAllApplication();

        if(loanApplications.isEmpty()){
            res.setCode(200);
            res.setMsg("查询成功,待审核申请为空");
            log.info("查询成功,待审核申请为空");
            res.setData(null);
            return res;
        }
        else {
            res.setCode(200);
            res.setMsg("查询成功");
            log.info("查询成功");
            res.setData(adminLoanMapper.checkAllApplication());
            return res;
        }
    }

    @Transactional
    public Res CheckApplication(int applicationId){
        log.info("进行待审核申请查询");

        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }


        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(adminLoanMapper.checkApplication(applicationId));
        return res;
    }

    @Transactional
    public Res Check (Check check){
        log.info("进行审核请求");

        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        try{
            int i = adminLoanMapper.updateLoanApplicationStatus(check);
            if(i>0){
                if (check.getStatus()==2){
                    LoanApplication loanApplication = adminLoanMapper.selectLoanApplicationById(check.getApplicationId());
                    if(loanApplication!=null){
                        int i1 = userLoanService.generateRepayment(loanApplication);

                        if (i1==1){
                            res.setCode(200);
                            res.setMsg("审核通过，还款计划已生成");
                            log.info("审核通过，还款计划已生成");
                            res.setData(null);
                            return res;
                        }
                        else {
                            res.setCode(500);
                            res.setMsg("审核通过，还款计划生成异常");
                            log.info("审核通过，还款计划生成异常");
                            res.setData(null);
                            return res;
                        }
                    }else{
                        res.setCode(500);
                        res.setMsg("申请id不存在");
                        log.info("申请id不存在");
                        res.setData(null);
                        return res;
                    }
                }else{
                    res.setCode(200);
                    res.setMsg("审核不通过");
                    log.info("审核不通过");
                    res.setData(check.getMessage());
                    return res;
                }
            }

            else {
                res.setCode(500);
                res.setMsg("失败，内部错误1");
                log.info("失败，内部错误1");
                res.setData(null);
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("失败，内部错误2");
            log.info("失败，内部错误2");
            log.info(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res allLoanApplication(){
        log.info("进行查询所有申请请求");
        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(adminLoanMapper.allLoanApplication());
        return res;
    }

    @Transactional
    public Res selectLoanApplicationById(Integer applicationId){
        log.info("进行特定申请查询请求");
        LoanApplication loanApplication = adminLoanMapper.selectLoanApplicationById(applicationId);
        if(loanApplication!=null){
            res.setCode(200);
            res.setMsg("查询成功");
            log.info("查询成功");
            res.setData(loanApplication);
            return res;
        }else{
            res.setCode(500);
            res.setMsg("查询失败，产品id不存在");
            log.info("查询失败，产品id不存在");
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res selectLoanApplicationByName(String name) {
        log.info("进行通过名字查询申请请求");
        User user = adminUserMapper.selectUserByName(name);
        if (user==null){
            res.setCode(500);
            res.setMsg("用户不存在");
            log.info("用户不存在");
            res.setData(null);
            return res;
        }
        List<LoanApplication> loanApplications = adminLoanMapper.selectLoanApplicationByUserId(user.getId());
        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(loanApplications);
        return res;
    }

    @Transactional
    public Res repaymentPlan(Integer applicationId) {
        log.info("进行查询还款计划申请");

        List<Repayment> repaymentList = adminLoanMapper.adminSelectRepaymentPlanByApplicationId(applicationId);
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
}
