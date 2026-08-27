# Android JNI 1.6 interface map

This branch records the complete JNI surface exposed by Android's `jni.h`: the 229 entries in `JNINativeInterface`, the five entries in `JNIInvokeInterface`, the three free VM-initialization symbols, the two ordinary library lifecycle exports, and the types, signatures, constants, ownership rules, and Android-specific restrictions needed to use them correctly.

JNI is not a second Android SDK. It is a small, general mechanism for crossing between native machine code and objects managed by Android Runtime (ART). Once native code has found an Android class and method, JNI can call it; therefore the set of *possible Android operations* is the entire public Android framework, not another finite list hidden inside JNI. This directory inventories the finite JNI machinery itself. Separate SDK inventories describe classes such as `Intent`, `Activity`, `ContentResolver`, and `ClipboardManager` that can be reached through that machinery.

## Pinned boundary

The target is Android's JNI 1.6 ABI as used by Android 14 / API level 34. Android states that it supports JNI 1.6 except for `DefineClass`, because Android executes DEX rather than ordinary JVM class files. The table is ABI-shaped: its order, types, and function-pointer calling convention matter to a D binding just as much as the names do.

The source of truth for the inventory is AOSP's [`include_jni/jni.h`](https://android.googlesource.com/platform/libnativehelper/+/HEAD/include_jni/jni.h). Oracle's [JNI function specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html) explains the VM-neutral contract, and Android's [JNI guidance](https://developer.android.com/training/articles/perf-jni) records ART-specific behavior.

## Files

- [`inventory.md`](inventory.md) — every callable entry and exported hook, with every typed family expanded.
- [`abi-types-signatures.md`](abi-types-signatures.md) — scalar and reference types, method descriptors, `jvalue`, function tables, C/D layout concerns, and return/error constants.
- [`classes-reflection.md`](classes-reflection.md) — version, class lookup, inheritance tests, and reflection conversion.
- [`objects-fields-methods.md`](objects-fields-methods.md) — allocation, constructors, method lookup/calls, and instance/static field access.
- [`strings-arrays-buffers.md`](strings-arrays-buffers.md) — UTF-16, modified UTF-8, object/primitive arrays, critical access, and direct byte buffers.
- [`references-exceptions-monitors.md`](references-exceptions-monitors.md) — local/global/weak references, pending exceptions, fatal errors, and Java monitors.
- [`registration-vm-threads.md`](registration-vm-threads.md) — native registration, `JavaVM`, thread attachment, library loading, and the Invocation API.
- [`android-art.md`](android-art.md) — class-loader traps, `NativeActivity`, CheckJNI, permissions, and what JNI does not bypass.
- [`ib-file-picker-route.md`](ib-file-picker-route.md) — the concrete JNI route for IB's document picker, shared URL, and pre-paint acceptance path.

## Binding stance

A D binding should reproduce the AOSP table exactly and then place small typed wrappers above it. The raw table is inherently unsafe: most misuse is not checked, almost every object-returning call can leave an exception pending, references have lifetimes not expressed by the C types, and `JNIEnv*` belongs to one thread. The higher layer should make those facts visible rather than pretending a raw `jobject` is an ordinary durable pointer.

