package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsFlow;
import com.goldnexusbackend.entity.PointsRule;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsRuleMapper;
import com.goldnexusbackend.service.SignService;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端积分接口：账户总览 / 账单 / 规则 / 签到。
 */
@RestController
@RequestMapping("/goldnexus/points")
@RequiredArgsConstructor
public class UserPointsController {

    private final PointsAccountMapper accountMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsBatchMapper batchMapper;
    private final PointsRuleMapper ruleMapper;
    private final SignService signService;

    // 账户总览
    @GetMapping("/account/summary")
    public Res summary() {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        if (cu == null) return new Res(401, "未登录", null);
        Integer userId = cu.getId();
        PointsAccount account = accountMapper.selectByUserId(userId);
        Integer expiringSoon = batchMapper.sumExpiring(userId, 7);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", account == null ? 0 : account.getBalance());
        data.put("totalEarned", account == null ? 0 : account.getTotalEarned());
        data.put("totalSpent", account == null ? 0 : account.getTotalSpent());
        data.put("expiringSoon", expiringSoon == null ? 0 : expiringSoon);
        return new Res(200, "查询成功", data);
    }

    // 积分账单(按月, month=yyyy-MM, SQL 分页)
    @GetMapping("/account/bill")
    public Res bill(@RequestParam String month,
                    @RequestParam(defaultValue = "1") int pageNum,
                    @RequestParam(defaultValue = "10") int pageSize) {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        if (cu == null) return new Res(401, "未登录", null);
        YearMonth ym = YearMonth.parse(month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
        int offset = (pageNum - 1) * pageSize;
        Map<String, Object> data = new HashMap<>();
        data.put("list", flowMapper.selectByUserAndTimePage(cu.getId(), start, end, offset, pageSize));
        data.put("total", flowMapper.countByUserAndTime(cu.getId(), start, end));
        return new Res(200, "查询成功", data);
    }

    // 积分规则
    @GetMapping("/rules")
    public Res rules() {
        List<PointsRule> rules = ruleMapper.selectAll();
        return new Res(200, "查询成功", rules);
    }

    // 签到
    @PostMapping("/sign")
    public Res sign() {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        if (cu == null) return new Res(401, "未登录", null);
        try {
            return signService.sign(cu.getId());
        } catch (RuntimeException e) {
            return new Res(500, e.getMessage(), null);
        }
    }
}
