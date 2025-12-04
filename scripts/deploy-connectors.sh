#!/bin/bash
set -e
CONNECT_URL="http://localhost:8083"
echo "Deploying PostgreSQL CDC Connector..."
curl -X POST -H "Content-Type: application/json" --data @connectors/postgres-cdc-config.json $CONNECT_URL/connectors
echo "Deploying Elasticsearch Sink Connector..."
curl -X POST -H "Content-Type: application/json" --data @connectors/elasticsearch-sink-config.json $CONNECT_URL/connectors
echo "All connectors deployed successfully"
