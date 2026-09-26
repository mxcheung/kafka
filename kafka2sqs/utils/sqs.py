import json
import boto3
from aws_lambda_powertools import Logger
from typing import Any, Dict

sqs_client = boto3.client("sqs")
logger = Logger(level="INFO")

def send_to_sqs(
    queue_url: str,
    contents: Dict[str, Any],
) -> Dict[str, Any]:
    response = sqs_client.send_message(
        QueueUrl=queue_url,
        MessageBody=json.dumps(contents),
    )

    logger.info(
        f"SQS send successful. Response: {response}"
    )

    return response
