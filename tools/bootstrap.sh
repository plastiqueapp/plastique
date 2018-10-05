#!/usr/bin/env bash

function write_config_file() {
    echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > $1
    echo "<resources>" >> $1
    echo "    <string name=\"api_client_id\">$2</string>" >> $1
    echo "    <string name=\"api_client_secret\">$3</string>" >> $1
    echo "</resources>" >> $1

    echo "Wrote $1"
}

while read -p "Enter client_id: " client_id && [ -z "$client_id" ]; do :; done
while read -p "Enter client_secret: " client_secret && [ -z "$client_secret" ]; do :; done

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
config_file="$project_dir/app/src/main/res/values/config.xml"

write_config_file "$config_file" "$client_id" "$client_secret"
