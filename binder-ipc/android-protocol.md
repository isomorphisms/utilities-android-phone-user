# Android protocol above the Binder driver

Knowing the Binder UAPI is necessary but not sufficient to call Android services. The kernel moves typed Binder objects and byte buffers between processes; Android defines additional conventions above that transport.

## Service discovery

A normal client begins with the Binder context manager, Android's service manager. Handle `0` is conventionally the context-manager endpoint. The client must encode the service-manager request using the protocol expected by the device and obtain a Binder handle for the named service. Service names such as telephony-related services are Android framework details, not part of Linux Binder itself.

## Interface tokens

AIDL-generated clients normally write an interface descriptor/token into the Parcel before method arguments. The server checks that token before dispatching the transaction. A direct backend therefore needs the exact descriptor string as well as the numeric transaction code.

## Transaction codes

The `code` field in `binder_transaction_data` is not globally meaningful. It is interpreted by the receiving Binder interface. AIDL-generated stubs assign codes to methods, usually relative to Binder's first-call transaction range. Hidden framework interfaces such as internal telephony AIDL can change across Android releases, which is one reason the SDK is a more stable contract than a raw service-specific Binder encoding.

## Parcel encoding

The Parcel layer defines representation of primitives, UTF-16 strings, nullable values, arrays, lists, Bundles, Parcelable objects, Binder handles, file descriptors, exceptions/status headers, and alignment. This is the major layer an Idriç Binder target must either implement or deliberately restrict.

For a first backend, implement only the Parcel forms required by a tiny harmless service-manager query and record the exact bytes. Expand the encoder/decoder as new service calls require new types rather than attempting all Parcelable machinery at once.

## AIDL directionality

`in`, `out`, and `inout` arguments affect which data is marshaled in the request and reply. One-way AIDL methods map to `TF_ONE_WAY`. Callback interfaces cause the nominal client to become a Binder server as well, bringing `BR_TRANSACTION`, replies, reference bookkeeping, and a Binder looper into scope.

## Errors

Keep four error domains distinct:

1. Linux syscall errors from `open`, `mmap`, or `ioctl`;
2. Binder driver protocol failures such as `BR_FAILED_REPLY`, `BR_DEAD_REPLY`, or frozen-target returns;
3. Parcel/AIDL exception or status values returned by the remote stub;
4. service-specific result/error codes.

Collapsing these together will make debugging a direct backend unnecessarily opaque.

## Permissions remain Android permissions

Raw Binder is not a privilege bypass. The receiving service sees the caller's process identity and performs permission/AppOps/role checks in the normal way. Direct Binder changes the client implementation boundary; it does not turn an ordinary app UID into the system UID.

## Version policy

The Linux Binder UAPI is comparatively stable. Hidden Android Binder interfaces are not the same kind of contract. Any direct binding to a hidden AIDL service should therefore record at minimum:

- Android release/API level;
- interface descriptor;
- exact AIDL/source revision;
- transaction code;
- request Parcel fixture;
- reply Parcel fixture;
- permissions/role assumptions;
- a test proving behavior on the intended phone.
