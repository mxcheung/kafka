import faust
import os
from datetime import timedelta

# Load Confluent Cloud credentials from environment variables
SASL_USERNAME = os.getenv("SASL_USERNAME", "<API_KEY>")
SASL_PASSWORD = os.getenv("SASL_PASSWORD", "<API_SECRET>")
BOOTSTRAP_SERVERS = os.getenv("BOOTSTRAP_SERVERS", "pkc-xxxxx.confluent.cloud:9092")

# Faust app with Confluent Cloud config
app = faust.App(
    "fraud_detection_app",
    broker=f"kafka://{BOOTSTRAP_SERVERS}",
    store="memory://",   # change to rocksdb:// for production
    broker_credentials=faust.SASLCredentials(
        username=SASL_USERNAME,
        password=SASL_PASSWORD,
        mechanism="PLAIN",   # Confluent uses SASL/PLAIN
        security_protocol="SASL_SSL",
    ),
)

# Transaction schema
class Transaction(faust.Record, serializer="json"):
    user_id: str
    amount: float
    location: str
    timestamp: str

# Kafka topics
transactions_topic = app.topic("transactions", value_type=Transaction)
fraud_alerts_topic = app.topic("fraud_alerts", value_type=Transaction)

# Stateful table
user_transactions = app.Table(
    "user_transactions",
    default=list,
).tumbling(timedelta(minutes=5), expires=timedelta(minutes=10))

# Fraud detection agent
@app.agent(transactions_topic)
async def detect_fraud(transactions):
    async for tx in transactions:
        user_transactions[tx.user_id].append(tx)
        total_amount = sum(t.amount for t in user_transactions[tx.user_id])

        if total_amount > 10000 or tx.amount > 5000:
            print(f"⚠️ Fraud detected for user {tx.user_id}: {tx}")
            await fraud_alerts_topic.send(value=tx)
