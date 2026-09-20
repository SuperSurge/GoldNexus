"""FastAPI 入口。

运行方式（在 智能客服模块/ 目录下执行，用项目 venv 的 python）：
    uvicorn app.main:app --reload
然后访问 http://127.0.0.1:8000/docs 查看并测试接口。

SSE 流式对话：POST /chat
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.router.chat import router as chat_router

app = FastAPI(title="智能客服模块（多数据源融合第三方贷款平台）")

# 允许跨域：uni-app 前端（Android APK）/ Web 端调用时需要
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat_router)


@app.get("/health")
def health():
    return {"status": "ok"}
