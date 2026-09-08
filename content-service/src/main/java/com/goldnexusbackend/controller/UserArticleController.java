package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.UserArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户端文章接口
 */
@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class UserArticleController {
    private final UserArticleService userArticleService;

    /** 获取已发布文章列表 */
    @PostMapping("/getArticles")
    public Res getArticles() {
        return userArticleService.getPublishedArticles();
    }

    /** 获取文章详情 */
    @PostMapping("/getArticleDetail")
    public Res getArticleDetail(@RequestBody Map<String, Integer> request) {
        return userArticleService.getArticleDetail(request.get("articleId"));
    }
}
