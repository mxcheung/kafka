#!/bin/sh
set -e

echo "=== Container startup ==="
echo "Timestamp: $(date -Iseconds)"
echo "Hostname : $(hostname)"

echo
echo "=== Environment variables (names only) ==="
env | cut -d= -f1 | sort

echo
echo "=== Selected environment variables ==="
echo "JAVA_OPTS=${JAVA_OPTS:-<not set>}"
echo "AWS_REGION=${AWS_REGION:-<not set>}"
echo "AWS_DEFAULT_REGION=${AWS_DEFAULT_REGION:-<not set>}"

echo
echo "=== Extracting keystore ==="
if [ -z "$KEYSTORE_SECRET" ]; then
  echo "WARNING: KEYSTORE_SECRET is not set"
else
  echo "$KEYSTORE_SECRET" \
    | jq -r '.keystore' \
    | base64 --decode \
    > /tmp/keystore.p12

  chmod 600 /tmp/keystore.p12
  echo "Keystore written to /tmp/keystore.p12"
fi

echo
echo "=== Starting Java ==="
exec java $JAVA_OPTS -jar /app/app.jar
