"""请求/响应模型。"""
from pydantic import BaseModel


class ChatRequest(BaseModel):
    message: str
    session_id: str = "default"   # 会话 id：区分不同用户/窗口（短期记忆隔离）
    user_id: str = "default"      # 用户 id：跨会话用户画像的 key


class ChatResponse(BaseModel):
    """非流式兜底返回（正常 /chat 走 SSE，此模型保留给测试/兜底）。"""
    answer: str
    session_id: str = "default"
