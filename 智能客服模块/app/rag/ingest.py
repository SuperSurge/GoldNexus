"""建库脚本：把 data/kb 下的原始文档切块、embedding、写入 Chroma。

运行方式（在项目根目录 智能客服模块/ 下）：
    python -m app.rag.ingest
或通过接口触发：POST /ingest

注意：首次运行会自动下载 embedding 模型（bge-large-zh-v1.5 约 1.3GB，走 hf-mirror 镜像），
之后建库很快。Chroma 持久化到 settings.chroma_dir（默认 ./data/chroma）。
"""
import glob
import os

from langchain_chroma import Chroma
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import settings
from app.rag.embed import get_embeddings
from app.rag.store import COLLECTION_NAME


def load_docs(kb_dir: str) -> list[Document]:
    """加载目录下所有 .txt/.md 文档，返回 Document 列表。"""
    docs = []
    patterns = [
        os.path.join(kb_dir, "**", "*.txt"),
        os.path.join(kb_dir, "**", "*.md"),
    ]
    files = set()
    for p in patterns:
        files.update(glob.glob(p, recursive=True))
    for path in sorted(files):
        try:
            with open(path, encoding="utf-8") as f:
                content = f.read()
        except Exception:
            continue
        if content.strip():
            docs.append(Document(
                page_content=content,
                metadata={"source": os.path.relpath(path, kb_dir)},
            ))
    return docs


def ingest() -> dict:
    """切块 + embedding + 写入 Chroma，返回建库统计。"""
    docs = load_docs(settings.kb_dir)
    if not docs:
        return {"ok": False, "documents": 0, "chunks": 0, "msg": f"未在 {settings.kb_dir} 找到任何文档"}

    splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
    chunks = splitter.split_documents(docs)

    # from_documents 会新建/覆盖同名 collection，等价于重建库
    Chroma.from_documents(
        chunks,
        get_embeddings(),
        persist_directory=settings.chroma_dir,
        collection_name=COLLECTION_NAME,
    )
    return {"ok": True, "documents": len(docs), "chunks": len(chunks), "msg": "建库完成"}


if __name__ == "__main__":
    result = ingest()
    print(result)
