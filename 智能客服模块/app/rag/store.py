"""Chroma 向量库读写 / 检索。

设计：先多召回、再精排。这里 retrieve() 故意多取候选（默认 10），交给 reranker
精排到 top-4，比「直接 top-4」命中率高（双塔模型只算 query/chunk 各自向量相似度，
丢失了精细交互，靠 reranker 抵消偏差）。
"""
import os

from langchain_chroma import Chroma

from app.config import settings
from app.rag.embed import get_embeddings

COLLECTION_NAME = "kb_docs"
_vectorstore = None


def get_vectorstore() -> Chroma:
    """懒加载单例。指定 persist_directory，否则重启丢库。"""
    global _vectorstore
    if _vectorstore is None:
        os.makedirs(settings.chroma_dir, exist_ok=True)
        _vectorstore = Chroma(
            persist_directory=settings.chroma_dir,
            embedding_function=get_embeddings(),
            collection_name=COLLECTION_NAME,
        )
    return _vectorstore


def _collection_exists() -> bool:
    """判断 Chroma 库是否已建（存在 sqlite 文件）。"""
    return os.path.exists(os.path.join(settings.chroma_dir, "chroma.sqlite3"))


def retrieve(query: str, k: int = None) -> list:
    """粗召回 top-k 候选文档。库为空/未建库/模型不可用时返回空列表，流程降级不报错。

    关键：未建库时直接返回空，不初始化 Chroma，也就不会触发 embedding 模型下载，
    保证 P0/P1 纯对话（不依赖 RAG 的场景）能立即跑通。
    """
    k = k or settings.rag_top_k
    if not _collection_exists():
        return []
    try:
        vs = get_vectorstore()
        return vs.similarity_search(query, k=k)
    except Exception:
        return []


def doc_count() -> int:
    """返回库里向量块数量（供 /ingest 返回结果展示）。"""
    try:
        return get_vectorstore()._collection.count()
    except Exception:
        return 0
