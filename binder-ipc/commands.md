# Binder userspace → driver commands (`BC_*`)

These command words are placed in the write buffer passed through `BINDER_WRITE_READ`. They are not Linux CPU instructions and they are not AIDL methods; they are the Binder driver's command protocol.

## Transactions

### `BC_TRANSACTION`
Sends a Binder transaction described by `binder_transaction_data`. `target.handle` identifies the remote Binder handle; `code` is the service/interface transaction code; `flags` controls behavior such as one-way calls; and the data/offset buffers describe the Parcel payload and embedded Binder objects.

### `BC_REPLY`
Replies to a synchronous Binder transaction using `binder_transaction_data`. A pure client may rarely generate this until it receives callbacks or publishes local Binder objects.

### `BC_TRANSACTION_SG`
Scatter/gather form of `BC_TRANSACTION`. It carries `binder_transaction_data_sg`, adding a `buffers_size` field so the driver can copy and fix up out-of-line buffers represented by Binder buffer objects.

### `BC_REPLY_SG`
Scatter/gather reply form corresponding to `BC_TRANSACTION_SG`.

## Buffer lifetime

### `BC_FREE_BUFFER`
Returns a transaction buffer previously delivered by the driver. Receiving `BR_TRANSACTION` or `BR_REPLY` can give userspace driver-allocated transaction data; this command tells Binder that userspace is finished with that buffer. A direct implementation must get this lifetime rule right or it will leak Binder transaction buffers.

## Strong and weak references

### `BC_INCREFS`
Adds a weak reference to the Binder object identified by a handle.

### `BC_ACQUIRE`
Adds a strong reference to the object identified by a handle.

### `BC_RELEASE`
Drops a strong reference to a handle.

### `BC_DECREFS`
Drops a weak reference to a handle.

### `BC_INCREFS_DONE`
Acknowledges a `BR_INCREFS` request for a local Binder node, carrying the node pointer and cookie.

### `BC_ACQUIRE_DONE`
Acknowledges a `BR_ACQUIRE` request for a local Binder node, again carrying pointer and cookie.

### `BC_ACQUIRE_RESULT`
Legacy acknowledgement associated with the unsupported acquire-attempt protocol. The UAPI describes it but notes the mechanism is not currently supported.

### `BC_ATTEMPT_ACQUIRE`
Legacy attempt to acquire a primary reference using priority plus descriptor. It is documented by the UAPI as not currently supported. It still belongs in an exhaustive decoder because the command number exists.

## Binder looper/thread-pool state

### `BC_REGISTER_LOOPER`
Registers a Binder thread that was spawned in response to the driver asking for another looper.

### `BC_ENTER_LOOPER`
Marks the calling thread as entering the Binder transaction loop. The driver uses this to maintain the count of available Binder service threads.

### `BC_EXIT_LOOPER`
Marks the calling thread as leaving the Binder transaction loop.

## Death notifications

### `BC_REQUEST_DEATH_NOTIFICATION`
Requests notification when the process owning a remote Binder handle dies. It carries a `binder_handle_cookie`; the opaque cookie lets userspace match the later death event to its own state.

### `BC_CLEAR_DEATH_NOTIFICATION`
Removes a previously requested death notification for a handle/cookie pair.

### `BC_DEAD_BINDER_DONE`
Acknowledges handling of `BR_DEAD_BINDER` for the supplied cookie.

## Freeze notifications

### `BC_REQUEST_FREEZE_NOTIFICATION`
Requests notification when the owner of a Binder handle transitions between frozen and unfrozen states. It uses a `binder_handle_cookie`.

### `BC_CLEAR_FREEZE_NOTIFICATION`
Cancels a freeze-state notification request.

### `BC_FREEZE_NOTIFICATION_DONE`
Acknowledges processing of a freeze notification, identified by its cookie.

## Minimal client subset

The first useful Idriç direct client does **not** need to implement all commands as emitters. It should nevertheless recognize the whole vocabulary. The minimal outgoing set is likely `BC_TRANSACTION`, `BC_FREE_BUFFER`, reference bookkeeping as actually required by returned handles, and possibly looper commands if callbacks are introduced. Keeping the exhaustive table separate from the minimal executable subset prevents a small first implementation from being mistaken for the whole Binder architecture.
