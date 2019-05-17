#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"/..
version_file=build.gradle

check_configuration() {
    if [[ ! -f "app/google-services.json" ]]; then
        echo "google-services.json is missing"
        exit 1
    fi
}

check_working_directory() {
    if [[ ! -z "$(git status --porcelain)" ]]; then
        echo "Working directory is dirty"
        exit 1
    fi
}

publish() {
    ./gradlew clean :app:lintProdRelease :app:publishProdReleaseBundle
}

increment_version() {
    version_code_pattern="\(.*versionCode: \)\([0-9]*\),"
    version_code=$(sed -n "s/$version_code_pattern/\2/p" "$version_file")
    new_version_code=$((version_code + 1))
    sed -i '' -e "s/$version_code_pattern/\1$new_version_code,/" "$version_file"

    echo "New versionCode: $new_version_code"
}

commit_version() {
    git add "$version_file"
    git commit -m "Bump versionCode for release"
}

check_configuration
check_working_directory

increment_version
publish
commit_version
