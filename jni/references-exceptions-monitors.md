# References, exceptions, and monitors

## Local reference frames

### `PushLocalFrame` and `PopLocalFrame`

`PushLocalFrame` reserves room for a bounded group of local references. `PopLocalFrame` deletes everything created in that frame while optionally promoting one result into the previous frame. This pair is useful around loops or helper routines that create many temporary managed objects. It controls reference-table lifetime, not the lifetime of native memory acquired from strings or arrays.

### `DeleteLocalRef`, `NewLocalRef`, and `EnsureLocalCapacity`

`DeleteLocalRef` releases one local handle early, which matters in long loops and on native-attached threads. `NewLocalRef` creates a strong local handle from another valid reference; it is the safe way to attempt to strengthen a weak global before use. `EnsureLocalCapacity` asks the VM to reserve capacity for a stated number of additional locals and leaves an exception when it cannot.

Local references belong to the current thread and current native frame. Arguments passed into a native method and almost all object results from JNI are locals. They must not be stored and reused after returning to managed code.

## Global references

### `NewGlobalRef` and `DeleteGlobalRef`

A global reference stays valid across native returns and may be used from attached threads. It also keeps the managed object alive. Every successful creation needs an intentional deletion; otherwise the native layer leaks managed reachability. A global reference's bit pattern may differ from a local reference to the same object.

### `IsSameObject`

Tests whether two handles refer to the same managed object. Native pointer equality is invalid for this purpose because different handles may name one object and handle values may be reused after deletion. `IsSameObject(ref, null)` is also the supported way to test whether a weak global has been cleared, though strengthening it first avoids a collection race.

## Weak global references

### `NewWeakGlobalRef` and `DeleteWeakGlobalRef`

A weak global can survive native frames and threads without keeping the object alive. Before doing anything with it, create a strong local or global reference; if strengthening returns null, collection has won. Delete the weak handle itself when it is no longer needed.

### `GetObjectRefType`

Reports whether a valid handle is local, global, weak global, or invalid. It is diagnostic/type information, not permission to use a deleted reference. Calling it with a stale, already-deleted handle is itself outside the contract.

## Pending exceptions

### `Throw` and `ThrowNew`

`Throw` marks an existing `Throwable` as pending. `ThrowNew` constructs one from an exception class and a modified-UTF-8 message. Native code then returns through the JNI boundary; setting an exception is not equivalent to a D control-flow throw and does not automatically stop subsequent native instructions.

### `ExceptionOccurred`, `ExceptionCheck`, and `ExceptionClear`

`ExceptionOccurred` returns a local reference to the pending throwable, while `ExceptionCheck` answers the common yes/no question without creating that reference. `ExceptionClear` deliberately removes the pending exception. Clearing merely to keep calling functions loses the managed failure; a wrapper should either translate it to a native error value or let it propagate.

With an exception pending, only a restricted cleanup/query subset of JNI operations is safe. The default rule is: check after any call that can throw, release native resources, and return rather than continuing a normal operation on null IDs or references.

### `ExceptionDescribe`

Prints the pending exception and stack trace to the runtime's diagnostic channel and clears it as part of the reference implementation behavior. It is a debugging operation, not production error transport; Android logging and a preserved exception are usually more useful.

### `FatalError`

Reports an unrecoverable VM/native integration error and terminates the process. It does not return. It is inappropriate for ordinary file-picker cancellation, a missing method, malformed input, or a network failure.

## Java monitors

### `MonitorEnter` and `MonitorExit`

Acquire and release the monitor associated with a managed object, corresponding to Java synchronization. Every successful enter must be paired with an exit on all native paths. Native language locks and Java monitors are separate systems; holding either while calling across the boundary can form deadlocks that neither side's type system reveals.

