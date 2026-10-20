package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.PointsProduct;
import com.goldnexusbackend.entity.PointsRule;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsRuleMapper;
import com.goldnexusbackend.service.AdminPointsService;
import com.goldnexusbackend.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端接口（/goldnexus/admin/points/**，isAdmin 校验在 service 内）。
 */
@RestController
@RequestMapping("/goldnexus/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {

    private final AdminProductService adminProductService;
    private final AdminPointsService adminPointsService;
    private final PointsRuleMapper ruleMapper;

    // ---- 商品管理 ----
    @PostMapping("/product/save")
    public Res productSave(@RequestBody PointsProduct product) {
        return adminProductService.save(product);
    }

    @PostMapping("/product/onoff")
    public Res productOnoff(@RequestBody Map<String, Integer> req) {
        return adminProductService.onoff(req.get("productId"), req.get("status"));
    }

    @PostMapping("/product/price")
    public Res productPrice(@RequestBody Map<String, Integer> req) {
        return adminProductService.price(req.get("productId"), req.get("newPrice"));
    }

    // ---- 规则管理 ----
    @GetMapping("/rule")
    public Res ruleList() {
        return new Res(200, "查询成功", ruleMapper.selectAll());
    }

    @PostMapping("/rule/update")
    public Res ruleUpdate(@RequestBody PointsRule rule) {
        return adminPointsService.updateRule(rule);
    }

    // ---- 订单管理 ----
    @PostMapping("/order/ship")
    public Res ship(@RequestBody Map<String, String> req) {
        return adminPointsService.ship(req.get("orderNo"), req.get("logisticsNo"));
    }

    @PostMapping("/order/codeVerify")
    public Res codeVerify(@RequestBody Map<String, String> req) {
        return adminPointsService.codeVerify(req.get("codeValue"));
    }

    // ---- 用户积分管理 ----
    @GetMapping("/query")
    public Res query(@RequestParam Integer userId) {
        return adminPointsService.queryUser(userId);
    }

    @PostMapping("/adjust")
    public Res adjust(@RequestBody Map<String, Object> req) {
        Integer userId = req.get("userId") == null ? null : ((Number) req.get("userId")).intValue();
        String changeType = (String) req.get("changeType");
        Integer amount = req.get("amount") == null ? null : ((Number) req.get("amount")).intValue();
        String reason = (String) req.get("reason");
        return adminPointsService.adjust(userId, changeType, amount, reason);
    }

    @PostMapping("/freeze")
    public Res freeze(@RequestBody Map<String, Integer> req) {
        return adminPointsService.freeze(req.get("userId"));
    }

    @PostMapping("/unfreeze")
    public Res unfreeze(@RequestBody Map<String, Integer> req) {
        return adminPointsService.unfreeze(req.get("userId"));
    }

    @PostMapping("/clear")
    public Res clear(@RequestBody Map<String, Integer> req) {
        return adminPointsService.clear(req.get("userId"));
    }
}
