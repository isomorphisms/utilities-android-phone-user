#!/bin/sh
set -eu

here=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
out="$here/mobilenet_v3_small.tflite"
url='https://storage.googleapis.com/mediapipe-models/image_embedder/mobilenet_v3_small/float32/1/mobilenet_v3_small.tflite'
expected_size=4117670
expected_sha256='bbbb4c51a55a53905af1daec995ca1aae355046f8839bb8c9f5ce9271394bc40'

verify() {
    actual_size=$(wc -c < "$1" | tr -d ' ')
    [ "$actual_size" = "$expected_size" ] || {
        printf 'wrong model size: got %s expected %s\n' "$actual_size" "$expected_size" >&2
        return 1
    }

    if command -v sha256sum >/dev/null 2>&1; then
        actual_sha256=$(sha256sum "$1" | awk '{print $1}')
        [ "$actual_sha256" = "$expected_sha256" ] || {
            printf 'wrong model sha256: %s\n' "$actual_sha256" >&2
            return 1
        }
    fi
}

if [ -e "$out" ]; then
    verify "$out"
    printf '%s\n' "already present and verified: $out"
    exit 0
fi

trap 'rm -f "$out.tmp"' EXIT HUP INT TERM

if command -v curl >/dev/null 2>&1; then
    curl --fail --location --output "$out.tmp" "$url"
elif command -v wget >/dev/null 2>&1; then
    wget --output-document="$out.tmp" "$url"
else
    printf '%s\n' 'need curl or wget to fetch the model' >&2
    exit 1
fi

verify "$out.tmp"
mv "$out.tmp" "$out"
trap - EXIT HUP INT TERM
printf 'downloaded and verified: %s\n' "$out"
