package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Check;
import com.goldnexusbackend.entity.LoanApplication;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.AdminLoanMapper;
import com.goldnexusbackend.service.AdminLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminLoanController {
    private final AdminLoanService adminLoanService;

    @PostMapping("/allLoanApplication")
    public Res allLoanApplication(){
        return adminLoanService.allLoanApplication();
    }

    @PostMapping("/selectLoanApplicationById")
    public Res selectLoanApplicationById(@RequestBody Map<String,Integer> request){
        return adminLoanService.selectLoanApplicationById(request.get("applicationId"));
    }

    @PostMapping("/selectLoanApplicationByName")
    public Res selectLoanApplicationByName(@RequestBody Map<String,String> request){
        return adminLoanService.selectLoanApplicationByName(request.get("name"));
    }

    @PostMapping("/checkAllApplication")
    public Res checkAllApplication(){
        return adminLoanService.CheckAllApplication();
    }

    @PostMapping("/checkApplication")
    public Res checkApplication(@RequestBody Map<String, Integer> request){
        return adminLoanService.CheckApplication(request.get("applicationId"));
    }

    @PostMapping("/check")
    public Res check(@RequestBody Check check){
        return adminLoanService.Check(check);
    }

    @PostMapping("/repaymentPlan")
    public Res repaymentPlan(@RequestBody Map<String,Integer> request){
        return adminLoanService.repaymentPlan(request.get("applicationId"));
    }
}
