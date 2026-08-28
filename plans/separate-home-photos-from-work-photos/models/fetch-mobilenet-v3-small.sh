#!/bin/sh
set -eu

here=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
out="$here/mobilenet_v3_small.tflite"
url='https://storage.googleapis.com/mediapipe-models/image_embedder/mobilenet_v3_small/float32/1/mobilenet_v3_small.tflite'

if [ -e "$out" ]; then
    printf '%s\n' "already exists: $out" >&2
    exit 0
fi

if command -v curl >/dev/null 2>&1; then
    curl --fail --location --output "$out.tmp" "$url"
elif command -v wget >/dev/null 2>&1; then
    wget --output-document="$out.tmp" "$url"
else
    printf '%s\n' 'need curl or wget to fetch the model' >&2
    exit 1
fi

mv "$out.tmp" "$out"
printf 'downloaded: %s\n' "$out"
wc -c "$out"
if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$out"
fi
