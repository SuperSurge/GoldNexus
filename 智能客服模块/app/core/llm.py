"""DeepSeek LLM 初始化。

关键坑：DeepSeek 走 OpenAI 兼容协议，直接复用 langchain_openai.ChatOpenAI，
把 base_url 指向 DeepSeek 即可，别用错 SDK。
"""
from langchain_openai import ChatOpenAI

from app.config import settings


def get_llm(streaming: bool = False) -> ChatOpenAI:
    """创建 LLM 实例。

    streaming=False：用于工具判断、画像抽取等「一次性拿到完整结果」的场景；
    streaming=True ：用于最终回复的 SSE 流式输出。
    """
    return ChatOpenAI(
        model=settings.deepseek_model,
        api_key=settings.deepseek_api_key,
        base_url=settings.deepseek_base_url,   # 关键：指向 DeepSeek
        temperature=0.3,
        streaming=streaming,
    )


# 默认非流式实例：工具判断 / 画像抽取复用同一个，避免反复建连接
llm = get_llm()
