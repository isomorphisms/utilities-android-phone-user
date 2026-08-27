# Objects, fields, and method calls

## Allocation and construction

### `AllocObject`

Allocates an instance without running a constructor. That leaves fields at VM defaults and can violate assumptions the managed class normally establishes in `<init>`. It is a specialized mechanism for runtimes and serializers, not the ordinary route for constructing Android objects.

### `NewObject`, `NewObjectV`, and `NewObjectA`

Construct an object and run the constructor named by a `jmethodID` obtained from `GetMethodID(class, "<init>", descriptor)`. The three variants differ only in how native arguments arrive: C variadic arguments, a `va_list`, or a `jvalue[]`. D should normally prefer `NewObjectA`; it is explicit and does not require recreating C's variadic ABI.

### `GetObjectClass`

Returns a local `jclass` reference for an object's actual runtime class. It is useful when the exact class is supplied by Android rather than known ahead of time, but repeated calls should not replace a stable, intentional class/ID cache.

### `IsInstanceOf`

Tests whether an object can be cast to a class. JNI specifies that a null object is compatible with any class, matching managed reference-cast behavior; callers that need a non-null value must test that separately.

## Finding members

### `GetMethodID` and `GetStaticMethodID`

Look up instance and static methods by exact name and descriptor. `GetMethodID` also finds constructors under `<init>`. A null result generally accompanies a pending exception, so a wrapper must inspect/propagate the exception rather than reporting only “missing method.” Cache successful IDs alongside a global class reference.

### `GetFieldID` and `GetStaticFieldID`

Look up instance and static fields by exact name and field descriptor. They have the same lifetime and pending-exception concerns as method IDs. A field ID is not interchangeable with the same-named field from an unrelated class.

## Calling instance methods

The ordinary call matrix is `Call<Type>Method`, `Call<Type>MethodV`, and `Call<Type>MethodA`, where `<Type>` is `Object`, `Boolean`, `Byte`, `Char`, `Short`, `Int`, `Long`, `Float`, `Double`, or `Void`. The selected function must match the descriptor's return type exactly. `Object` covers every managed reference return, including arrays and strings; the caller may then use the appropriately typed JNI reference.

The unsuffixed variant consumes C varargs, `V` consumes `va_list`, and `A` consumes `jvalue[]`. These are not three different managed operations. They are three native argument-passing forms for the same virtual dispatch. Again, `A` is the cleanest base for D-generated calls.

Every managed call may execute arbitrary managed code and throw. If a Java exception becomes pending, native code must generally stop making unrelated JNI calls, preserve or clear the exception intentionally, release any native resources it owns, and return across the boundary so the managed caller can receive it.

## Calling nonvirtual methods

The `CallNonvirtual<Type>Method`, `V`, and `A` matrix has the same ten return-type families, but it also receives the class whose implementation must be invoked. It bypasses ordinary virtual override selection in the manner specified by JNI. This is occasionally necessary for superclass implementation calls; it should not become the default simply because it appears more direct.

## Calling static methods

The `CallStatic<Type>Method`, `V`, and `A` matrix again covers `Object`, all eight primitives, and `Void`. It receives a class rather than an instance. The lookup must have used `GetStaticMethodID`; passing an instance-method ID is invalid even if the text name and descriptor happen to match.

## Reading and writing instance fields

The getters are `GetObjectField`, `GetBooleanField`, `GetByteField`, `GetCharField`, `GetShortField`, `GetIntField`, `GetLongField`, `GetFloatField`, and `GetDoubleField`. The setters are the corresponding `Set<Type>Field` operations. Choose the operation that exactly matches the field descriptor; JNI does not perform a language-level numeric conversion for a mismatched accessor.

An object-valued getter returns a local reference. An object setter stores a managed reference and therefore participates in ART's garbage-collector barriers; native code must not attempt to replace it with a raw memory write into the object's presumed layout.

## Reading and writing static fields

`GetStaticObjectField` through `GetStaticDoubleField` and `SetStaticObjectField` through `SetStaticDoubleField` operate on class storage using a `jfieldID` returned by `GetStaticFieldID`. Static initialization may occur as part of class/member use and may throw. These calls are the supported boundary; native code has no portable license to infer where ART stores a static field.

