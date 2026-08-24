#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
build_dir="${TMPDIR:-/tmp}/programmers-unicode-pad-tests"
mkdir -p "$build_dir"

common_flags="-std=c17 -Wall -Wextra -Werror -Wpedantic -Wconversion -Wshadow -g"
sanitizers="-fsanitize=address,undefined -fno-omit-frame-pointer"
includes="-I$project_dir/app/src/main/c"

cc $common_flags $sanitizers $includes \
    "$project_dir/app/src/main/c/pad_model.c" \
    "$project_dir/app/src/test-c/pad_model_test.c" \
    -o "$build_dir/pad_model_test"

cc $common_flags $sanitizers $includes \
    "$project_dir/app/src/main/c/pad_model.c" \
    "$project_dir/app/src/main/c/pad_ui.c" \
    "$project_dir/app/src/test-c/pad_ui_test.c" \
    -o "$build_dir/pad_ui_test"

ASAN_OPTIONS=detect_leaks=0:halt_on_error=1 UBSAN_OPTIONS=halt_on_error=1 \
    "$build_dir/pad_model_test"
ASAN_OPTIONS=detect_leaks=0:halt_on_error=1 UBSAN_OPTIONS=halt_on_error=1 \
    "$build_dir/pad_ui_test"
