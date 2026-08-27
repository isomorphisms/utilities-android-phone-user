# Native registration, the VM, and threads

## Registration

### `RegisterNatives`

Associates an array of `JNINativeMethod` records with native declarations on one managed class. Each record supplies the exact managed name, descriptor, and native function pointer. Android recommends doing this from `JNI_OnLoad`, usually exposing only that lifecycle symbol from the shared library. Registration centralizes the boundary, avoids encoded exported names, and makes descriptor errors fail near load time.

### `UnregisterNatives`

Removes registered native implementations for a class. Normal applications rarely need it; libraries ordinarily remain registered for the class loader's lifetime. It is not a substitute for releasing global references, native allocations, threads, file descriptors, or other library state.

### `GetJavaVM`

Retrieves the process's `JavaVM*` from a valid `JNIEnv*`. The VM handle may be stored for later thread attachment. Do not instead store the current `JNIEnv*` globally, because that environment is tied to the current thread.

## Library lifecycle exports

### `JNI_OnLoad`

ART calls this optional export when the shared library is loaded through the managed library-loading mechanism. It receives `JavaVM*`; the library normally calls `GetEnv` for JNI 1.6, resolves/caches classes and member IDs, calls `RegisterNatives`, and returns `JNI_VERSION_1_6`. Returning an unsupported version or failure makes loading fail.

### `JNI_OnUnload`

ART may call this when the owning class loader and library are unloaded. The call occurs in an uncertain managed context, so cleanup should be conservative. Android application libraries often live for the process lifetime, but that common outcome is not a reason to omit explicit ownership rules.

## `JavaVM` thread operations

### `GetEnv`

Asks whether the current thread is already attached and requests a `JNIEnv*` for a specified JNI version. It returns `JNI_EDETACHED` rather than attaching automatically. This is the correct first question in code that may run on either an Android callback thread or a D/native-created thread.

### `AttachCurrentThread`

Attaches a native-created thread to ART and returns that thread's `JNIEnv*`. The thread then appears as a managed `Thread`, can call JNI, and must be detached before it exits. `JavaVMAttachArgs` can supply JNI version, modified-UTF-8 thread name, and an optional global `ThreadGroup` reference.

### `AttachCurrentThreadAsDaemon`

Attaches with daemon status. A daemon thread does not keep a standalone JVM alive during VM shutdown, but Android owns ART's process lifecycle anyway. The reference, exception, class-loader, and detach rules are otherwise the same.

### `DetachCurrentThread`

Detaches the calling native thread. It must have no managed frames still executing. Detachment releases that attached thread's local-reference table, but relying on detach as bulk cleanup lets long-lived threads accumulate locals; loops should delete them as they go.

### `DestroyJavaVM`

Requests VM shutdown in an embedded-JVM process. An Android APK does not own ART and must not try to destroy it. The slot exists because Android uses the standard Invocation API layout, not because it is an application lifecycle control.

## Free Invocation API symbols

### `JNI_GetDefaultJavaVMInitArgs`

Queries default creation arguments for a requested JNI version in an embedding host. Android applications receive an existing ART VM and do not use this to configure the phone runtime.

### `JNI_CreateJavaVM`

Creates a VM in a native host under the standard Invocation API. This is not how an APK starts ART, and Android permits only one VM in a process. Treat it as header completeness, not an IB implementation route.

### `JNI_GetCreatedJavaVMs`

Returns handles for already-created VMs in embedding environments. Android native application code is normally handed its VM through `JNI_OnLoad`, `GetJavaVM`, or `ANativeActivity` rather than discovering it this way.

