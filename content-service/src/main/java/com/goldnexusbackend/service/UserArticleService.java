package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Article;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户端文章服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserArticleService {
    private final ArticleMapper articleMapper;

    Res res = new Res();

    /** 获取所有已发布文章列表 */
    @Transactional
    public Res getPublishedArticles() {
        log.info("进行查询已发布文章列表请求");
        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(articleMapper.selectPublishedArticles());
        return res;
    }

    /** 获取文章详情 */
    @Transactional
    public Res getArticleDetail(Integer articleId) {
        log.info("进行查询文章详情请求");

        Article article = articleMapper.selectArticleById(articleId);
        if (article == null || article.getIsPublished() != 1) {
            res.setCode(500);
            res.setMsg("文章不存在或未发布");
            log.info("文章不存在或未发布");
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
