import websocket

def on_connect(ws):
    print("Conectado ao servidor.")

def on_message(ws, message):
    print(f"Mensagem do servidor: {message}")

def on_error(ws, error):
    print(f"Erro: {error}")

def on_close(ws):
    print("Conexão encerrada.") 

def on_open(ws):
    print("Conexão aberta.")
    ws.send("Olá, servidor!")

if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://localhost:8765",
                            on_message=on_message,
                            on_error=on_error,
                            on_close=on_close,
                            on_open=on_open
                        )
    ws.run_forever()
