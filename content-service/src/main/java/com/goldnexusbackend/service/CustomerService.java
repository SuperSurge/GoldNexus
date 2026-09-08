package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.CustomerServiceMessage;
import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.CustomerServiceMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerServiceMapper customerServiceMapper;

    private final Res res = new Res();

    /** 用户发送消息 */
    @Transactional
    public Res userSendMessage(String content) {
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setUser_id(currentUser.getId());
        message.setAdmin_id(null);
        message.setSender_role("USER");
        message.setContent(content);
        message.setIs_read(0);
        message.setCreated_time(LocalDateTime.now());

        try {
            customerServiceMapper.insertMessage(message);
            res.setCode(200);
            res.setMsg("发送成功");
            res.setData(null);
            log.info("用户id={} 发送客服消息成功", currentUser.getId());
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("发送失败");
            res.setData(null);
            log.info("用户id={} 发送客服消息失败: {}", currentUser.getId(), e.getMessage());
        }
        return res;
    }

    /** 用户查看自己的聊天记录 */
    public Res userGetHistory() {
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        List<CustomerServiceMessage> messages =
                customerServiceMapper.selectMessagesByUserId(currentUser.getId());

        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(messages != null ? messages : Collections.emptyList());
        log.info("用户id={} 查询客服聊天记录，共{}条", currentUser.getId(),
                messages != null ? messages.size() : 0);
        return res;
    }

    /** 管理员获取所有发起过对话的用户列表 */
    public Res adminGetUserList() {
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("无权限");
            res.setData(null);
            return res;
        }

        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(customerServiceMapper.selectConversationSummaries());
        log.info("管理员查询客服用户列表");
        return res;
    }

    /** 管理员查看与某用户的聊天记录，同时将未读标为已读 */
    @Transactional
    public Res adminGetHistory(Integer userId) {
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("无权限");
            res.setData(null);
            return res;
        }

        // 标为已读
        customerServiceMapper.markMessagesAsRead(userId);

        List<CustomerServiceMessage> messages =
                customerServiceMapper.selectMessagesByUserId(userId);

        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(messages != null ? messages : Collections.emptyList());
        log.info("管理员查询与用户id={}的聊天记录，共{}条", userId,
                messages != null ? messages.size() : 0);
        return res;
    }

    /** 管理员发送回复 */
    @Transactional
    public Res adminSendMessage(Map<String, Object> request) {
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("无权限");
            res.setData(null);
            return res;
        }

        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        Integer userId = (Integer) request.get("userId");
        String content = (String) request.get("content");

        if (userId == null || content == null || content.trim().isEmpty()) {
            res.setCode(500);
            res.setMsg("参数不完整");
            res.setData(null);
            return res;
        }

        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setUser_id(userId);
        message.setAdmin_id(currentUser.getId());
        message.setSender_role("ADMIN");
        message.setContent(content);
        message.setIs_read(1); // 管理员发出的消息默认已读
        message.setCreated_time(LocalDateTime.now());

        try {
            customerServiceMapper.insertMessage(message);
            res.setCode(200);
            res.setMsg("回复成功");
            res.setData(null);
            log.info("管理员id={} 回复用户id={} 成功", currentUser.getId(), userId);
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("回复失败");
            res.setData(null);
            log.info("管理员id={} 回复用户id={} 失败: {}", currentUser.getId(), userId, e.getMessage());
        }
        return res;
    }
}
