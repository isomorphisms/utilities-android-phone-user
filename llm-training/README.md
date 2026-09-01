# LLM training collector

Draft phone utility for collecting human training signals for a replacement LLM.

The app source belongs here. Training records are data, not application source; the temporary data sink is the `training-data` branch of `bl4ckb4ll/blackball` until a dedicated data repository exists.

## First useful version

The phone UI should make four kinds of contribution quick enough to do repeatedly:

1. **Preference pair** — show the same prompt with response A and response B. Choose A, B, tie, or skip. An optional note says why.
2. **Ideal response** — enter a prompt and write the response the model should have produced.
3. **Critique + rewrite** — keep the model response, state what is wrong with it, then supply a corrected response.
4. **Ranking** — rank three or more candidate responses to the same prompt. This is useful when pairwise comparison would throw away information already available on screen.

A fifth, deliberately weaker signal may be added later: categorical labels such as factual error, instruction failure, bad reasoning, needless verbosity, citation failure, or style problem. Labels should supplement rather than replace an actual preference, critique, or demonstration when the trainer can provide one.

## Trainer identity

Every submitted record must contain `trainer_username`.

Do **not** bake a trainer's trust weight into each record. Raw submissions should remain append-only evidence. A separate downstream trainer policy can map usernames to weights or exclusion status. That makes it possible to demote, exclude, or restore a trainer later without editing the original records.

The app should refuse to submit until a username has been set. The username is visible in the review screen before upload.

## Record format

One record per JSON file keeps concurrent Git writes simple and makes the corpus naturally append-only.

Suggested path:

```text
records/<trainer_username>/<year>/<month>/<record_id>.json
```

Common envelope:

```json
{
  "schema_version": 1,
  "record_id": "uuid-or-other-collision-resistant-id",
  "created_at": "RFC-3339 timestamp",
  "trainer_username": "alice",
  "kind": "preference_pair",
  "prompt": "...",
  "context": null,
  "payload": {},
  "tags": [],
  "source": null
}
```

`payload` depends on `kind`:

### preference_pair

```json
{
  "response_a": "...",
  "response_b": "...",
  "choice": "a",
  "reason": "optional free text"
}
```

Allowed choices: `a`, `b`, `tie`, `skip`.

### ideal_response

```json
{
  "response": "..."
}
```

### critique_rewrite

```json
{
  "original_response": "...",
  "critique": "...",
  "rewritten_response": "..."
}
```

### ranking

```json
{
  "responses": [
    {"id": "a", "text": "..."},
    {"id": "b", "text": "..."},
    {"id": "c", "text": "..."}
  ],
  "ranking": ["b", "a", "c"],
  "reason": "optional free text"
}
```

## Phone behavior

The utility should be offline-first:

- save unfinished work locally;
- queue completed records locally;
- show `queued`, `sent`, and `failed` counts;
- never discard a record merely because network upload failed;
- show the exact JSON before upload when requested;
- allow copying/exporting queued records independent of GitHub upload.

The default screen should emphasize large thumb-sized actions rather than a settings-heavy form: `Compare`, `Write ideal`, `Critique + rewrite`, `Rank`.

## GitHub submission

Do not embed a shared repository credential in the APK.

For the initial private/test version, each authorized trainer can supply their own GitHub credential and username. The credential is local secret state and must never be written into a training record.

Target for now:

```text
repository: bl4ckb4ll/blackball
branch: training-data
path: records/<trainer_username>/...
```

If the app is distributed more broadly, replace ad-hoc personal tokens with per-user GitHub authorization or a small submission service. Authorization and training identity are related but distinct: the record must still carry the explicit trainer username used by the training pipeline.

## Downstream trainer policy

Keep trainer weighting outside the raw corpus, for example:

```json
{
  "alice": {"weight": 1.0, "status": "active"},
  "bob": {"weight": 0.25, "status": "demoted"},
  "charlie": {"weight": 0.0, "status": "excluded"}
}
```

The training/export step applies this policy when constructing a dataset. The collector should not silently rewrite or delete existing records when policy changes.

## Not decided yet

- exact Android implementation and Idriç/native boundary;
- GitHub authorization mechanism for distributed trainers;
- whether imported model conversations carry model/version metadata;
- whether prompts may include attachments or only text;
- dataset licensing/consent language for trainers.

The first implementation should keep the record format independent of those choices.