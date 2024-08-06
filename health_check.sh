#!/bin/bash
# health_check.sh

# Check if the Kafka consumer process is running
pgrep -f consumer.py > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "Kafka consumer is running."
  exit 0
else
  echo "Kafka consumer is not running."
  exit 1
fi