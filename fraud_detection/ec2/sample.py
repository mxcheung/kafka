# app.py
import faust
import ssl

# Build SSLContext with hostname verification enabled
ssl_context = ssl.create_default_context(
    purpose=ssl.Purpose.SERVER_AUTH
)
# (Optional) load custom CA if needed:
# ssl_context.load_verify_locations(cafile="/path/to/ca.pem")

# Explicitly enforce hostname verification
ssl_context.check_hostname = True
ssl_context.verify_mode = ssl.CERT_REQUIRED

# Configure Faust App
app = faust.App(
    "demo_app",
    broker="kafka://pkc-xxxxx.region.confluent.cloud:9092",  # your Confluent Cloud broker
    broker_credentials=faust.SASLCredentials(
        username="YOUR_API_KEY",
        password="YOUR_API_SECRET",
        mechanism="PLAIN",        # Confluent Cloud uses SASL/PLAIN
    ),
    security_protocol="SASL_SSL",
    ssl_context=ssl_context,      # attach SSLContext
    value_serializer="json",
)

# Topic
topic = app.topic("demo-topic", value_type=dict)

# Agent
@app.agent(topic)
async def consume(stream):
    async for event in stream:
        print("Received:", event)

# Run via PyCharm or python app.py
if __name__ == "__main__":
    app.main()