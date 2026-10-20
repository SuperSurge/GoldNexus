package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsAdjust;
import com.goldnexusbackend.entity.PointsBatch;
import com.goldnexusbackend.entity.PointsCode;
import com.goldnexusbackend.entity.PointsFlow;
import com.goldnexusbackend.entity.PointsOrder;
import com.goldnexusbackend.entity.PointsRule;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsAdjustMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsCodeMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsOrderMapper;
import com.goldnexusbackend.mapper.PointsRuleMapper;
import com.goldnexusbackend.mapper.PointsSignMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端：规则维护、订单发货/核销、用户积分查询/调账/冻结/解冻/清零。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPointsService {

    private final PointsRuleMapper ruleMapper;
    private final PointsOrderMapper orderMapper;
    private final PointsCodeMapper codeMapper;
    private final PointsAccountMapper accountMapper;
    private final PointsBatchMapper batchMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsSignMapper signMapper;
    private final PointsAdjustMapper adjustMapper;

    // 规则维护
    @Transactional(rollbackFor = Exception.class)
    public Res updateRule(PointsRule rule) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        if (rule.getId() == null) return new Res(500, "缺少规则id", null);
        ruleMapper.update(rule);
        return new Res(200, "规则更新成功", null);
    }

    // 实体发货
    @Transactional(rollbackFor = Exception.class)
    public Res ship(String orderNo, String logisticsNo) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        PointsOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) return new Res(500, "订单不存在", null);
        if (logisticsNo == null || logisticsNo.isBlank()) return new Res(500, "请填写物流单号", null);
        orderMapper.ship(orderNo, logisticsNo);
        return new Res(200, "发货成功", null);
    }

    // 核销兑换码
    @Transactional(rollbackFor = Exception.class)
    public Res codeVerify(String codeValue) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        PointsCode code = codeMapper.selectByCode(codeValue);
        if (code == null) return new Res(500, "兑换码不存在", null);
        if (codeMapper.verify(codeValue) == 0) return new Res(500, "核销失败(状态异常)", null);
        return new Res(200, "核销成功", null);
    }

    // 查询用户积分(账户/批次/流水/签到)
    public Res queryUser(Integer userId) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        Map<String, Object> data = new HashMap<>();
        data.put("account", accountMapper.selectByUserId(userId));
        data.put("batches", batchMapper.selectByUser(userId));
        data.put("flows", flowMapper.selectByUser(userId));
        data.put("signs", signMapper.selectByUser(userId));
        return new Res(200, "查询成功", data);
    }

    // 调账(留痕)
    @Transactional(rollbackFor = Exception.class)
    public Res adjust(Integer userId, String changeType, Integer amount, String reason) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        if (reason == null || reason.isBlank()) return new Res(500, "请填写调账原因", null);
        if (amount == null || amount <= 0) return new Res(500, "调账积分必须为正数", null);
        if (!"ADD".equals(changeType) && !"SUB".equals(changeType)) {
            return new Res(500, "changeType 必须为 ADD/SUB", null);
        }
        PointsAccount account = accountMapper.selectByUserId(userId);
        if (account == null) return new Res(500, "账户不存在", null);
        Integer adminId = currentAdminId();
        // 写调账单(留痕)
        PointsAdjust adjust = new PointsAdjust();
        adjust.setAdminId(adminId);
        adjust.setUserId(userId);
        adjust.setChangeType(changeType);
        adjust.setAmount(amount);
        adjust.setReason(reason);
        adjust.setStatus("APPROVED");
        adjustMapper.insert(adjust);
        // 写流水 + 更新余额
        PointsFlow flow = new PointsFlow();
        flow.setUserId(userId);
        flow.setBizType("ADJUST");
        flow.setBizNo("adjust-" + adjust.getId());
        flow.setOperatorId(adminId);
        flow.setRemark(reason);
        if ("ADD".equals(changeType)) {
            // 建批次(365天有效期), 保证余额=Σ批次
            LocalDateTime now = LocalDateTime.now();
            PointsBatch batch = new PointsBatch();
            batch.setUserId(userId);
            batch.setSourceType("ADJUST");
            batch.setSourceBizNo("adjust-" + adjust.getId());
            batch.setPointsEarned(amount);
            batch.setPointsRemaining(amount);
            batch.setEarnedAt(now);
            batch.setExpiresAt(now.plusDays(365));
            batch.setStatus(1);
            batchMapper.insert(batch);
            flow.setFlowType("ADJUST_ADD");
            flow.setAmount(amount);
            flowMapper.insert(flow);
            accountMapper.addEarned(userId, amount);
        } else {
            // 临期优先扣批次
            int remaining = amount;
            for (PointsBatch b : batchMapper.selectAvailableForUpdate(userId)) {
                if (remaining <= 0) break;
                int deduct = Math.min(b.getPointsRemaining(), remaining);
                batchMapper.deductRemaining(b.getId(), deduct);
                remaining -= deduct;
            }
            if (remaining > 0) {
                throw new RuntimeException("积分不足");
            }
            flow.setFlowType("ADJUST_SUB");
            flow.setAmount(-amount);
            flowMapper.insert(flow);
            accountMapper.deductBalance(userId, amount);
        }
        return new Res(200, "调账成功", null);
    }

    // 冻结
    @Transactional(rollbackFor = Exception.class)
    public Res freeze(Integer userId) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        accountMapper.updateStatus(userId, 2);
        return new Res(200, "已冻结", null);
    }

    // 解冻
    @Transactional(rollbackFor = Exception.class)
    public Res unfreeze(Integer userId) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        accountMapper.updateStatus(userId, 1);
        return new Res(200, "已解冻", null);
    }

    // 清零(写 REVOKE 流水留痕)
    @Transactional(rollbackFor = Exception.class)
    public Res clear(Integer userId) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        PointsAccount account = accountMapper.selectByUserId(userId);
        if (account == null) return new Res(500, "账户不存在", null);
        Integer adminId = currentAdminId();
        // 撤销所有可用批次(保证 余额=Σ批次 不变式)
        batchMapper.revokeAllByUser(userId);
        if (account.getBalance() != null && account.getBalance() > 0) {
            PointsFlow flow = new PointsFlow();
            flow.setUserId(userId);
            flow.setFlowType("REVOKE");
            flow.setBizType("CLEAR");
            flow.setBizNo("clear-" + System.currentTimeMillis());
            flow.setAmount(-account.getBalance());
            flow.setOperatorId(adminId);
            flow.setRemark("管理员清零");
            flowMapper.insert(flow);
        }
        accountMapper.clear(userId);
        return new Res(200, "已清零", null);
    }

    private Integer currentAdminId() {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        return cu == null ? null : cu.getId();
    }
}
