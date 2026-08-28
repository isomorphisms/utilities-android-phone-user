# Text-search algorithm inventory

This is a catalog of candidates, not a menu to implement indiscriminately.
Pattern length and count, corpus size, query repetition, memory, update rate,
required offsets, text semantics, and target hardware all matter.

## Direct literal scans

| Family | Strong fit | Costs and boundary conditions | Relevance here |
| --- | --- | --- | --- |
| single-byte scan / `memchr` shape | one-byte needle or locating an anchor byte | still needs verification for longer needles | first baseline; word-at-a-time or SIMD may accelerate it |
| naive scan with anchor and verify | tiny inputs/patterns; correctness oracle | worst case repeats comparisons | indispensable simple baseline, not the only competitor |
| Knuth–Morris–Pratt | streaming input and guaranteed linear time | prefix table; often more work than simpler methods on ordinary short text | useful when rows/chunks must be streamed without backing up |
| Two-Way string matching | linear-time, constant-extra-space general substring search | less obvious to implement and inspect | competent conventional baseline for dynamic needles |
| Boyer–Moore / Horspool | moderate or long needles with random access and useful skip distances | setup table; weak skips on small alphabets/adversarial text | relevant to `ag` and traditional fixed-string search |
| Rabin–Karp / rolling hash | many windows or several equal-length patterns; chunked filtering | collisions require exact verification; arithmetic setup | possible candidate generator, never a hash-only oracle |
| Shift-And / Shift-Or / Bitap | short patterns fitting in one or a few machine words | mask table; long patterns need multiple words or another algorithm | exact connection between earlier FPGA state and ARM register bits |
| BNDM/SBNDM-style bit-parallel matching | short patterns, backward filtering, word-sized state | specialist setup and length boundaries | later comparison candidate, not a first implementation |
| SWAR or SIMD candidate filtering | scan many bytes/candidate positions per instruction | alignment, tail handling, setup, target capability, and downclock/energy effects | measure ordinary ARM integer operations first; NEON only when the phone exposes it |

Useful specializations can be simpler than a general algorithm:

- length 0: application policy, not a scan loop;
- length 1: byte search;
- very short fixed length: load/XOR/combine/test when bounds and alignment are
  explicit;
- compile-time fixed pattern: precompute masks/skip tables in the artifact;
- dynamic phone query: build setup tables at query time and include setup cost.

## State machines and many patterns

| Family | Strong fit | Important distinction |
| --- | --- | --- |
| explicit DFA transition | one known finite pattern/state machine | transition representation may be branches, a data table, or a branch table |
| trie | shared prefixes among several fixed literals | trie traversal alone does not handle every restart/failure efficiently |
| Aho–Corasick | many fixed literals in one pass | automaton construction and memory can dominate for a one-off small query |
| Commentz–Walter / Wu–Manber families | multiple patterns with opportunities to skip | more setup and workload sensitivity than Aho–Corasick |
| SIMD multi-literal filters, including Teddy-style designs | several short literals on SIMD-capable CPUs | filters produce candidates that still require exact verification |

A data-table DFA and a computed-branch DFA implement the same transition
function but not the same machine behavior. `TBB`/`TBH` may compact dense
action dispatch; they do not automatically accelerate a literal scan. For many
scanners, a data lookup followed by an ordinary loop branch is cheaper and
easier for predictors than computed control transfer per input byte.

## Regular-expression engines

"Regex" is not one semantic language or one algorithm.

| Engine family | Properties | Boundary |
| --- | --- | --- |
| Thompson NFA / Pike-style VM | predictable, normally linear in input times automaton size for a regular feature set | richer captures and extensions need additional machinery |
| full or lazy DFA | fast transition loop; can amortize repeated queries | state explosion or cache growth must be bounded |
| bit-parallel NFA | many NFA states updated in word bits | word/multiword boundaries resemble Shift-And issues |
| backtracking VM / PCRE with optional JIT | rich dialect, captures, lookaround, backreferences depending on engine | worst-case behavior and supported syntax differ; not interchangeable with a regular-language engine |
| FPGA pipelined NFA | spatially update many states and streams | area, clock, memory bandwidth, pattern compilation, and boundary carry are first-class costs |

The Silver Searcher deliberately has separate literal and PCRE paths. The phone
application should retain that separation if regex is added; literal text
should not be escaped into a regex and sent through the heaviest engine by
default.

## Derived indexes

Indexes trade write/build cost, storage, privacy exposure, and staleness for
faster repeated queries.

| Index | Answers well | Limits |
| --- | --- | --- |
| ordinary field index | date, box, read state, status, address prefix/equality | not arbitrary body substring search |
| inverted token index | word, prefix, phrase, Boolean term queries, ranking | token semantics; not arbitrary interior substring |
| trigram/n-gram postings | arbitrary substring candidate generation | short queries need fallback; normalization/folding must match; exact verification still required |
| row/chunk Bloom filters | cheap negative rejection | false positives; cannot locate a match or prove a positive without verification |
| suffix array/tree/automaton | general substring search over a stable corpus | build/update and memory complexity; message corpus changes |
| FM-index / compressed suffix array | compressed full-text substring search | substantially more implementation complexity; updates are awkward |
| prefix trie | normalized phone numbers, addresses, or word prefixes | not interior body search |

SQLite FTS5 has both token indexes and a
[trigram tokenizer](https://www.sqlite.org/fts5.html#the_trigram_tokenizer) that
supports general substring matching. That makes it a useful measured candidate,
not an automatic Android dependency. We must first decide whether to bundle a
reviewed SQLite build, whether the phone's available build exposes the needed
feature through a supported boundary, and whether its index/storage behavior
fits the no-Java/native application path.

## Approximate and ranked search

- Wagner–Fischer-style dynamic programming gives a clear edit-distance oracle.
- Myers' bit-vector algorithm packs an edit-distance frontier into machine
  words and is especially relevant to the register-state experiments.
- grams can shortlist candidates before exact edit-distance verification.
- a BK-tree is useful for a metric dictionary of terms; it is not by itself an
  arbitrary-substring index over entire message bodies.
- BM25 and similar scores rank token evidence. They do not preserve
  newest-first membership semantics unless ordering is explicitly composed.

Approximate text search needs a threshold, unit, normalization, token/window
policy, and ranking/tie rule. Without those, "fuzzy" is not a defined
operation.

## Semantic retrieval

Embeddings plus a flat or approximate-nearest-neighbor search can support
meaning-based retrieval. This is a separate later lane because it introduces:

- a model and model version;
- vector precision and dimension;
- message chunking;
- an evaluation set defining useful similarity;
- false-positive/false-negative behavior unlike literal search;
- significant index size and rebuild cost on an Android Go phone;
- privacy questions if any text leaves the device.

It should never replace the exact literal oracle. It can be offered beside it
after a small local model and a real message-retrieval test set justify the
cost.

## Previously discussed FPGA equality-mask construction

For text `T` and fixed pattern `P` of length `m`, construct one equality vector
for each pattern position:

```text
E_j[k] = 1 exactly when T[k] = P[j]
```

Candidate start `i` matches exactly when:

```text
H[i] = AND over j=0..m-1 of E_j[i+j]
```

All `H[i]` bits are produced in parallel. `OR(H)` answers membership; `H`
itself is the compact match-location bitmap. A block implementation must carry
the last `m - 1` text bytes, or equivalent partial state, so matches crossing a
block boundary are not lost.

The CPU Shift-And plan spatializes fewer states into register bits. The FPGA
construction spatializes candidate positions/states into gates and wires. The
connection is real; the performance conclusion still requires bandwidth,
clock, setup, and transfer measurements.

## Selection observations for a compiler/planner

Record at least:

- semantic operation and regex/text dialect;
- pattern known at compile time or dynamic;
- pattern byte length and pattern count;
- expected row/body sizes and total corpus bytes;
- one query or repeated queries;
- streaming/random-access availability;
- result kind: membership, first, all, count, or top `k`;
- normalization/case-fold policy;
- code-size, setup-time, memory, latency, throughput, energy, and storage bounds;
- target word/SIMD width and measured capabilities;
- index freshness and privacy constraints.

Selection may happen at compilation for fixed patterns, once per query for a
dynamic needle, or adaptively after inspecting pattern/workload facts. The
selector and rejected alternatives should remain inspectable.

## Primary references

- Knuth, Morris, and Pratt, [Fast Pattern Matching in Strings](https://doi.org/10.1137/0206024).
- Boyer and Moore, [A Fast String Searching Algorithm](https://doi.org/10.1145/359842.359859).
- Aho and Corasick, [Efficient String Matching](https://doi.org/10.1145/360825.360855).
- Baeza-Yates and Gonnet, [A New Approach to Text Searching](https://doi.org/10.1145/135239.135243).
- Karp and Rabin, [Efficient Randomized Pattern-Matching Algorithms](https://doi.org/10.1147/rd.312.0249).
- Myers, [A Fast Bit-Vector Algorithm for Approximate String Matching](https://doi.org/10.1145/316542.316550).

