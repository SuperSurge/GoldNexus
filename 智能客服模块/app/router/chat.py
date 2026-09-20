"""对话接口。

| 方法   | 路径                  | 说明                                   |
|--------|-----------------------|----------------------------------------|
| POST   | /chat                 | 对话，SSE 流式返回                     |
| GET    | /history/{session_id} | 拉取历史记录                           |
| DELETE | /history/{session_id} | 清空会话                               |
| POST   | /ingest               | 触发知识库重建（管理用，首次会下载模型）|
"""
import json

from fastapi import APIRouter
from sse_starlette.sse import EventSourceResponse

from app.core.chain import run
from app.memory import memory as mem
from app.rag.ingest import ingest
from app.rag.store import doc_count
from app.schemas import ChatRequest

router = APIRouter()


@router.post("/chat")
async def chat_endpoint(req: ChatRequest):
    """SSE 流式对话。

    事件协议（前端按 event 类型处理）：
      token          —— 逐字文本
      transfer_human —— 转人工提示
      done           —— 本轮结束
    """
    async def event_gen():
        async for ev in run(req.session_id, req.user_id, req.message):
            yield {
                "event": ev["event"],
                "data": json.dumps(ev["data"], ensure_ascii=False),
            }

    return EventSourceResponse(event_gen())


@router.get("/history/{session_id}")
def get_history(session_id: str):
    return {"session_id": session_id, "history": mem.load_history(session_id)}


@router.delete("/history/{session_id}")
def delete_history(session_id: str):
    mem.clear_history(session_id)
    return {"ok": True, "msg": f"已清空会话 {session_id}"}


@router.post("/ingest")
def do_ingest():
    """触发知识库重建。首次会下载 embedding 模型（约 1.3GB），耗时较长，属正常。"""
    result = ingest()
    result["vector_count"] = doc_count()
    return result
