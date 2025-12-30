#!/bin/sh
set -e

# Extract keystore from ECS secret
echo "$KEYSTORE_SECRET" \
  | jq -r '.keystore' \
  | base64 --decode \
  > /tmp/keystore.p12

chmod 600 /tmp/keystore.p12

# Default JAVA_OPTS if not provided
JAVA_OPTS="${JAVA_OPTS:-}"

# Run Java as PID 1
exec java $JAVA_OPTS -jar /app/app.jar
