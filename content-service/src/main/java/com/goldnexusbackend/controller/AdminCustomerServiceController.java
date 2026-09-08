package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能客服 — 管理员端
 */
@RestController
@RequestMapping("/goldnexus/admin/cs")
@RequiredArgsConstructor
public class AdminCustomerServiceController {

    private final CustomerService customerService;

    /** 管理员获取发起过对话的用户列表 */
    @PostMapping("/userList")
    public Res getUserList() {
        return customerService.adminGetUserList();
    }

    /** 管理员查看与某用户的聊天记录 */
    @PostMapping("/history")
    public Res getHistory(@RequestBody Map<String, Integer> request) {
        return customerService.adminGetHistory(request.get("userId"));
    }

    /** 管理员回复用户 */
    @PostMapping("/send")
    public Res sendMessage(@RequestBody Map<String, Object> request) {
        return customerService.adminSendMessage(request);
    }
}
