# Candidate capability map

This is a map from possible functions to human-goal stories. It is not a feature checklist and does not settle algorithms or screen layout.

| Candidate capability | Stories served | Notes |
| --- | --- | --- |
| Exact body-text retrieval | G1, G2, G3 | A known literal fragment must remain a reliable path even if broader matching is later added. |
| Broader or approximate retrieval | G1, G3 | Useful when the remembered wording is incomplete; results must remain inspectable. |
| Participant or conversation narrowing | G1, G2, G3, G4 | Contacts, raw addresses, group threads, and renamed contacts require an explicit identity policy. |
| Date-range narrowing and chronological ordering | G2, G3, G4, G5, G6 | Dates are both clues and provenance, not merely display decoration. |
| Add or intersect several clues | G3 | Preserve which messages support each candidate period. |
| Expand a result into surrounding context | G1, G2, G3, G6 | The amount of context should be controllable. |
| Stable locator from a result or attachment to its origin | G1, G4, G5, G6 | Needed for “jump to context” and for returning without losing place. |
| Select a coherent message span | G2 | A contiguous span may be safer than arbitrary cherry-picking; this remains open. |
| Present or share a selected span | G2 | Possible forms include faithful text or a rendered view. Redaction and provenance need an explicit policy. |
| Per-conversation media-only view | G4 | Preserve date, direction, attachment type, and source coverage. |
| All-conversation media view | G3, G5 | Must not claim completeness when MMS or RCS data is unavailable. |
| Return from context to the same result/gallery position | G4, G5, G6 | Prevents a useful context link from turning browsing into repeated restarts. |
| Coverage report | G4, G5, G7, G8 | State which SMS/MMS/RCS stores and attachment types were actually read. |
| Reviewed bulk media selection | G7, G8 | Select all, a conversation, a date range, a media type, or explicit items while showing count and size. |
| Copy to Android's ordinary media library | G7 | Preserve original data and provenance where the platform permits it. |
| Destination adapter rather than one hard-coded cloud | G7, G10 | Candidate boundaries include MediaStore, a directory, WebDAV, CLI/API upload, and hosted encrypted services. |
| Resumable transfer with deduplication and a result ledger | G7 | Report copied, duplicate, skipped, unsupported, and failed items. |
| Sender/conversation exclusion rules | G7, G8 | Prevent known junk sources from entering a gallery or backup without deleting source history. |
| Separate hide, exclude, exported-copy delete, and source delete actions | G8 | These have different risk and Android permission requirements. |
| Exact bulk-action preview and confirmation | G8 | Show target count, size, representative media, and affected records before any destructive action. |
| Overlapping work and family contexts | G9 | A photo may belong to neither, one, or both; classification must be correctable. |
| Explicit albums/labels and source-based rules | G9 | Deterministic organization must remain available even if classification is later added. |
| Encryption, metadata, AI, export, and recovery capability report | G10 | “Private,” “self-hosted,” and “end-to-end encrypted” must not be collapsed into one label. |

## Proposed views, still below the story layer

1. **Search results:** matching messages with participant, date, direction, and a small context preview.
2. **Conversation context:** a read-only timeline positioned at the selected result.
3. **One-conversation media:** images or other selected attachment types from one conversation.
4. **All-message media:** a chronological gallery across supported conversations.
5. **Selected conversation span:** a faithful preview before any share or export action.
6. **Transfer preview and ledger:** the chosen photos, destination, duplicate decisions, progress, failures, and resumable state.
7. **Cleanup preview:** a read-only account of everything a hide, exclusion, or deletion operation would affect.
8. **Work/family context:** an explicit or rule-derived view whose contents can be corrected before browsing or sharing.

These views may be separate, combined, or replaced. The stories do not depend on this exact decomposition.

## Open design questions

- What counts as the same person or conversation when a contact is renamed, has several numbers, or appears in group threads?
- Should a disagreement excerpt be restricted to one contiguous span, or can several separated spans be presented together?
- Which share formats preserve order, participants, time, and unmodified text without exposing unrelated messages?
- Should images, animated images, video, audio, and other MMS parts share one media browser or have separate views?
- How are missing files, expired attachments, duplicates, and unsupported RCS records represented?
- Does “jump to context” initially open this utility's own read-only timeline, or can another messaging application reliably accept an exact message locator?
- How are several weak clues combined while showing why a candidate matched?
- Which retrieval mechanisms are exact, normalized, approximate, semantic, or attachment-based? This belongs in a later search-engine plan, not in the user stories.
- When Android cannot delete a single message attachment independently, does cleanup hide/exclude it, delete an exported copy, or require deleting the containing MMS/message?
- Which first destination boundaries are sufficient: Android MediaStore, a document-tree directory, WebDAV, or a service API/CLI?
- Which metadata accompanies an exported photo without leaking an entire private conversation?
- How are work and family contexts assigned and corrected without silently uploading the wrong material?
- Which threat model is promised: provider policy, self-hosted plaintext, server-side encryption, or client-side end-to-end encryption?
- Can all AI be disabled, and where do thumbnails, EXIF data, face labels, and search indexes exist?
- What is the independent restore procedure? Synchronization and one self-hosted server are not by themselves a backup.
