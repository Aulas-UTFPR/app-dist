import socket

def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    server_address = ('localhost', 12345)
    server_socket.bind(server_address)

    server_socket.listen(1)
    print("Servidor aguardando conexões na porta 12345...")

    while True:
        connection, client_address = server_socket.accept()
        try:
            print(f"Conexão estabelecida com {client_address}")

            data = connection.recv(1024)
            print(f"Recebido: {data.decode()}")

            if data:
                response = "Obrigado por se conectar!"
                connection.sendall(response.encode())
            else:
                print("Nenhum dado recebido. Encerrando conexão.")
                break
        finally:
            connection.close()

if __name__ == "__main__":
    start_server()