import json
import boto3
from kafka import KafkaConsumer
from botocore.exceptions import ClientError

# 1. Initialize the AWS SQS Client
# Ensure your AWS credentials are set via environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
# or via an IAM Role if running on AWS infrastructure.
sqs_client = boto3.client('sqs', region_name='us-east-1')
SQS_QUEUE_URL = 'https://amazonaws.com'

# 2. Initialize the Kafka Consumer
# Configured to automatically deserialize JSON payloads
consumer = KafkaConsumer(
    'your-kafka-topic',
    bootstrap_servers=['localhost:9092'],
    group_id='kafka-to-sqs-bridge',
    auto_offset_reset='earliest',
    value_deserializer=lambda x: json.loads(x.decode('utf-8'))
)

print("Starting Kafka to SQS bridge...")

# 3. Stream loop
try:
    for message in consumer:
        kafka_payload = message.value
        print(f"Received from Kafka (Offset {message.offset}): {kafka_payload}")
        
        try:
            # Forward the payload into the SQS Queue
            response = sqs_client.send_message(
                QueueUrl=SQS_QUEUE_URL,
                MessageBody=json.dumps(kafka_payload)
            )
            print(f"Successfully sent to SQS. Message ID: {response.get('MessageId')}")
            
        except ClientError as e:
            print(f"Failed to forward message to SQS: {e}")
            # Add error handling or dead-letter-queue (DLQ) logic here if critical

except KeyboardInterrupt:
    print("\nStopping bridge pipeline...")
finally:
    consumer.close()
