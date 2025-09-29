
# 1. Launch EC2 instance

ec2 $ cd /home/cloudshell-user/kafka/fraud_detection/ec2
ec2 $ . ./ec2_setup.sh 
🔍 Getting latest Amazon Linux 2023 AMI ID...
✅ Found AMI: ami-00ca32bbc84273381
✅ IAM role AmazonSSMRoleForInstancesQuickSetup already exists.
🔧 Checking security group...
✅ Reusing existing security group: sg-08c456562db4dba2a
🚀 Launching EC2 instance...
✅ Launched EC2 Instance: i-069b56dfd394a48af
⏳ Waiting until instance is running...
✅ Instance is running.
🌍 Public IP: 54.242.61.54
👉 Connect with Session Manager:
aws ssm start-session --target i-069b56dfd394a48af --region us-east-1
👉 Or test HTTP access (after installing a web server):
curl http://54.242.61.54


Choose Amazon Linux 2 (or Ubuntu 22.04 if you prefer).

t3.small or larger is fine for testing.

Open inbound ports:

22 (SSH)

9092 (Kafka broker, if self-hosting Kafka)

6066 (Faust web UI)
