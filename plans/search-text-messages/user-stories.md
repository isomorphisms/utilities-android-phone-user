# Human-goal user stories

The wording below deliberately starts with a situation in a person's life. Candidate screens, indexes, filters, and algorithms are recorded separately.

## G1 — Find an old message from partial memory

**Situation:** I remember part of something that was said, but I may not remember the exact wording, date, or conversation.

**Story:** When I partly remember an old message, I want to recover plausible matches and inspect them in their original conversations, so I can find the thing I actually remember.

**A satisfactory result means:**

- an exact remembered fragment can retrieve the exact matching messages;
- weaker clues can be narrowed without requiring the person to know the date first;
- every result identifies its conversation, direction, and time when those facts are available;
- opening a result reveals surrounding messages rather than an isolated sentence;
- the person, not an opaque ranking, makes the final decision about which result is right.

## G2 — Show what was said during a disagreement

**Situation:** I am in an argument or disagreement with somebody and want to show a series of messages between us to another person.

**Story:** When the meaning of a disagreement depends on a sequence of messages, I want to find and present the relevant part of the conversation with enough context to be understood, so another person can see what was actually said.

**A satisfactory result means:**

- the chosen messages retain chronological order;
- the participants, incoming/outgoing direction, and dates or times are clear;
- the message contents are not silently rewritten;
- I can include enough earlier or later context to avoid presenting an accidental fragment;
- unrelated private messages do not have to be included;
- presentation or sharing is a deliberate step after retrieval.

Selecting a range, exporting it, making an image, or producing plain text are candidate capabilities. They are not the story itself.

## G3 — Reconstruct when an event happened

**Situation:** I cannot remember when an event happened. For example, I cannot remember when we went to Disneyland, but I remember several different messages associated with that period.

**Story:** When no single remembered message gives me the date, I want to combine several weak clues from message history, so I can identify the likely period and verify it from the surrounding conversation.

**A satisfactory result means:**

- I can begin with any clue I actually remember: words, a person, a conversation, an attachment, or a rough period;
- I can add another clue without starting the search over;
- candidate dates or conversation windows remain traceable to the messages that produced them;
- nearby messages can confirm or disprove a candidate;
- the utility presents evidence and lets me decide; it does not claim certainty merely because several clues are close together.

“Cross-correlate” is a useful description of a possible capability here, but it is not yet a commitment to a particular scoring or indexing algorithm.

## G4 — Revisit photos exchanged with one person

**Situation:** Photos exchanged in one conversation may matter as much as photos saved in the phone gallery, but they are difficult to revisit by scrolling through years of text.

**Story:** When I want to remember the pictures exchanged with one person or group, I want to browse those pictures without stepping through every text message, while retaining when and where each picture came from.

**A satisfactory result means:**

- only media belonging to the chosen conversation is shown;
- the media can be traversed in chronological order;
- the date and sender/direction remain available;
- opening a picture can lead to its original message context;
- unavailable or unsupported attachments are not silently counted as successfully indexed.

A per-conversation media gallery is the leading candidate capability for this story.

## G5 — Find a message photo without remembering who sent it

**Situation:** I remember a photo that arrived through messaging, but I do not remember which person or group sent it.

**Story:** When the photo is the clue I remember, I want to browse photos from all accessible message conversations, so I can recognize it without guessing the sender first.

**A satisfactory result means:**

- photos from all supported message sources can be viewed together;
- chronological browsing works even before a participant is chosen;
- each photo retains or reveals its originating conversation, sender/direction, and date;
- after recognition, I can narrow to that conversation or inspect its message context;
- the utility says when its source coverage is incomplete.

A global message-media gallery is the leading candidate capability for this story.

## G6 — Recover the context of a found photo

**Situation:** A photo alone may not tell me why it was sent, what event it belongs to, or what people said about it.

**Story:** After I find a photo from message history, I want to return to the exact place it came from, so I can read the surrounding messages and understand it.

**A satisfactory result means:**

- the attachment has a stable locator back to its originating message or message window;
- surrounding messages appear in their original order;
- conversation, direction, and time remain visible;
- returning to the gallery or result set does not lose my place.

“Jump to message context” is the leading candidate capability. Whether that opens an internal read-only timeline or another message-history surface remains a design question.

## G7 — Preserve useful message photos outside the messaging app

**Situation:** Important photos can remain trapped in years of message history and may never reach the ordinary photo library or its backup.

**Story:** When photos from messages matter to me, I want to save all of them—or a reviewed subset—to a photo library or backup destination I choose, so they remain preserved and browsable independently of the messaging application.

**A satisfactory result means:**

- I can act on every supported message photo, one conversation, a date range, or an explicit selection;
- the destination is selectable rather than tied to Google or another single provider;
- original bytes and useful provenance are preserved when available;
- duplicates, failures, skipped items, and unsupported MMS/RCS sources are reported;
- large transfers can resume;
- copying a photo does not delete its message-history source.

Android MediaStore, a plain directory, WebDAV, and application-specific upload adapters are candidate capabilities.

## G8 — Remove disposable message photos in bulk

**Situation:** Recurring senders can fill message history or a derived gallery with images I do not value, such as political-fundraising spam.

**Story:** When one source contributes large amounts of disposable media, I want to exclude or remove that clutter in one reviewed operation, so meaningful photos remain easy to find and junk is not copied into my long-term backup.

**A satisfactory result means:**

- I can define the set by conversation/sender, date, media type, or explicit selection;
- I see the exact count, size, and representative contents before acting;
- hiding from a gallery, excluding from export, deleting an exported copy, and deleting source data are visibly different actions;
- any destructive source operation states whether it affects an attachment, a whole MMS/message, or another record;
- the final report says exactly what changed and what failed;
- recoverable operations are used when available.

This story lies beyond the read-only first slice. It records a need, not permission to add an unreviewed bulk-delete command.

## G9 — Keep work and family photos separate

**Situation:** A technician may need to find a photo of a fault, damaged part, or machine condition and show it to a boss through Slack or text, while family photos—including photos of children—are mixed into the same gallery.

**Story:** When one phone contains both work and family photos, I want distinct work and family contexts, so I can find and show the relevant work evidence without exposing unrelated personal pictures.

**A satisfactory result means:**

- a work view can be opened without nearby family thumbnails;
- a family view is not filled with machine faults, labels, receipts, and other work evidence;
- explicit albums, labels, and known source conversations can be used without trusting an opaque classifier;
- I can correct a classification and allow a photo to belong to more than one context;
- the share preview shows exactly what will leave the phone;
- separation does not require destructive duplication or deletion.

Albums, profiles, overlapping views, and source-based rules are candidate capabilities.

## G10 — Back up family photos without provider-side AI access

**Situation:** Some parents do not want a cloud provider to possess decryptable photos of their children or run provider-side AI on those photos when the children did not choose that exposure.

**Story:** When I back up family photos, I want a privacy boundary I can understand and control, so preservation does not require trusting an advertising or social platform with plaintext access or provider-side analysis.

**A satisfactory result means:**

- the system distinguishes transport/at-rest encryption from end-to-end encryption;
- AI is absent, disabled, on-device, or confined to hardware I control;
- visible metadata and thumbnail treatment are documented;
- originals and metadata have a bulk, non-proprietary export path;
- family sharing, account recovery, and key-loss consequences are explicit;
- a self-hosted instance has an independent restore plan rather than being mistaken for the backup itself.

Self-hosting, end-to-end encryption, local AI, and a provider's written policy are separate properties. [Issue #21](https://github.com/isomorphisms/utilities-android-phone-user/issues/21) compares current destinations.

