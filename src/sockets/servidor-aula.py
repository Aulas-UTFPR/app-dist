import socket
import time

HOST = 'localhost'
PORT = 5001
BUFFER_SIZE = 1024

# Criar um socket TCP/IP
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Associar o socket ao endereço e porta
server_socket.bind((HOST, PORT))

# Abre a porta para conexões
server_socket.listen(1)

print(f'Servidor ouvindo em {HOST}:{PORT}')

# Aceitar uma conexão
conexao, cliente = server_socket.accept()

print(f'Conexão estabelecida com {cliente}')

while True:
    try:
        # Receber dados do cliente
        dados = conexao.recv(1024)

        # Decodificar os dados recebidos
        dados_recebidos = dados.decode('utf-8')

        print(f'Dados recebidos: {dados_recebidos}')

        if dados_recebidos.strip() == 'sair':
            print('Cliente desconectado.')
    finally:
        print('Conexão perdida.')
        break
    # Responder ao cliente
    conexao.sendall(b'Ate mais!\n')

# Fechar a conexão
server_socket.close()