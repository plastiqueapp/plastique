#!/usr/bin/env bash
set -e

cd "$(dirname "${BASH_SOURCE[0]}")"/..
version_file=build.gradle

# Ensure that working directory is clean
if [[ ! -z "$(git status --porcelain)" ]]; then
    echo "Working directory is dirty"
    exit 1
fi

# Ensure that google-services.json exists
if [[ ! -f "app/google-services.json" ]]; then
    echo "google-services.json is missing"
    exit 1
fi

# Increment versionCode
version_code_pattern="\(.*versionCode: \)\([0-9]*\),"
version_code=$(sed -n "s/$version_code_pattern/\2/p" "$version_file")
new_version_code=$((version_code + 1))
sed -i '' -e "s/$version_code_pattern/\1$new_version_code,/" "$version_file"

echo "New versionCode: $new_version_code"

# Publish
./gradlew clean :app:lintProdRelease :app:publishProdReleaseBundle

# Commit changes
git add "$version_file"
git commit -m "Bump versionCode for release"
