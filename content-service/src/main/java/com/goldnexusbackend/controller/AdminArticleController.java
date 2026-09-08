package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Article;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AdminArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员文章管理接口
 */
@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminArticleController {
    private final AdminArticleService adminArticleService;

    /** 新增文章 */
    @PostMapping("/addArticle")
    public Res addArticle(@RequestBody Article article) {
        return adminArticleService.addArticle(article);
    }

    /** 修改文章 */
    @PostMapping("/updateArticle")
    public Res updateArticle(@RequestBody Article article) {
        return adminArticleService.updateArticle(article);
    }

    /** 删除文章 */
    @PostMapping("/deleteArticle")
    public Res deleteArticle(@RequestBody Map<String, Integer> request) {
        return adminArticleService.deleteArticle(request.get("articleId"));
    }

    /** 查询所有文章列表（含草稿，不含正文） */
    @PostMapping("/selectAllArticles")
    public Res selectAllArticles() {
        return adminArticleService.selectAllArticles();
    }

    /** 查询文章详情（含正文） */
    @PostMapping("/selectArticleDetail")
    public Res selectArticleDetail(@RequestBody Map<String, Integer> request) {
        return adminArticleService.selectArticleDetail(request.get("articleId"));
    }
}
