# messages

The first slice is deliberately smaller than a messaging app: prove that Idriç can consume ordinary SMS rows from the Android system message store.

## First boundary

Treat `content://sms` as a flat row source.

Read only these columns initially:

- `_id`
- `address` — preserve the provider's raw string; do not resolve contacts
- `body`
- `date`
- `type`
- `read`
- `status`

Sort newest first and stop after the requested number of cursor rows in our own code. Do not depend on provider-specific `LIMIT` syntax in the SQL sort string.

The Idriç model and injected store boundary live in `idric/SmsStore.idric`.

## Android bridge

There is no NDK SMS-store API and this should not become a direct SQLite/file reader. The Android-specific adapter should be one narrow JNI bridge:

```text
Idriç SmsStoreDriver.readRecent n
  -> native Android adapter
  -> NativeActivity / Context
  -> getContentResolver()
  -> ContentResolver.query(content://sms, projection, null, null, "date DESC")
  -> Cursor
  -> at most n SmsRow values
```

For the read probe the manifest/runtime permission is `android.permission.READ_SMS`.

The first phone oracle is simply: after permission is granted, ask for a small number of recent rows and show or log the decoded row fields. That proves the store boundary before building any message UI around it.

## Deliberately absent

This slice does not model or implement:

- contacts or contact-name lookup
- MMS
- RCS
- group-message semantics
- thread/participant rendering
- attachments or media
- composing/sending SMS
- writing rows back to the SMS provider

Sending plain SMS can be a separate adjacent transport boundary later; Android exposes that through `SmsManager`, not by treating the SMS provider as a Unix-style writable stream.
