#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "usage: $0 APK" >&2
    exit 2
fi

apk=$1
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
build_tools="${ANDROID_HOME:?ANDROID_HOME is required}/build-tools/36.0.0"

test -s "$apk"

source_code=$(find "$project_dir/app/src" -type f \
    \( -name '*.java' -o -name '*.kt' -o -name '*.kts' \) -print)
test -z "$source_code"

entries=$(unzip -Z1 "$apk")
if printf '%s\n' "$entries" | grep -Eq '^classes([0-9]+)?\.dex$'; then
    echo "unexpected DEX bytecode in native APK" >&2
    exit 1
fi

for abi in arm64-v8a armeabi-v7a x86_64; do
    printf '%s\n' "$entries" | grep -Fx "lib/$abi/libunicode_pad.so" >/dev/null
done

badging=$($build_tools/aapt2 dump badging "$apk")
printf '%s\n' "$badging" | grep -F "package: name='com.isomorphisms.programmersunicodepad'" >/dev/null
printf '%s\n' "$badging" | grep -F "launchable-activity: name='android.app.NativeActivity'" >/dev/null

permissions=$($build_tools/aapt2 dump permissions "$apk")
if printf '%s\n' "$permissions" | grep -F 'uses-permission:' >/dev/null; then
    echo "standalone pad unexpectedly requests an Android permission" >&2
    printf '%s\n' "$permissions" >&2
    exit 1
fi

$build_tools/zipalign -c -P 16 -v 4 "$apk" >/dev/null
$build_tools/apksigner verify --verbose "$apk" >/dev/null

echo "check-apk: native, DEX-free, permission-free APK verified"
