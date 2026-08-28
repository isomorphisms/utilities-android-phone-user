# Search text messages: technical plan

Status: planning only. No search implementation is claimed on this branch.

This work starts from the read-only SMS boundary in
[`../../messages/README.md`](../../messages/README.md) and
[`../../messages/idric/SmsStore.idric`](../../messages/idric/SmsStore.idric).
The Android SMS provider remains the canonical source. A search index, if
measurements later justify one, is derived data that may be deleted and
rebuilt.

The broader [user stories](user-stories.md) deliberately include message
history that people experience as SMS, MMS, or RCS. This first executable lane
has SMS body rows only. It must report that coverage honestly rather than
claiming that SMS-only results satisfy attachment or RCS stories.

The user-facing feature may be called **Search messages**. The implementation
should not collapse every operation or every algorithm into the word `grep`.
Filtering rows, finding an exact substring, matching a regular expression,
looking for misspellings, ranking words, and retrieving semantically similar
messages have different contracts.

## Technical documents

- [`operations.md`](operations.md) defines the distinct programmer-visible
  operations and the result details that must be settled before optimizing.
- [`algorithms.md`](algorithms.md) inventories scan, automaton, index, fuzzy,
  and semantic-search families without declaring one universal winner.
- [`arm-thumb-and-fpga.md`](arm-thumb-and-fpga.md) connects this application to
  the existing ARM/Thumb fixtures and the earlier FPGA grep plan.
- [`silver-searcher.md`](silver-searcher.md) reviews The Silver Searcher (`ag`),
  including what is useful here and why it is closer to recursive grep than to
  GNU `find`.
- [`roadmap-and-tests.md`](roadmap-and-tests.md) gives the smallest useful phone
  path, later index gates, measurements, and correctness corpus.

## Recovered starting points

These are existing artifacts, not new claims:

- `messages-sms-store` already models `_id`, raw `address`, `body`, `date`,
  message box, read state, and status, newest first, through a narrow injected
  driver.
- `idric-arm-thumb` issue
  [#9](https://github.com/isomorphisms/idric-arm-thumb/issues/9) reserves a
  measured table/computed-branch experiment for fixed-string search.
- `idric-arm-thumb` issue
  [#10](https://github.com/isomorphisms/idric-arm-thumb/issues/10) reserves a
  Shift-And packed-bit experiment.
- The `branching-dispatch-fixtures` branch contains
  [`IBSubstringCatStep.idric`](https://github.com/isomorphisms/idric-arm-thumb/blob/branching-dispatch-fixtures/tests/branching/IBSubstringCatStep.idric),
  a one-byte DFA transition for the literal `cat`. It is currently an
  acceptance fixture that is expected to reach the backend's unsupported
  lowering boundary; it is not an executable substring-search backend.
- `computer-science` issues
  [#23](https://github.com/isomorphisms/computer-science/issues/23) and
  [#24](https://github.com/isomorphisms/computer-science/issues/24) record the
  many-way-dispatch and FPGA-to-register-bit connections.
- The earlier FPGA plan used two complementary shapes: equality masks for
  wide exact fixed-string matching, and a pipelined regex/NFA engine for many
  patterns and independent streams, returning compact match-location bitmaps.
  That FPGA material was recovered from prior planning; no installed FPGA-grep
  repository was found during this review.

## Current decisions

1. Keep all access to `content://sms` read-only.
2. Do not make provider-specific SQL text matching the correctness boundary.
3. Establish an exact literal oracle before regex, fuzzy, ranked, or semantic
   search.
4. Search bodies first; raw-address matching and structured filters are
   adjacent operations, not contacts machinery.
5. Return newest matching messages first for the first application slice.
6. Measure a direct scan on the real phone before building a persistent index.
7. Preserve a search operation through Idriç/compiler IR when target-specific
   lowering is studied; do not reconstruct it from a flattened character loop.
8. Compare specialized lowering with a competent conventional baseline.
9. Keep ARM, FPGA, and index choices replaceable behind one explicit semantic
   contract.

## Deliberately not decided here

- the final spelling of compiler "adverbs";
- whether the first user-facing default is verbatim, Unicode-normalized, or
  Unicode case-insensitive search;
- whether a local trigram/token index is worth its storage and privacy cost;
- whether regular expressions belong in the first application;
- whether fuzzy and semantic searches should share one query box or appear as
  separate operations;
- whether any unusual ARM instruction wins on the actual phone.

