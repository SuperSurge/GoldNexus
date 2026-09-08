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
 * 智能客服 — 用户端
 */
@RestController
@RequestMapping("/goldnexus/user/cs")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final CustomerService customerService;

    /** 用户发送消息 */
    @PostMapping("/send")
    public Res sendMessage(@RequestBody Map<String, String> request) {
        return customerService.userSendMessage(request.get("content"));
    }

    /** 用户获取聊天记录 */
    @PostMapping("/history")
    public Res getHistory() {
        return customerService.userGetHistory();
    }
}
