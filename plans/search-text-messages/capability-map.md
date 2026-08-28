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
| Coverage report | G4, G5 | State which SMS/MMS/RCS stores and attachment types were actually read. |

## Proposed views, still below the story layer

1. **Search results:** matching messages with participant, date, direction, and a small context preview.
2. **Conversation context:** a read-only timeline positioned at the selected result.
3. **One-conversation media:** images or other selected attachment types from one conversation.
4. **All-message media:** a chronological gallery across supported conversations.
5. **Selected conversation span:** a faithful preview before any share or export action.

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
