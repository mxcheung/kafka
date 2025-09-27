# 🛡️ Kafka Streaming Fraud Detection with Faust

This project demonstrates how to build a **real-time fraud detection system** using **Apache Kafka** and **Faust** (Python stream processing library).  
It consumes transactions from Kafka, applies **rule-based** and/or **machine learning** fraud detection, and publishes suspicious transactions to a `fraud_alerts` topic.

---

## 🚀 Features
- Real-time streaming fraud detection
- Rule-based checks (e.g., high-value or frequent transactions)
- Stateful windowed aggregations (per user, per time window)
- Optional ML model integration for advanced detection
- Scalable & fault-tolerant with Kafka partitions

---

## 📂 Architecture

