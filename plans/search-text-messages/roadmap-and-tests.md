# Phone roadmap, measurements, and tests

The first useful version should be a small read-only search, not an indexer,
regex laboratory, and semantic engine delivered at once.

## Gate 0: preserve the existing store boundary

- keep `content://sms` canonical;
- keep `_id`, raw `address`, `body`, `date`, `type`, `read`, and `status`;
- query newest first;
- do not depend on provider-specific `LIMIT` or full-text SQL behavior;
- do not resolve contacts, read provider database files directly, send SMS, or
  write provider rows;
- define one reviewed conversion from Android strings to valid UTF-8.

Android's public [`Telephony.Sms`](https://developer.android.com/reference/android/provider/Telephony.Sms)
contract identifies the text-message provider and columns. Access remains
through [`ContentResolver`](https://developer.android.com/reference/android/content/ContentResolver)
and the existing narrow JNI adapter.

## Gate 1: exact scan oracle

Implement and host-test one operation:

```text
body_contains_verbatim_utf8(query, body) -> Bool
```

Then scan injected `SmsRow` values and return newest matching row IDs/rows.
The first implementation may be a clear scalar reference. It must have explicit
bounds, never read past a body, and be cancellable between rows.

Acceptance:

- exact expected matching `_id` sequence;
- newest-first ordering;
- duplicate bodies remain duplicate rows;
- first match in one body permits body-level early exit;
- newest `k` result limit permits cursor-level early exit;
- no provider writes;
- no contacts/MMS/RCS dependency.

## Gate 2: useful first UI query

Expose:

- body literal query;
- optional raw-address literal query;
- inbox/sent/all filter;
- read/unread/all filter;
- date range;
- result limit and cancel;
- newest matches first;
- a short body excerpt, timestamp, box, and raw address.

If user-facing case-insensitive search is added here, specify and test the
Unicode/canonical policy. Do not label an ASCII-only trick as general
case-insensitive text search.

## Gate 3: competent scan alternatives

Against the same oracle, add only enough implementations to answer measured
questions:

1. scalar anchor-and-verify;
2. a competent general dynamic-needle baseline such as Two-Way;
3. Shift-And for word-sized patterns;
4. an optional word/NEON filter if the phone capability and corpus justify it.

Measure setup separately from scanning. Most message bodies are short, and a
fancy algorithm may lose before its skip table or masks pay for themselves.

## Gate 4: decide whether to index

First measure the real corpus:

- row count;
- total and percentile body byte lengths;
- total searchable bytes;
- typical query lengths;
- cold and warm full-scan latency;
- latency to newest 1, 10, and 100 matches;
- peak additional memory;
- cancellation latency.

Build a derived index only if the direct scan misses a stated target.

A first index experiment may compare:

- trigram postings plus exact verification;
- SQLite FTS5 trigram if a supported/native deployment is acceptable;
- a token index for word/phrase search, which is a different operation;
- compact per-row/chunk negative filters.

Index rules:

- store stable row identity and enough version/fingerprint data to detect
  change;
- handle insertions, deletions, and edited provider rows;
- make a complete rebuild safe and obvious;
- never treat index presence as authority over provider content;
- measure build time, incremental update time, bytes on disk, and query time;
- document that grams, terms, and embeddings reveal information about private
  message text even when full bodies are not copied;
- keep everything local unless a later user decision explicitly changes that.

## Gate 5: richer operations

Add these as separate vertical slices only after literal retrieval works:

- multiple literals with Aho–Corasick or another measured baseline;
- an explicitly bounded regex language and engine;
- Unicode-normalized/case-folded literal search with highlight mapping;
- fuzzy spelling search with an edit-distance oracle;
- token ranking;
- local semantic retrieval with its own evaluation set.

Each slice gets its own semantic oracle and failure cases. A single query box
may dispatch among them later, but they do not become one operation internally.

## Correctness corpus

At minimum include:

- empty body and empty query application policy;
- query longer than body;
- match at beginning, middle, and end;
- no match after a long partial prefix;
- overlapping occurrences such as `ana` in `banana`;
- repeated-prefix patterns such as `ababaca`;
- 1, 2, 3, 8, 16, 31, 32, and 33-byte patterns;
- a match crossing every internal scan/chunk boundary;
- line breaks and tabs inside one SMS body;
- ASCII case pairs and non-letter bytes;
- precomposed and decomposed canonically equivalent Unicode text;
- emoji/supplementary scalars, combining marks, right-to-left text, and ZWJ
  sequences;
- bridge policy for unpaired UTF-16 surrogates or replacement characters;
- duplicate messages with distinct `_id` values;
- identical timestamps with a deterministic tie rule;
- inbox/sent/read/date/address filter combinations;
- cancellation during a large scan;
- stale, missing, corrupt, and rebuilt derived indexes.

Use a small hand-auditable corpus plus randomized differential tests against a
simple scalar oracle. Preserve every mismatch as a fixture.

## Performance receipt

For every implementation record:

```text
semantic mode and Unicode version
result kind and limit
pattern bytes/count and setup time
row count and bytes considered/scanned
matches/offsets hash
cold/warm state
wall and CPU time
peak additional memory
index/table/code bytes
target device, ISA mode, and SIMD capability
compiler/backend commit and emitted-code hash
```

For direct ARM work also keep the emitted assembly and disassembly inspectable.
QEMU proves behavior; only the real phone supplies the relevant performance
receipt.

## First implementation handoff

The next coding branch should stop after Gates 0 and 1 unless a missing store
bridge prevents a phone probe. It should not build an index or add regex as a
substitute for proving that the exact body scan and result ordering are right.

