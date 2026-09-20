"""本地 embedding 封装。

关键坑：DeepSeek 只有 chat 接口、没有 embedding 接口，向量化必须另找方案。
这里选本地 `BAAI/bge-large-zh-v1.5`（中文金融文档效果好、离线免费、CPU 可跑），
通过 sentence-transformers 加载，包一层 langchain 的 Embeddings 接口给 Chroma 用。

归一化（normalize_embeddings=True）对 bge 检索效果至关重要：归一化后余弦相似度
等价于向量内积，Chroma 的 cosine 距离才能算准。
"""
import os

# ⚠️ 镜像必须在 import sentence_transformers 之前设置：
# huggingface_hub 在 import 时就把 endpoint 读成常量了，之后再改 os.environ 无效，
# 会导致直连 huggingface.co 超时（国内无法访问）。
os.environ.setdefault("HF_ENDPOINT", "https://hf-mirror.com")

from sentence_transformers import SentenceTransformer  # noqa: E402
from langchain_core.embeddings import Embeddings  # noqa: E402

from app.config import settings  # noqa: E402


class BgeEmbeddings(Embeddings):
    """把 sentence-transformers 包成 langchain 的 Embeddings 接口。"""

    def __init__(self, model_name: str):
        self.model = SentenceTransformer(model_name)

    def embed_documents(self, texts):
        return self.model.encode(list(texts), normalize_embeddings=True).tolist()

    def embed_query(self, text):
        return self.model.encode(text, normalize_embeddings=True).tolist()


_embeddings = None


def get_embeddings() -> BgeEmbeddings:
    """懒加载单例：首次调用才下载/加载 bge 模型（约 1.3GB），避免启动时卡住。"""
    global _embeddings
    if _embeddings is None:
        _embeddings = BgeEmbeddings(settings.embed_model)
    return _embeddings
