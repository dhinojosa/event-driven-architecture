#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Function to check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed." >&2
    exit 1
fi

SR_URL="http://localhost:8081"
NAMESPACE="com.evolutionnext.order.events"

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

# 1. Upload OrderItem (Leaf node)
echo "1. Uploading OrderItem..." >&2
VERSION_ITEM=$(upload_schema "../avro/OrderItem.avsc" "$NAMESPACE.OrderItemMessage" "$NAMESPACE.OrderItemMessage" "")

# 2. Upload Order (Depends on OrderItem)
echo "2. Uploading Order..." >&2
cat <<EOF > "$TMP_DIR/ref_order.json"
[
  {"name": "$NAMESPACE.OrderItemMessage", "subject": "$NAMESPACE.OrderItemMessage", "version": $VERSION_ITEM}
]
EOF
VERSION_ORDER=$(upload_schema "../avro/Order.avsc" "$NAMESPACE.OrderMessage" "$NAMESPACE.OrderMessage" "$TMP_DIR/ref_order.json")

# 3. Upload OrderCancelled (No dependencies)
echo "3. Uploading OrderCancelled..." >&2
VERSION_CANCELLED=$(upload_schema "../avro/OrderCancelled.avsc" "$NAMESPACE.OrderCancelledMessage" "$NAMESPACE.OrderCancelledMessage" "")

# 4. Upload OrderPlaced (Depends on Order)
echo "4. Uploading OrderPlaced..." >&2
cat <<EOF > "$TMP_DIR/ref_placed.json"
[
  {"name": "$NAMESPACE.OrderMessage", "subject": "$NAMESPACE.OrderMessage", "version": $VERSION_ORDER}
]
EOF
VERSION_PLACED=$(upload_schema "../avro/OrderPlaced.avsc" "$NAMESPACE.OrderPlacedMessage" "$NAMESPACE.OrderPlacedMessage" "$TMP_DIR/ref_placed.json")

# 5. Upload ROOT: OrderEvent (Depends on Created, Cancelled, Placed)
echo "5. Uploading ROOT: OrderEvent..." >&2
FINAL_SUBJECT="order-value"

cat <<EOF > "$TMP_DIR/ref_event.json"
[
  {"name": "$NAMESPACE.OrderCancelledMessage", "subject": "$NAMESPACE.OrderCancelledMessage", "version": $VERSION_CANCELLED},
  {"name": "$NAMESPACE.OrderPlacedMessage", "subject": "$NAMESPACE.OrderPlacedMessage", "version": $VERSION_PLACED}
]
EOF

# Capture output not needed for the final step, but function prints logs to stderr
RESULT=$(upload_schema "../avro/OrderEvent.avsc" "$FINAL_SUBJECT" "$NAMESPACE.OrderEventMessage" "$TMP_DIR/ref_event.json")

echo "----------------------------------------------------------------" >&2
echo "All schemas registered successfully!" >&2
