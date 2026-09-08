package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Article;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.ArticleMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员文章管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminArticleService {
    private final ArticleMapper articleMapper;

    Res res = new Res();

    /** 新增文章 */
    @Transactional
    public Res addArticle(Article article) {
        log.info("进行新增文章请求");

        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        if (article.getTitle() == null || article.getTitle().isBlank()) {
            res.setCode(500);
            res.setMsg("文章标题不能为空");
            log.info("文章标题不能为空");
            res.setData(null);
            return res;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            article.setCreateTime(now);
            article.setUpdateTime(now);
            int i = articleMapper.insertArticle(article);
            if (i > 0) {
                res.setCode(200);
                res.setMsg("新增文章成功");
                log.info("新增文章成功");
                res.setData(null);
            } else {
                res.setCode(500);
                res.setMsg("新增文章失败");
                log.info("新增文章失败");
                res.setData(null);
            }
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("新增文章失败，内部错误");
            log.info("新增文章失败，内部错误");
            log.info(e.getMessage());
            res.setData(null);
        }
        return res;
    }

    /** 修改文章 */
    @Transactional
    public Res updateArticle(Article article) {
        log.info("进行修改文章请求");

        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        if (article.getArticleId() == null) {
            res.setCode(500);
            res.setMsg("文章ID不能为空");
            log.info("文章ID不能为空");
            res.setData(null);
            return res;
        }

        Article existing = articleMapper.selectArticleById(article.getArticleId());
        if (existing == null) {
            res.setCode(500);
            res.setMsg("文章不存在");
            log.info("文章不存在");
            res.setData(null);
            return res;
        }

        try {
            article.setUpdateTime(LocalDateTime.now());
            int i = articleMapper.updateArticle(article);
            if (i > 0) {
                res.setCode(200);
                res.setMsg("修改文章成功");
                log.info("修改文章成功");
                res.setData(null);
            } else {
                res.setCode(500);
                res.setMsg("修改文章失败");
                log.info("修改文章失败");
                res.setData(null);
            }
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("修改文章失败，内部错误");
            log.info("修改文章失败，内部错误");
            log.info(e.getMessage());
            res.setData(null);
        }
        return res;
    }

    /** 删除文章 */
    @Transactional
    public Res deleteArticle(Integer articleId) {
        log.info("进行删除文章请求");

        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        Article existing = articleMapper.selectArticleById(articleId);
        if (existing == null) {
            res.setCode(500);
            res.setMsg("文章不存在");
            log.info("文章不存在");
            res.setData(null);
            return res;
        }

        try {
            int i = articleMapper.deleteArticleById(articleId);
            if (i > 0) {
                res.setCode(200);
                res.setMsg("删除文章成功");
                log.info("删除文章成功");
                res.setData(null);
            } else {
                res.setCode(500);
                res.setMsg("删除文章失败");
                log.info("删除文章失败");
                res.setData(null);
            }
        } catch (Exception e) {
            res.setCode(500);
            res.setMsg("删除文章失败，内部错误");
            log.info("删除文章失败，内部错误");
            log.info(e.getMessage());
            res.setData(null);
        }
        return res;
    }

    /** 查询所有文章列表（含草稿，不含正文） */
    @Transactional
    public Res selectAllArticles() {
        log.info("进行查询所有文章列表请求");

        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(articleMapper.selectAllArticles());
        return res;
    }

    /** 根据ID查询文章详情（含正文） */
    @Transactional
    public Res selectArticleDetail(Integer articleId) {
        log.info("进行查询文章详情请求");

        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        Article article = articleMapper.selectArticleById(articleId);
        if (article == null) {
            res.setCode(500);
            res.setMsg("文章不存在");
            log.info("文章不存在");
            res.setData(null);
            return res;
        }

        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(article);
        return res;
    }
}
