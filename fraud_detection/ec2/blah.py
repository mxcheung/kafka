from confluent_kafka import Consumer

conf = {
    "bootstrap.servers": "pkc-xxxxx.us-central1.gcp.confluent.cloud:9092",
    "security.protocol": "SASL_SSL",
    "sasl.mechanisms": "PLAIN",
    "sasl.username": "YOUR_API_KEY",
    "sasl.password": "YOUR_API_SECRET",
    "group.id": "testgroup",
    "auto.offset.reset": "earliest"
}

c = Consumer(conf)
c.subscribe(["demo-topic"])

msg = c.poll(10)
print(msg)
c.close()