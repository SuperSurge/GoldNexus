package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsBatch;
import com.goldnexusbackend.entity.PointsFlow;
import com.goldnexusbackend.entity.PointsMessage;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分过期与提醒定时任务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpireService {

    private final PointsBatchMapper batchMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsAccountMapper accountMapper;
    private final PointsMessageMapper messageMapper;

    // 每日 00:30 批量过期
    @Scheduled(cron = "0 30 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireTask() {
        List<PointsBatch> expired = batchMapper.selectExpired();
        if (expired.isEmpty()) {
            return;
        }
        // 批量置过期(一条 UPDATE, 不逐行)
        batchMapper.batchExpire();
        for (PointsBatch b : expired) {
            // EXPIRE 流水(只增不改)
            PointsFlow flow = new PointsFlow();
            flow.setUserId(b.getUserId());
            flow.setBatchId(b.getId());
            flow.setFlowType("EXPIRE");
            flow.setBizType("EXPIRE");
            flow.setBizNo("expire-" + b.getId());
            flow.setAmount(-b.getPointsRemaining());
            flow.setRemark("积分过期");
            flowMapper.insert(flow);
            // 扣余额镜像(校验影响行数, 失败则回滚避免账本不一致)
            if (accountMapper.deductBalance(b.getUserId(), b.getPointsRemaining()) == 0) {
                log.warn("过期扣减失败(余额不足): userId={}, batchId={}", b.getUserId(), b.getId());
                throw new RuntimeException("积分账本不一致");
            }
        }
        log.info("过期任务完成: 处理 {} 个批次", expired.size());
    }

    // 每日 09:00 过期提醒(扫 7 天内过期, 按用户汇总, 去重)
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void remindTask() {
        List<PointsBatch> expiring = batchMapper.selectAllExpiring(7);
        Map<Integer, Integer> userPoints = new HashMap<>();
        for (PointsBatch b : expiring) {
            userPoints.merge(b.getUserId(), b.getPointsRemaining(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> e : userPoints.entrySet()) {
            // 简单去重: 已有未读提醒则跳过
            if (messageMapper.countUnreadExpiring(e.getKey()) > 0) {
                continue;
            }
            PointsMessage msg = new PointsMessage();
            msg.setUserId(e.getKey());
            msg.setType("POINT_EXPIRING");
            msg.setContent("您有 " + e.getValue() + " 积分将于 7 天内过期, 请尽快使用");
            msg.setIsRead(0);
            messageMapper.insert(msg);
        }
        log.info("提醒任务完成: 提醒 {} 个用户", userPoints.size());
    }
}
