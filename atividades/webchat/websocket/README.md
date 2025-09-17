## Repository Structure

```
websocket/
├─ server.py
├─ requirements.txt
├─ Dockerfile
```

## Build Image

```bash
docker build -t chat-ws .
```

## Run Containers

Servidor:
```bash
docker run -d --name chat-ws -p 7071:7071 chat-ws
```

Cliente:
```bash
cd client/
docker run --rm -p 8080:80 \
  -v "$PWD":/usr/share/nginx/html:ro \
  --name webchat-client nginx:alpine
```

Acessar o cliente:
```
http://localhost:8080/
```



