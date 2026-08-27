# Strings, arrays, and direct buffers

## UTF-16 strings

### `NewString`

Constructs a managed string from a counted sequence of `jchar` UTF-16 code units. Because the length is explicit, embedded zero code units do not terminate the input. The function does not validate the application's higher-level Unicode normalization policy; it transports code units.

### `GetStringLength`

Returns the number of UTF-16 code units, not Unicode scalar values, grapheme clusters, or bytes. A supplementary character represented by a surrogate pair therefore contributes two.

### `GetStringChars` and `ReleaseStringChars`

Acquire a read-only UTF-16 view that may be either a copy or VM-managed storage. The `isCopy` result is informational; correct code releases the pointer in either case and never keeps it after release. Acquiring characters does not produce a JNI local reference, so returning from the native method does not replace the required release call.

### `GetStringRegion`

Copies a specified UTF-16 slice into caller-provided storage. It avoids a separate get/release lifetime and is often the simplest bounded operation when native code needs only part of a string. The destination is not automatically zero-terminated.

### `GetStringCritical` and `ReleaseStringCritical`

Request the most direct UTF-16 access the VM can provide. Between the pair, native code must not block, perform arbitrary JNI work, or run for an unbounded time because the VM may have constrained garbage collection. This is a narrow performance lever, not a generally safer `GetStringChars`.

## Modified UTF-8 strings

### `NewStringUTF`

Constructs a managed string from zero-terminated **modified UTF-8**, not ordinary UTF-8. Modified UTF-8 encodes U+0000 as a two-byte sequence and represents supplementary characters through encoded UTF-16 surrogate code units. Passing arbitrary network/file UTF-8 directly is therefore unsafe; validate and convert it or use the UTF-16 functions.

### `GetStringUTFLength`

Returns the byte count of the modified-UTF-8 representation, excluding the terminator. It is not the ordinary UTF-8 byte length and certainly not the character count.

### `GetStringUTFChars` and `ReleaseStringUTFChars`

Acquire and release a zero-terminated modified-UTF-8 byte view. As with UTF-16 access, the pointer may be a copy and must always be released. For IB's ordinary UTF-8 files and fetched network bytes, converting deliberately is preferable to silently treating this format as UTF-8.

### `GetStringUTFRegion`

Copies a substring, selected in UTF-16 code units, into caller-provided modified-UTF-8 storage. The function does not append a terminator. The caller must allocate for the encoded worst case or compute a safe bound.

## Object arrays

### `GetArrayLength`

Returns the element count of any managed array, object or primitive. The count is a `jsize` (`jint`).

### `NewObjectArray`

Creates an array with an element class and an initial value used for every slot. The initial reference may be null. The array is a local reference, while each stored element is managed by ART.

### `GetObjectArrayElement` and `SetObjectArrayElement`

Read or write one element. A get creates a local reference that should be deleted inside large loops. A set can throw `ArrayStoreException` when the value is incompatible and can throw for an invalid index.

## Primitive arrays

### `New<Type>Array`

`NewBooleanArray`, `NewByteArray`, `NewCharArray`, `NewShortArray`, `NewIntArray`, `NewLongArray`, `NewFloatArray`, and `NewDoubleArray` allocate zero-initialized managed arrays of the named primitive type.

### `Get<Type>ArrayElements` and `Release<Type>ArrayElements`

The eight get operations return native pointers that may address pinned managed storage or temporary copies. The paired release accepts mode `0`, `JNI_COMMIT`, or `JNI_ABORT`. Mode `0` is the normal complete transaction. `JNI_COMMIT` may require a later release because it retains a copied buffer. `JNI_ABORT` discards changes only when the buffer was a copy; it cannot rewind writes already made to directly exposed storage.

These functions do not lock an array against simultaneous managed or native access. Synchronization and race freedom remain the program's responsibility.

### `Get<Type>ArrayRegion` and `Set<Type>ArrayRegion`

The eight get-region functions copy a bounded range into native storage; the eight set-region functions copy a bounded native range into the managed array. For small or one-shot transfers they avoid pin/copy ambiguity and make the copy direction explicit. Bounds errors appear as pending managed exceptions.

### `GetPrimitiveArrayCritical` and `ReleasePrimitiveArrayCritical`

Provide a type-erased critical access pair for primitive arrays. Like critical string access, the region must be extremely short: do not block, call arbitrary managed methods, perform file/network I/O, or retain the pointer. A D wrapper should make the critical lifetime lexical and difficult to escape.

## Direct byte buffers

### `NewDirectByteBuffer`

Wraps native memory in a managed `java.nio.ByteBuffer`. The native allocation must remain valid for every managed use; the buffer does not by itself establish how or when D frees that memory. This can avoid repeated copies for a stable native buffer but needs explicit ownership.

### `GetDirectBufferAddress`

Returns the starting native address of a compatible direct buffer, or null when the object is not a supported direct buffer. The address is meaningful only while the underlying storage remains alive.

### `GetDirectBufferCapacity`

Returns the direct buffer's capacity in bytes, or a negative result when the object is not a compatible direct buffer. Capacity is not the same as the buffer's current position, limit, or remaining byte count.

