package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    /** 文章ID（自增主键） */
    private Integer articleId;
    /** 文章标题 */
    private String title;
    /** 文章正文 */
    private String content;
    /** 文章摘要 */
    private String summary;
    /** 封面图片URL */
    private String coverImage;
    /** 发布状态：0-草稿，1-已发布 */
    private Integer isPublished;
    /** 作者（管理员用户名） */
    private String author;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
