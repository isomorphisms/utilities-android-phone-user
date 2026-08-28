# Search operations: do not call everything grep

The first design task is to say what answer is wanted. Algorithm selection
comes afterward.

## Operation families

| Operation | Meaning | Typical result | Candidate implementation families |
| --- | --- | --- | --- |
| `filter_messages` | Select rows by date, box, read state, status, or raw address | Ordered message rows | predicate scan, provider selection after portability tests, ordinary field indexes |
| `contains_literal` | Does one field contain one exact literal? | Boolean per row or matching rows | byte scan, Two-Way, KMP, Boyer–Moore family, Shift-And, word/SIMD scan |
| `find_literal` | Find first or all occurrences of one literal | offsets/spans | same literal families, with an occurrence contract |
| `contains_any` | Does a field contain any member of a literal set? | matching row plus optional pattern IDs | repeated scan for tiny sets, trie, Aho–Corasick, bit-parallel or SIMD multi-literal filters |
| `contains_all` | Does a field contain every requested literal? | Boolean or matching rows | repeated exact search, multi-pattern automaton plus seen-pattern state, index intersection |
| `match_regex` | Match a stated regex language | rows, spans, and perhaps captures | Thompson NFA, DFA/lazy DFA, bit-parallel NFA, PCRE-style backtracking/JIT when its richer semantics are requested |
| `find_words` | Match tokens, prefixes, or phrases under a tokenizer | rows and term positions | inverted index, FTS, token scan |
| `find_near_text` | Allow spelling/edit differences under a stated distance | rows and distances | edit-distance dynamic programming, Myers bit-vector method, gram candidates plus verification |
| `rank_terms` | Rank records by term evidence rather than simple membership | scored rows | inverted index with BM25 or another stated score |
| `find_similar_meaning` | Retrieve messages by a model-defined notion of semantic similarity | scored rows | embeddings plus exact or approximate nearest-neighbor search |

The last operation is not a more advanced exact substring search. It can return
a message containing none of the query text, and can miss a literal match. It
needs its own label and evaluation.

## `grep`, `find`, and message retrieval

`grep` conventionally selects text lines using a pattern. A message database
contains records and fields, and the application normally returns whole
messages ordered by time. It is reasonable to say "grep-like scan" for one
implementation, but the application operation is message retrieval.

GNU `find` is primarily a filesystem-entry selector and action runner: it walks
a tree, tests path/name/metadata predicates, and performs actions. Structured
SMS filtering is closer to that selection shape than literal body matching is.
The two shapes can be composed:

```text
enumerate SMS rows
  -> filter date/box/read/address
  -> search selected bodies
  -> order/project results
```

That pipeline is more informative than calling the whole thing grep.

## Result contracts that change the program

These choices are semantics, not mere performance flags:

- membership, first offset, all offsets, count, or top `k` rows;
- overlapping versus non-overlapping occurrences;
- message-level results versus one result per occurrence;
- newest-first, oldest-first, or relevance ranking;
- body, raw address, or both fields;
- exact case, ASCII folding, or Unicode case folding;
- raw code-unit equality, canonical normalization, or compatibility
  normalization;
- byte, Unicode scalar, UTF-16 code-unit, or grapheme-cluster offsets;
- whole-word boundaries and the Unicode/text-segmentation rules behind them;
- whether an empty query means recent messages, every possible empty-string
  position, or a rejected search request;
- whether a result limit permits early termination;
- whether chunk boundaries and embedded line breaks are transparent.

No optimizer may change one of these while claiming to have selected another
algorithm for the same operation.

## Proposed first semantic oracle

The first executable primitive should be deliberately narrow:

```text
body_contains_verbatim_utf8(query, body) -> Bool
```

Contract:

- both inputs are valid UTF-8 produced by one reviewed Android-to-Idriç bridge;
- matching is exact byte substring matching;
- an empty query is rejected by the application boundary;
- membership is sufficient, so scanning one body may stop on its first match;
- the surrounding cursor is newest first;
- a request for the newest `k` matches may stop after `k` matching rows;
- duplicate rows and duplicate bodies remain distinct by `_id`.

This is an oracle, not the final human-text policy. It gives every later
algorithm and backend one unambiguous answer to reproduce.

## Human-text modes after the oracle

Add explicit modes rather than silently changing the oracle:

1. `verbatim` — the exact UTF-8 contract above;
2. `canonical` — normalize query and body to a stated Unicode normalization
   form before exact matching;
3. `case_folded` — apply a pinned Unicode default case-folding policy plus a
   stated normalization policy;
4. `ascii_case_folded` — only if clearly named and range-guarded; toggling bit
   5 is not Unicode case folding.

Normalization and folding can expand or reorder text. A highlighted result
therefore needs a mapping from the transformed match span back to the original
message. Returning only message membership avoids that mapping in the first
gate.

Unicode normalization is specified by
[UAX #15](https://www.unicode.org/reports/tr15/). Word and grapheme boundaries
are specified by [UAX #29](https://www.unicode.org/reports/tr29/). Pin the
Unicode data version in a real implementation so results do not depend on an
unrecorded platform table.

## Application query versus compiler primitive

The application may expose one search box and translate it into an explicit
query value. The compiler should see the distinctions rather than a stringly
bag of flags. A useful abstract shape is:

```text
query semantics
  + fields
  + result kind
  + text equivalence policy
  + ordering/limit
  + resource constraints
```

The first five items determine observable behavior. Resource constraints such
as `low_memory`, `no_persistent_index`, `compiled_pattern`, or
`battery_sparing` are candidates for the provisional compiler "adverb" layer.

