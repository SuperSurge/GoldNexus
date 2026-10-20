package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.GrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部发放接口（服务间直连，不经网关），供 user/loan 服务在业务成功后回调。
 */
@RestController
@RequestMapping("/goldnexus/internal")
@RequiredArgsConstructor
public class InternalGrantController {

    private final GrantService grantService;

    @PostMapping("/grant")
    public Res grant(@RequestBody Map<String, Object> body) {
        String eventType = (String) body.get("eventType");
        String bizNo = (String) body.get("bizNo");
        Integer userId = body.get("userId") == null ? null : ((Number) body.get("userId")).intValue();
        Integer points = body.get("points") == null ? null : ((Number) body.get("points")).intValue();
        if (eventType == null || bizNo == null || userId == null) {
            return new Res(500, "参数不完整", null);
        }
        try {
            grantService.grant(eventType, bizNo, userId, points);
            return new Res(200, "ok", null);
        } catch (RuntimeException e) {
            return new Res(500, e.getMessage(), null);
        }
    }
}
