package com.goldnexusbackend.controller;
import com.goldnexusbackend.service.UserPenaltyService;
import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.UserLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class UserLoanController {
    private final UserLoanService userLoanService;
    private final UserPenaltyService userPenaltyService;

    @PostMapping("/products")
    public Res selectAllProducts() {
        return userLoanService.selectAllProducts();
    }

    @PostMapping("/loanApply")
    public Res loanApply(@RequestBody LoanApplication loanApplication) {
        return userLoanService.loanApply(loanApplication);
    }

    @PostMapping("/recordApplication")
    public Res recordApplication(){
        return userLoanService.recordApplication();
    }

    @PostMapping("/repayPlans")
    public Res repayPlans(@RequestBody Map<String, Integer> request) {
        Integer applicationId = request.get("applicationId");
        userPenaltyService.check_overdue();//查询还款计划时检查一遍是否逾期
        return userLoanService.repaymentPlan(applicationId);
    }

    @PostMapping("/applicationCount")
    public Res applicationCount() {
        return userLoanService.loanApplicationCountByUserId();
    }
}
