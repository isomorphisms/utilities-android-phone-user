# Complete Android JNI 1.6 callable inventory

This checklist expands every typed family. It contains **229 `JNIEnv` operations**, followed by the `JavaVM` operations and the symbols exchanged while loading or creating a VM. Four reserved slots precede `GetVersion` in `JNINativeInterface`; three reserved slots precede `DestroyJavaVM` in `JNIInvokeInterface`. They occupy ABI space but are not callable operations.

## Version, classes, and reflection — 9

- `GetVersion`
- `DefineClass` — present in the table but not implemented by Android
- `FindClass`
- `FromReflectedMethod`
- `FromReflectedField`
- `ToReflectedMethod`
- `GetSuperclass`
- `IsAssignableFrom`
- `ToReflectedField`

## Exceptions — 7

- `Throw`
- `ThrowNew`
- `ExceptionOccurred`
- `ExceptionDescribe`
- `ExceptionClear`
- `FatalError`
- `ExceptionCheck`

## Local, global, and weak references — 13

- `PushLocalFrame`
- `PopLocalFrame`
- `NewGlobalRef`
- `DeleteGlobalRef`
- `DeleteLocalRef`
- `IsSameObject`
- `NewLocalRef`
- `EnsureLocalCapacity`
- `NewWeakGlobalRef`
- `DeleteWeakGlobalRef`
- `GetObjectRefType`
- `GetObjectClass`
- `IsInstanceOf`

`GetObjectClass` and `IsInstanceOf` are also object-introspection operations; they are counted here once.

## Object construction — 4

- `AllocObject`
- `NewObject`
- `NewObjectV`
- `NewObjectA`

## Instance method lookup and calls — 61

- `GetMethodID`
- `CallObjectMethod`
- `CallObjectMethodV`
- `CallObjectMethodA`
- `CallBooleanMethod`
- `CallBooleanMethodV`
- `CallBooleanMethodA`
- `CallByteMethod`
- `CallByteMethodV`
- `CallByteMethodA`
- `CallCharMethod`
- `CallCharMethodV`
- `CallCharMethodA`
- `CallShortMethod`
- `CallShortMethodV`
- `CallShortMethodA`
- `CallIntMethod`
- `CallIntMethodV`
- `CallIntMethodA`
- `CallLongMethod`
- `CallLongMethodV`
- `CallLongMethodA`
- `CallFloatMethod`
- `CallFloatMethodV`
- `CallFloatMethodA`
- `CallDoubleMethod`
- `CallDoubleMethodV`
- `CallDoubleMethodA`
- `CallVoidMethod`
- `CallVoidMethodV`
- `CallVoidMethodA`
- `CallNonvirtualObjectMethod`
- `CallNonvirtualObjectMethodV`
- `CallNonvirtualObjectMethodA`
- `CallNonvirtualBooleanMethod`
- `CallNonvirtualBooleanMethodV`
- `CallNonvirtualBooleanMethodA`
- `CallNonvirtualByteMethod`
- `CallNonvirtualByteMethodV`
- `CallNonvirtualByteMethodA`
- `CallNonvirtualCharMethod`
- `CallNonvirtualCharMethodV`
- `CallNonvirtualCharMethodA`
- `CallNonvirtualShortMethod`
- `CallNonvirtualShortMethodV`
- `CallNonvirtualShortMethodA`
- `CallNonvirtualIntMethod`
- `CallNonvirtualIntMethodV`
- `CallNonvirtualIntMethodA`
- `CallNonvirtualLongMethod`
- `CallNonvirtualLongMethodV`
- `CallNonvirtualLongMethodA`
- `CallNonvirtualFloatMethod`
- `CallNonvirtualFloatMethodV`
- `CallNonvirtualFloatMethodA`
- `CallNonvirtualDoubleMethod`
- `CallNonvirtualDoubleMethodV`
- `CallNonvirtualDoubleMethodA`
- `CallNonvirtualVoidMethod`
- `CallNonvirtualVoidMethodV`
- `CallNonvirtualVoidMethodA`

## Instance fields — 19

- `GetFieldID`
- `GetObjectField`
- `GetBooleanField`
- `GetByteField`
- `GetCharField`
- `GetShortField`
- `GetIntField`
- `GetLongField`
- `GetFloatField`
- `GetDoubleField`
- `SetObjectField`
- `SetBooleanField`
- `SetByteField`
- `SetCharField`
- `SetShortField`
- `SetIntField`
- `SetLongField`
- `SetFloatField`
- `SetDoubleField`

## Static methods — 31

- `GetStaticMethodID`
- `CallStaticObjectMethod`
- `CallStaticObjectMethodV`
- `CallStaticObjectMethodA`
- `CallStaticBooleanMethod`
- `CallStaticBooleanMethodV`
- `CallStaticBooleanMethodA`
- `CallStaticByteMethod`
- `CallStaticByteMethodV`
- `CallStaticByteMethodA`
- `CallStaticCharMethod`
- `CallStaticCharMethodV`
- `CallStaticCharMethodA`
- `CallStaticShortMethod`
- `CallStaticShortMethodV`
- `CallStaticShortMethodA`
- `CallStaticIntMethod`
- `CallStaticIntMethodV`
- `CallStaticIntMethodA`
- `CallStaticLongMethod`
- `CallStaticLongMethodV`
- `CallStaticLongMethodA`
- `CallStaticFloatMethod`
- `CallStaticFloatMethodV`
- `CallStaticFloatMethodA`
- `CallStaticDoubleMethod`
- `CallStaticDoubleMethodV`
- `CallStaticDoubleMethodA`
- `CallStaticVoidMethod`
- `CallStaticVoidMethodV`
- `CallStaticVoidMethodA`

## Static fields — 19

- `GetStaticFieldID`
- `GetStaticObjectField`
- `GetStaticBooleanField`
- `GetStaticByteField`
- `GetStaticCharField`
- `GetStaticShortField`
- `GetStaticIntField`
- `GetStaticLongField`
- `GetStaticFloatField`
- `GetStaticDoubleField`
- `SetStaticObjectField`
- `SetStaticBooleanField`
- `SetStaticByteField`
- `SetStaticCharField`
- `SetStaticShortField`
- `SetStaticIntField`
- `SetStaticLongField`
- `SetStaticFloatField`
- `SetStaticDoubleField`

## Strings — 12

- `NewString`
- `GetStringLength`
- `GetStringChars`
- `ReleaseStringChars`
- `NewStringUTF`
- `GetStringUTFLength`
- `GetStringUTFChars`
- `ReleaseStringUTFChars`
- `GetStringRegion`
- `GetStringUTFRegion`
- `GetStringCritical`
- `ReleaseStringCritical`

## Object arrays — 4

- `GetArrayLength`
- `NewObjectArray`
- `GetObjectArrayElement`
- `SetObjectArrayElement`

`GetArrayLength` also works for primitive arrays and is counted once here.

## Primitive arrays — 42

- `NewBooleanArray`
- `NewByteArray`
- `NewCharArray`
- `NewShortArray`
- `NewIntArray`
- `NewLongArray`
- `NewFloatArray`
- `NewDoubleArray`
- `GetBooleanArrayElements`
- `GetByteArrayElements`
- `GetCharArrayElements`
- `GetShortArrayElements`
- `GetIntArrayElements`
- `GetLongArrayElements`
- `GetFloatArrayElements`
- `GetDoubleArrayElements`
- `ReleaseBooleanArrayElements`
- `ReleaseByteArrayElements`
- `ReleaseCharArrayElements`
- `ReleaseShortArrayElements`
- `ReleaseIntArrayElements`
- `ReleaseLongArrayElements`
- `ReleaseFloatArrayElements`
- `ReleaseDoubleArrayElements`
- `GetBooleanArrayRegion`
- `GetByteArrayRegion`
- `GetCharArrayRegion`
- `GetShortArrayRegion`
- `GetIntArrayRegion`
- `GetLongArrayRegion`
- `GetFloatArrayRegion`
- `GetDoubleArrayRegion`
- `SetBooleanArrayRegion`
- `SetByteArrayRegion`
- `SetCharArrayRegion`
- `SetShortArrayRegion`
- `SetIntArrayRegion`
- `SetLongArrayRegion`
- `SetFloatArrayRegion`
- `SetDoubleArrayRegion`
- `GetPrimitiveArrayCritical`
- `ReleasePrimitiveArrayCritical`

## Native registration, monitors, and VM retrieval — 5

- `RegisterNatives`
- `UnregisterNatives`
- `MonitorEnter`
- `MonitorExit`
- `GetJavaVM`

## Direct byte buffers — 3

- `NewDirectByteBuffer`
- `GetDirectBufferAddress`
- `GetDirectBufferCapacity`

## `JavaVM` invocation table — 5

- `DestroyJavaVM`
- `AttachCurrentThread`
- `DetachCurrentThread`
- `GetEnv`
- `AttachCurrentThreadAsDaemon`

## Free VM initialization/query symbols — 3

- `JNI_GetDefaultJavaVMInitArgs`
- `JNI_CreateJavaVM`
- `JNI_GetCreatedJavaVMs`

Android applications normally receive an already-running ART instance and do not create or destroy it. These symbols belong to the standard header and Invocation API, but they are not the route by which an APK starts Android's VM.

## Native-library exports — 2

- `JNI_OnLoad`
- `JNI_OnUnload`

These are implemented by a native library and called by the VM. They are not entries supplied through `JNIEnv` or `JavaVM`.

