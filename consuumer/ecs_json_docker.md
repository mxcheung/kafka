
```
#!/bin/bash
set -e

# The environment variable injected by ECS
SECRET_JSON="$KEYSTORE_SECRET"

# Extract base64 keystore and write to file
echo "$SECRET_JSON" | jq -r '.keystore' | base64 --decode > /tmp/keystore.p12

echo "Keystore written to /tmp/keystore.p12"

# Run your main application
exec "$@"
```
