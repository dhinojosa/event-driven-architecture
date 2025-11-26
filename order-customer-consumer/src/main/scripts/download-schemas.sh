#!/bin/bash

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed."
    exit 1
fi

SCHEMA_REGISTRY_URL="http://localhost:8081"
SUBJECT="customer-value"
OUTPUT_FILE="../avro/CustomerCreated.avsc"

echo "Downloading latest schema for subject: $SUBJECT"

# Ensure the target directory exists
mkdir -p "$(dirname "$OUTPUT_FILE")"

# Fetch the full JSON response
RESPONSE=$(curl -s "$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions/latest")

# Check for errors in response (curl success but registry error)
if [[ $? -ne 0 || $RESPONSE == *"error_code"* ]]; then
    echo "Error: Failed to download schema. Registry response: $RESPONSE"
    exit 1
fi

# Use jq -r to extract the raw string content of the 'schema' field
echo "$RESPONSE" | jq -r .schema > "$OUTPUT_FILE"

if [ $? -eq 0 ]; then
    # Pretty print the output file for readability (optional)
    jq . "$OUTPUT_FILE" > "$OUTPUT_FILE.tmp" && mv "$OUTPUT_FILE.tmp" "$OUTPUT_FILE"
    echo "Schema successfully saved to $OUTPUT_FILE"
else
    echo "Error: Failed to parse schema with jq."
    exit 1
fi
