package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 对账：余额镜像 == Σ可用批次剩余 == Σ流水。三者一致才算平。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReconcileService {

    private final PointsAccountMapper accountMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsBatchMapper batchMapper;

    public boolean reconcile(Integer userId) {
        PointsAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            log.warn("账户不存在: {}", userId);
            return false;
        }
        Integer flowSum = flowMapper.sumAmount(userId);
        Integer batchSum = batchMapper.sumRemaining(userId);
        boolean ok = account.getBalance().equals(flowSum) && account.getBalance().equals(batchSum);
        if (!ok) {
            log.warn("对账不平: userId={}, balance={}, flowSum={}, batchSum={}",
                    userId, account.getBalance(), flowSum, batchSum);
        }
        return ok;
    }
}
