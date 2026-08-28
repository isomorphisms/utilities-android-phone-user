# Recovered grep control-flow and FPGA notebook

This file records the earlier brainstorming about grep-like search, CPU control
flow, bit-parallel state, and FPGA spatialization. It is deliberately broader
than the first SMS-search implementation. None of the unusual mechanisms below
is presumed faster until it wins a measured comparison on the target.

The important recurring idea is that a search loop does not have to be expressed
as a nest of `if` statements inside a conventional `for` loop. State can live in
control flow, in table data, in machine-word bits, in SIMD lanes, or physically
in gates and wires.

## 1. The ordinary baseline stays important

Before any exotic lowering, preserve a boring oracle:

```text
for every possible start position:
    compare the pattern bytes
    report an exact match
```

For a one-byte needle this reduces to a byte scan. For a short literal, an
anchor-byte scan plus exact verification is a useful baseline. The point of the
other designs is to change where work and state live, not to change what
`contains_literal`, `first_offset`, or `all_offsets` mean.

For SMS search specifically, the first implementation should remain able to
scan the body of each read-only message row directly. Indexes and unusual
machine code are later interchangeable implementations of the same operation.

## 2. Control flow that is not just `if` / `for`

### 2.1 Jump tables

A dense state or token value can select among several destinations through a
jump table instead of testing every case in sequence.

Conceptually:

```text
state 0 -> handler_0
state 1 -> handler_1
state 2 -> handler_2
...
```

The table can contain data offsets, code offsets, or addresses depending on the
instruction set and representation. This is especially natural for parsers,
bytecode interpreters, protocol dispatch, and DFA actions.

It is not automatically the best literal-search loop. An indirect branch for
every input byte can be worse than a data-table transition followed by one
predictable loop branch.

### 2.2 Thumb-2 `TBB` and `TBH`

ARM Thumb-2 has table-branch instructions that make dense bounded dispatch
particularly compact:

- `TBB` uses byte entries;
- `TBH` uses halfword entries.

They are worth testing for small DFA/action dispatch when the branch targets fit
the encoding. They are a code-generation lever, not a promise that a DFA should
be represented as executable branches.

The earlier compiler experiments therefore separate:

1. the semantic state transition;
2. a transition represented as ordinary conditionals;
3. a transition represented as table data;
4. a transition represented as a branch table.

Only the last representation specifically calls for `TBB`/`TBH`.

### 2.3 Computed / indirect branch

A register or table can hold the next code destination and transfer control to
it directly. On ARM/Thumb this can ultimately use a register branch such as
`BX` after the target has been formed or loaded.

This is useful when the program already has a handler identity or state-to-code
mapping. It is the machine-code analogue of saying “continue at the code for
this state” rather than repeatedly asking a chain of Boolean questions.

Costs that must be measured include indirect-branch prediction, target-table
loads, code locality, and code size.

### 2.4 Threaded interpretation

A bytecode or token stream can contain or index the destination for the next
operation. Each handler finishes by dispatching directly to the next handler.
This removes a central `switch` from the conceptual interpreter loop.

Variants include direct and indirect threading. The exact representation is a
runtime/ABI decision, but the general idea is relevant to:

- HTML/token scanners;
- compact parsers;
- display-list or bytecode interpreters;
- finite-state search machinery where states have substantial actions.

For a tiny literal matcher, threading may be too much machinery. The point is
to retain it as a control-flow option for larger scanners rather than force all
state machines through nested conditionals.

### 2.5 Tail jumps

When one state or handler is finished and no return is required, transfer
straight to the next state/handler instead of making a call that must later
return. This is another way to make the control-flow graph itself represent the
machine state.

### 2.6 Predication / conditional execution

Some small decisions can be represented as conditionally executed operations or
mask-based selection instead of branches. Thumb-2 has limited conditional
execution through `IT`; other targets have their own mechanisms.

This is useful only for short operations. Turning large mutually exclusive
paths into predicated work can execute too much useless work and enlarge code.

### 2.7 Branchless selection with masks

A comparison can produce a bit/mask used to select or update values without a
control transfer. Typical search uses include:

- advancing or clearing packed state;
- filtering candidate bytes;
- accumulating a match bitmap;
- selecting one of two small values;
- recording flags while keeping the main scan loop predictable.

This moves state from the branch predictor into integer registers.

### 2.8 Bit-test branches

Some architectures can branch directly on a bit or zero/nonzero test rather
than materializing a larger comparison sequence. This is target-specific and
should be represented as a backend opportunity, not as a source-language search
algorithm.

### 2.9 Loop unrolling

Several input positions can be handled in one loop body. This reduces loop
control overhead and exposes more independent work, at the cost of code size,
tail handling, and possibly instruction-cache pressure.

For fixed small kernels, a completely unrolled comparison can be useful. For
large scans, partial unrolling is the more realistic experiment.

### 2.10 Event-driven control flow

Not every repeated operation should be represented as a polling loop. Embedded
work can advance on interrupts/events. This was discussed with crank/cam and
other control workloads rather than grep itself, but it belongs in the same
catalog because it is another alternative to “keep asking in a loop.”

It is generally not the right representation for an in-memory SMS-body scan.

### 2.11 Zero-overhead and circular-addressing loops

DSP-oriented machines often provide hardware loop counters and circular address
updates. Those can remove explicit loop-control instructions or modulo-address
arithmetic from streaming kernels.

The Android ARMv7 phone should not pretend to have these features. They matter
to the broader compiler-backend catalog and to future DSP/automotive targets.

## 3. State can live in data instead of branches

### 3.1 DFA transition table

Instead of branching on both current state and input byte, use them to index a
transition table:

```text
next_state = transition[current_state, byte]
```

The scan then has a regular load/update/loop shape. This often gives the branch
predictor an easier job than computed control transfer per byte.

The table can be dense, sparse, compressed, or specialized for the alphabet.
The representation is an implementation choice; the DFA semantics are not.

### 3.2 Trie and failure transitions

For many literals with shared prefixes, a trie represents prefix state once.
Aho-Corasick adds failure transitions so many literals can be found in one
streaming pass without restarting from every input position.

This is the many-pattern version of making state explicit instead of nesting
more `if` tests.

## 4. State can live in register bits

### 4.1 Shift-And / Shift-Or / Bitap

For a short pattern, one machine word can represent many partial-match states at
once. Each input byte updates the whole frontier with shifts, Boolean operations,
and a character mask.

The important conceptual move is:

```text
one Boolean branch per state
```

becoming

```text
many state bits updated by a few word operations
```

This was one of the clearest bridges between the ARM compiler work and the FPGA
grep idea.

Pattern lengths around the machine-word boundary deserve explicit tests. The
existing ARM plan calls out 31/32/33-byte cases so no implementation silently
changes semantics at the word edge.

### 4.2 Myers bit-vector edit distance

Approximate matching can also pack a dynamic-programming frontier into machine
bits. This is a later fuzzy-search lane, separate from exact literal search, but
it shows that the same “states in bits” idea generalizes beyond grep.

### 4.3 SWAR

Ordinary integer registers can be treated as several packed byte/field lanes.
Loads, XORs, arithmetic, and bit tricks can reject multiple candidate positions
without requiring SIMD instructions.

This is attractive on the ARMv7 phone because it can be tested before depending
on NEON availability or paying vector setup costs.

## 5. State can live in SIMD lanes

SIMD/NEON can compare several bytes or candidate positions in parallel, build a
candidate mask, and then verify only surviving positions.

Useful shapes include:

- compare many input bytes against an anchor byte;
- compare several pattern positions against aligned input vectors;
- update several independent streams in parallel;
- multi-literal filters that cheaply reject most positions.

SIMD filtering still needs an exact verifier unless the vector construction
itself proves the complete literal. Setup, alignment, tails, frequency effects,
and energy all belong in the measurement.

## 6. State can live physically in FPGA gates

The older FPGA grep experiment was a fixed-string UTF-8 matcher with an initial
simple streaming interface. The conceptual interface was an 8-bit parallel data
bus with a small handshake around signals such as:

```text
WR      input byte is being presented / written
RDY     matcher can accept input
MATCH   a match has been recognized
```

The exact electrical protocol was intentionally small enough to test the search
circuit before committing to PCIe, SPI, or a kernel-driver design.

Earlier host-side access possibilities included:

- direct user-space GPIO streaming with `libgpiod` for the simplest bring-up;
- UIO plus `mmap` for small memory-mapped registers;
- a tiny misc device / `write` or `ioctl` interface exposing something like
  `/dev/grepchip` when a real kernel boundary became worthwhile;
- later PCIe or SPI only after the search datapath itself justified the extra
  transport work.

The performance question was always end-to-end. A fast circuit does not help if
feeding it costs more than scanning the bytes on the CPU.

### 6.1 Wide equality-mask construction

For text block `T` and pattern `P` of length `m`, create an equality vector for
each pattern position:

```text
E_j[k] = 1 exactly when T[k] = P[j]
```

Align the vectors by pattern position and AND them:

```text
H[i] = AND over j=0..m-1 of E_j[i+j]
```

`H` is a compact bitmap of all matching start positions. `OR(H)` gives simple
membership, while population count or bitmap enumeration gives counts and
offsets.

A block implementation must preserve the last `m - 1` bytes or equivalent
partial state across the block boundary.

This is the spatial analogue of bit-parallel CPU matching: the CPU puts many
states in word bits; the FPGA places many comparisons/states in gates and wires.

### 6.2 Many patterns simultaneously

An FPGA becomes more interesting when it evaluates many fixed patterns in the
same pass. Shared broadcast of each input byte can feed many comparators or
state machines, with compact pattern-ID and location results emitted at the
end.

This can change the economics relative to sending the same text through the
host once per pattern.

### 6.3 Multiple input streams

Independent matcher lanes can process several streams concurrently when memory
and I/O bandwidth can keep them fed. The useful output is usually compact match
metadata rather than copying the entire source text through the device twice.

### 6.4 Pipelined regex / NFA engine

A richer FPGA design can compile a regular-expression automaton into spatial
state and update many NFA states each cycle. This is distinct from the simple
fixed-string equality-mask circuit:

- it needs a pattern compiler;
- it has a defined regex feature set;
- area scales with automaton/state representation;
- captures/backreferences and other non-regular extensions are separate
  problems;
- block/stream boundary state must be explicit.

The hardware can be attractive for long streams, many patterns, or low-latency
continuous filtering. It should not be assumed beneficial for a handful of tiny
SMS rows.

### 6.5 Put the matcher beside the bytes

The strongest FPGA case is when the search hardware is physically or logically
close to memory/storage and the bytes already pass it. This avoids making the
CPU ship a corpus to a peripheral solely for search.

The earlier intuition was therefore closer to “search beside storage/memory”
than “attach a magic grep accelerator to every small application.”

## 7. Search kernels worth keeping distinct

Do not call every retrieval method `grep`.

### Exact one-literal search

Candidates include:

- naive/anchor-and-verify;
- Two-Way;
- Boyer-Moore/Horspool;
- KMP for streaming boundaries;
- Shift-And/Shift-Or for short patterns;
- SWAR/SIMD filters;
- fixed-pattern compile-time specialization.

### Many fixed literals

Candidates include:

- trie;
- Aho-Corasick;
- Wu-Manber / Commentz-Walter families;
- SIMD multi-literal filters;
- FPGA many-pattern spatial matchers.

### Regular expressions

Candidates include:

- Thompson/Pike NFA;
- DFA/lazy DFA;
- bit-parallel NFA;
- backtracking/JIT engines for richer dialects;
- FPGA pipelined NFA for a deliberately bounded regular feature set.

### Approximate search

Candidates include:

- dynamic-programming edit distance as oracle;
- Myers bit-vector edit distance;
- n-gram candidate filters followed by exact distance verification.

### Indexed substring search

Candidates include:

- trigram/n-gram postings;
- suffix-family indexes;
- FM-index/compressed suffix structures;
- Bloom filters as negative filters only.

These answer different questions and have different setup/storage/update costs.

## 8. Where branching experiments are especially interesting

The same control-flow catalog applies beyond SMS search. Earlier examples
included:

- exact grep / fgrep and bioinformatics motif scanning;
- FASTA/FASTQ and `bioawk`-like linear passes;
- HTML tokenization;
- UTF-8 / Unicode scanning;
- CSS selector dispatch;
- XML parsing;
- IP/network dispatch;
- CAN/UDS and transmission-state dispatch;
- display-list / bytecode interpretation;
- catalog or stored-corpus substring filtering.

These workloads differ enough that the compiler should not learn one universal
“clever branch trick.” It should preserve the operation and choose among
measured implementations.

## 9. Compiler consequences

Do not erase useful facts before the backend sees them. A literal-search
operation can carry facts such as:

- fixed pattern or dynamic query;
- byte length;
- one pattern or many;
- streaming or random access;
- membership, first offset, all offsets, or count;
- overlap policy;
- normalization/case policy;
- setup reuse across rows;
- index availability;
- target word width/SIMD features;
- code-size, memory, energy, and latency preferences.

Then lowering can choose a family explicitly. The emitted sequence and selector
should remain inspectable.

This is the useful sense of the earlier “adverb” idea: constraints such as
`low_memory`, `streaming`, `small_code`, `battery_sparing`, `inspectable`, or
`compiled_pattern` can influence implementation without changing the meaning of
search.

By contrast, `case_insensitive`, `approximate`, `all_offsets`, and
`newest_first` change observable behavior and belong in the operation contract.

## 10. What to measure

For CPU implementations record at least:

- setup time;
- total bytes scanned;
- loads/stores;
- branch count and, where available, misprediction evidence;
- table/mask/code bytes;
- matches and exact offsets;
- elapsed time on the real phone;
- energy/battery evidence when the difference is large enough to matter.

For FPGA implementations also record:

- clock rate;
- bytes accepted per cycle;
- pipeline latency;
- LUT/FF/BRAM or equivalent area;
- block-boundary state;
- host-to-device and device-to-host transfer time;
- result bandwidth;
- number of simultaneous patterns/streams;
- end-to-end throughput including transport.

QEMU or a software model can establish semantics but not phone performance.
Likewise, circuit simulation can establish logic behavior but not the complete
host/FPGA throughput claim.

## 11. Immediate relevance to `search-text-messages`

The phone utility should begin with the simplest exact read-only body scan that
is easy to verify. The unusual mechanisms are preserved here because they give
us a real workload for the compiler/backend experiments:

```text
SMS rows -> exact search contract -> boring oracle
                         |
                         +-> scalar scan
                         +-> table-driven DFA
                         +-> branch-table DFA experiment
                         +-> Shift-And for short needles
                         +-> SWAR / optional NEON filter
                         +-> later repeated-query index
```

For a small changing SMS corpus, an elaborate index or FPGA is unlikely to be
the first winner. That does not make the experiments irrelevant: the same
semantic operation can be reused on large stored corpora, browser text,
bioinformatics files, and direct compiler-backend benchmarks without hiding the
comparison behind C/RefC/Java.

## Existing cross-repository experiment hooks

The current related planning already points to:

- `idric-arm-thumb` issue #9: compare conditional, transition-data, and
  table/computed branch implementations;
- `idric-arm-thumb` issue #10: Shift-And with explicit word-boundary cases;
- `idric-arm-thumb` issue #11: maintain the browser substring workload without
  treating an ABI fixture as a completed search runtime;
- `computer-science` issue #23: general many-way control-flow experiments;
- `computer-science` issue #24: compare FPGA states-in-gates with CPU
  states-in-bits/SIMD-lanes.

Those experiments should feed measurements back into this catalog rather than
turning one machine trick into the definition of search.
