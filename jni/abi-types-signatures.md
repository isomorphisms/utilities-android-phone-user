# ABI, types, names, and signatures

## Two function tables

`JNIEnv*` is the handle used for almost every crossing into managed objects. In C it leads to a table of 229 function pointers. In C++ the header wraps that table with member-looking convenience calls, but the machine boundary is still table dispatch. A D declaration must preserve the C layout, pointer depth, calling convention, field order, and the four reserved leading slots. Reordering functions or declaring `JNIEnv` as the table rather than a pointer to the table shifts every call to the wrong address.

`JavaVM*` leads to a second table with three reserved slots and five thread/VM operations. A process-wide `JavaVM*` may be retained. A `JNIEnv*` may not be moved between threads: ART gives each attached thread its own environment pointer.

## Primitive types

- `jboolean`: unsigned 8-bit storage; use `JNI_FALSE` or `JNI_TRUE`, not D's ABI-dependent assumptions about `bool`.
- `jbyte`: signed 8-bit integer.
- `jchar`: unsigned 16-bit UTF-16 code unit, not a complete Unicode scalar value.
- `jshort`: signed 16-bit integer.
- `jint`: signed 32-bit integer.
- `jlong`: signed 64-bit integer. Android recommends this rather than `jint` when a managed field carries a native pointer, including on a 32-bit build that may later gain a 64-bit target.
- `jfloat`: IEEE-754 binary32.
- `jdouble`: IEEE-754 binary64. JNI fixes this width even when application-level policy otherwise prefers narrower floats.
- `jsize`: an alias of `jint`; array and string sizes in this interface are signed 32-bit values.

## Reference and identifier types

`jobject`, `jclass`, `jstring`, `jarray`, each typed array reference, `jthrowable`, and `jweak` are opaque managed references. Their bit patterns are not object addresses, stable identities, or durable handles. In C the subtypes collapse to opaque pointers; the C++ declarations add type distinctions for compile-time checking. A D binding can reproduce distinct opaque handle types without claiming anything about their representation.

`jmethodID` and `jfieldID` are opaque lookup results. They are commonly cached because lookup by text is comparatively expensive. They are valid only while the defining class remains loaded; keeping a global reference to the class is the ordinary way to keep a cached ID's class alive.

## `jvalue`

`jvalue` is a union with fields `z`, `b`, `c`, `s`, `i`, `j`, `f`, `d`, and `l`. The `...A` call variants take an array of these values, one per managed argument. This is usually the cleanest call form for a D binding because it avoids C variadic promotion rules and the platform-specific representation of `va_list`.

## Method and field descriptors

JNI looks up a field by `(class, name, field signature)` and a method by `(class, name, method signature)`. Primitive descriptors are `Z` boolean, `B` byte, `C` char, `S` short, `I` int, `J` long, `F` float, `D` double, and `V` void. An object is `L` plus its slash-separated binary class name plus `;`, such as `Ljava/lang/String;`. An array adds one `[` per dimension. A method puts its parameter descriptors in parentheses followed by its return descriptor.

For example, `(ILjava/lang/String;)D` means “take an `int` and a `String`, return a `double`.” Constructors are looked up under the special name `<init>` and must return `V`. A wrong descriptor usually yields a pending lookup exception and a null ID; it is not a request for language-level overload resolution.

## Native method names and registration

The VM can discover an exported name derived from the package, class, method, and—when required—encoded overload signature. That naming scheme is brittle and exposes more symbols. Android recommends explicit `RegisterNatives` from `JNI_OnLoad`: a `JNINativeMethod` record contains the managed method name, descriptor, and native function pointer. Explicit registration makes the intended boundary auditable and fails during library loading rather than on the first accidental call.

## Constants and status values

The Android header defines `JNI_VERSION_1_1`, `JNI_VERSION_1_2`, `JNI_VERSION_1_4`, and `JNI_VERSION_1_6`; an Android binding should request `JNI_VERSION_1_6`. General results are `JNI_OK`, `JNI_ERR`, `JNI_EDETACHED`, `JNI_EVERSION`, `JNI_ENOMEM`, `JNI_EEXIST`, and `JNI_EINVAL`.

Primitive-array release mode `0` copies back when needed and releases the acquired buffer. `JNI_COMMIT` copies changes back but retains a copied buffer for a later release. `JNI_ABORT` releases without copying a temporary copy back; it cannot undo writes made through a directly pinned pointer. Reference kinds are `JNIInvalidRefType`, `JNILocalRefType`, `JNIGlobalRefType`, and `JNIWeakGlobalRefType`.

