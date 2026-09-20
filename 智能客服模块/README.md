# 智能客服模块（金纽贷 · 多数据源融合第三方贷款平台）

综设项目「多数据源融合第三方贷款平台」的智能客服模块，对应《智能客服模块开发流程》任务 2。

**定位**：平台客服「三层保障」中的第二层——智能客服（只承接规则库未命中的长尾问题，置信度不足转人工）。

| 层 | 归属 | 说明 |
|---|---|---|
| ① 规则库前置匹配 | 前置（不在本模块） | 300+ 高频 FAQ，命中即返回、<50ms |
| ② 智能客服（本模块） | 本模块 | DeepSeek + RAG + 记忆 + 工具调用 |
| ③ 人工兜底 | 下游对接 | 置信度不足 / 用户要求 → `transfer_to_human` |

**技术栈**：Python 3.13 + FastAPI + LangChain + DeepSeek + ChromaDB + bge embedding + bge reranker

---

## 目录结构

```
智能客服模块/
├── app/
│   ├── main.py              # FastAPI 入口（CORS + 路由）
│   ├── config.py            # 配置加载（.env → 绝对路径）
│   ├── schemas.py           # 请求/响应模型
│   ├── router/chat.py       # /chat(SSE) /history /ingest
│   ├── core/
│   │   ├── llm.py           # DeepSeek LLM 初始化
│   │   ├── chain.py         # 主流程编排（run_core + run）
│   │   └── prompt.py        # Prompt 模板 + 防幻觉约束
│   ├── tools/               # 4 个工具（查产品/算息/查单/转人工）
│   ├── rag/                 # embed / store / rerank / ingest
│   └── memory/              # 会话历史 + 用户画像
├── data/
│   ├── kb/                  # 知识库原始文档（金融文档）
│   ├── chroma/              # Chroma 持久化向量库（建库后生成）
│   └── memory/              # 用户画像持久化（运行时生成）
├── scripts/evaluate.py      # P6 评估脚本（规则 + LLM 裁判）
├── requirements.txt
└── .env / .env.example
```

---

## 快速开始

### 1. 配置

`.env` 已就位（含真实 DeepSeek key）。如需重建：`cp .env.example .env` 后填 `DEEPSEEK_API_KEY`。

### 2. 装依赖

```bash
# 用项目 venv（已装全部依赖）。若在其他环境，按需安装：
pip install -r requirements.txt
```

> 本机推荐复用 `Desktop/大模型/code/.venv`（Python 3.13.2，依赖已齐）。

### 3. 建库（P2，首次会下载 embedding 模型约 1.3GB）

```bash
python -m app.rag.ingest
# 或启动后 POST /ingest
```

### 4. 启动

```bash
# 在 智能客服模块/ 目录下
uvicorn app.main:app --reload
```

打开 http://127.0.0.1:8000/docs ，POST `/chat`：

```json
{"message": "借30000元分12期年利率12%，等额本息，每月还多少？", "session_id": "u1", "user_id": "u1"}
```

### 5. 评估（P6）

```bash
python scripts/evaluate.py
```

---

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/chat` | 对话，SSE 流式（事件：`token` / `transfer_human` / `done`） |
| GET | `/history/{session_id}` | 拉取历史记录 |
| DELETE | `/history/{session_id}` | 清空会话 |
| POST | `/ingest` | 触发知识库重建 |

---

## 进度

- [x] P0 环境 + DeepSeek 连通
- [x] P1 模型对话（/chat）
- [x] P2 RAG + reranker 精排（embed/store/rerank/ingest）
- [x] P3 函数调用 + 防幻觉（4 工具 + bind_tools + 硬约束）
- [x] P4 记忆（会话历史 + 用户画像抽取）
- [x] P5 流式（SSE）+ 转人工
- [x] P6 评估（规则 + LLM 裁判）

---

## 关键坑（已处理）

1. **DeepSeek 无 embedding** → 本地 bge-large-zh-v1.5，`HF_ENDPOINT` 走 hf-mirror 镜像。
2. **DeepSeek 是 OpenAI 兼容** → `ChatOpenAI` 配 `base_url`。
3. **金融防幻觉** → 系统 Prompt 硬约束：关键数据只能来自工具/知识库，无来源则「以平台实际审批为准」。
4. **会话隔离** → 按 `session_id` 存历史，`user_id` 存画像。
5. **流式** → `streaming=True` + SSE。
6. **Chroma 持久化** → 指定 `persist_directory`。
7. **召回与精排分离** → 召回 top-10 再 reranker 精排 top-4。

---

## 切换 reranker 实现

默认用 `sentence_transformers.CrossEncoder` 加载 `bge-reranker-v2-m3`（免装 FlagEmbedding）。
如需改用文档里的 FlagEmbedding：

```bash
pip install FlagEmbedding
```

并把 `app/rag/rerank.py` 的 `get_reranker()` 换成：

```python
from FlagEmbedding import FlagReranker
_reranker = FlagReranker(settings.rerank_model, use_fp16=True)
# rerank() 里 scores = reranker.compute_score(pairs, normalize=True)
```
