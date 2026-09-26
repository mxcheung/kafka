from aws_lambda_powertools import Logger
import os
from utils.kafka import (KAFKA_SAMPLE_TOPIC, SAMPLE_INSTRUCTION_QUEUE_URL)
from message_handler.message_handler import MessageHandler

logger = Logger(level="INFO")

SLEEP_INTERVAL = int(os.getenv("SLEEP_INTERVAL", "60"))

CONSUMER_GROUP = 'CONSUMER_GROUP'
DISPATCHER_VERSION = '260925-001'


def main():
    logger.info("Setting up some environment variables, secrets, and the consumer.")
    consumer_group = get_environment_variable_or_raise(CONSUMER_GROUP)
    sample_topic = get_environment_variable_or_raise(KAFKA_SAMPLE_TOPIC)
    sample_instruction_queue_url = get_environment_variable_or_raise(SAMPLE_INSTRUCTION_QUEUE_URL)
    topics = [sample_topic]
    logger.info("Configured Kafka topics: %s", topics)

    handlers = {
        process_instruction_topic:  ProcessInstructionHandler(process_instruction_queue_url),
    }

    server_config, registry_config = get_kafka_cloud_sasl_credentials()

    consumer = get_consumer(
        topics=topics,
        server_config=server_config,
        registry_config=registry_config,
        consumer_group=consumer_group,
    )

    deadletter_producer = get_producer(
        topic=get_environment_variable_or_raise(KAFKA_DEADLETTER_TOPIC),
        server_config=server_config,
        registry_config=registry_config,
    )

    logger.info(
        "Finished setting up environment variables, secrets, and the consumer. Starting indefinite polling (interval 10s)."
    )
    logger.info(f"Now dispatcher-service version is : {DISPATCHER_VERSION}.")

    while True:
        msg = consumer.poll(10.0)

        if msg is None:
            continue
        elif msg.error():
            logger.error(
                f"Consumer error: {msg.error()}. Cannot proceed. Trying to drop a message for topic {msg.topic()}.")
            continue

        logger.info(f"Received message from topic: {msg.topic()}.")
        logger.info(f"Message contents: {msg}.")
        consume_with_deadletter(
            message=msg,
            handler_function=handlers[msg.topic()],
            deadletter_producer=deadletter_producer,
            message_consumer=consumer,
        )
