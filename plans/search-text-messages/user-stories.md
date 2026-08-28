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
