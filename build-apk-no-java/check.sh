#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

sh -n "$project_dir/build-apk.sh"
sh -n "$project_dir/example/app.conf"

if find "$project_dir" -type f \( -name '*.java' -o -name '*.kt' -o -name '*.gradle' \) -print | grep -q .; then
    echo "Java, Kotlin, or Gradle source found" >&2
    exit 1
fi

if grep -n -E 'classes\.dex|gradlew|keytool' "$project_dir/build-apk.sh"; then
    echo "DEX, Gradle wrapper, or Java keytool route found" >&2
    exit 1
fi

echo "no Java, Kotlin, DEX, or Gradle build input"
