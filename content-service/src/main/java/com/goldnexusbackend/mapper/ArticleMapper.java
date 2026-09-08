package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文章管理 Mapper
 */
@Mapper
public interface ArticleMapper {

    /** 新增文章，使用自增主键 */
    @Insert("INSERT INTO article (title, content, summary, coverImage, isPublished, author, createTime, updateTime) " +
            "VALUES (#{title}, #{content}, #{summary}, #{coverImage}, #{isPublished}, #{author}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "articleId")
    int insertArticle(Article article);

    /** 更新文章 */
    @Update("UPDATE article SET title = #{title}, content = #{content}, summary = #{summary}, " +
            "coverImage = #{coverImage}, isPublished = #{isPublished}, updateTime = #{updateTime} " +
            "WHERE articleId = #{articleId}")
    int updateArticle(Article article);

    /** 根据ID删除文章 */
    @Delete("DELETE FROM article WHERE articleId = #{articleId}")
    int deleteArticleById(Integer articleId);

    /** 根据ID查询文章 */
    @Select("SELECT * FROM article WHERE articleId = #{articleId}")
    Article selectArticleById(Integer articleId);

    /** 查询所有文章列表（管理员端，含草稿，不含正文） */
    @Select("SELECT articleId, title, summary, coverImage, isPublished, author, createTime, updateTime " +
            "FROM article ORDER BY createTime DESC")
    List<Article> selectAllArticles();

    /** 查询所有已发布文章列表（用户端，不含正文） */
    @Select("SELECT articleId, title, summary, coverImage, isPublished, author, createTime, updateTime " +
            "FROM article WHERE isPublished = 1 ORDER BY createTime DESC")
    List<Article> selectPublishedArticles();
}
