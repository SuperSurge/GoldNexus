package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.ExpireService;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运维/测试端点：手动触发积分过期与过期提醒定时任务（默认由 cron 在 00:30 / 09:00 自动执行）。
 */
@RestController
@RequestMapping("/goldnexus/admin/points")
@RequiredArgsConstructor
public class OpsController {

    private final ExpireService expireService;

    @PostMapping("/ops/expire")
    public Res expire() {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        expireService.expireTask();
        return new Res(200, "已触发过期任务", null);
    }

    @PostMapping("/ops/remind")
    public Res remind() {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        expireService.remindTask();
        return new Res(200, "已触发提醒任务", null);
    }
}
