from confluent_kafka.avro import AvroConsumer

# Schema Registry config
consumer_config = {
    'bootstrap.servers': 'pkc-xxxxx.us-central1.gcp.confluent.cloud:9092',
    'security.protocol': 'SASL_SSL',
    'sasl.mechanisms': 'PLAIN',
    'sasl.username': 'YOUR_API_KEY',
    'sasl.password': 'YOUR_API_SECRET',
    'group.id': 'test-group',
    'auto.offset.reset': 'earliest',
    'schema.registry.url': 'https://<SR_ENDPOINT>',
    'basic.auth.user.info': '<SR_API_KEY>:<SR_API_SECRET>',
}

consumer = AvroConsumer(consumer_config)

# Subscribe to topic
consumer.subscribe(['users-avro'])

try:
    while True:
        msg = consumer.poll(10)
        if msg is None:
            continue
        if msg.error():
            print("Consumer error:", msg.error())
            continue

        # msg.value() is already deserialized as a Python dict
        user = msg.value()
        print("Received user:", user)

except KeyboardInterrupt:
    pass
finally:
    consumer.close()