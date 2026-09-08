# 文章推送功能说明文档

## 一、数据库建表语句

在 `goldnexus` 数据库中执行以下 SQL 创建文章表：

```sql
CREATE TABLE IF NOT EXISTS article (
    articleId   INT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID（自增主键）',
    title       VARCHAR(255) NOT NULL        COMMENT '文章标题',
    content     TEXT          NOT NULL        COMMENT '文章正文',
    summary     VARCHAR(500)  DEFAULT NULL    COMMENT '文章摘要',
    coverImage  VARCHAR(500)  DEFAULT NULL    COMMENT '封面图片URL',
    isPublished INT           DEFAULT 0      COMMENT '发布状态：0-草稿，1-已发布',
    author      VARCHAR(100)  DEFAULT NULL    COMMENT '作者（管理员用户名）',
    createTime  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
```

---

## 二、新增文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| 实体类 | `entity/Article.java` | 文章实体，含 title、content、summary、coverImage、isPublished 等字段 |
| Mapper | `mapper/ArticleMapper.java` | 文章数据库操作接口（增删改查） |
| 管理员服务 | `service/AdminArticleService.java` | 管理员增删改查文章，含权限校验 |
| 用户端服务 | `service/UserArticleService.java` | 用户查询已发布文章 |
| 管理员控制器 | `controller/AdminArticleController.java` | 管理员端接口 |
| 用户端控制器 | `controller/UserArticleController.java` | 用户端接口 |

---

## 三、接口说明

所有接口统一返回格式：
```json
{
  "code": 200,       // 200-成功, 500-失败
  "msg": "操作成功",  // 提示信息
  "data": {}         // 返回数据（可能为 null）
}
```

### 3.1 管理员端接口

基础路径：`/goldnexus/admin`

#### 3.1.1 新增文章

- **URL**: `POST /goldnexus/admin/addArticle`
- **认证**: 需携带管理员 JWT Token
- **请求体**:

```json
{
  "title": "文章标题",
  "content": "文章正文内容...",
  "summary": "文章摘要（可选）",
  "coverImage": "https://example.com/cover.jpg（可选）",
  "isPublished": 1,
  "author": "管理员名"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 文章标题 |
| content | String | 否 | 文章正文 |
| summary | String | 否 | 文章摘要 |
| coverImage | String | 否 | 封面图片URL |
| isPublished | Integer | 否 | 0-草稿, 1-发布（默认0） |
| author | String | 否 | 作者署名 |

- **响应示例**:
```json
{ "code": 200, "msg": "新增文章成功", "data": null }
```

#### 3.1.2 修改文章

- **URL**: `POST /goldnexus/admin/updateArticle`
- **认证**: 需携带管理员 JWT Token
- **请求体**:

```json
{
  "articleId": 1,
  "title": "修改后的标题",
  "content": "修改后的正文...",
  "summary": "修改后的摘要",
  "coverImage": "https://example.com/new_cover.jpg",
  "isPublished": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| articleId | Integer | 是 | 文章ID |
| title | String | 否 | 文章标题 |
| content | String | 否 | 文章正文 |
| summary | String | 否 | 文章摘要 |
| coverImage | String | 否 | 封面图片URL |
| isPublished | Integer | 否 | 0-草稿, 1-发布 |

- **响应示例**:
```json
{ "code": 200, "msg": "修改文章成功", "data": null }
```

#### 3.1.3 删除文章

- **URL**: `POST /goldnexus/admin/deleteArticle`
- **认证**: 需携带管理员 JWT Token
- **请求体**:

```json
{ "articleId": 1 }
```

- **响应示例**:
```json
{ "code": 200, "msg": "删除文章成功", "data": null }
```

#### 3.1.4 查询所有文章列表（含草稿，不含正文）

- **URL**: `POST /goldnexus/admin/selectAllArticles`
- **认证**: 需携带管理员 JWT Token
- **请求体**: 无

- **响应示例**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "articleId": 1,
      "title": "文章标题",
      "summary": "摘要",
      "coverImage": "https://...",
      "isPublished": 1,
      "author": "admin",
      "createTime": "2026-07-01T10:30:00",
      "updateTime": "2026-07-01T10:30:00"
    }
  ]
}
```

> 列表接口不返回 `content` 字段，减小响应体积。查看正文请调用 `selectArticleDetail`。

#### 3.1.5 查询文章详情（含正文）

- **URL**: `POST /goldnexus/admin/selectArticleDetail`
- **认证**: 需携带管理员 JWT Token
- **请求体**:

```json
{ "articleId": 1 }
```

- **响应示例**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "articleId": 1,
    "title": "文章标题",
    "content": "文章正文内容...",
    "summary": "摘要",
    "coverImage": "https://...",
    "isPublished": 1,
    "author": "admin",
    "createTime": "2026-07-01T10:30:00",
    "updateTime": "2026-07-01T10:30:00"
  }
}
```

---

### 3.2 用户端接口

基础路径：`/goldnexus/user`

#### 3.2.1 获取已发布文章列表（不含正文）

- **URL**: `POST /goldnexus/user/getArticles`
- **认证**: 需携带用户 JWT Token
- **请求体**: 无

- **响应示例**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "articleId": 1,
      "title": "已发布文章",
      "summary": "摘要",
      "coverImage": "https://...",
      "isPublished": 1,
      "author": "admin",
      "createTime": "2026-07-01T10:30:00",
      "updateTime": "2026-07-01T10:30:00"
    }
  ]
}
```

> 仅返回 `isPublished = 1` 的文章，按创建时间倒序排列。不返回 `content` 正文字段，点击详情再加载正文。

#### 3.2.2 获取文章详情

- **URL**: `POST /goldnexus/user/getArticleDetail`
- **认证**: 需携带用户 JWT Token
- **请求体**:

```json
{ "articleId": 1 }
```

- **响应示例**:
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "articleId": 1,
    "title": "文章标题",
    "content": "文章正文内容...",
    "summary": "文章摘要",
    "coverImage": "https://...",
    "isPublished": 1,
    "author": "admin",
    "createTime": "2026-07-01T10:30:00",
    "updateTime": "2026-07-01T10:30:00"
  }
}
```

> 仅返回已发布的文章，草稿或不存在时返回失败。

---

## 四、权限说明

- 管理员端接口（`/goldnexus/admin/*`）通过 `SecurityContextHelper.isAdmin()` 校验 JWT Token 中的管理员角色，非管理员调用会返回 `"用户无权限"`。
- 用户端接口（`/goldnexus/user/*`）需携带有效的用户 JWT Token，无需额外角色校验。
- 用户只能查看 `isPublished = 1` 的已发布文章，无法查看草稿。
