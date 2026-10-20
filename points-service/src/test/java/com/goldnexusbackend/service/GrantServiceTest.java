package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsRule;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsDailyQuotaMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsRuleMapper;
import com.goldnexusbackend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GrantService 单元测试：幂等拦截 + 余额上限。
 */
@ExtendWith(MockitoExtension.class)
class GrantServiceTest {

    @Mock PointsRuleMapper ruleMapper;
    @Mock PointsFlowMapper flowMapper;
    @Mock PointsBatchMapper batchMapper;
    @Mock PointsAccountMapper accountMapper;
    @Mock PointsDailyQuotaMapper quotaMapper;
    @Mock UserMapper userMapper;

    @InjectMocks GrantService grantService;

    @BeforeEach
    void setUp() {
        // @Value 字段在单元测试中不注入, 手动设置
        ReflectionTestUtils.setField(grantService, "expireDays", 365);
        ReflectionTestUtils.setField(grantService, "dailyLimit", 100);
        ReflectionTestUtils.setField(grantService, "balanceCap", 500000);
    }

    @Test
    void testGrantIdempotency() {
        PointsRule rule = new PointsRule();
        rule.setEventType("REGISTER");
        rule.setRequireVerified(0);
        rule.setName("注册奖励");
        when(ruleMapper.selectByEventType("REGISTER")).thenReturn(rule);

        PointsAccount account = new PointsAccount();
        account.setUserId(1);
        account.setStatus(1);
        account.setBalance(0);
        when(accountMapper.selectByUserId(1)).thenReturn(account);

        // 流水已存在 -> 唯一索引冲突, 模拟重复发放
        when(flowMapper.insert(any())).thenThrow(new DuplicateKeyException("dup"));

        grantService.grant("REGISTER", "1", 1, 20);

        // 幂等拦截: 不应继续扣日额度/建批次/加余额
        verify(quotaMapper, never()).addUsed(any(), any(), any());
        verify(batchMapper, never()).insert(any());
        verify(accountMapper, never()).addEarnedWithCap(any(), any(), any());
    }

    @Test
    void testGrantBalanceCapReached() {
        PointsRule rule = new PointsRule();
        rule.setEventType("REGISTER");
        rule.setRequireVerified(0);
        when(ruleMapper.selectByEventType("REGISTER")).thenReturn(rule);

        PointsAccount account = new PointsAccount();
        account.setUserId(1);
        account.setStatus(1);
        account.setBalance(499999); // 再发 20 将超 50 万
        when(accountMapper.selectByUserId(1)).thenReturn(account);

        grantService.grant("REGISTER", "1", 1, 20);

        // 余额将超上限: 不发分, 不写流水
        verify(flowMapper, never()).insert(any());
    }
}
