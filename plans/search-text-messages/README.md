# Search text messages

This folder records why somebody searches message history before deciding how the search is implemented.

## Keep three layers separate

| Layer | Question | Example |
| --- | --- | --- |
| Human goal | What is the person actually trying to accomplish? | “I need to show somebody what was said during a disagreement.” |
| Capability | What must the utility let the person do? | Select and present a coherent span of one conversation. |
| Mechanism | How might the program do it? | Message identifiers, indexes, exact matching, ranking, or another search algorithm. |

A capability is not automatically a user story. “Give me a gallery,” “filter by date,” “export a range,” and “jump to a message” are possible ways to serve a human goal. Likewise, no user story should commit the project to one algorithm or call every kind of retrieval grep.

## Working boundaries

- The first useful slice remains read-only retrieval and presentation.
- Later stories include deliberate export and possible bulk cleanup. Hiding an item, excluding it from backup, deleting an exported copy, and deleting source message data are different actions.
- Any source deletion requires its own reviewed design, an exact preview, and a clear account of what Android permits; recording the story does not silently expand the first slice into a message writer.
- It is separate from [issue #9](https://github.com/isomorphisms/utilities-android-phone-user/issues/9), which asks whether this repository should eventually contain a default SMS application.
- “Text messages” is the ordinary human name for the history visible in a messaging app. The underlying records may be SMS, MMS, or RCS. Every implementation must state which sources it can actually read.
- Photo stories cannot be satisfied by SMS-body access alone. Missing MMS or RCS coverage must be visible rather than silently presented as a complete gallery.
- A result must retain provenance: conversation or participants, sender/direction, date and time when available, and a route back to surrounding context.
- Nothing is shared outside the phone merely because it was found. Sharing, export, and backup are separate, deliberate actions.
- Export destinations must remain selectable. The plan must not assume Google Photos, Google Drive, Instagram, Facebook, or any other single provider.

The current executable data boundary is narrower than the human stories: [`../../messages/README.md`](../../messages/README.md) covers read-only SMS rows only. The technical plan begins there and treats broader MMS/RCS/media coverage as separate, explicit later work.

## Human goals and capabilities

- [user-stories.md](user-stories.md) contains the human situations and satisfactory outcomes.
- [capability-map.md](capability-map.md) records candidate functions and the stories they may serve.
- [issues.md](issues.md) links the corresponding repository issues.

## Technical planning

- [technical-plan.md](technical-plan.md) reconciles the SMS store, earlier ARM/Thumb and FPGA work, and the current decisions.
- [operations.md](operations.md) separates literal, regex, token, fuzzy, ranked, semantic, and structured retrieval contracts.
- [algorithms.md](algorithms.md) inventories scan, automaton, index, approximate, and semantic-search families.
- [arm-thumb-and-fpga.md](arm-thumb-and-fpga.md) records what already exists and where target-specific lowering belongs.
- [silver-searcher.md](silver-searcher.md) reviews The Silver Searcher (`ag`) and its find-like candidate-selection layer.
- [roadmap-and-tests.md](roadmap-and-tests.md) gives implementation gates, measurements, and the correctness corpus.

The user stories should grow when a new human situation appears, not merely whenever another algorithm, filter, or screen is proposed. The technical files may evolve as implementations are measured, but they must continue to point back to the human goals and explicit source coverage.

