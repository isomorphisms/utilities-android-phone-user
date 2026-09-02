#!/bin/sh
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
manifest="$root/app/src/main/AndroidManifest.xml"
report="$root/receipts/NOT_RUN.json"
main="$root/app/src/main/java/org/isomorphisms/pdffillerspike/MainActivity.kt"

if grep -Eq 'android.permission.INTERNET|MANAGE_EXTERNAL_STORAGE|READ_EXTERNAL_STORAGE|WRITE_EXTERNAL_STORAGE' "$manifest"; then
    echo "FAIL manifest contains a forbidden permission" >&2
    exit 1
fi

for gate in OPEN FORM SAVE FLAT_TEXT CHECK; do
    grep -Fq "\"$gate\"" "$report"
    grep -Fq "Gate.$gate" "$main"
done

skip_count=$(grep -c '"outcome": "SKIP"' "$report")
test "$skip_count" -eq 5

grep -Fq '1.0.0-beta01' "$root/app/build.gradle.kts"
grep -Fq 'compileSdk = 36' "$root/app/build.gradle.kts"
grep -Fq 'compileSdkExtension = 19' "$root/app/build.gradle.kts"
grep -Fq 'targetSdk = 36' "$root/app/build.gradle.kts"

echo "PASS source contract"
echo "SKIP OPEN FORM SAVE FLAT_TEXT CHECK: no device in source check"
