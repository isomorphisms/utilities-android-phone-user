# Binder driver → userspace returns (`BR_*`)

These command words appear in the read buffer supplied through `BINDER_WRITE_READ`. A robust direct Binder backend should decode every named return even if its first executable slice only expects a few of them.

## General status

### `BR_ERROR`
Carries a signed error code from the driver command stream.

### `BR_OK`
No payload. Generic successful completion marker used by parts of the protocol.

### `BR_TRANSACTION_COMPLETE`
Reports that the most recently submitted transaction/reply command has been accepted by the driver. This is also generated for asynchronous transactions; it does **not** mean that a synchronous remote method has returned.

### `BR_FAILED_REPLY`
The last transaction failed, for example because the driver could not allocate required resources. This is a Binder transport failure rather than a service-defined exception.

### `BR_DEAD_REPLY`
The target of the last transaction is dead. Treat this separately from an ordinary service error.

### `BR_FROZEN_REPLY`
A synchronous target is frozen, so the transaction could not proceed normally.

### `BR_TRANSACTION_PENDING_FROZEN`
An asynchronous transaction targets a frozen process and is pending accordingly.

### `BR_ONEWAY_SPAM_SUSPECT`
The driver suspects excessive one-way traffic after oneway-spam detection has been enabled.

## Transactions and replies

### `BR_TRANSACTION`
Delivers an incoming transaction as `binder_transaction_data`. A process that exposes Binder objects or receives callbacks must parse this and eventually send `BC_REPLY` for synchronous calls.

### `BR_TRANSACTION_SEC_CTX`
Variant of an incoming transaction that also carries a security-context pointer in `binder_transaction_data_secctx`. It shares the same base Binder return command number as `BR_TRANSACTION` but has a distinct encoded size/type and therefore must be decoded distinctly.

### `BR_REPLY`
Delivers the reply to a previously issued synchronous Binder transaction. For a simple client this is one of the central return forms: decode the transaction data, parse the returned Parcel, then release the driver buffer correctly.

## Reference bookkeeping for local objects

### `BR_INCREFS`
The driver requests that userspace acknowledge an added weak reference to a local Binder node. Carries `binder_ptr_cookie`; acknowledge with `BC_INCREFS_DONE`.

### `BR_ACQUIRE`
The driver requests acknowledgement of a new strong reference to a local node. Acknowledge with `BC_ACQUIRE_DONE`.

### `BR_RELEASE`
Indicates release of a strong reference to a local Binder node.

### `BR_DECREFS`
Indicates release of a weak reference to a local Binder node.

### `BR_ACQUIRE_RESULT`
Result for the legacy acquire-attempt protocol. The UAPI marks that protocol unsupported, but an exhaustive decoder should know the command.

### `BR_ATTEMPT_ACQUIRE`
Legacy request to attempt acquisition of a local object. Also marked unsupported in the UAPI.

## Thread-pool control

### `BR_NOOP`
No payload. Do nothing and continue parsing the read buffer. Historically useful because the driver can replace a no-op slot with a spawn request.

### `BR_SPAWN_LOOPER`
The driver has determined that the process needs another Binder service thread. A libbinder-style process spawns one and registers it with `BC_REGISTER_LOOPER`.

### `BR_FINISHED`
Legacy/unsupported request for a thread-pool thread to stop.

## Death notifications

### `BR_DEAD_BINDER`
The remote Binder object associated with a registered death cookie has died. Userspace should handle the event and acknowledge it using `BC_DEAD_BINDER_DONE`.

### `BR_CLEAR_DEATH_NOTIFICATION_DONE`
Confirms completion of a requested death-notification removal for the supplied cookie.

## Freeze notifications

### `BR_FROZEN_BINDER`
Reports a transition in the frozen state of a process associated with a registered Binder handle. The payload contains the caller's cookie and the new frozen/unfrozen state.

### `BR_CLEAR_FREEZE_NOTIFICATION_DONE`
Confirms completion of removal of a freeze notification.

## Minimal client expectations

A first service-manager lookup client should primarily expect `BR_TRANSACTION_COMPLETE`, `BR_REPLY`, and error/death returns, but it must parse the stream command-by-command rather than assuming one return per ioctl. Once callbacks or exported Binder objects are supported, `BR_TRANSACTION`, reference bookkeeping, death notifications, and thread-pool control become active parts of the implementation.
