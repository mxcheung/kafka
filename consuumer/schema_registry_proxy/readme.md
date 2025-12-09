Confluent Kafka Python client (confluent-kafka) does not use requests and never officially supported:

```
client._rest_client.session.proxies
```

That workaround used to work only for Confluent Cloud REST endpoints, 
but in recent versions (≥ 2.3) the internal REST client was rewritten and no longer exposes .session, so proxy injection no longer works.

# Correct way to use a proxy with Confluent Kafka (2024–2025)

```
from confluent_kafka.schema_registry import SchemaRegistryClient

schema_conf = {
    "url": "https://xyz.schemaregistry.confluent.cloud",
    "basic.auth.user.info": "...",
    "proxy": "http://your-proxy:8080",  # <-- new param works in recent versions
}

schema_client = SchemaRegistryClient(schema_conf)
```
