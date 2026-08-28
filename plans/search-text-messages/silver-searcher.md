# The Silver Searcher (`ag`) review

Reviewed source: Geoff Greer's
[`ggreer/the_silver_searcher`](https://github.com/ggreer/the_silver_searcher),
including its
[`README`](https://github.com/ggreer/the_silver_searcher/blob/master/README.md),
[`ag(1)` manual](https://github.com/ggreer/the_silver_searcher/blob/master/doc/ag.1.md),
[`search.c`](https://github.com/ggreer/the_silver_searcher/blob/master/src/search.c),
and [`ignore.c`](https://github.com/ggreer/the_silver_searcher/blob/master/src/ignore.c).

## What it is

`ag` is primarily a recursive **content searcher**, explicitly described by its
manual as "Like grep or ack, but faster." It is not primarily a GNU `find`
replacement. GNU `find` walks filesystem entries, tests names/metadata, and
performs actions; `ag` normally walks files in order to match their contents.

The memory that it felt find-like is still reasonable. Unlike plain historical
`grep`, `ag` owns a large part of the traversal and file-selection pipeline:

- recursive directory walking with a depth bound;
- symlink and one-device policy;
- hidden, ignored, binary, compressed, and file-type decisions;
- `.gitignore`, `.hgignore`, `.ignore`, and explicit ignore patterns;
- `-G` to restrict searched filenames by a regex;
- `-g` to print filenames matching a pattern without doing ordinary content
  output;
- `-l`/`-L` to print files with or without content matches;
- NUL-separated filename output for composition with other tools.

So it combines a find-like **candidate enumerator/filter** with a grep-like
**content matcher** and a result formatter.

## Actual architecture

The source separates several useful jobs:

```text
parse query/options
  -> walk directories and load local ignore rules
  -> filter candidate entries
  -> enqueue files
  -> worker threads map/read/decompress and search
  -> synchronized result formatting/statistics
```

Important implementation choices in the reviewed source:

- worker threads search queued files in parallel;
- files are normally memory-mapped where the platform policy enables it;
- literal search has a dedicated path including Boyer–Moore search;
- regex search uses PCRE, with study/JIT support when available;
- binary-file detection and optional compressed-file handling happen around
  the search path;
- simple ignore names/extensions are kept in sorted arrays and binary searched,
  while glob-like patterns take other paths;
- the output modes are separate from match mechanics.

This is an engineered pipeline, not one magic search algorithm. Much of its
speed came from searching fewer files, parallelizing independent files, and
reducing I/O overhead—not only from Boyer–Moore.

## Translation to SMS rows

| `ag` concept | Message-search analogue | Keep? |
| --- | --- | --- |
| directory traversal | iterate a newest-first SMS cursor | yes, but no filesystem recursion |
| filename/path metadata | `_id`, date, box, read state, status, raw address | yes |
| ignore/file-type rules | explicit user filters | yes, as typed predicates rather than ignore-file syntax |
| file contents | SMS `body` | yes |
| filename regex `-G` | address/thread/field predicate | adjacent structured operation |
| literal versus regex path | literal versus explicitly requested regex operation | yes |
| files-with-matches `-l` | message rows whose body matches | this is the first result shape |
| count/only-match/context modes | message count, spans/highlights, surrounding messages | later explicit result kinds |
| per-file worker queue | cursor batches or independent body jobs | only if measurements justify threading |
| `mmap` each file | provider cursor strings or a local contiguous snapshot | do not copy mechanically |
| ignore binary/compressed files | not applicable to decoded SMS text | no |

## Lessons worth keeping

1. **Candidate pruning is part of search.** Date/box/address filters can avoid
   body work just as ignore rules avoid opening files.
2. **Keep literal and regex semantics separate.** A literal query deserves a
   small direct path.
3. **Separate enumeration, matching, and formatting.** This makes later indexes
   or backends replaceable.
4. **Result shape permits early exits.** Membership or newest `k` matches can
   stop earlier than all occurrences.
5. **Measure total pipeline work.** Bytes skipped, bytes scanned, setup, cursor
   cost, result formatting, and cold/warm state all matter.
6. **Do not attribute a pipeline win to one algorithm.** `ag`'s README itself
   names threads, `mmap`, Boyer–Moore, PCRE JIT/study, and ignore lookup.

## What not to transplant

- no filesystem or ignore-file abstraction for SMS;
- no assumption that `mmap` is available or helpful through `ContentResolver`;
- no automatic eight-thread search on an Android Go phone;
- no PCRE dependency for the first literal query;
- no binary/compressed-file detection;
- no source-tree file-type catalog;
- no persistent index: `ag` is fundamentally a fresh traversal/search tool.

The best first reuse is architectural: a narrow enumerator, cheap typed
filters, a chosen matcher, and explicit result modes.

