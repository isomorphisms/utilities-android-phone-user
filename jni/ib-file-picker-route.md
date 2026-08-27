# IB: document picker, shared URL, and pre-paint route

This is a concrete consumer of the general JNI table, not an additional JNI function family.

## Select one or several local inputs

From `ANativeActivity.clazz`, D obtains the object's class and looks up the inherited activity methods it needs. It constructs `android.content.Intent` with action `android.intent.action.OPEN_DOCUMENT`, adds category `android.intent.category.OPENABLE`, sets a bounded MIME policy such as `text/plain` (or a deliberate allow-list), and sets `android.intent.extra.ALLOW_MULTIPLE` when the acceptance run wants several fixtures. It then asks the activity to launch the system picker.

The picker returns `content://` URIs, not ordinary filesystem paths. One selection is available from `Intent.getData`; multiple selections may arrive through `Intent.getClipData`. Each URI is a managed local reference while being inspected. IB should preserve the order Android returns and preserve duplicate inputs unless the importer explicitly says otherwise.

## Read without broad storage permission

For each URI, D calls through the activity's `ContentResolver`. `openFileDescriptor(uri, "r")` yields a `ParcelFileDescriptor`; `detachFd` can transfer ownership of the underlying Linux descriptor to native code. After detachment, D owns the descriptor and must close it. If using managed streams instead, every stream close and exception path must be equally explicit.

If IB must reopen the exact document after reboot, the launch intent requests persistable URI authority and the result is passed to `takePersistableUriPermission`. That authority applies only when the provider offered it. The simpler import policy is to copy accepted bytes immediately into IB's durable canonical store and treat the external URI as provenance rather than long-term storage.

## Receive a URL shared from another app

IB declares an `ACTION_SEND` intent filter for `text/plain`. On creation or `onNewIntent`, D reads `Intent.EXTRA_TEXT`, validates that the received value is one bounded absolute HTTP(S) address, and appends an import event. A shared string containing commentary plus a URL is not silently equivalent to a raw URL-list file; that belongs to an explicit later recognizer.

## Pre-paint acceptance run

The first useful run should select two or three small plaintext fixtures, copy them into the durable import area, recognize URL lines, and create bounded display projections. The viewer should paint the first local text immediately; network acquisition is a separate stage. If network permission or HTTP/TLS fails, local import and display must still pass and the fetch failure must be recorded without erasing the current page.

A clean acceptance record distinguishes: picker launch, picker cancellation, URI grant, descriptor open, byte import, URL recognition, fetch, pre-paint production, and visible replacement. Those are different failure boundaries even though several are reached through the same generic JNI method-call machinery.

## Minimal JNI operations actually needed

This route uses only a small subset of the full table: `GetObjectClass`, `FindClass`, `GetMethodID`, `GetStaticFieldID` or literal construction as appropriate, `NewObjectA`, `CallObjectMethodA`, `CallVoidMethodA`, string construction/conversion, local/global reference management, and exception checks. Worker execution additionally needs `GetJavaVM`, `GetEnv`, `AttachCurrentThread`, and `DetachCurrentThread`. The documentation remains exhaustive because later Android facilities reuse the same table rather than inventing another foreign-function mechanism.

