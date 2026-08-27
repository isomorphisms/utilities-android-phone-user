# Versions, classes, inheritance, and reflection

### `GetVersion`

Returns the JNI version understood by the current environment. On the Android target the binding requests and expects JNI 1.6. This describes the JNI table contract, not the Android SDK level; `sdkVersion` or Android framework APIs answer the latter question.

### `DefineClass`

The standard JNI slot attempts to define a class from ordinary JVM class bytes and a class loader. Android deliberately does not implement it because ART consumes DEX rather than `.class` bytecode. It must remain in the table at its specified position for ABI compatibility, but an Android backend must mark it unsupported instead of treating it as a usable class-loading mechanism.

### `FindClass`

Finds a class by slash-separated binary name, such as `android/content/Intent`. The important Android trap is the active class loader. A native method called from application code inherits a useful application-class context, while a native-created attached thread may search from the bootstrap/system loader and fail to find application classes. Cache needed application classes during `JNI_OnLoad`, accept a `Class` from managed code, or cache an application `ClassLoader` and invoke `loadClass` deliberately.

### `GetSuperclass`

Returns the represented class's superclass, or null for `java.lang.Object` and interfaces. The returned `jclass` is a local reference and follows the ordinary local-reference lifetime rules.

### `IsAssignableFrom`

Tests whether an instance of the first class can be safely treated as the second class. The parameter order is easy to reverse when translating from an English “is X assignable from Y?” question, so a wrapper should choose an unambiguous name and test both a positive and a negative case.

### `FromReflectedMethod` and `FromReflectedField`

Convert managed reflection objects representing a method, constructor, or field into opaque `jmethodID` or `jfieldID` handles. These are bridges between Java reflection and the cheaper ID-based JNI operations; they do not create a reference that keeps the defining class loaded.

### `ToReflectedMethod` and `ToReflectedField`

Convert a JNI ID back into a managed reflection object. The caller supplies the defining class and whether the member is static. The resulting object is a local reference and may leave an exception pending on failure.

