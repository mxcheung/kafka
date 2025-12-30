#!/bin/sh
set -e

echo "$KEYSTORE_SECRET" \
  | jq -r '.keystore' \
  | base64 --decode \
  > /tmp/keystore.p12

exec "$@"
