# Imagem leve com Python
FROM python:3.12-slim

# Evita bytecode e buffer em logs
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Cria diretório de trabalho
WORKDIR /app

# Instala dependências do sistema (opcional, mas útil p/ debug)
# RUN apt-get update && apt-get install -y --no-install-recommends \
#     curl ca-certificates netcat-openbsd \
#  && rm -rf /var/lib/apt/lists/*

# Copia reqs e instala (usar wheels quando possível)
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copia o servidor
COPY server.py ./

# Expõe a porta do WS
EXPOSE 7071

# Usuário não-root (boa prática)
RUN useradd -m appuser
USER appuser

# Comando de inicialização
CMD ["python", "server.py"]
