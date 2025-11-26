#!/bin/bash

SCHEMA_REGISTRY_URL="http://localhost:8081"
SCHEMA_FILE="../avro/ProductCreated.avsc"
SUBJECT="product-value"

if [ ! -f "$SCHEMA_FILE" ]; then
    echo "Schema file not found: $SCHEMA_FILE"
    exit 1
fi

SCHEMA_JSON=$(cat "$SCHEMA_FILE" | sed 's/"/\\"/g' | tr -d '\n')
PAYLOAD="{\"schema\": \"$SCHEMA_JSON\"}"

curl -X POST -H "Content-Type: application/json" \
     --data "$PAYLOAD" \
     "$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions"

if [ $? -ne 0 ]; then
    echo "Failed to upload schema"
    exit 1
fi
