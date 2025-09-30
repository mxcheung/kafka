from confluent_kafka import Consumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer

# Schema Registry config
schema_registry_conf = {
    "url": "https://<SR_ENDPOINT>",
    "basic.auth.user.info": "<SR_API_KEY>:<SR_API_SECRET>"
}

schema_registry_client = SchemaRegistryClient(schema_registry_conf)

# Fetch the latest schema for your topic
subject_name = "users-avro-value"  # usually <topic>-value
schema = schema_registry_client.get_latest_version(subject_name).schema.schema_str

# Create Avro deserializer
avro_deserializer = AvroDeserializer(schema_registry_client, schema)

# Kafka consumer config
consumer_conf = {
    "bootstrap.servers": "pkc-xxxxx.us-central1.gcp.confluent.cloud:9092",
    "security.protocol": "SASL_SSL",
    "sasl.mechanisms": "PLAIN",
    "sasl.username": "YOUR_API_KEY",
    "sasl.password": "YOUR_API_SECRET",
    "group.id": "test-group",
    "auto.offset.reset": "earliest"
}

consumer = Consumer(consumer_conf)
consumer.subscribe(["users-avro"])

try:
    while True:
        msg = consumer.poll(10)
        if msg is None:
            continue
        if msg.error():
            print("Consumer error:", msg.error())
            continue

        # Deserialize Avro manually
        user = avro_deserializer(msg.value(), None)
        print("Received user:", user)

except KeyboardInterrupt:
    print("Exiting...")

finally:
    consumer.close()