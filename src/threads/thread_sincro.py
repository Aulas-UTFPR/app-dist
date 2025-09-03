import threading
import time
import random

saldo = 1000
lock = threading.Lock()
lock1 = threading.Lock()

def depositar(valor, iteracoes):
    global saldo
    for _ in range(iteracoes):
      with lock1:
        inicio = saldo
        time.sleep(random.uniform(0.001, 0.01))
        fim = inicio + valor
        saldo = fim

def sacar(valor, iteracoes):
    global saldo
    for _ in range(iteracoes):
       with lock1:
        inicio = saldo
        time.sleep(random.uniform(0.001, 0.01))
        fim = inicio - valor
        saldo = fim

threads = []
for _ in range(10):
    t = threading.Thread(target=depositar, args=(100, 100))
    threads.append(t)
    t.start()

for _ in range(10):
    t = threading.Thread(target=sacar, args=(100, 100))
    threads.append(t)
    t.start()

print("Efetuando operações...")

for t in threads:
    t.join()

print(f"Saldo final da conta: {saldo}")