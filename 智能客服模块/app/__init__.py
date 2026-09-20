"""包初始化。

关键：在「最早」设置 HuggingFace 镜像环境变量。huggingface_hub 在 import 时就把
endpoint 读成常量了，之后改 os.environ 无效；而 langchain_openai 等依赖会在本包
子模块之前 import huggingface_hub，所以必须在这里（app 包最顶层）就设好，
否则直连 huggingface.co 会超时（国内无法访问）。
"""
import os

os.environ.setdefault("HF_ENDPOINT", "https://hf-mirror.com")
os.environ.setdefault("HF_HUB_ENDPOINT", "https://hf-mirror.com")
# 禁用 xet 存储协议：xet 走 cas-bridge.xethub.hf.co 桥接服务器，国内反复超时，
# 禁用后走 hf-mirror 普通 HTTP 下载，更稳。
os.environ.setdefault("HF_HUB_DISABLE_XET", "1")
