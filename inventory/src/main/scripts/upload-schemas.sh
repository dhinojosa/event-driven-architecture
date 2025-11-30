#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Function to check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed." >&2
    exit 1
fi

SR_URL="http://localhost:8081"
NAMESPACE="com.evolutionnext.inventory.events"

# Helper function to upload a schema
# Usage: upload_schema <FILE_PATH> <SUBJECT> <FULL_SCHEMA_NAME> <JSON_FILE_WITH_REFERENCES_OR_NULL>
upload_schema() {
    local file_path=$1
    local subject=$2
    local schema_name=$3
    local references_json=$4

    echo "----------------------------------------------------------------" >&2
    echo "Processing $subject..." >&2

    if [ ! -f "$file_path" ]; then
        echo "Error: File not found $file_path" >&2
        exit 1
    fi

    # Escape the schema content for JSON
    local schema_content
    schema_content=$(jq -Rs . < "$file_path")

    # Read references from the temp file, or default to empty array
    # We use jq -c . to ensure it's a compact single-line valid JSON string
    local refs="[]"
    if [ -n "$references_json" ] && [ -f "$references_json" ]; then
        refs=$(jq -c . "$references_json")
    fi

    # Construct the payload
    local payload
    payload=$(jq -n \
                  --arg schema "$schema_content" \
                  --argjson refs "$refs" \
                  '{schema: ($schema | fromjson), schemaType: "AVRO", references: $refs}')

    # Upload
    local response
    response=$(curl -s -X POST -H "Content-Type: application/json" \
                   --data "$payload" \
                   "$SR_URL/subjects/$subject/versions")

    # Check for error
    if echo "$response" | grep -q "error_code"; then
        echo "Failed to upload $subject: $response" >&2
        exit 1
    fi

    # Extract global ID for logging
    local id
    id=$(echo "$response" | jq '.id')
    echo "Success! Registered $subject with ID: $id" >&2

    # Return ONLY the version number to stdout for capture
    local version_response
    version_response=$(curl -s "$SR_URL/subjects/$subject/versions/latest")
    echo "$version_response" | jq '.version'
}

# Create a temp directory for reference files
TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

# 1. Upload ProductCreated (Leaf node)
echo "1. Uploading ProductCreated..." >&2
VERSION_PRODUCT=$(upload_schema "../avro/ProductCreated.avsc" "$NAMESPACE.ProductCreatedMessage" "$NAMESPACE.ProductCreatedMessage" "")

# 2. Upload PriceChanged (Leaf node)
echo "2. Uploading PriceChanged..." >&2
VERSION_PRICE=$(upload_schema "../avro/PriceChanged.avsc" "$NAMESPACE.PriceChangedMessage" "$NAMESPACE.PriceChangedMessage" "")

# 3. Upload StockChanged (Leaf node)
echo "3. Uploading StockChanged..." >&2
VERSION_STOCK=$(upload_schema "../avro/StockChanged.avsc" "$NAMESPACE.StockChangedMessage" "$NAMESPACE.StockChangedMessage" "")

# 4. Upload ROOT: InventoryEvent (Depends on ProductCreated, PriceChanged, StockChanged)
echo "4. Uploading ROOT: InventoryEvent..." >&2
FINAL_SUBJECT="inventory-value"

cat <<EOF > "$TMP_DIR/ref_event.json"
[
  {"name": "$NAMESPACE.ProductCreatedMessage", "subject": "$NAMESPACE.ProductCreatedMessage", "version": $VERSION_PRODUCT},
  {"name": "$NAMESPACE.PriceChangedMessage", "subject": "$NAMESPACE.PriceChangedMessage", "version": $VERSION_PRICE},
  {"name": "$NAMESPACE.StockChangedMessage", "subject": "$NAMESPACE.StockChangedMessage", "version": $VERSION_STOCK}
]
EOF

# Capture output not needed for the final step, but function prints logs to stderr
RESULT=$(upload_schema "../avro/InventoryEvent.avsc" "$FINAL_SUBJECT" "$NAMESPACE.InventoryEventMessage" "$TMP_DIR/ref_event.json")

echo "----------------------------------------------------------------" >&2
echo "All schemas registered successfully!" >&2
