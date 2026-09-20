"""两层记忆。

短期（会话历史）：按 session_id 存进程内 dict，保留最近 N 轮，超长截断。
  —— 让「我之前说借 3 万」这种跨轮指代能生效，切换用户不串。
长期（用户画像）：按 user_id 跨会话存 JSON 落盘，记录借款金额、期限偏好、
  征信等级、联系方式、职业收入等，每次对话后由 chain 层抽取更新。
"""
import json
import os

from app.config import settings

# ====================== 短期：会话历史 ======================
# 进程内 dict，重启即清空（生产可换 Redis）；按 session_id 隔离，避免串会话。
histories: dict[str, list[dict]] = {}


def load_history(session_id: str) -> list[dict]:
    """返回该会话最近 N 轮历史，每轮 {"user": ..., "assistant": ...}。"""
    return histories.get(session_id, [])[-settings.history_max_turns:]


def save_turn(session_id: str, user_msg: str, assistant_msg: str) -> None:
    """追加一轮对话并截断到最近 N 轮。"""
    h = histories.setdefault(session_id, [])
    h.append({"user": user_msg, "assistant": assistant_msg})
    histories[session_id] = h[-settings.history_max_turns:]


def clear_history(session_id: str) -> None:
    histories.pop(session_id, None)


# ====================== 长期：用户画像 ======================
# 画像字段（value 为 None 表示暂未采集到）
EMPTY_PROFILE = {
    "借款金额": None,
    "期限偏好": None,
    "征信等级": None,
    "联系方式": None,
    "职业收入": None,
}


def _profiles_path() -> str:
    os.makedirs(settings.memory_dir, exist_ok=True)
    return os.path.join(settings.memory_dir, "profiles.json")


def _load_all_profiles() -> dict:
    path = _profiles_path()
    if os.path.exists(path):
        try:
            with open(path, encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return {}
    return {}


def load_profile(user_id: str) -> dict:
    """读取用户画像，缺失字段补 None。"""
    all_profiles = _load_all_profiles()
    profile = dict(EMPTY_PROFILE)
    profile.update(all_profiles.get(user_id, {}))
    return profile


def save_profile(user_id: str, profile: dict) -> None:
    """合并写入用户画像（只保留 EMPTY_PROFILE 里的字段）。"""
    all_profiles = _load_all_profiles()
    cleaned = {k: profile.get(k) for k in EMPTY_PROFILE if profile.get(k)}
    all_profiles[user_id] = cleaned
    with open(_profiles_path(), "w", encoding="utf-8") as f:
        json.dump(all_profiles, f, ensure_ascii=False, indent=2)
