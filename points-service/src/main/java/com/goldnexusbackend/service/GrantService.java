package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsBatch;
import com.goldnexusbackend.entity.PointsDailyQuota;
import com.goldnexusbackend.entity.PointsFlow;
import com.goldnexusbackend.entity.PointsRule;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsDailyQuotaMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsRuleMapper;
import com.goldnexusbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 通用积分发放引擎：所有获得渠道(注册/实名/签到/结清)的唯一入口。
 * 事务内完成：规则校验 -> 幂等写流水 -> 原子扣日额度 -> 写批次 -> 更新余额镜像。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GrantService {

    @Value("${points.expire-days:365}")
    private int expireDays;   // 积分有效期(天)
    @Value("${points.daily-limit:100}")
    private int dailyLimit;   // 单日发放上限
    @Value("${points.balance-cap:500000}")
    private int balanceCap;   // 账户余额上限

    private final PointsRuleMapper ruleMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsBatchMapper batchMapper;
    private final PointsAccountMapper accountMapper;
    private final PointsDailyQuotaMapper quotaMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void grant(String eventType, String bizNo, Integer userId, Integer points) {
        // 1) 规则校验
        PointsRule rule = ruleMapper.selectByEventType(eventType);
        if (rule == null) {
            log.warn("积分规则不存在, 忽略: {}", eventType);
            return;
        }
        // 未显式指定分值(或非法)时, 以规则表为准(REGISTER/REAL_NAME 等固定分值)
        if (points == null || points <= 0) {
            points = rule.getPoints();
        }
        // 实名门槛
        if (rule.getRequireVerified() != null && rule.getRequireVerified() == 1) {
            User user = userMapper.selectUserById(userId);
            if (user == null || user.getAuthentication() == null || user.getAuthentication() != 1) {
                log.warn("用户 {} 未实名, 不发分: {}", userId, eventType);
                return;
            }
        }
        // 账户校验(不存在则自动开户)
        PointsAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            account = createAccount(userId);
        } else if (account.getStatus() != 1) {
            log.warn("账户 {} 状态异常, 不发分", userId);
            return;
        }
        // 余额上限(预检, 友好提示)
        if (account.getBalance() + points > balanceCap) {
            log.warn("用户 {} 余额将超上限, 不发分", userId);
            return;
        }
        // 2) 幂等: 写流水(唯一索引 uk_biz(bizType,bizNo) 兜底)
        try {
            PointsFlow flow = new PointsFlow();
            flow.setUserId(userId);
            flow.setFlowType("EARN");
            flow.setBizType(eventType);
            flow.setBizNo(bizNo);
            flow.setAmount(points);
            flow.setRemark(rule.getName());
            flowMapper.insert(flow);
        } catch (DuplicateKeyException e) {
            log.info("积分已发放, 幂等拦截: {} {}", eventType, bizNo);
            return;
        }
        // 3) 原子扣日额度(条件 UPDATE, 影响行数=0 表示已达上限)
        LocalDate today = LocalDate.now();
        ensureDailyQuota(userId, today);
        if (quotaMapper.addUsed(userId, today, points) == 0) {
            throw new RuntimeException("已达单日积分上限");
        }
        // 4) 写批次(expiresAt = earnedAt + 365天)
        LocalDateTime now = LocalDateTime.now();
        PointsBatch batch = new PointsBatch();
        batch.setUserId(userId);
        batch.setSourceType(eventType);
        batch.setSourceBizNo(bizNo);
        batch.setPointsEarned(points);
        batch.setPointsRemaining(points);
        batch.setEarnedAt(now);
        batch.setExpiresAt(now.plusDays(expireDays));
        batch.setStatus(1);
        batchMapper.insert(batch);
        // 5) 更新余额镜像(条件 UPDATE 原子兜底防超上限)
        if (accountMapper.addEarnedWithCap(userId, points, balanceCap) == 0) {
            throw new RuntimeException("已达账户余额上限");
        }
    }

    private PointsAccount createAccount(Integer userId) {
        PointsAccount account = new PointsAccount();
        account.setUserId(userId);
        account.setStatus(1);
        account.setBalance(0);
        account.setTotalEarned(0);
        account.setTotalSpent(0);
        try {
            accountMapper.insert(account);
        } catch (DuplicateKeyException e) {
            // 并发开户已存在, 重新查询
            return accountMapper.selectByUserId(userId);
        }
        return account;
    }

    private void ensureDailyQuota(Integer userId, LocalDate today) {
        PointsDailyQuota q = new PointsDailyQuota();
        q.setUserId(userId);
        q.setQuotaDate(today);
        q.setUsed(0);
        q.setDailyLimit(dailyLimit);
        try {
            quotaMapper.insert(q);
        } catch (DuplicateKeyException e) {
            // 当日额度行已存在, 忽略
        }
    }
}
