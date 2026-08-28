#!/bin/sh
set -eu

die() {
    echo "build-apk-no-java: $*" >&2
    exit 2
}

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
case ${1:-} in
    /*) app_dir=$1 ;;
    '') app_dir=$script_dir/example ;;
    *) app_dir=$PWD/$1 ;;
esac
app_dir=$(CDPATH= cd -- "$app_dir" 2>/dev/null && pwd) || die "app directory not found: ${1:-example}"

config=$app_dir/app.conf
[ -f "$config" ] || die "missing configuration: $config"
# app.conf is deliberately a shell fragment so projects can use environment overrides.
# shellcheck source=/dev/null
. "$config"

: "${OUTPUT_NAME:?set OUTPUT_NAME in app.conf}"
: "${LIB_NAME:?set LIB_NAME in app.conf}"
: "${MIN_SDK:?set MIN_SDK in app.conf}"
: "${TARGET_SDK:?set TARGET_SDK in app.conf}"
: "${VERSION_CODE:?set VERSION_CODE in app.conf}"
: "${VERSION_NAME:?set VERSION_NAME in app.conf}"
: "${SOURCE_FILES:?set SOURCE_FILES in app.conf}"

ABI_LIST=${ABI_LIST:-armeabi-v7a}
APK_SIGNER_ARGS=${APK_SIGNER_ARGS:-}
MANIFEST=${MANIFEST:-AndroidManifest.xml}
RES_DIR=${RES_DIR:-res}
CFLAGS=${CFLAGS:--std=c17 -O2 -fPIC -ffunction-sections -fdata-sections -Wall -Wextra -Werror -Wpedantic}
CPPFLAGS=${CPPFLAGS:-}
LDFLAGS=${LDFLAGS:--shared -Wl,--no-undefined -Wl,--gc-sections -Wl,-z,relro,-z,now -Wl,-z,max-page-size=16384}
LDLIBS=${LDLIBS:--landroid}

sdk_root=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
[ -n "$sdk_root" ] || die "set ANDROID_SDK_ROOT"

ndk_root=${ANDROID_NDK_ROOT:-${ANDROID_NDK_HOME:-}}
if [ -z "$ndk_root" ]; then
    for candidate in "$sdk_root"/ndk/*; do
        [ -d "$candidate" ] && ndk_root=$candidate
    done
fi
[ -n "$ndk_root" ] && [ -d "$ndk_root" ] || die "set ANDROID_NDK_ROOT"

if [ -n "${ANDROID_BUILD_TOOLS:-}" ]; then
    build_tools=$ANDROID_BUILD_TOOLS
else
    build_tools=
    for candidate in "$sdk_root"/build-tools/*; do
        [ -d "$candidate" ] && build_tools=$candidate
    done
fi
[ -n "$build_tools" ] || die "Android Build Tools not found under $sdk_root/build-tools"

platform_jar=${ANDROID_PLATFORM_JAR:-$sdk_root/platforms/android-$TARGET_SDK/android.jar}
aapt2=$build_tools/aapt2
zipalign=$build_tools/zipalign

case $(uname -s) in
    Linux) default_host_tag=linux-x86_64 ;;
    Darwin) default_host_tag=darwin-x86_64 ;;
    *) die "unsupported host; set NDK_HOST_TAG after adding its mapping" ;;
esac
host_tag=${NDK_HOST_TAG:-$default_host_tag}
toolchain=$ndk_root/toolchains/llvm/prebuilt/$host_tag

[ -x "$aapt2" ] || die "missing executable: $aapt2"
[ -x "$zipalign" ] || die "missing executable: $zipalign"
[ -f "$platform_jar" ] || die "missing platform: $platform_jar"
command -v zip >/dev/null 2>&1 || die "zip is required"
[ -f "$app_dir/$MANIFEST" ] || die "missing manifest: $app_dir/$MANIFEST"

build_dir=$app_dir/build
object_root=$build_dir/objects
stage_dir=$build_dir/stage
mkdir -p "$object_root" "$stage_dir"
rm -rf "$object_root" "$stage_dir/lib"
mkdir -p "$object_root" "$stage_dir/lib"

compiler_for_abi() {
    case $1 in
        armeabi-v7a) echo "$toolchain/bin/armv7a-linux-androideabi${MIN_SDK}-clang" ;;
        arm64-v8a) echo "$toolchain/bin/aarch64-linux-android${MIN_SDK}-clang" ;;
        x86) echo "$toolchain/bin/i686-linux-android${MIN_SDK}-clang" ;;
        x86_64) echo "$toolchain/bin/x86_64-linux-android${MIN_SDK}-clang" ;;
        *) die "unsupported ABI: $1" ;;
    esac
}

for abi in $ABI_LIST; do
    compiler=$(compiler_for_abi "$abi")
    [ -x "$compiler" ] || die "missing compiler: $compiler"
    object_dir=$object_root/$abi
    library_dir=$stage_dir/lib/$abi
    mkdir -p "$object_dir" "$library_dir"

    objects=
    object_number=0
    for source in $SOURCE_FILES; do
        object_number=$((object_number + 1))
        object=$object_dir/$object_number.o
        # Project-controlled flags intentionally split into ordinary compiler arguments.
        # shellcheck disable=SC2086
        "$compiler" $CPPFLAGS $CFLAGS -c "$app_dir/$source" -o "$object"
        objects="$objects $object"
    done

    # shellcheck disable=SC2086
    "$compiler" $LDFLAGS $objects $LDLIBS -o "$library_dir/lib$LIB_NAME.so"
done

compiled_resources=$build_dir/resources.zip
resource_args=
if [ -d "$app_dir/$RES_DIR" ] && find "$app_dir/$RES_DIR" -type f -print -quit | grep -q .; then
    rm -f "$compiled_resources"
    "$aapt2" compile --dir "$app_dir/$RES_DIR" -o "$compiled_resources"
    resource_args="-R $compiled_resources"
fi

base_apk=$build_dir/base.apk
unaligned_apk=$build_dir/$OUTPUT_NAME-$VERSION_NAME-unaligned-unsigned.apk
unsigned_apk=$build_dir/$OUTPUT_NAME-$VERSION_NAME-unsigned.apk
signed_apk=$build_dir/$OUTPUT_NAME-$VERSION_NAME.apk

# shellcheck disable=SC2086
"$aapt2" link \
    -I "$platform_jar" \
    --manifest "$app_dir/$MANIFEST" \
    --min-sdk-version "$MIN_SDK" \
    --target-sdk-version "$TARGET_SDK" \
    --version-code "$VERSION_CODE" \
    --version-name "$VERSION_NAME" \
    $resource_args \
    -o "$base_apk"

cp "$base_apk" "$unaligned_apk"
unaligned_absolute=$(CDPATH= cd -- "$build_dir" && pwd)/$(basename "$unaligned_apk")
(
    cd "$stage_dir"
    # Native libraries stay uncompressed so Android can map them directly.
    # shellcheck disable=SC2046
    zip -0 -q "$unaligned_absolute" $(find lib -type f -print)
)

"$zipalign" -f -P 16 4 "$unaligned_apk" "$unsigned_apk"

if [ -z "${APK_SIGNER:-}" ]; then
    echo "$unsigned_apk"
    exit 0
fi

[ -x "$APK_SIGNER" ] || die "APK_SIGNER is not executable: $APK_SIGNER"
keystore=${ANDROID_KEYSTORE:-}
[ -n "$keystore" ] && [ -f "$keystore" ] || die "set ANDROID_KEYSTORE to an existing key"

# APK_SIGNER_ARGS is for signer-specific options. Keep secrets out of app.conf.
# shellcheck disable=SC2086
"$APK_SIGNER" sign $APK_SIGNER_ARGS --ks "$keystore" --in "$unsigned_apk" --out "$signed_apk"
"$APK_SIGNER" verify --verbose "$signed_apk"
echo "$signed_apk"
