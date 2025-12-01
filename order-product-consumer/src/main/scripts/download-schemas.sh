#!/bin/bash

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed."
    exit 1
fi

SCHEMA_REGISTRY_URL="http://localhost:8081"
OUTPUT_DIR="../avro"

# Ensure the target directory exists
mkdir -p "$OUTPUT_DIR"

# Function to download a schema
# Usage: download_schema <SUBJECT> <FILENAME> [VERSION]
download_schema() {
    local SUBJECT=$1
    local FILENAME=$2
    local VERSION=${3:-latest} # Default to 'latest' if not provided
    local OUTPUT_FILE="$OUTPUT_DIR/$FILENAME"

    echo "Downloading schema for subject: $SUBJECT (Version: $VERSION)..."

    # Fetch the full JSON response
    RESPONSE=$(curl -s "$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions/$VERSION")

    # Check for errors in response (curl success but registry error)
    if [[ $? -ne 0 || $RESPONSE == *"error_code"* ]]; then
        echo "Error: Failed to download schema for $SUBJECT (Version: $VERSION). Registry response: $RESPONSE"
        return 1
    fi

    # Use jq -r to extract the raw string content of the 'schema' field
    echo "$RESPONSE" | jq -r .schema > "$OUTPUT_FILE"

    if [ $? -eq 0 ]; then
        # Pretty print the output file for readability (optional)
        jq . "$OUTPUT_FILE" > "$OUTPUT_FILE.tmp" && mv "$OUTPUT_FILE.tmp" "$OUTPUT_FILE"
        echo "Schema successfully saved to $OUTPUT_FILE"
    else
        echo "Error: Failed to parse schema with jq."
        return 1
    fi
}

# Download schemas with specific versions to lock the contract
# Replace '1' with the actual version numbers you want to lock to
download_schema "com.evolutionnext.inventory.events.ProductCreatedMessage" "ProductCreated.avsc" 1
download_schema "com.evolutionnext.inventory.events.PriceChangedMessage" "PriceChanged.avsc" 1
download_schema "com.evolutionnext.inventory.events.StockChangedMessage" "StockChanged.avsc" 1

# For the root event, you might also want to lock the version
download_schema "inventory-value" "InventoryEvent.avsc" 1

echo "All downloads complete."
