# chat_server.py
from __future__ import annotations
import contextlib
from datetime import datetime, timedelta, timezone
from typing import Dict, Optional, Set, List
import os, json, asyncio

from fastapi.concurrency import asynccontextmanager

import jwt
from passlib.context import CryptContext
from fastapi import FastAPI, Depends, HTTPException, WebSocket, WebSocketDisconnect, Query
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

# NEW: import the pika client from separate file
from rabbit_pika import PikaClient

import asyncio

MAIN_LOOP: asyncio.AbstractEventLoop | None = None

# =========================
# Configuração
# =========================
JWT_SECRET = os.getenv("JWT_SECRET", "troque-este-segredo")  # use Secret Manager/variável de ambiente em produção
JWT_ALG = os.getenv("JWT_ALG", "HS256")
ACCESS_TTL_MIN = int(os.getenv("ACCESS_TTL_MIN", "60"))

pwd = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/login")

# RabbitMQ
RABBITMQ_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@172.17.0.3:5672/")
AMQP_EXCHANGE = os.getenv("AMQP_EXCHANGE", "chat.topic")

# Usuários pré-definidos (apenas estes podem usar o chat)
_raw_users = {
    "alice": "alice@123",
    "bob":   "bob@123",
    "carol": "carol@123",
}
USERS: Dict[str, Dict[str, str]] = {
    u: {"username": u, "password_hash": pwd.hash(p)} for u, p in _raw_users.items()
}

# =========================
# Modelos HTTP
# =========================
class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_at: int

class MeOut(BaseModel):
    username: str

class PublicUser(BaseModel):
    username: str
    online: bool = Field(description="Se o usuário está conectado via WebSocket")

# =========================
# Helpers de tempo/JWT
# =========================
def _now():
    return datetime.now(timezone.utc)

def _iso():
    return _now().isoformat()

def make_access_token(sub: str) -> TokenOut:
    exp = _now() + timedelta(minutes=ACCESS_TTL_MIN)
    payload = {
        "sub": sub,
        "type": "access",
        "iat": int(_now().timestamp()),
        "exp": int(exp.timestamp()),
    }
    token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)
    return TokenOut(access_token=token, expires_at=int(exp.timestamp()))

def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expirado")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Token inválido")

def current_username(token: str = Depends(oauth2_scheme)) -> str:
    payload = decode_token(token)
    if payload.get("type") != "access":
        raise HTTPException(status_code=401, detail="Token não é de acesso")
    username = payload.get("sub")
    if username not in USERS:
        raise HTTPException(status_code=403, detail="Usuário não autorizado")
    return username

# =========================
# RabbitMQ Client - Connection/Lifecycle
# =========================
pika_client: PikaClient | None = None

def _on_user_msg(username: str, body: bytes) -> bool:
    try:
        msg = json.loads(body.decode("utf-8"))
    except Exception:
        msg = {"type": "system", "text": body.decode("utf-8", "ignore")}

    if MAIN_LOOP is None:
        return False

    fut = asyncio.run_coroutine_threadsafe(manager.send_to(username, msg), MAIN_LOOP)
    try:
        ok = fut.result(timeout=2.0)   # manager.send_to returns True/False
        return bool(ok)
    except Exception:
        return False
    
@asynccontextmanager
async def lifespan(app: FastAPI):
    global pika_client, MAIN_LOOP
    MAIN_LOOP = asyncio.get_running_loop()  # <-- store the running loop
    pika_client = PikaClient(RABBITMQ_URL, AMQP_EXCHANGE, _on_user_msg)
    pika_client.start()
    for u in USERS.keys():
        pika_client.ensure_user_queue(u)
    try:
        yield
    finally:
        if pika_client:
            pika_client.stop()
# =========================

# =========================
# App & CORS
# =========================
app = FastAPI(
    title="Chat em FastAPI + JWT (HTTP + WS) + RabbitMQ (pika)",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # restrinja em produção
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
# =========================

# =========================
# Camada API (auth, perfil, listagem)
# =========================
@app.post("/login", response_model=TokenOut)
def login(form: OAuth2PasswordRequestForm = Depends()):
    user = USERS.get(form.username)
    if not user or not pwd.verify(form.password, user["password_hash"]):
        raise HTTPException(status_code=401, detail="Credenciais inválidas")
    return make_access_token(form.username)

@app.get("/me", response_model=MeOut)
def me(username: str = Depends(current_username)):
    return MeOut(username=username)

@app.get("/users", response_model=List[PublicUser])
def list_users(_: str = Depends(current_username)):
    return [PublicUser(username=u, online=u in manager.active) for u in sorted(USERS.keys())]

@app.get("/users/online", response_model=List[PublicUser])
def list_online(_: str = Depends(current_username)):
    return [PublicUser(username=u, online=True) for u in sorted(manager.active.keys())]

@app.get("/healthz")
def healthz():
    return {"status": "ok", "time": _iso()}
# =========================


# =========================
# Helper - Gerenciamento de Conexões WebSocket
# =========================
class ConnectionManager:
    def __init__(self):
        self.active: Dict[str, WebSocket] = {}  # username -> ws
        self.typing: Set[str] = set()           # usuários atualmente digitando

    async def connect(self, username: str, websocket: WebSocket):
        old = self.active.get(username)
        if old:
            try:
                await old.close(code=4001)
            except Exception:
                pass
        self.active[username] = websocket

    def disconnect(self, username: str):
        self.active.pop(username, None)
        self.typing.discard(username)

    async def send_to(self, username: str, payload: dict) -> bool:
        ws = self.active.get(username)
        if not ws:
            return False
        try:
            await ws.send_json(payload)
            return True
        except Exception:
            self.disconnect(username)
            return False

    async def broadcast(self, payload: dict):
        dead: Set[str] = set()
        for user, ws in self.active.items():
            try:
                await ws.send_json(payload)
            except Exception:
                dead.add(user)
        for u in dead:
            self.disconnect(u)

    def roster_payload(self) -> dict:
        return {
            "type": "presence",
            "online": sorted(self.active.keys()),
            "timestamp": _iso(),
        }

    def typing_payload(self) -> dict:
        return {
            "type": "typing",
            "users": sorted(self.typing),
            "timestamp": _iso(),
        }

manager = ConnectionManager()
# =========================

# =========================
# WebSocket Endpoint
# =========================
@app.websocket("/ws")
async def ws_endpoint(websocket: WebSocket, token: Optional[str] = Query(default=None)):
    await websocket.accept()
    if not token:
        await websocket.close(code=4401)  # Unauthorized
        return

    # Autenticação via JWT
    try:
        payload = decode_token(token)
        if payload.get("type") != "access":
            await websocket.close(code=4401); return
        username = payload.get("sub")
        if username not in USERS:
            await websocket.close(code=4403); return
    except HTTPException:
        await websocket.close(code=4401); return

    # Conecta e anuncia presença
    await manager.connect(username, websocket)
    await manager.broadcast({"type": "system", "text": f"{username} entrou no chat.", "timestamp": _iso()})
    await manager.broadcast(manager.roster_payload())
    await manager.broadcast(manager.typing_payload())  # mantém cliente em sincronia

    # Configura fila/consumer RabbitMQ para este usuário
    if pika_client:
        pika_client.ensure_user_queue(username, ttl_ms=None)
        pika_client.start_user_consumer(username)

    try:
        while True:
            raw = await websocket.receive_text()
            try:
                obj = json.loads(raw)
            except Exception:
                obj = {"type": "message", "text": raw}

            kind = obj.get("type")

            if kind == "who":
                await websocket.send_json(manager.roster_payload())
                await websocket.send_json(manager.typing_payload())
                continue

            if kind == "typing":
                state = str(obj.get("state", "")).lower()
                if state == "start":
                    manager.typing.add(username)
                elif state == "stop":
                    manager.typing.discard(username)
                await manager.broadcast(manager.typing_payload())
                continue

            if kind == "message":
                text = str(obj.get("text", "")).strip()
                if not text:
                    continue
                to_user = obj.get("to")
                payload_msg = {
                    "type": "message",
                    "sender": username,
                    "text": text,
                    "to": to_user,
                    "sent_at": _iso(),
                }
                # =========================
                # RabbitMQ Client - Envio de Mensagem
                # =========================
                if to_user:
                    if pika_client:
                        pika_client.ensure_user_queue(to_user)
                        pika_client.publish(f"user.{to_user}", payload_msg)
                    await manager.send_to(username, {
                        "type": "delivery", "to": to_user, "status": "queued", "timestamp": _iso()
                    })
                else: # broadcast
                    if pika_client:
                        for u in USERS.keys():
                            pika_client.ensure_user_queue(u)
                            pika_client.publish(f"user.{u}", payload_msg)
                continue
                # =========================

            # Mensagens desconhecidas: ignore ou logue
            await manager.send_to(username, {"type": "error", "message": "invalid_payload", "timestamp": _iso()})

    except WebSocketDisconnect:
        manager.disconnect(username)
        if pika_client:
            pika_client.stop_user_consumer(username)
        await manager.broadcast({"type": "system", "text": f"{username} saiu do chat.", "timestamp": _iso()})
        await manager.broadcast(manager.roster_payload())
        await manager.broadcast(manager.typing_payload())
    except Exception:
        manager.disconnect(username)
        if pika_client:
            pika_client.stop_user_consumer(username)
        with contextlib.suppress(Exception):
            await websocket.close(code=1011)
# =========================