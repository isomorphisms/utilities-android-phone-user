# build-apk-no-java

A small, Gradle-free route from native source to an Android APK.

The application contains no Java, Kotlin, or `classes.dex`. Android's built-in
`NativeActivity` loads the native library named by the manifest.

## What the build does

1. NDK Clang compiles one shared library per requested ABI.
2. `aapt2` compiles optional resources and creates the base APK.
3. `zip` inserts the native libraries without compressing them.
4. `zipalign` aligns the APK and its native libraries.
5. An optional APK signer signs and verifies the aligned APK.

There is no Gradle project, Gradle wrapper, Maven repository, Java compiler, or
DEX step.

## Requirements

- POSIX shell
- Android SDK platform and Build Tools (`aapt2` and `zipalign`)
- Android NDK
- `zip`
- an APK-signing command only when producing an installable signed APK

`aapt2`, `zipalign`, NDK Clang, and this script are native executables. Google's
SDK `apksigner` is not: its launcher invokes a JVM. To keep the entire host
build Java-free, set `APK_SIGNER` to a native, apksigner-compatible signer that
supports at least APK Signature Scheme v2. The script deliberately does not
download or trust one automatically.

Without `APK_SIGNER`, the result is an aligned unsigned APK. That cleanly keeps
assembly separate from the choice and custody of a signing implementation and
private key.

## Try the included native app

```sh
export ANDROID_SDK_ROOT=/path/to/android-sdk
export ANDROID_NDK_ROOT=/path/to/android-ndk

./build-apk.sh example
```

The result is written under `example/build/`. It contains a small ARMv7 native
app by default; the screen is painted dark red so a launch is visible.

To build more ABIs:

```sh
ABI_LIST='armeabi-v7a arm64-v8a x86_64' ./build-apk.sh example
```

To sign, keep the key outside Git and provide a compatible signer:

```sh
APK_SIGNER=/path/to/native-signapk \
ANDROID_KEYSTORE=/secure/path/app.jks \
./build-apk.sh example
```

The signer is invoked using this small interface:

```text
SIGNER sign --ks KEYSTORE --in ALIGNED_APK --out SIGNED_APK
SIGNER verify --verbose SIGNED_APK
```

If a signer needs extra arguments, put them in `APK_SIGNER_ARGS`. Do not put
passwords in a tracked configuration file.

## Use it for another app

Copy `example/`, then edit:

- `app.conf`: build names, SDK levels, ABIs, sources, flags, and libraries;
- `AndroidManifest.xml`: application ID, label, permissions, and native library
  name;
- `src/`: C, D, Idriç-generated objects, or any other inputs accepted by the
  selected compiler and linker.

`app.conf` is a shell fragment read by `build-apk.sh`. Paths in it are relative
to the app directory. A project may override `CC_<ABI>` to point at another
compiler or replace `SOURCE_FILES` with already-generated source.

The example intentionally has no resource directory. If an app has `res/`, the
script compiles it with `aapt2` and includes it.

## Useful commands

```sh
./build-apk.sh example          # build; sign only when APK_SIGNER is set
adb install -r example/build/native-example-0.1.0.apk
rm -rf example/build            # clean
```

The signing key determines application identity across updates. Back it up and
never commit it.
