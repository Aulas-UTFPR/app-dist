# ===== Stage 1: build (instala dependências) =====
FROM python:3.11-slim AS builder

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# Dependências do sistema (se precisar compilar libs)
RUN apt-get update && apt-get install -y --no-install-recommends build-essential curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia requisitos primeiro (melhor cache)
COPY requirements.txt ./
RUN pip install --upgrade pip && \
    pip wheel --no-cache-dir --wheel-dir /wheels -r requirements.txt

# ===== Stage 2: runtime (imagem mínima) =====
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PORT=8000 \
    UVICORN_WORKERS=2 \
    UVICORN_HOST=0.0.0.0

# cria usuário não-root
RUN useradd -m appuser

WORKDIR /app

# Copia wheels e instala, depois remove cache
COPY --from=builder /wheels /wheels
RUN pip install --no-cache-dir /wheels/*

# Copia a aplicação
COPY chat_server.py /app/chat_server.py

# Exponha a porta do serviço
EXPOSE 8000

# Healthcheck simples (ajuste o path se quiser)
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD python -c "import urllib.request, os; \
    urllib.request.urlopen(f'http://127.0.0.1:{os.getenv(\"PORT\",\"8000\")}/healthz').read()" || exit 1

# roda como usuário não-root
USER appuser

# CMD: uvicorn (com headers de proxy; útil atrás de Nginx/ALB)
CMD ["sh", "-c", "uvicorn chat_server:app --host ${UVICORN_HOST} --port ${PORT} --workers ${UVICORN_WORKERS} --proxy-headers --forwarded-allow-ips='*'"]