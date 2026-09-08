# AI智能客服功能说明文档

## 一、功能概述

在现有的**人工客服**（`/goldnexus/user/cs/*`）之外，新增独立的 **AI 智能客服** 功能。

- 用户向 AI 客服发送消息，系统调用 DeepSeek API 生成回复并返回
- 所有对话记录持久化到数据库，支持历史查询
- 每次请求携带最近 20 条对话历史作为上下文，保证对话连贯性
- 系统提示词可在 `application.yml` 中按平台规则自由修改

---

## 二、新增文件清单

| 文件 | 说明 |
|---|---|
| `entity/AiChatMessage.java` | AI 聊天消息实体类 |
| `mapper/AiChatMapper.java` | MyBatis 数据访问层 |
| `service/AiChatService.java` | 业务逻辑层（含 DeepSeek API 调用） |
| `controller/AiChatController.java` | 用户端 REST 接口 |
| `application.yml` | 新增 `deepseek` 配置段 |

**未修改**项目其他任何已有代码。

---

## 三、数据库建表语句

在 `goldnexus` 数据库中执行以下 SQL：

```sql
CREATE TABLE ai_chat_message (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id     INT          NOT NULL COMMENT '用户ID',
    role        VARCHAR(16)  NOT NULL COMMENT '角色: user=用户 / assistant=AI',
    content     TEXT         NOT NULL COMMENT '消息内容',
    created_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_time (user_id, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能客服消息表';
```

---

## 四、配置说明

在 `src/main/resources/application.yml` 中找到 `deepseek` 配置段，按需修改：

```yaml
deepseek:
  api:
    key: YOUR_DEEPSEEK_API_KEY_HERE   # ← 替换为你的 DeepSeek API Key
    url: https://api.deepseek.com/v1/chat/completions
    model: deepseek-chat              # 可选 deepseek-chat / deepseek-reasoner
  system-prompt: |
    你是一个专业的金融借贷风控平台AI客服。
    ...
```

- **api.key**：必填，在 [DeepSeek 开放平台](https://platform.deepseek.com/) 获取
- **api.model**：`deepseek-chat`（通用对话）或 `deepseek-reasoner`（推理增强）
- **system-prompt**：系统提示词，可按照平台的业务规则和话术风格自由修改

---

## 五、API 接口

### 5.1 发送消息

```
POST /goldnexus/user/aiChat/send
```

**请求体：**
```json
{
  "message": "我的信用分是多少？借款额度怎么算？"
}
```

**成功响应：**
```json
{
  "code": 200,
  "msg": "回复成功",
  "data": "您好！信用分是根据您的个人信息和借款记录综合评估的..."
}
```

**错误响应：**
```json
{
  "code": 500,
  "msg": "AI服务暂时不可用，请稍后重试",
  "data": null
}
```

### 5.2 查询聊天记录

```
POST /goldnexus/user/aiChat/history
```

**请求体：** 无（需登录，自动获取当前用户）

**成功响应：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "id": 1,
      "user_id": 3,
      "role": "user",
      "content": "我的信用分是多少？",
      "created_time": "2026-07-01T10:30:00"
    },
    {
      "id": 2,
      "user_id": 3,
      "role": "assistant",
      "content": "您好！根据系统记录...",
      "created_time": "2026-07-01T10:30:05"
    }
  ]
}
```

---

## 六、架构说明

```
用户端请求
    │
    ▼
AiChatController    (/goldnexus/user/aiChat/*)
    │
    ▼
AiChatService       (业务逻辑 + DeepSeek API调用)
    │
    ├──▶ AiChatMapper    (ai_chat_message 表读写)
    │
    └──▶ RestTemplate    (HTTPS → api.deepseek.com)
```

- 遵循项目三层架构：Controller → Service → Mapper
- 使用 `RestTemplate`（Spring Boot 内置）调用 DeepSeek API
- API Key 通过 `@Value` 从配置文件注入，不硬编码
- 认证复用现有 JWT 机制（`SecurityContextHelper.getCurrentUser()`）
