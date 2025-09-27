import faust
from datetime import timedelta

app = faust.App(
    'fraud_detection_app',
    broker='kafka://localhost:9092',
    store='memory://'
)

class Transaction(faust.Record, serializer='json'):
    user_id: str
    amount: float
    location: str
    timestamp: str


user_transactions = app.Table(
    'user_transactions',
    default=list,
).tumbling(timedelta(minutes=5), expires=timedelta(minutes=10))

@app.agent(transactions_topic)
async def detect_fraud(transactions):
    async for tx in transactions:
        user_transactions[tx.user_id].append(tx)
        total_amount = sum(t.amount for t in user_transactions[tx.user_id])

        if total_amount > 10000 or tx.amount > 5000:
            print(f"⚠️ Fraud detected for user {tx.user_id}: {tx}")
            await fraud_alerts_topic.send(value=tx)
