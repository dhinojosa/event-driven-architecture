#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Function to check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed." >&2
    exit 1
fi

SR_URL="http://localhost:8081"
TARGET_DIR="../avro"
mkdir -p "$TARGET_DIR"

# The root subject we want to download (must match what was uploaded)
ROOT_SUBJECT="order-value"

echo "Starting schema download for subject: $ROOT_SUBJECT"

# Helper function to download a schema and its references recursively
# Usage: download_schema <SUBJECT> <VERSION>
download_schema() {
    local subject=$1
    local version=$2

    echo "Downloading $subject (v$version)..."

    # Fetch the full schema object (contains schema string + references list)
    local response
    if [ "$version" == "latest" ]; then
        response=$(curl -s "$SR_URL/subjects/$subject/versions/latest")
    else
        response=$(curl -s "$SR_URL/subjects/$subject/versions/$version")
    fi

    # Check if request failed
    if echo "$response" | grep -q "error_code"; then
        echo "Error downloading $subject: $response" >&2
        exit 1
    fi

    # 1. Extract and Save the Schema Content
    # We assume the schema name matches the filename we want.
    # We try to parse the "name" from the AVRO schema definition itself to name the file.
    local schema_content
    schema_content=$(echo "$response" | jq -r '.schema')

    local schema_name
    schema_name=$(echo "$schema_content" | jq -r '.name')

    if [ "$schema_name" == "null" ]; then
         # Fallback if name isn't in the top level (unlikely for Record types)
         schema_name="$subject"
    fi

    local file_path="$TARGET_DIR/$schema_name.avsc"

    # Only save if it doesn't exist or overwrite? Let's overwrite to be fresh.
    echo "$schema_content" | jq . > "$file_path"
    echo "Saved: $file_path"

    # 2. Process References (Recursion)
    # Extract the references array
    local references_json
    references_json=$(echo "$response" | jq '.references // []')

    # Iterate over each reference
    # We use jq to generate a list of "subject version" pairs to loop over safely in bash
    echo "$references_json" | jq -r '.[] | "\(.subject) \(.version)"' | while read -r ref_subject ref_version; do
        if [ -n "$ref_subject" ]; then
            # Recursively download the referenced schema
            # (Note: In a real recursive script, we might want to track visited subjects to avoid infinite loops,
            # but AVRO DAGs are usually strictly hierarchical here)
            download_schema "$ref_subject" "$ref_version"
        fi
    done
}

# Start the process
download_schema "$ROOT_SUBJECT" "latest"

echo "----------------------------------------------------------------"
echo "All schemas downloaded to $TARGET_DIR"
