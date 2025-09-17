# Como Rodar

```bash
pip install fastapi uvicorn pydantic
uvicorn main:app --reload
```

O **unicorn** é um ASGI (Asynchronous Server Gateway Interface). Mas a API pode rodar em um servidor padrão, como Apache HTTPD e Nginx.

## Documentação automática

Swagger UI → http://127.0.0.1:8000/docs

ReDoc → http://127.0.0.1:8000/redoc


## Tarefa Extra

Para quem quiser, pode tentar rodar a API na nuvem, como por exemplo, usando o Google App Engine ou AWS Cloud Run.