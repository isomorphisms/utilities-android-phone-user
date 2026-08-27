# Binder ioctl operations

Pinned vocabulary from the Binder UAPI. These are the operations issued with `ioctl(fd, request, arg)` against a Binder device.

## `BINDER_WRITE_READ`

The main transport operation. Userspace supplies a `binder_write_read` containing a write buffer of `BC_*` commands and a read buffer into which the driver emits `BR_*` returns. Either side may have zero length, so the same ioctl is used for write-only, read-only, and combined exchanges. The driver updates `write_consumed` and `read_consumed`; callers must not assume one call consumes everything.

## `BINDER_SET_IDLE_TIMEOUT`

Sets the Binder process idle timeout. This is process/thread-pool policy rather than transaction data. A small direct client probably does not need to change it, but it is part of the UAPI and therefore belongs in the inventory.

## `BINDER_SET_MAX_THREADS`

Sets the maximum number of Binder threads the driver may ask this process to create for its transaction-serving pool. A client that never publishes Binder objects may be able to remain much simpler than a general libbinder process, but the command is still relevant once callbacks or local Binder objects appear.

## `BINDER_SET_IDLE_PRIORITY`

Sets the scheduling priority used for idle Binder threads. It is chiefly thread-pool policy and not needed to form a transaction.

## `BINDER_SET_CONTEXT_MGR`

Registers the calling process as the Binder context manager using the older interface. The Android service manager occupies this role for the normal Binder context. Ordinary applications should consume that service rather than attempt to become it.

## `BINDER_THREAD_EXIT`

Tells the driver that the calling Binder thread is exiting. This is part of correct lifecycle bookkeeping for a process that participates in a Binder thread pool.

## `BINDER_VERSION`

Returns `struct binder_version`. The protocol version differs for 32-bit Binder userspace (`7`) and the normal 64-bit form (`8`). This should be one of the first probes on the 32-bit Android phone target because it verifies that our structure layout assumptions match the kernel ABI before we attempt a transaction.

## `BINDER_GET_NODE_DEBUG_INFO`

Iterates debugging information for Binder nodes. It exposes node pointer/cookie values and strong/weak-reference state. This is diagnostic machinery, not an ordinary app-facing primitive.

## `BINDER_GET_NODE_INFO_FOR_REF`

Returns strong/weak reference counts for a Binder handle. It is another diagnostic/introspection operation and has permission/usage constraints in the driver.

## `BINDER_SET_CONTEXT_MGR_EXT`

Registers a context manager using a `flat_binder_object`, allowing the newer extended representation. As with `BINDER_SET_CONTEXT_MGR`, this is service-manager infrastructure, not something an ordinary phone utility should normally invoke.

## `BINDER_FREEZE`

Requests freeze or unfreeze behavior for a Binder process via `binder_freeze_info`. Android uses Binder freeze awareness as part of process-management behavior. A normal app does not need this to make RPCs.

## `BINDER_GET_FROZEN_INFO`

Queries whether synchronous or asynchronous transactions arrived for a process in relation to freezing. It operates on `binder_frozen_status_info`.

## `BINDER_ENABLE_ONEWAY_SPAM_DETECTION`

Enables driver detection of excessive one-way asynchronous Binder traffic. When enabled, the driver can report `BR_ONEWAY_SPAM_SUSPECT`.

## `BINDER_GET_EXTENDED_ERROR`

Retrieves `binder_extended_error` after a failed operation. It gives an operation id, the associated Binder return command, and a negative errno parameter. A direct Binder backend should preserve this information rather than collapse all transaction failures into a generic error.

## Error handling

The UAPI calls out two driver-level errors explicitly: `EINTR` means retry the ioctl, while `ECONNREFUSED` means the Binder driver is no longer accepting operations from the process and subsequent operations will fail as well. The backend should distinguish these from Binder protocol returns such as `BR_FAILED_REPLY` and `BR_DEAD_REPLY`.
