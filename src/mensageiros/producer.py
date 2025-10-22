import pika

# Conexão com RabbitMQ
conn = pika.BlockingConnection(pika.ConnectionParameters("localhost"))
ch = conn.channel()

# Declara exchange do tipo direct
ch.exchange_declare(exchange="ex.direct", exchange_type="direct")

# Envia mensagem com routing key "hello"
msg = "Olá AMQP!"
ch.basic_publish(exchange="ex.direct", routing_key="hello", body=msg)

print("Producer enviou:", msg)
conn.close()