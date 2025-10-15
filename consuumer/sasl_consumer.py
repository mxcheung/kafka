from confluent_kafka import Consumer
import logging, json

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

conf = {
    'bootstrap.servers': 'your-broker:9092',
    'group.id': 'my-group',
    'auto.offset.reset': 'earliest',
    'enable.auto.commit': False,

    # SASL/SSL config
    'security.protocol': 'SASL_SSL',           # Use SSL + SASL
    'sasl.mechanisms': 'PLAIN',               # Could be PLAIN, SCRAM-SHA-256, SCRAM-SHA-512, GSSAPI
    'sasl.username': 'your-username',
    'sasl.password': 'your-password',

    # Optional SSL verification
    'ssl.ca.location': '/path/to/ca.pem',     # Root CA for broker validation
    'ssl.certificate.location': '/path/to/client.crt',  # If using client certificate
    'ssl.key.location': '/path/to/client.key'          # Private key for client certificate
}

consumer = Consumer(conf)
consumer.subscribe(['my-topic'])

while True:
    msg = consumer.poll(1.0)
    if msg is None:
        continue
    if msg.error():
        logger.error(f"Kafka error: {msg.error()}")
        continue

    try:
        value = msg.value().decode('utf-8')
        data = json.loads(value)
        logger.info(f"Consumed message offset={msg.offset()}: {data}")
        consumer.commit(message=msg)
    except Exception as e:
        logger.error(f"Failed to process message offset={msg.offset()}: {e}")