#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: bash analysis.sh <path_to_folder>"
  exit 1
fi

folder_path="$1"

parent_dir="$(dirname "$folder_path")"

sample_file="${parent_dir}/sampleFile.txt"

> "$sample_file"

echo "Uploading files from $folder_path to storage..."

go run push_to_holmes.go --gateway http://127.0.0.1:8090 --user test --pw test --tags '["tag1","tag2"]' --comment "mycomment" --insecure --workers 5 --src virusshare --dir "$folder_path" &
upload_pid=$!

wait "$upload_pid"

echo "Files successfully uploaded."
echo "Creating sample file..."

for file_path in "${folder_path}"/*.exe; do
  sha256_hash=$(sha256sum "$file_path" | awk '{print $1}')
  filename=$(basename "$file_path")
  echo "$sha256_hash $filename src1" >> "$sample_file"
done

echo "Sample file created."
echo "Pushing files to Holmes..."

go run push_to_holmes.go --gateway http://localhost:8090 --user test --pw test --tags '["tag1","tag2"]' --comment "mycomment" --insecure --tasking --file "$sample_file" --tasks '{"CUCKOO": [] }'

echo "Analysis has started."
