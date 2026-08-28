# ARM/Thumb, FPGA, and compiler boundary

The application should own what a message search means. The backend should own
how a preserved operation is lowered on one target. The hardware experiment
should own whether spatialized search actually wins after data movement.

## What exists now

| Artifact | Present state | What it does not prove |
| --- | --- | --- |
| `messages-sms-store` | read-only `SmsRow` and injected `readRecent` boundary | no body search, index, or UI |
| ARM `branching-dispatch-fixtures` | source fixtures including one DFA step for `cat`; frontend/source-ABI gate | no executable string runtime or accepted branch lowering |
| ARM issue [#9](https://github.com/isomorphisms/idric-arm-thumb/issues/9) | future comparison of conditionals, transition data, and table/computed branches | no claim that `TBB`/`TBH` wins |
| ARM issue [#10](https://github.com/isomorphisms/idric-arm-thumb/issues/10) | future Shift-And and 31/32/33-byte boundary experiment | no implemented packed search |
| ARM issue [#11](https://github.com/isomorphisms/idric-arm-thumb/issues/11) | maintained IB workload and evidence boundary | explicitly rejects recursive `List Char`, Float32 result sentinels, and RefC/C/Java escape hatches as the real browser route |
| Computer Science [#23](https://github.com/isomorphisms/computer-science/issues/23) | general many-way control-flow experiment | does not identify one universal dispatch shape |
| Computer Science [#24](https://github.com/isomorphisms/computer-science/issues/24) | FPGA states in gates versus CPU states in bits/SIMD lanes | does not establish a speedup |

That distinction matters because the current ARM fixture is easy to mistake for
a partly working text-search implementation. It is a specification-shaped test
that currently stops at a reviewed unsupported boundary.

## ARM lowering candidates

For one exact search primitive, the backend may compare:

1. a simple scalar byte/anchor scan;
2. ordinary conditional DFA transitions;
3. a transition-data table plus one loop branch;
4. `TBB`/`TBH` for dense bounded action dispatch;
5. `BX`/computed transfer only when a real handler/control target is already
   represented;
6. Shift-And/Shift-Or register state for short patterns;
7. word-at-a-time candidate filtering;
8. optional NEON candidate filtering and verification;
9. a conventional Two-Way or Boyer–Moore-family baseline.

These are not interchangeable at the microarchitectural level. A branch table
may reduce code size yet hurt prediction. A transition-data lookup may avoid an
indirect control transfer. Shift-And can remove most per-state branching but
pay for a mask lookup. A vector filter may reject candidates cheaply but still
need scalar verification.

Inspect emitted instructions, object bytes, table bytes, setup work, loads,
branches, matches, and total bytes scanned. Measure on the actual ARMv7 phone;
QEMU is a semantic oracle, not a performance oracle.

## Preserve the operation through the compiler

Flattening `contains_literal` into arbitrary recursive character code too early
throws away facts the backend needs:

- one fixed literal versus a dynamic query;
- membership versus all offsets;
- overlap policy;
- valid byte slice and bounds;
- case/normalization policy;
- pattern length and alphabet;
- whether setup may be reused across many rows.

The intended path is:

```text
message-search semantics and oracle
  -> typed Idriç search operation
  -> target-independent operation plus observations
  -> selected implementation family
  -> ARM/Thumb, portable CPU, index, or hardware lowering
```

No RefC/C route is needed to justify the direct backend. A competent native or
library implementation can still serve as a comparison oracle without becoming
the production dependency.

## "Adverbs" in this example

Potential implementation constraints/preferences include:

- `low_memory`;
- `no_persistent_index`;
- `streaming`;
- `compiled_pattern`;
- `bounded_setup`;
- `small_code`;
- `battery_sparing`;
- `inspectable`;
- `runtime_free`.

They constrain how an already-defined operation is realized. In contrast,
`case_insensitive`, `approximate`, `all_offsets`, and `newest_first` alter the
observable contract and should not be smuggled in as performance adverbs.

The phone's query is usually dynamic, so some selection happens once per query
rather than at application compilation. The compiler can still emit several
small implementations and an inspectable selector based on query length,
pattern count, available features, and index state.

## FPGA lanes

The recovered FPGA plans contain at least two distinct hardware operations:

### Wide fixed-string equality masks

- compare many text positions with each fixed pattern position in parallel;
- shift/align equality vectors and AND them into a match bitmap;
- carry `m - 1` bytes or partial state across blocks;
- return membership, count, or compact positions.

This is attractive when the text block is already resident beside the logic.
Moving small SMS bodies across a slow host/FPGA boundary can erase the win.

### Pipelined regex/NFA and many streams

- compile many patterns into NFA-like state;
- update many states each cycle;
- accept many independent input streams;
- emit compact match-location/pattern-ID results rather than copying all text
  back.

This is not just a larger version of the equality-mask circuit. It has a
different pattern language, state compiler, resource model, and result shape.

## Ownership

| Layer | Owns |
| --- | --- |
| phone utilities | SMS fields, user query, ordering, privacy, exact oracle, UI results |
| Idriç/compiler | typed search semantics, preserved facts, implementation-selection seam |
| ARM/Thumb backend | legal code sequences, ABI, tables/masks, target-specific selector, disassembly evidence |
| Computer Science planner/catalog | algorithm descriptions, applicability conditions, cost/evidence records, adverbs |
| FPGA grep work | circuits, block-boundary state, bandwidth/clock/area evidence, hardware receipt |

Cross-repository links should point to the semantic oracle and measured result,
not duplicate one implementation as if it defined every layer.

