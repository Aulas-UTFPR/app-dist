import asyncio # permite programação assíncrona wait/async
import websockets # biblioteca para trabalhar com WebSockets

clientes = set() # conjunto para armazenar clientes conectados

async def echo(websocket):
    clientes.add(websocket)
    async for message in websocket:
        print(f"Mensagem {message} recebida do cliente {websocket.remote_address}")
        if clientes:
            print(f"Clientes conectados: {len(clientes)}")
            for client in clientes:
                if client != websocket:
                    await client.send(f"Cliente {websocket.remote_address} disse: {message}")
        await websocket.send(f"Recebi sua mensagem!")

async def main():
    async with websockets.serve(echo, "localhost", 8765):
        print("Servidor WebSocket rodando em ws://localhost:8765")
        await asyncio.Future()  # Mantém o servidor ativo

if __name__ == "__main__":
    asyncio.run(main())
    
