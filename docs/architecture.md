# Architecture Guide

## Overview
StreamBridge implements a Change Data Capture (CDC) pipeline using Apache Kafka Connect.

## Data Flow
1. **PostgreSQL** generates WAL entries for row-level changes
2. **Debezium Connector** reads WAL and produces CDC events to Kafka
3. **Custom SMTs** transform events (PII masking, topic routing)
4. **Elasticsearch Sink** consumes transformed events and indexes documents
