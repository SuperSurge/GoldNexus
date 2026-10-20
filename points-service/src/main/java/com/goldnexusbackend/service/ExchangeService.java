package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAccount;
import com.goldnexusbackend.entity.PointsBatch;
import com.goldnexusbackend.entity.PointsCode;
import com.goldnexusbackend.entity.PointsFlow;
import com.goldnexusbackend.entity.PointsOrder;
import com.goldnexusbackend.entity.PointsProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsAccountMapper;
import com.goldnexusbackend.mapper.PointsBatchMapper;
import com.goldnexusbackend.mapper.PointsCodeMapper;
import com.goldnexusbackend.mapper.PointsFlowMapper;
import com.goldnexusbackend.mapper.PointsOrderMapper;
import com.goldnexusbackend.mapper.PointsProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 兑换服务：校验 -> 幂等(requestId) -> 事务内 临期优先扣批次/扣库存/写SPEND流水/扣余额/建单；
 * 虚拟码 FOR UPDATE 锁码绑定。下单与支付合并为一步。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    @Value("${points.min-points:100}")
    private int minPoints; // 起付线

    private final PointsProductMapper productMapper;
    private final PointsOrderMapper orderMapper;
    private final PointsBatchMapper batchMapper;
    private final PointsFlowMapper flowMapper;
    private final PointsAccountMapper accountMapper;
    private final PointsCodeMapper codeMapper;

    @Transactional(rollbackFor = Exception.class)
    public Res exchange(String requestId, Integer productId, String addressInfo, Integer userId) {
        // 1) 幂等快速路径
        PointsOrder existing = orderMapper.selectByRequestId(userId, requestId);
        if (existing != null) {
            return orderResult(existing);
        }
        // 2) 商品校验
        PointsProduct product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return new Res(500, "商品不存在或已下架", null);
        }
        if (product.getPointsPrice() < minPoints) {
            return new Res(500, "积分不足起付线", null);
        }
        if ("VIP".equals(product.getItemType())) {
            return new Res(500, "VIP权益暂未开放", null);
        }
        // 3) 账户校验
        PointsAccount account = accountMapper.selectByUserId(userId);
        if (account == null || account.getStatus() != 1) {
            return new Res(500, "账户状态异常", null);
        }
        if (account.getBalance() < product.getPointsPrice()) {
            return new Res(500, "积分不足", null);
        }
        // 4) 实体商品校验收货地址与库存
        if ("PHYSICAL".equals(product.getItemType())) {
            if (addressInfo == null || addressInfo.isBlank()) {
                return new Res(500, "请填写收货地址", null);
            }
            if (product.getStock() <= 0) {
                return new Res(500, "库存不足", null);
            }
        }
        int cost = product.getPointsPrice();
        String orderNo = generateOrderNo();
        // 5) 事务内扣减: 临期优先扣批次
        deductBatches(userId, cost);
        // 6) 扣库存(实体, 条件 UPDATE 防超卖)
        if ("PHYSICAL".equals(product.getItemType()) && productMapper.deductStock(productId) == 0) {
            throw new RuntimeException("库存不足");
        }
        // 7) 写 SPEND 流水 + 扣余额(条件 UPDATE 防超扣)
        PointsFlow flow = new PointsFlow();
        flow.setUserId(userId);
        flow.setFlowType("SPEND");
        flow.setBizType("EXCHANGE");
        flow.setBizNo(orderNo);
        flow.setAmount(-cost);
        flow.setRemark("兑换:" + product.getName());
        flowMapper.insert(flow);
        if (accountMapper.deductSpent(userId, cost) == 0) {
            throw new RuntimeException("积分不足");
        }
        // 8) 建单(uk_request 幂等)
        PointsOrder order = new PointsOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setPointsCost(cost);
        order.setItemType(product.getItemType());
        order.setStatus("PAID");
        order.setAddressInfo(addressInfo);
        order.setRequestId(requestId);
        // 9) 虚拟码: 锁码绑定
        if ("VIRTUAL_CODE".equals(product.getItemType())) {
            PointsCode code = codeMapper.selectAvailableForUpdate(productId);
            if (code == null) {
                throw new RuntimeException("兑换码库存不足");
            }
            codeMapper.lockCode(code.getCodeValue(), userId, orderNo);
            order.setCodeValue(code.getCodeValue());
        }
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("请勿重复提交");
        }
        return orderResult(order);
    }

    private Res orderResult(PointsOrder order) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("codeValue", order.getCodeValue());
        return new Res(200, "兑换成功", data);
    }

    // 临期优先扣批次(先到期先用)
    private void deductBatches(Integer userId, int cost) {
        List<PointsBatch> batches = batchMapper.selectAvailableForUpdate(userId);
        int remaining = cost;
        for (PointsBatch b : batches) {
            if (remaining <= 0) break;
            int deduct = Math.min(b.getPointsRemaining(), remaining);
            batchMapper.deductRemaining(b.getId(), deduct);
            remaining -= deduct;
        }
        if (remaining > 0) {
            throw new RuntimeException("积分不足");
        }
    }

    private String generateOrderNo() {
        return "PO" + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000);
    }
}
