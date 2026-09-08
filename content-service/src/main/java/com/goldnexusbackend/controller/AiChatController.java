package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI智能客服 — 用户端
 */
@RestController
@RequestMapping("/goldnexus/user/aiChat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /** 用户发送消息给AI客服 */
    @PostMapping("/send")
    public Res sendMessage(@RequestBody Map<String, String> request) {
        return aiChatService.sendMessage(request.get("message"));
    }

    /** 用户获取AI聊天记录 */
    @PostMapping("/history")
    public Res getHistory() {
        return aiChatService.getHistory();
    }
}
