#!/bin/bash
set -euo pipefail

LOGFILE="error.log"
exec 2>>"$LOGFILE"   # send all stderr to error.log

AWS_REGION="us-east-1"
INSTANCE_NAME="my-ec2-ssm"
SG_NAME="my-ec2-sg-ssm"
KEY_NAME="my-ec2-key"

echo "🔍 Getting latest Amazon Linux 2023 AMI ID..."
AMI_ID=$(aws ec2 describe-images \
    --owners amazon \
    --filters "Name=name,Values=al2023-ami-2023*" "Name=architecture,Values=x86_64" \
    --query 'Images[0].ImageId' \
    --region "$AWS_REGION" \
    --output text)

echo "✅ Found AMI: $AMI_ID"

# -------------------
# IAM Role
# -------------------
ROLE_NAME="AmazonSSMRoleForInstancesQuickSetup"
if ! aws iam get-role --role-name "$ROLE_NAME" >/dev/null 2>&1; then
  echo "🔧 Creating IAM role $ROLE_NAME..."
  aws iam create-role \
      --role-name "$ROLE_NAME" \
      --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Principal": { "Service": "ec2.amazonaws.com" },
            "Action": "sts:AssumeRole"
          }
        ]
      }'
  aws iam attach-role-policy \
      --role-name "$ROLE_NAME" \
      --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
  aws iam create-instance-profile --instance-profile-name "$ROLE_NAME"
  aws iam add-role-to-instance-profile \
      --instance-profile-name "$ROLE_NAME" \
      --role-name "$ROLE_NAME"
  echo "✅ IAM role created."
else
  echo "✅ IAM role $ROLE_NAME already exists."
f
