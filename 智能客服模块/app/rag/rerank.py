"""Reranker 重排（cross-encoder 精排）。

为什么需要：embedding 是「双塔」模型，只算 query 与 chunk 各自的向量相似度，丢失了
query<->chunk 的精细交互；而 LLM 生成时会逐字比对两者语义，两者对「相关性」的判断
偏好不一致，导致召回靠前的其实不相关、真正相关的被挤到后面。Reranker 把 query 和
chunk 拼在一起过一遍编码器做深度交互打分，抵消这个偏差。

实现选型：文档默认 FlagEmbedding 的 FlagReranker（bge-reranker-v2-m3），但 FlagEmbedding
是额外依赖（本项目 venv 未装，且装新包有扰动 chromadb 版本的风险）。这里改用
sentence-transformers 自带的 CrossEncoder 加载同一个 bge-reranker 模型，效果等价、免装。
如确需 FlagEmbedding，见 README「切换 reranker 实现」。
"""
from app.config import settings

_reranker = None


def get_reranker():
    global _reranker
    if _reranker is None:
        from sentence_transformers import CrossEncoder
        _reranker = CrossEncoder(settings.rerank_model)
    return _reranker


def rerank(query: str, docs: list, top_n: int = None) -> list:
    """对召回候选做深度交互打分，精排取 top_n。

    降级策略：docs 为空返回空；docs 不超过 top_n 直接返回；reranker 模型下载失败
    时按召回顺序取前 top_n（保证流程不崩）。
    """
    top_n = top_n or settings.rag_rerank_top_n
    if not docs:
        return []
    if len(docs) <= top_n:
        return docs
    try:
        rk = get_reranker()
        pairs = [[query, d.page_content] for d in docs]
        scores = rk.predict(pairs)
        ranked = sorted(zip(docs, scores), key=lambda x: float(x[1]), reverse=True)
        return [d for d, _ in ranked[:top_n]]
    except Exception:
        return docs[:top_n]
