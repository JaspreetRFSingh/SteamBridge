# StreamBridge — A Custom Kafka Connect CDC Pipeline

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Kafka](https://img.shields.io/badge/Kafka-3.x-green.svg)](https://kafka.apache.org/)

A production-grade Change Data Capture (CDC) pipeline built on Apache Kafka and Kafka Connect.

## 📋 Overview

StreamBridge captures row-level changes from PostgreSQL using Debezium, transforms events through custom Single Message Transforms (SMTs), and sinks them to Elasticsearch.

## 🏗️ Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│ PostgreSQL  │────▶│ Debezium Source  │────▶│   Apache    │────▶│ Custom SMT +    │────▶│  Elasticsearch  │
│  (Source)   │     │  Connector       │     │   Kafka     │     │ Sink Connector  │     │    (Sink)       │
└─────────────┘     └──────────────────┘     └─────────────┘     └──────────────────┘     └─────────────────┘
```

## 🔑 Key Components

### 1. Custom Kafka Connect Source Connector
- REST API-based source connector
- Offset management and schema handling

### 2. Debezium CDC Pipeline
- Log-based CDC from PostgreSQL
- Schema evolution handling

### 3. Custom Single Message Transform (SMT)
- PII masking (email, phone, SSN)
- Operation-based topic routing

### 4. Operational Excellence
- Health checks and readiness probes
- Dead Letter Queue (DLQ)
- Prometheus metrics and Grafana dashboards

### 5. Infrastructure as Code
- Docker Compose for local development
- Kubernetes Helm charts for production

## 🛠️ Tech Stack

- **Java 17** - Primary development language
- **Apache Kafka & Kafka Connect** - Streaming platform
- **Debezium** - CDC connector for PostgreSQL
- **PostgreSQL** - Source database
- **Elasticsearch** - Target data store
- **Docker Compose** - Local orchestration
- **Kubernetes + Helm** - Production deployment
- **Prometheus + Grafana** - Monitoring

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.8+
- kubectl (for Kubernetes deployment)
- Helm 3+

### Docker Compose Setup

```bash
# Start the entire infrastructure
docker-compose up -d

# Check connector status
curl -s http://localhost:8083/connectors | jq
```

### Build from Source

```bash
# Build all connectors and transforms
mvn clean package

# Run unit tests
mvn test
```

### Kubernetes Deployment

```bash
# Install Helm chart
helm install streambridge ./helm/streambridge

# Check deployment status
kubectl get pods -l app=streambridge
```

## 📁 Project Structure

```
streambridge/
├── connectors/
│   ├── rest-source-connector/     # Custom REST API source connector
│   └── elasticsearch-sink/        # Enhanced Elasticsearch sink connector
├── transforms/
│   └── cdc-transform/             # Custom SMT for CDC events
├── config/
│   └── docker-compose/            # Docker Compose configurations
├── monitoring/
│   ├── prometheus/                # Prometheus configuration
│   └── grafana/                   # Grafana dashboards
├── scripts/                       # Utility scripts
├── docs/                          # Documentation
└── helm/                          # Helm charts
```

## ⚙️ Configuration

### Source Connector Configuration

```json
{
  "name": "postgresql-cdc",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "postgres",
    "database.password": "postgres",
    "database.dbname": "streambridge",
    "database.server.name": "streambridge-server",
    "table.include.list": "public.users,public.orders",
    "plugin.name": "pgoutput",
    "snapshot.mode": "initial",
    "transforms": "piiMask,routeByOperation",
    "transforms.piiMask.type": "com.streambridge.transforms.CdcPiiMaskTransform",
    "transforms.routeByOperation.type": "com.streambridge.transforms.OperationRouterTransform"
  }
}
```

## 📊 Monitoring

Key metrics available in Grafana:
- Connector lag and throughput
- Error rates and DLQ volume
- Kafka consumer/producer metrics
- JVM performance metrics

## 📚 Documentation

- [Architecture Guide](docs/architecture.md)
- [Custom Connector Development](docs/custom-connector.md)
- [Operational Runbook](docs/operations.md)
- [Kubernetes Deployment](docs/kubernetes.md)

## License

Apache License 2.0

## Author

Jaspreet R Singh
