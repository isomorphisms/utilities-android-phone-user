# Algorithm choice as an adverb

This photo utility is a useful example of a recurring compiler/program-design distinction: the same goal can stay fixed while successive choices say *how* it is achieved.

The word `adverb` is informal here, but it captures the direction of the distinction.

## Goal versus successive how-choices

For this utility:

```text
human goal
  find and send a work-problem photo without family-photo clutter

how?
  separate the photo library into work/home-oriented contexts

how?
  inspect visual evidence and classify only when sufficiently confident

how?
  local model
  OR remote model
  OR another separator not evaluated yet

how locally?
  MobileNet V3 Small embedding + small separator
  OR another local model/rule/cascade
```

Each lower level can change without changing the level above it.

This is useful to preserve explicitly in code and design documents. Otherwise a temporary implementation such as `MobileNet` tends to become confused with the requirement itself.

## Effects, dependencies and costs are different things

The local/remote split also shows why `side effect` is too coarse a word for every practical consequence.

### Remote classifier

A remote classifier has a genuine runtime effect/dependency:

```text
network I/O
```

That brings availability, latency, privacy, authentication and failure behavior into the semantics of the operation.

### Local classifier

A local classifier can be pure with respect to outside services during inference:

```text
pixels + model bytes -> classification
```

But it still has resource and deployment costs:

```text
model storage
RAM
CPU/GPU/NPU time
battery
scan latency
possibly one-time model download
```

Model size is not naturally a Haskell-style `IO` effect. Neither is execution time. They are still important properties of the algorithm choice.

A useful vocabulary is therefore:

- **effect** — interaction with the outside world during the operation, such as network I/O or writing a file;
- **dependency/requirement** — something that must exist, such as a model file, network service, camera permission, or LiteRT runtime;
- **resource cost** — storage, memory, battery, CPU, bandwidth;
- **latency** — time before the result is available;
- **privacy boundary** — which data crosses which machine/process/service boundary;
- **failure mode** — what happens when the dependency is absent or the algorithm is uncertain.

These can accompany an algorithm choice without being confused with the function's main result.

## Open design spaces need an explicit remainder

There are two different meanings of an `else`/`other` case worth keeping separate.

### Runtime remainder

For photo classification the runtime remainder is real data:

```text
work_candidate
home_candidate
unknown
```

`unknown` means the classifier inspected a particular photo and declined to decide.

### Design-space remainder

At design time there is another remainder:

```text
local_model
remote_model
other_strategy_not_evaluated
```

This does **not** mean a runtime algorithm named `other` has been implemented. It says our survey is intentionally non-exhaustive. We looked at two useful leaves and stopped because that was enough to move the project forward.

That is closer to an epistemic marker—"there may be other cases I did not investigate"—than to Haskell's `Maybe`.

If represented as a closed datatype too early, the type can accidentally claim more knowledge than we actually have. Depending on the program, the right representation may instead be an extensible strategy interface plus documentation that only two implementations are currently known/tested.

## A tree rather than a flat case switch

The choices naturally form a tree:

```text
send useful work photo
└── reduce mixed-gallery clutter
    └── separate photos
        ├── local
        │   ├── MobileNet embedding + tiny separator
        │   └── other local method
        ├── remote
        │   ├── large hosted classifier
        │   └── other remote method
        └── other separation family not yet surveyed
```

A flat enum loses the fact that some alternatives differ at different levels of abstraction.

That hierarchy is the main reusable computer-science note: **preserve the verb/goal, then represent the successive how-choices separately, together with the effects, dependencies, costs and failure modes introduced at each choice.**
