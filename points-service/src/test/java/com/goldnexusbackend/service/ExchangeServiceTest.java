package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsCodeMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsOrderMapper;
import com.goldnexusbackend.mapper.PointsProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExchangeService 单元测试：积分不足拒绝兑换。
 */
@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock PointsProductMapper productMapper;
    @Mock PointsOrderMapper orderMapper;
    @Mock PointsBatchMapper batchMapper;
    @Mock PointsFlowMapper flowMapper;
    @Mock PointsAccountMapper accountMapper;
    @Mock PointsCodeMapper codeMapper;

    @InjectMocks ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exchangeService, "minPoints", 100);
    }

    @Test
    void testExchangeInsufficientBalance() {
        when(orderMapper.selectByRequestId(1, "req-1")).thenReturn(null);

        PointsProduct product = new PointsProduct();
        product.setId(1);
        product.setName("保温杯");
        product.setItemType("PHYSICAL");
        product.setPointsPrice(500);
        product.setStatus(1);
        product.setStock(10);
        when(productMapper.selectById(1)).thenReturn(product);

        PointsAccount account = new PointsAccount();
        account.setUserId(1);
        account.setStatus(1);
        account.setBalance(100); // 不足 500
        when(accountMapper.selectByUserId(1)).thenReturn(account);

        Res res = exchangeService.exchange("req-1", 1, "Beijing", 1);

        assertEquals(500, res.getCode());
        assertEquals("积分不足", res.getMsg());
        // 未进入扣批次/扣库存/建单
        verify(batchMapper, never()).selectAvailableForUpdate(any());
        verify(productMapper, never()).deductStock(any());
        verify(orderMapper, never()).insert(any());
    }
}
