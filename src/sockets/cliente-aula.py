import socket

HOST = 'localhost'
PORT = 5001

# Criar um socket TCP/IP
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Conectar ao servidor
client_socket.connect((HOST, PORT))

# Enviar dados para o servidor
mensagem = 'Ola, servidor!'
client_socket.sendall(mensagem.encode('utf-8'))

# Receber resposta do servidor
resposta = client_socket.recv(1024)
print(f'Resposta do servidor: {resposta.decode("utf-8")}')

# Enviar mensagem de despedida
client_socket.sendall(b'Ate mais!\n')

# Fechar o socket
client_socket.close()