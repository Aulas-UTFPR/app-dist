from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="API Simples com FastAPI")

# Modelo
class User(BaseModel):
    id: int
    name: str
    email: str

# "Banco" em memória
users = []

# Rotas
@app.get("/")
def root():
    return {"msg": "Bem-vindo à API FastAPI!"}

@app.get("/users")
def list_users():
    return users

@app.get("/users/{user_id}")
def get_user(user_id: int):
    for u in users:
        if u.id == user_id:
            return u
    raise HTTPException(status_code=404, detail="Usuário não encontrado")

@app.post("/users", status_code=201)
def create_user(user: User):
    if any(u.id == user.id for u in users):
        raise HTTPException(status_code=400, detail="ID já existe")
    users.append(user)
    return user

@app.delete("/users/{user_id}", status_code=204)
def delete_user(user_id: int):
    global users
    users = [u for u in users if u.id != user_id]
    return None