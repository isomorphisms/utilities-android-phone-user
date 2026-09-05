#!/bin/sh
set -eu

root=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)
build=$(mktemp -d "${TMPDIR:-/tmp}/android-clipboard.XXXXXX")
trap 'rm -rf "$build"' EXIT HUP INT TERM

cc -std=c11 -Wall -Wextra -Werror -pedantic \
  -I"$root" \
  "$root/utf8.c" "$root/test_utf8.c" \
  -o "$build/test_utf8"
"$build/test_utf8"

: "${JAVA_HOME:?JAVA_HOME must name a JDK for jni.h}"
case "$(uname -s)" in
  Linux) jni_platform=linux ;;
  Darwin) jni_platform=darwin ;;
  *)
    echo "unsupported host for JNI header syntax check: $(uname -s)" >&2
    exit 2
    ;;
esac

cc -std=c11 -Wall -Wextra -Werror -pedantic \
  -I"$root" \
  -I"$JAVA_HOME/include" \
  -I"$JAVA_HOME/include/$jni_platform" \
  -c "$root/clipboard_jni.c" \
  -o "$build/clipboard_jni.o"

printf '%s\n' 'PASS: JNI bridge compiles against host JNI headers'
