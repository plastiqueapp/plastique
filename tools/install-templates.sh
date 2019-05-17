#!/usr/bin/env bash
set -euo pipefail

os_name=$(uname -s)
if [[ ! "$os_name" = "Darwin" ]]; then
    echo "Unsupported OS. Install templates manually."
    exit 1
fi

tools_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

function install_templates() {
    echo "Installing templates to $1"
    rsync -a --delete "$tools_dir/templates/" "$1/Contents/plugins/android/lib/templates/plastique/"
}

for app in /Applications/Android\ Studio*.app; do
    install_templates "$app"
done
