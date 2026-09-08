package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.AiChatMessage;
import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.AiChatMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final AiChatMapper aiChatMapper;

    private final Res res = new Res();

    /** DeepSeek API地址 */
    @Value("${deepseek.api.url}")
    private String apiUrl;

    /** DeepSeek API密钥 — 请替换为你的key */
    @Value("${deepseek.api.key}")
    private String apiKey;

    /** DeepSeek模型名称 */
    @Value("${deepseek.api.model}")
    private String model;

    /** 系统提示词 — 可在application.yml中按平台规则修改 */
    @Value("${deepseek.system-prompt}")
    private String systemPrompt;

    /** 携带的历史消息条数（user+assistant成对计算） */
    private static final int CONTEXT_MESSAGE_COUNT = 20;

    /**
     * 用户发送消息给AI客服，返回AI的回复
     */
    @Transactional
    public Res sendMessage(String content) {
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        if (content == null || content.trim().isEmpty()) {
            res.setCode(500);
            res.setMsg("消息不能为空");
            res.setData(null);
            return res;
        }

        // 1. 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setUser_id(currentUser.getId());
        userMessage.setRole("user");
        userMessage.setContent(content.trim());
        userMessage.setCreated_time(LocalDateTime.now());

        try {
            aiChatMapper.insertMessage(userMessage);
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("消息保存失败");
            res.setData(null);
            log.info("用户id={} AI消息保存失败: {}", currentUser.getId(), e.getMessage());
            return res;
        }

        // 2. 调用DeepSeek API
        String reply;
        try {
            reply = callDeepSeek(currentUser.getId());
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("AI服务暂时不可用，请稍后重试");
            res.setData(null);
            log.info("用户id={} DeepSeek API调用失败: {}", currentUser.getId(), e.getMessage());
            return res;
        }

        // 3. 保存AI回复
        AiChatMessage assistantMessage = new AiChatMessage();
        assistantMessage.setUser_id(currentUser.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        assistantMessage.setCreated_time(LocalDateTime.now());

        try {
            aiChatMapper.insertMessage(assistantMessage);
        } catch (Exception e) {
            // AI回复已生成，保存失败仅记录日志，不影响返回
            log.info("用户id={} AI回复保存失败: {}", currentUser.getId(), e.getMessage());
        }

        res.setCode(200);
        res.setMsg("回复成功");
        res.setData(reply);
        log.info("用户id={} AI客服回复成功", currentUser.getId());
        return res;
    }

    /**
     * 获取当前用户的AI聊天记录
     */
    public Res getHistory() {
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        List<AiChatMessage> messages =
                aiChatMapper.selectMessagesByUserId(currentUser.getId());

        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(messages != null ? messages : Collections.emptyList());
        log.info("用户id={} 查询AI聊天记录，共{}条", currentUser.getId(),
                messages != null ? messages.size() : 0);
        return res;
    }

    /**
     * 调用DeepSeek API，携带最近对话历史作为上下文
     */
    private String callDeepSeek(Integer userId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 构建 messages 数组
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // 最近的对话历史（正序排列）
        List<AiChatMessage> recentMessages =
                aiChatMapper.selectRecentMessages(userId, CONTEXT_MESSAGE_COUNT);
        if (recentMessages != null && !recentMessages.isEmpty()) {
            // selectRecentMessages 返回的是DESC（最新在前），需反转为时间正序
            for (int i = recentMessages.size() - 1; i >= 0; i--) {
                AiChatMessage msg = recentMessages.get(i);
                Map<String, String> historyMsg = new HashMap<>();
                historyMsg.put("role", msg.getRole());
                historyMsg.put("content", msg.getContent());
                messages.add(historyMsg);
            }
        }

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        Map body = response.getBody();
        if (body == null) {
            return "AI服务异常，请稍后重试";
        }

        List<?> choices = (List<?>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "AI没有返回结果，请稍后重试";
        }

        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        if (message == null) {
            return "AI返回格式错误，请稍后重试";
        }

        Object content = message.get("content");
        return content != null ? content.toString() : "AI返回内容为空";
    }
}
