import asyncio
import json
import logging
import secrets
import websockets

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

PORT = 7071
CLIENTS = {}  # websocket -> {"id": str, "color": int}

def uuid4_like() -> str:
    alphabet = "0123456789abcdef"
    def rnd(n): 
        return "".join(secrets.choice(alphabet) for _ in range(n))
    return f"{rnd(8)}-{rnd(4)}-4{rnd(3)}-a{rnd(3)}-{rnd(12)}"

async def broadcast(payload: dict):
    if not CLIENTS:
        return
    message = json.dumps(payload)
    await asyncio.gather(*(ws.send(message) for ws in CLIENTS.keys()), return_exceptions=True)

async def handler(ws):
    client_id = uuid4_like()
    color = secrets.randbelow(361)  # 0..360 (matiz)
    CLIENTS[ws] = {"id": client_id, "color": color}
    logging.info("Client connected: %s color=%s", client_id, color)

    await broadcast({"type": "presence", "event": "join", "sender": client_id, "color": color})

    try:
        async for raw in ws:
            try:
                msg = json.loads(raw)
            except json.JSONDecodeError:
                msg = {"type": "chat", "text": str(raw)}

            meta = CLIENTS.get(ws, {})
            msg["sender"] = meta.get("id")
            msg["color"] = meta.get("color")

            await broadcast(msg)

    except websockets.ConnectionClosedOK:
        pass
    except websockets.ConnectionClosedError:
        pass
    finally:
        meta = CLIENTS.pop(ws, None)
        if meta:
            logging.info("Client disconnected: %s", meta["id"])
            await broadcast({"type": "presence", "event": "leave", "sender": meta["id"], "color": meta["color"]})

async def main():
    async with websockets.serve(handler, "0.0.0.0", PORT):
        logging.info("WebSocket server up on ws://localhost:%d", PORT)
        await asyncio.Future()  # mantém o servidor ativo

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logging.info("Shutting down...")
