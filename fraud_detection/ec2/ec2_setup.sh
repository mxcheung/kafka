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
fi

# -------------------
# Security Group
# -------------------
echo "🔧 Checking security group..."
SG_ID=$(aws ec2 describe-security-groups \
    --filters Name=group-name,Values="$SG_NAME" \
    --region "$AWS_REGION" \
    --query 'SecurityGroups[0].GroupId' \
    --output text 2>/dev/null || echo "None")

if [ "$SG_ID" == "None" ] || [ "$SG_ID" == "null" ]; then
  echo "🔧 Creating new security group $SG_NAME..."
  SG_ID=$(aws ec2 create-security-group \
      --group-name "$SG_NAME" \
      --description "EC2 SG for Session Manager + SSH + HTTP/HTTPS" \
      --region "$AWS_REGION" \
      --query 'GroupId' \
      --output text)
  echo "✅ Security group created: $SG_ID"

  echo "🌐 Adding inbound rules for SSH (22), HTTP (80), HTTPS (443)..."
  aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 22 --cidr 0.0.0.0/0 --region "$AWS_REGION"
  aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 80 --cidr 0.0.0.0/0 --region "$AWS_REGION"
  aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 443 --cidr 0.0.0.0/0 --region "$AWS_REGION"
  echo "✅ Inbound rules added."
else
  echo "✅ Reusing existing security group: $SG_ID"
fi

# -------------------
# Key Pair
# -------------------
echo "🔧 Checking key pair..."
if ! aws ec2 describe-key-pairs --key-names "$KEY_NAME" --region "$AWS_REGION" >/dev/null 2>&1; then
  echo "🔧 Creating new key pair $KEY_NAME..."
  aws ec2 create-key-pair \
      --key-name "$KEY_NAME" \
      --region "$AWS_REGION" \
      --query 'KeyMaterial' \
      --output text > "${KEY_NAME}.pem"
  chmod 400 "${KEY_NAME}.pem"
  echo "✅ Key pair created and saved as ${KEY_NAME}.pem"
else
  echo "✅ Key pair $KEY_NAME already exists (not overwriting local file)."
fi

# -------------------
# Launch Instance
# -------------------
echo "🚀 Launching EC2 instance..."
INSTANCE_ID=$(aws ec2 run-instances \
    --image-id "$AMI_ID" \
    --count 1 \
    --instance-type t2.micro \
    --key-name "$KEY_NAME" \
    --security-group-ids "$SG_ID" \
    --iam-instance-profile Name="$ROLE_NAME" \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$INSTANCE_NAME}]" \
    --region "$AWS_REGION" \
    --query 'Instances[0].InstanceId' \
    --output text)

echo "✅ Launched EC2 Instance: $INSTANCE_ID"

echo "⏳ Waiting until instance is running..."
aws ec2 wait instance-running --instance-ids "$INSTANCE_ID" --region "$AWS_REGION"
echo "✅ Instance is running."

PUBLIC_IP=$(aws ec2 describe-instances \
    --instance-ids "$INSTANCE_ID" \
    --region "$AWS_REGION" \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text)

echo "🌍 Public IP: $PUBLIC_IP"
echo "👉 Connect with SSH:"
echo "ssh -i ${KEY_NAME}.pem ec2-user@$PUBLIC_IP"
echo "👉 Or connect with Session Manager:"
echo "aws ssm start-session --target $INSTANCE_ID --region $AWS_REGION"
echo "ℹ️ All errors (if any) are logged in: $LOGFILE"
