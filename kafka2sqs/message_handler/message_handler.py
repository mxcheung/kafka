import boto3
from aws_lambda_powertools import Logger
from typing import Dict
from utils.sqs import send_to_sqs

sqs_client = boto3.client("sqs")
logger = Logger(level="INFO")


class MessageHandler:
    def __init__(self, queue_url: str):
        self.queue_url = queue_url

    def __call__(self, contents: Dict):
        send_to_sqs(
            queue_url=self.queue_url,
            contents=contents,
        )
