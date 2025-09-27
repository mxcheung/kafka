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
