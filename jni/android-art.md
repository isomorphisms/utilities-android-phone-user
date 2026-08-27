# Android/ART-specific rules

## Android supports JNI 1.6, with one conspicuous hole

Android documents support for JNI 1.6 except `DefineClass`. Android applications package DEX, and application code must use Android's class loaders rather than feeding ordinary JVM class bytes through JNI. The function remains physically present so the rest of the table has the standard ABI.

## `NativeActivity` already hands native code the bridge

An `ANativeActivity` contains `JavaVM* vm`, `JNIEnv* env`, and `jobject clazz`. Despite the name, `clazz` is the actual `NativeActivity` instance, not a class object. The `env` member is valid only on the main thread on which Android invokes the activity callbacks. Worker threads use `vm` to attach and obtain their own environments.

This is the clean D-only Android entry: Android instantiates its built-in `NativeActivity`, loads the D shared library, and gives the native code the activity object and JNI handles. D can then call inherited `Activity`/`Context` methods without adding an application-authored Java wrapper.

## JNI reaches framework objects; it does not bypass Android

Calling `Activity.startActivityForResult`, `ContentResolver.openFileDescriptor`, `ClipboardManager`, or another framework method through JNI has the same Android permissions, lifecycle, exported-component, storage, and background-execution rules as calling it from Kotlin. JNI changes the calling language, not the authority of the process.

Likewise, JNI does not provide networking. A native app may use native sockets/curl after declaring `android.permission.INTERNET`, or it may call an Android networking class through JNI. ICU is a Unicode library and is unrelated to this choice.

## Class loaders

`FindClass` uses the class-loader context associated with the current managed/native call. In `JNI_OnLoad` and in native methods called by application classes, application lookup generally works. A thread created entirely in D and then attached to ART may see only the system class loader. Cache application classes during loading, pass required `Class` objects across the boundary, or cache the application's `ClassLoader.loadClass` method.

Class references cached past the current call must be global references. Method and field IDs can be cached, but their defining class must remain loaded.

## References are handles, not pointers

ART may move objects. `jobject` values are indirect handles and can differ even when they name the same object. Never cast a managed reference to a D object pointer, use it as a stable identity key, compare it with native equality, or retain a local reference after its frame/thread ends.

## Strings are not ordinary UTF-8

The `UTF`-named JNI functions use modified UTF-8. Android files, URLs, JSON, and network bodies ordinarily use standard UTF-8. IB should decode ordinary UTF-8 itself and cross JNI in UTF-16 when it needs a `String`, rather than allowing malformed or supplementary text to be reinterpreted through `NewStringUTF`.

## Exceptions are a second result channel

Many JNI functions return null, zero, or a negative value *and* leave a managed exception pending. D wrappers should model both. Continuing after a failed `GetMethodID` and calling through the null ID is a common crash pattern. For the file picker, cancellation is an ordinary result; a pending security, class, or URI exception is a different result and should be recorded distinctly.

## CheckJNI

ART's CheckJNI mode detects many invalid references, wrong member kinds, bad signatures, pending-exception misuse, invalid release pointers, and thread errors. The phone acceptance harness should run once with CheckJNI enabled when practical. Passing CheckJNI does not prove lifetimes or application semantics, but it catches precisely the ABI misuse most likely in a new D table binding.

## Keep crossings coarse

The expensive and error-prone part is usually not one indirect call; it is repeated marshalling, reference creation, exception checks, and thread switching. IB should make a few coarse platform requests—open picker, return selected URIs, open descriptor, report result—instead of constructing its document model by thousands of tiny JNI calls.

