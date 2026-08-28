# How to back up text messages

This branch starts from `messages-sms-store`, so the existing read-only Android SMS-store boundary remains the base.

The question here is narrower than building another full messaging app: how should a small Android utility preserve SMS/MMS outside the provider database, and which existing open-source implementations are worth studying?

## Upstream source policy

Do **not** copy whole upstream repositories into ordinary folders just to study them.

Use this split:

- Our notes, experiments, formats, and eventual backup implementation live as ordinary folders in this repository.
- A whole independently maintained upstream project that we actually need to build or inspect in-tree should be a Git submodule, pinned to a known commit, under `messages/backup/upstream/`.
- A small mechanism that we deliberately adopt or rewrite becomes ordinary source in our tree, with the upstream origin and license recorded where required.

Why prefer submodules for whole upstream projects:

1. The upstream repository keeps its own identity and history instead of becoming an unexplained vendor dump.
2. Updating or comparing upstream revisions is explicit.
3. The phone-utilities repository does not absorb many megabytes of unrelated history and generated Android build files.
4. License boundaries stay much easier to see.

Why *not* add every candidate as a submodule immediately:

1. Submodules add clone/update friction (`--recurse-submodules` or `git submodule update --init`).
2. Most candidate repositories will only contribute one or two ideas.
3. We should first identify the few implementations whose source is genuinely worth keeping pinned.

So this branch begins with a catalog, not a pile of vendored source trees.

## First-pass distinct implementations worth reading

### `tmo1/sms-ie` — SMS Import / Export

https://github.com/tmo1/sms-ie

Probably the strongest current reference for a plain-data design. It exports and imports SMS and MMS as NDJSON/ZIP, including MMS binary data, and uses Android's Storage Access Framework. That means the same exporter can target local storage, SD/USB storage, or a cloud provider exposed through SAF (for example Nextcloud or an rclone-backed document provider) without baking one cloud vendor into the SMS code.

This is a particularly useful reference for:

- complete provider-row serialization;
- MMS attachment handling;
- stable, inspectable backup formats;
- import/restore semantics;
- Storage Access Framework boundaries.

### `jberkel/sms-backup-plus` — SMS Backup+

https://github.com/jberkel/sms-backup-plus

The classic network-oriented design. It backs SMS, MMS, and call logs into Gmail/IMAP and can restore SMS/call-log data. It is old and largely in maintenance mode, but it is still a major independent design family rather than another XML-file exporter.

Useful reference for:

- incremental backup state;
- IMAP representation of messages;
- scheduled/background backup;
- duplicate avoidance;
- restore behavior.

Do not count its many GitHub forks as separate designs unless a fork has materially diverged.

### `FossifyOrg/Messages`

https://github.com/FossifyOrg/Messages

A full open-source SMS/MMS client rather than a dedicated backup utility, but it has message export/import support. Worth reading because backup is integrated with a real messaging app and current Android behavior.

Useful reference for:

- export/import from a production messenger;
- provider interaction under current Android versions;
- backup-file compatibility and migration behavior.

### `quik-sms/quik`

https://github.com/quik-sms/quik

Another full open-source SMS client, descended from QKSMS, with explicit message backup/restore support.

Useful as a second independent messenger implementation so we do not accidentally treat Fossify's choices as Android requirements.

### `mrrfv/open-android-backup`

https://github.com/mrrfv/open-android-backup

Broader than SMS: a device-backup project with a native Android companion. It currently exports SMS as CSV (viewable backup rather than automatic SMS restore) and packages backups with compression/encryption.

Useful reference for:

- moving SMS data off-device without making the phone app itself a cloud client;
- desktop/ADB companion workflows;
- encrypted archive packaging;
- simple CSV export.

### `borabuyukbas/PhoneBackup`

https://github.com/borabuyukbas/PhoneBackup

A small Android backup/restore application covering SMS plus contacts, calendars, and call logs. Much smaller and less established than the projects above, but it appears to be an independent implementation rather than a fork and is therefore useful as a compact comparison point.

## Adjacent references

### Epistolaire

https://gitlab.com/hydrargyrum/epistolaire

Not GitHub, but worth keeping in the survey because its design is unusually close to the minimal utility we might want: dump SMS, MMS, and MMS photos to JSON/files on phone storage, then let rsync, Syncthing, ownCloud, etc. move those ordinary files elsewhere. It avoids coupling message extraction to network backup entirely.

### `eblah/nextcloud-messagevault`

https://github.com/eblah/nextcloud-messagevault

This is the opposite half of the problem: a Nextcloud-side importer/viewer. It ingests XML produced by the proprietary Android app SMS Backup & Restore. It is not itself an Android SMS exporter, so do not count it as one, but it is useful evidence for what a self-hosted archive/browser could look like.

## Search-result noise that should not inflate the count

GitHub searches for `android sms backup` return many repositories that should not be counted as independent, usable backup applications:

- forks of SMS Backup+;
- parsers, HTML viewers, cleaners, and converters for SMS Backup & Restore XML;
- import-only migration scripts;
- tiny ContentResolver tutorials and student projects;
- repositories containing only product screenshots/readmes rather than the app source;
- abandoned experiments with no meaningful backup/restore path.

For example, `DeveshRx/SMS-Drive-for-Android` advertises cloud SMS backup, but the current public repository contains only assets and a README, not the application source, so it is not a source codebase we can study.

## Count

A literal GitHub name/description search produces well over a dozen repositories. After collapsing forks and excluding parsers, viewers, tutorials, source-less product pages, and trivial experiments, the set of **distinct substantial implementations worth studying is roughly half a dozen on GitHub**, plus a few adjacent projects such as Epistolaire and Nextcloud Message Vault.

So "under a dozen real codebases" is a reasonable working estimate if *real codebase* means an independent implementation with enough source and behavior to teach us something. It is not a defensible claim if every hobby/demo repository is counted.

## Likely design for this repository

The simplest architecture to test first is:

```text
Android SMS/MMS provider
        |
        v
read-only extractor
        |
        v
stable ordinary backup format
  - NDJSON metadata
  - attachment files
        |
        v
Storage Access Framework destination
  - local storage
  - SD / USB
  - Nextcloud provider
  - rclone/RSAF provider
  - anything else Android exposes as a document destination
```

This keeps "read my messages correctly" separate from "where should these bytes live?" and avoids creating a different SMS implementation for every cloud service.

The first concrete comparison should be `tmo1/sms-ie` versus the much simpler Epistolaire extraction model, with SMS Backup+ used mainly for incremental/scheduled-backup ideas.