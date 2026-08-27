# Binder wire structures and flags

The command names are only the outer grammar. Direct Binder work also requires the exact structure layout expected by the kernel and a parser for the object records embedded in transaction data.

## Architecture-dependent scalar types

Binder deliberately defines `binder_size_t` and `binder_uintptr_t` so the UAPI follows Binder userspace bitness. The phone target is 32-bit userspace, so `BINDER_IPC_32BIT` implies protocol version 7 and 32-bit Binder pointer/size fields. Do not silently substitute the host compiler's native pointer width when generating fixtures on a 64-bit workstation.

## `binder_write_read`

Contains `write_size`, `write_consumed`, `write_buffer`, `read_size`, `read_consumed`, and `read_buffer`. This is the envelope for `BINDER_WRITE_READ`. Both command streams are byte streams and may contain multiple records. Parsing must advance by the encoded command's argument size and honor partial consumption.

## `binder_transaction_data`

The core transaction record. Important fields are:

- `target.handle` for an outgoing remote handle or `target.ptr` where appropriate for returned/local nodes;
- `cookie` for the target local Binder object;
- `code`, the service/interface transaction code;
- `flags`;
- `sender_pid` and `sender_euid` on received transactions;
- `data_size` and `offsets_size`;
- pointers to the transaction data buffer and object-offset array, or the small inline `buf[8]` representation.

The Binder driver transports the bytes; Android Parcel/AIDL defines what those bytes mean.

## Transaction flags

### `TF_ONE_WAY`
Asynchronous one-way call with no normal reply. This is a semantic change, not merely an optimization.

### `TF_ROOT_OBJECT`
Marks transaction contents as the component's root object.

### `TF_STATUS_CODE`
Indicates that the payload is a 32-bit status code.

### `TF_ACCEPT_FDS`
Allows the reply to contain file descriptors.

### `TF_CLEAR_BUF`
Requests that the driver clear the transaction buffer when processing is complete. Useful where payloads may contain sensitive data.

### `TF_UPDATE_TXN`
Allows an outdated pending asynchronous transaction to be updated/replaced according to Binder's oneway queuing behavior.

## Object records inside Parcels

Binder's offset array points at special object records in the transaction buffer. The header type distinguishes what the driver must translate or fix up.

### `flat_binder_object`
Represents either a local Binder object or a handle to a remote Binder object, with flags plus binder/handle and cookie fields. This is the classic object used when passing Binder interfaces.

### `binder_fd_object`
Represents a file descriptor embedded in a transaction. The driver duplicates/translates the descriptor into the receiving process.

### `binder_buffer_object`
Represents an out-of-line userspace buffer used by scatter/gather transactions. It can identify a parent buffer so the driver can repair a pointer inside that parent after copying.

### `binder_fd_array_object`
Describes an array of file descriptors located inside a parent Binder buffer object. The kernel must process each descriptor rather than copying the bytes opaquely.

## Helper structures

`binder_transaction_data_secctx` adds a security-context pointer to an incoming transaction. `binder_transaction_data_sg` adds `buffers_size` for scatter/gather transfer. `binder_ptr_cookie`, `binder_handle_cookie`, `binder_pri_desc`, and `binder_pri_ptr_cookie` carry the small reference, notification, and legacy-acquisition payloads used by the `BC_*`/`BR_*` protocols.

`binder_version`, `binder_node_debug_info`, `binder_node_info_for_ref`, `binder_freeze_info`, `binder_frozen_status_info`, `binder_frozen_state_info`, and `binder_extended_error` are ioctl or notification payloads rather than ordinary application Parcels.

## Implementation rule

Every structure should have a byte-level fixture for the 32-bit target. A host-side struct that happens to print the same field values is not an oracle: offsets, alignment, pointer width, and ioctl encoded sizes are part of the ABI.
