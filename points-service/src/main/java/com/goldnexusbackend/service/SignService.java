package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsSign;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.mapper.PointsSignMapper;
import com.goldnexusbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 签到服务：实名校验 -> 连签加成 -> 记签到(唯一索引防重复) -> 走 GrantService 发放。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignService {

    private final PointsSignMapper signMapper;
    private final UserMapper userMapper;
    private final GrantService grantService;

    @Transactional(rollbackFor = Exception.class)
    public Res sign(Integer userId) {
        // 1) 实名校验
        User user = userMapper.selectUserById(userId);
        if (user == null || user.getAuthentication() == null || user.getAuthentication() != 1) {
            return new Res(500, "请先完成实名认证", null);
        }
        LocalDate today = LocalDate.now();
        // 2) 防重复签到
        if (signMapper.selectByUserAndDate(userId, today) != null) {
            return new Res(500, "今日已签到", null);
        }
        // 3) 连签天数与积分(基础5分, 连签+2/天, 封顶30)
        int consecutive = calcConsecutive(userId, today);
        int points = Math.min(5 + (consecutive - 1) * 2, 30);
        // 4) 记签到(唯一索引 uk_sign 兜底)
        PointsSign sign = new PointsSign();
        sign.setUserId(userId);
        sign.setSignDate(today);
        sign.setConsecutive(consecutive);
        sign.setPoints(points);
        try {
            signMapper.insert(sign);
        } catch (DuplicateKeyException e) {
            return new Res(500, "今日已签到", null);
        }
        // 5) 发放(同一事务, 幂等键 = userId + 日期)
        grantService.grant("SIGN_IN", userId + "-" + today, userId, points);

        Map<String, Object> data = new HashMap<>();
        data.put("consecutiveDays", consecutive);
        data.put("points", points);
        return new Res(200, "签到成功", data);
    }

    private int calcConsecutive(Integer userId, LocalDate today) {
        PointsSign latest = signMapper.selectLatest(userId);
        if (latest != null && latest.getSignDate().equals(today.minusDays(1))) {
            return latest.getConsecutive() + 1;
        }
        return 1;
    }
}
