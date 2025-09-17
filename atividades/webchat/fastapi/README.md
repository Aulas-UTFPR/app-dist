# Chat Server — FastAPI + JWT + WebSocket

Servidor de chat com:

- **HTTP (REST)** para autenticação, perfil, listagem de usuários e healthcheck.

- **WebSocket** para mensagens em tempo real, presença (quem está online) e indicadores de digitação.

O servidor usa uma lista de **usuários pré-definidos** (ex.: alice, bob, carol), todos com a senha sendo *nomedousuario*123.

## 1. Pré-requisitos

- Docker 24+ (para container)
- Insomnia / Postman (para testar HTTP)
- Um navegador (para testar o cliente WebSocket simples)

## 2. Rodando o Servidro no Docker

Arquivos esperados:

- `chat_server.py` (o arquivo do servidor)
- `Dockerfile`
- `requirements.txt`

Caso já tenha os arquivos do container prontos, siga abaixo.

Build & Run (Docker):

```bash
docker build -t chat-server:latest .
docker run --rm -p 8000:8000 \
  -e JWT_SECRET='um-segredo-bem-aleatorio' \
  --name chat-server chat-server:latest
```

## 3. Guia para Implementação do Cliente

O cliente precisa:

1. **Autenticar-se** via HTTP (`/login`) para obter um token JWT.  
2. **Usar o token** para acessar endpoints protegidos (`/me`, `/users`).  
3. **Conectar-se ao WebSocket** (`/ws?token=<JWT>`) para participar do chat em tempo real.  
4. Implementar **funções básicas**: enviar mensagens, receber mensagens, ver usuários online e exibir quem está digitando.

### Login (obter JWT)
O cliente deve fazer um `POST` para `/login` com `username` e `password`.

Exemplo com **cURL**:
```bash
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=alice&password=alice@123" \
     http://127.0.0.1:8000/login
````

### Endpoints Úteis

Ver meu perfil:
```
GET /me
Authorization: Bearer <JWT>
```

Verificar todos os usuários:
```
GET /users
Authorization: Bearer <JWT>
```

Verificar somente usuários online:
```
GET /users/online
Authorization: Bearer <JWT>
```

### Conexão com o WebSocket

URL de conexão
```
ws://127.0.0.1:8000/ws?token=<JWT>
```

### Exemplo de Chat do Cliente

```
<!doctype html>
<meta charset="utf-8">
<title>WebChat Cliente</title>
<body>
  <h1>Cliente WebChat</h1>

  <input id="token" placeholder="Cole seu JWT aqui" size="80">
  <button id="connect">Conectar</button>
  <hr>

  <input id="msg" placeholder="Mensagem..." size="60">
  <input id="to" placeholder="Para (opcional)">
  <button id="send">Enviar</button>
  <hr>

  <h3>Online</h3>
  <pre id="presence"></pre>

  <h3>Digitando</h3>
  <pre id="typing"></pre>

  <h3>Mensagens</h3>
  <pre id="log"></pre>

<script>
let ws;

function log(msg) {
  document.getElementById('log').textContent += JSON.stringify(msg) + "\n";
}
function setPresence(list) {
  document.getElementById('presence').textContent = JSON.stringify(list, null, 2);
}
function setTyping(list) {
  document.getElementById('typing').textContent = JSON.stringify(list, null, 2);
}

document.getElementById('connect').onclick = () => {
  const token = document.getElementById('token').value.trim();
  ws = new WebSocket(`ws://127.0.0.1:8000/ws?token=${encodeURIComponent(token)}`);

  ws.onopen = () => {
    log("Conectado.");
    ws.send(JSON.stringify({ type: "who" }));
  };
  ws.onmessage = (e) => {
    const msg = JSON.parse(e.data);
    if (msg.type === "presence") return setPresence(msg.online);
    if (msg.type === "typing") return setTyping(msg.users);
    log(msg);
  };
};

document.getElementById('send').onclick = () => {
  const text = document.getElementById('msg').value;
  const to = document.getElementById('to').value.trim();
  const payload = to ? { type:"message", text, to } : { type:"message", text };
  ws.send(JSON.stringify(payload));
};
</script>
</body>
```


### Checklist do cliente

- [ ] Fazer login e guardar o JWT.
- [ ] Usar o JWT no header HTTP (Authorization: Bearer <JWT>).
- [ ] Conectar no WebSocket com o JWT na query string.
- [ ] Implementar handlers para eventos: presence, typing, message, system, delivery.
- [ ] Permitir broadcast e DM.
- [ ] Mostrar usuários online e quem está digitando.