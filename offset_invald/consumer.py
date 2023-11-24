from kafka import KafkaConsumer, TopicPartition

topic = 'your_topic'
group_id = 'your_consumer_group'
bootstrap_servers = 'your_bootstrap_servers'
sasl_mechanism = 'PLAIN'
security_protocol = 'SASL_PLAINTEXT'
sasl_plain_username = 'your_username'
sasl_plain_password = 'your_password'

consumer = KafkaConsumer(
    topic,
    group_id=group_id,
    bootstrap_servers=bootstrap_servers,
    security_protocol=security_protocol,
    sasl_mechanism=sasl_mechanism,
    sasl_plain_username=sasl_plain_username,
    sasl_plain_password=sasl_plain_password,
    auto_offset_reset='earliest',  # or 'latest' or 'none' depending on your use case
)

# Manually reset offset
partition = 0  # example partition
offset = 10  # example offset
consumer.seek(TopicPartition(topic, partition), offset)

# Now the consumer will start consuming from the specified offset in the specified partition.
