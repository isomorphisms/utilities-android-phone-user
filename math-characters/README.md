# Programmer's Unicode Pad

A small standalone Android pad for entering the symbols and text macros from the Programmer's Keyboard layouts. Tap keys into the buffer, tap **COPY**, switch to the target app, and paste.

The APK contains native C machine code only:

- no Java or Kotlin source
- no generated `classes.dex`
- no app classes or inheritance
- no network or Android permissions
- `arm64-v8a`, `armeabi-v7a`, and `x86_64` native libraries

Android's platform-provided `NativeActivity`, `Canvas`, and clipboard service supply the OS boundary. The application state, layouts, editing, touch handling, rendering orchestration, and clipboard call site are C.

## Pages

The first page keeps the earlier Math Characters set: arrows, number sets, ordinary digits, superscripts, and subscripts.

The other eight pages are the current hardware-keyboard groups:

1. Math
2. Programming
3. Regular Expressions
4. Concept Separation
5. Movement
6. Incantation Assistance
7. Several Pastebins
8. Signals

The layouts were transcribed from `isomorphisms/programmers-keyboard` at commit `43f900c61a3f03d612e19d5cbacbab820b5c0dc3`. That includes the paired `⟦ ⟧` evaluation key, Unicode minus `−`, quad space, all four regex rows, `7×` through `29×`, and the three pastebin slots. The separately confirmed thin-space key is carried forward beside quad space.

Paired delimiters insert both characters and leave the cursor between them. Pastebin slots last for the current app process. Signal keys insert explicit names such as `SIGTERM`; a standalone Android character pad does not pretend it can signal another process.

## Why this is a pad, not an IME

Android requires a software keyboard service to inherit from `InputMethodService`, which is a Java framework class loaded from DEX bytecode. A true IME therefore cannot also satisfy “no Java/Kotlin, no classes, no virtual-machine bytecode.” This build takes the native-compatible side of that boundary: a UnicodePad-style standalone buffer and clipboard.

## Tests

Run the platform-independent behavior suite on Linux:

```sh
./run-host-tests.sh
```

It compiles with strict warnings plus address and undefined-behavior sanitizers. The tests cover:

- every page, row, label, output, and UTF-8 string
- Unicode-aware insertion, cursor movement, and backspace
- paired insertion and exact quad/thin/ordinary spaces
- repeat counts, line movement, undo, history, and pastebin slots
- buffer-capacity failure without partial writes
- touch hit-testing for every key in portrait and landscape

The Android workflow then builds the APK, verifies that it is signed, aligned, permission-free, DEX-free, and contains every target ABI, and launches it in an API 29 emulator. The emulator taps a symbol, copies it, changes pages, taps a second key, captures the screen, and checks that the native process remains alive without a fatal log entry.

## Build

The build directly composes NDK Clang, `aapt2`, `zip`, `zipalign`, and `apksigner`. It needs JDK 17 for the Android signing program, Android SDK/build-tools 36, and NDK 27.2.12479018:

```sh
./build-apk.sh
```

The debug APK is `math-characters/app/build/outputs/apk/debug/app-debug.apk`. Pull-request runs upload it as `programmers-unicode-pad-debug-apk`.
