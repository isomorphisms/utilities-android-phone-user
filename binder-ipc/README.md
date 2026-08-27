# Binder IPC target

This branch records Binder as a direct Android backend target rather than pretending the Android SDK is the only possible ABI.

The device-independent interface should still describe the operation we want (for example, read SMS messages or prepare an SMS send), while this directory records the Linux Binder UAPI and the Android Binder protocol machinery needed to implement that operation directly.

## Scope

The low-level vocabulary is pinned to the Linux/Android Binder UAPI in `include/uapi/linux/android/binder.h`. The important distinction is between:

1. **ioctls** on `/dev/binder` such as `BINDER_WRITE_READ`;
2. **BC commands** written by userspace to the Binder driver;
3. **BR returns** written by the driver into the userspace read buffer;
4. the transaction/object structures carried by those commands;
5. a higher Android protocol layered on top: Parcel encoding, service-manager lookup, interface tokens, AIDL transaction codes, permissions, and service-specific data.

Those are separate contracts. A program can understand every Binder driver command and still not know how to invoke `ISms` or `IContentProvider`.

## Files

- [`ioctls.md`](ioctls.md) — every Binder ioctl in the pinned UAPI.
- [`commands.md`](commands.md) — every `BC_*` userspace-to-driver command.
- [`returns.md`](returns.md) — every `BR_*` driver-to-userspace return.
- [`wire-objects.md`](wire-objects.md) — transaction flags and the structures/objects that make the protocol useful.
- [`android-protocol.md`](android-protocol.md) — what still has to be implemented above the kernel Binder ABI.
- [`sms-safety.md`](sms-safety.md) — rules for SMS experimentation in a public repository.

## First implementation boundary

A good first executable Binder slice is not "send an SMS". It is:

1. open the Binder device;
2. ask `BINDER_VERSION` and verify the expected 32-bit protocol on the phone;
3. `mmap` the Binder receive region;
4. perform a minimal `BINDER_WRITE_READ` exchange;
5. acquire the service manager and perform a harmless lookup;
6. record exact Parcel bytes and return decoding in tests.

Only after those primitives are reproducible should a service-specific backend be added.
