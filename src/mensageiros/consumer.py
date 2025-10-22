import pika

def callback(ch, method, properties, body):
    print("Consumer recebeu:", body.decode())

conn = pika.BlockingConnection(pika.ConnectionParameters("localhost"))
ch = conn.channel()

# Cria fila e liga ao exchange
ch.queue_declare(queue="fila_hello")
ch.queue_bind(exchange="ex.direct", queue="fila_hello", routing_key="hello")

# Consome
ch.basic_consume(queue="fila_hello", on_message_callback=callback, auto_ack=True)

print("Aguardando mensagens...")
ch.start_consuming()