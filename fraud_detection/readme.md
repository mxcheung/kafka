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

Transactions Topic → Faust Fraud Detection App → Fraud Alerts Topic
│
└── Stateful Tables (per-user history, windowed totals)


---

## ⚙️ Setup

### 1. Install Dependencies
```bash
pip install faust joblib



transactions_topic = app.topic('transactions', value_type=Transaction)
fraud_alerts_topic = app.topic('fraud_alerts', value_type=Transaction)
