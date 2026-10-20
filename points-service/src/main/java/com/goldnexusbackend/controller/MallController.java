package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.ExchangeRequest;
import com.goldnexusbackend.entity.PointsOrder;
import com.goldnexusbackend.entity.PointsProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsOrderMapper;
import com.goldnexusbackend.mapper.PointsProductMapper;
import com.goldnexusbackend.service.ExchangeService;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端商城接口：商品列表/详情、兑换下单、我的订单。
 */
@RestController
@RequestMapping("/goldnexus/mall")
@RequiredArgsConstructor
public class MallController {

    private final PointsProductMapper productMapper;
    private final PointsOrderMapper orderMapper;
    private final ExchangeService exchangeService;

    // 商品列表(SQL 分页)
    @GetMapping("/products")
    public Res products(@RequestParam(defaultValue = "1") int pageNum,
                        @RequestParam(defaultValue = "10") int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        Map<String, Object> data = new HashMap<>();
        data.put("list", productMapper.selectOnShelfPage(offset, pageSize));
        data.put("total", productMapper.countOnShelf());
        return new Res(200, "查询成功", data);
    }

    // 商品详情
    @GetMapping("/products/{id}")
    public Res productDetail(@PathVariable Integer id) {
        PointsProduct p = productMapper.selectById(id);
        return new Res(200, "查询成功", p);
    }

    // 兑换下单(下单=支付合并为一步)
    @PostMapping("/order/create")
    public Res create(@RequestBody ExchangeRequest req) {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        if (cu == null) return new Res(401, "未登录", null);
        if (req.getRequestId() == null || req.getProductId() == null) {
            return new Res(500, "缺少参数 requestId/productId", null);
        }
        try {
            return exchangeService.exchange(req.getRequestId(), req.getProductId(), req.getAddressInfo(), cu.getId());
        } catch (RuntimeException e) {
            return new Res(500, e.getMessage(), null);
        }
    }

    // 我的订单(SQL 分页)
    @GetMapping("/order/list")
    public Res list(@RequestParam(defaultValue = "1") int pageNum,
                    @RequestParam(defaultValue = "10") int pageSize) {
        CurrentUser cu = SecurityContextHelper.getCurrentUser();
        if (cu == null) return new Res(401, "未登录", null);
        int offset = (pageNum - 1) * pageSize;
        Map<String, Object> data = new HashMap<>();
        data.put("list", orderMapper.selectByUserPage(cu.getId(), offset, pageSize));
        data.put("total", orderMapper.countByUser(cu.getId()));
        return new Res(200, "查询成功", data);
    }

    // 订单详情
    @GetMapping("/order/detail")
    public Res detail(@RequestParam String orderNo) {
        PointsOrder order = orderMapper.selectByOrderNo(orderNo);
        return new Res(200, "查询成功", order);
    }
}
