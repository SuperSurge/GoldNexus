"""配置加载：从 .env 读取，集中管理所有环境变量。

字段名会自动映射同名环境变量（大小写不敏感）：
  deepseek_api_key <-> DEEPSEEK_API_KEY
  chroma_dir       <-> CHROMA_DIR
  embed_model      <-> EMBED_MODEL
  ...
"""
import os

from pydantic_settings import BaseSettings, SettingsConfigDict

# 项目根目录（config.py 在 app/ 下，往上两级）
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=os.path.join(BASE_DIR, ".env"),  # 用绝对路径，避免受启动目录影响
        env_file_encoding="utf-8",
        extra="ignore",  # 忽略 .env 里未声明的变量，避免报错
    )

    # ===== DeepSeek（OpenAI 兼容协议，无 embedding 接口）=====
    deepseek_api_key: str
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-chat"

    # ===== RAG =====
    kb_dir: str = "./data/kb"                  # 知识库原始文档目录
    chroma_dir: str = "./data/chroma"          # Chroma 持久化目录
    embed_model: str = "BAAI/bge-large-zh-v1.5"  # 本地 embedding（中文）
    rerank_model: str = "BAAI/bge-reranker-v2-m3"  # 本地 reranker（cross-encoder）
    rag_top_k: int = 10        # 粗召回候选数
    rag_rerank_top_n: int = 4  # 精排后进 Prompt 的片段数

    # ===== 记忆 =====
    memory_dir: str = "./data/memory"   # 画像等持久化目录
    history_max_turns: int = 10         # 短期记忆保留最近 N 轮


settings = Settings()


def _abs(p: str) -> str:
    """相对路径统一转成基于项目根的绝对路径（并规范化掉 ./ 等）。"""
    return os.path.normpath(p if os.path.isabs(p) else os.path.join(BASE_DIR, p))


# 路径类字段统一转绝对路径，保证任意启动目录下都定位正确
settings.kb_dir = _abs(settings.kb_dir)
settings.chroma_dir = _abs(settings.chroma_dir)
settings.memory_dir = _abs(settings.memory_dir)
