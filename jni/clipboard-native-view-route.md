# Clipboard and native view routes

These are two concrete Android consumers of the existing JNI inventory. They do not add another foreign-function table or require an application-authored Java/Kotlin class. The repository already exercises both routes in [`math-characters/app/src/main/c/native_main.c`](../math-characters/app/src/main/c/native_main.c); D can declare the same NDK types and make the same table calls.

## Copy text to the Android clipboard

The smallest useful clipboard route is write-only and user initiated:

1. Start with `ANativeActivity.clazz`, the live `NativeActivity` object Android supplied.
2. Use `GetObjectClass` and `GetMethodID` to resolve `Context.getSystemService` with descriptor `(Ljava/lang/String;)Ljava/lang/Object;`.
3. Call it with the service name `clipboard` and retain the returned local `ClipboardManager` reference only for this operation.
4. Find `android/content/ClipData` and resolve the static method `newPlainText` with descriptor `(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;`.
5. Construct a short label and the selected text as managed strings, call `ClipData.newPlainText`, and pass the result to `ClipboardManager.setPrimaryClip` with descriptor `(Landroid/content/ClipData;)V`.
6. Check for a pending exception after lookup/construction and after `setPrimaryClip`, delete every local reference created by the route, and detach only if this call attached the thread.

The existing `copy_to_clipboard` function is the executable example. It uses `GetEnv`/`AttachCurrentThread`, `GetObjectClass`, `FindClass`, `GetMethodID`, `GetStaticMethodID`, `NewStringUTF`, object/static/void calls, `ExceptionCheck`, local-reference deletion, and conditional `DetachCurrentThread`—all entries already described by this directory.

`NewStringUTF` is sufficient for that picker's bounded, known-valid UTF-8 buffer, but it is not a general UTF-8 decoder: JNI's `UTF` functions use modified UTF-8. An importer handling arbitrary file or web text should decode it itself and construct a UTF-16 `jstring` with `NewString`.

Reading is a separate route. A focused foreground view may call `hasPrimaryClip`, `getPrimaryClip`, `ClipData.getItemAt`, and `ClipData.Item.coerceToText`; `getPrimaryClip` can return null when the app lacks input focus and is not the default input method. A clipboard history therefore cannot be treated as a reliable background feed. The first acceptance run should prove an explicit **Copy All** action, paste the result into another app, and record lookup failure, clip construction failure, and clipboard write failure separately.

## Cross the native window/view boundary

`ANativeWindow` belongs to the NDK, not to the `JNIEnv` table. `NativeActivity` and `android_native_app_glue` deliver it as `android_app.window`. The smallest route used here keeps that native lifecycle boundary and crosses into managed graphics only for drawing:

1. Wait until `APP_CMD_INIT_WINDOW` has supplied a non-null `app.window`; also request redraw after resize, content-rectangle change, and regained focus.
2. Read the physical buffer size with `ANativeWindow_getWidth` and `ANativeWindow_getHeight`. Use `android_app.contentRect` for the drawable viewport so system-bar insets do not shift input and painting apart.
3. Obtain the current thread's `JNIEnv*`, then call the NDK bridge `ANativeWindow_toSurface(env, window)`. It returns a local `android.view.Surface` reference.
4. Resolve `Surface.lockCanvas(Rect)` with descriptor `(Landroid/graphics/Rect;)Landroid/graphics/Canvas;`, passing null for the whole surface. Draw the already-produced, bounded pre-paint through `Canvas`/`Paint` calls.
5. Always pair a successful lock with `Surface.unlockCanvasAndPost(Canvas)`, including cleanup after a partial draw. Delete the `Surface`, `Canvas`, classes, and other local references before detaching the thread.
6. Stop drawing as soon as the window becomes null or the activity is being destroyed. Never keep the local `Surface` reference or a locked `Canvas` across callbacks.

The repository's `begin_canvas`, `render`, `end_canvas`, `content_viewport`, and `handle_command` functions make this boundary concrete. The NDK owns window delivery and dimensions; JNI only exposes the platform `Surface` and graphics objects. This is therefore one composed route, not a new interface layer.

For IB's first visible pre-paint, the acceptance condition is deliberately small: after a non-null window arrives, lock one canvas, fill the background, draw one bounded local projection, post it, and remain responsive through one resize or content-rectangle change. Record `no window`, `surface conversion`, `canvas lock`, `draw`, and `post` as distinct failures. Document import and network fetch remain independent of this display boundary.

## Sources

- [Android `ClipboardManager`](https://developer.android.com/reference/android/content/ClipboardManager)
- [Android `Surface`](https://developer.android.com/reference/android/view/Surface)
- [Android NDK `ANativeWindow`](https://developer.android.com/ndk/reference/group/a-native-window)

