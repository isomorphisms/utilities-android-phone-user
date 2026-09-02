# Archive ActBlue spam

Goal: reduce political-fundraising SMS clutter by classifying messages locally on the phone and hiding high-confidence ActBlue fundraising messages from the normal human reading flow. The first target is ActBlue-linked fundraising traffic. Do not report messages as spam automatically.

The requirement is **not** to stop the carrier/Android SMS stack from receiving the message. It is to prevent the user from having to visually filter through five fundraising messages to find a message from family, work, or another real correspondent.

## First useful architecture

A read-only SMS utility is sufficient for the first useful version:

1. read the SMS provider;
2. classify messages locally;
3. present a normal filtered inbox containing messages the user likely wants to see;
4. put high-confidence ActBlue fundraising messages in a separate local archive view;
5. keep every hidden message recoverable and auditable.

This means becoming the default SMS app is **not a prerequisite** for the useful filtering feature. A later default-SMS implementation may allow tighter control over storage and notifications, but it is a separate enhancement rather than the starting requirement.

The filtered inbox can therefore sit on top of the existing read-only SMS reader work.

## Policy: three outcomes, not just spam/not-spam

```text
INBOX
HOLD_ALLOWED_STATE
ARCHIVE_ACTBLUE
```

`ARCHIVE_ACTBLUE` must have a deliberately high precision threshold. The project is useful even if it catches only 50% of the nuisance messages, provided the false-positive rate is extremely low.

`HOLD_ALLOWED_STATE` prevents the out-of-state filter from swallowing locally relevant political messages. Configuration should be explicit:

```text
allowed_states = ["MI"]
```

A message that otherwise looks like ActBlue fundraising but has credible Michigan relevance is not auto-hidden from the main political-message view. Initially, err toward `HOLD_ALLOWED_STATE` rather than trying to infer whether every mention of Michigan is sincere/local enough.

## Classifier

Start with a small linear classifier rather than a language model.

A hashed sparse feature vector plus a logistic-regression or linear-SVM weight vector is enough for this problem and maps directly to a hyperplane separator.

Useful features:

- character 3-5 grams from message body;
- word/bigram hashes;
- normalized URL/domain fragments;
- explicit `actblue`, `secure.actblue.com`, donation-link and fundraising-template indicators;
- sender/short-code history, but never make sender alone decisive;
- phrases such as donation asks, match claims, deadline language, candidate/race language, `STOP` boilerplate;
- state names, postal abbreviations, congressional-district patterns, governor/senate/house race language;
- whether an allowed state is mentioned;
- repeated template similarity to previously labeled ActBlue messages.

Feature hashing avoids keeping a large vocabulary. A 16,384-feature F16 weight vector is only 32 KiB. Even several separate vectors are trivial on a phone.

### Training

Keep training local too. The user labels real messages:

```text
KEEP
ARCHIVE_ACTBLUE
ALLOWED_STATE
```

Store labeled examples locally. Incremental SGD/logistic regression or a perceptron-style update is enough for an initial implementation. No TensorFlow/PyTorch runtime is required.

For safety, training and deployment thresholds are separate. The model can learn from every label while automatic hiding remains conservative.

## High-precision decision rule

Do not interpret a raw model score as trustworthy probability without calibration. Start with a conservative score gate.

```text
if allowed_state_relevance(message):
    HOLD_ALLOWED_STATE
else if strong_actblue_evidence(message) and model_score(message) >= archive_threshold:
    ARCHIVE_ACTBLUE
else:
    INBOX
```

`strong_actblue_evidence` can include an ActBlue domain/template match or multiple independent fundraising indicators. This makes the first version intentionally biased toward false negatives rather than false positives.

Later, once there is enough labeled data, calibrate the model score on a held-out local validation set and choose `archive_threshold` against an explicit maximum false-positive target.

## Presentation layer

The important product behavior is simple:

```text
NORMAL INBOX
  family
  friends
  work
  transactional messages
  ambiguous messages
  locally relevant political messages

ARCHIVED POLITICAL FUNDRAISING
  high-confidence ActBlue messages
```

The user should not need to delete, report, swipe, or mentally skip the archived messages during ordinary SMS use.

An unread count for the normal inbox should exclude `ARCHIVE_ACTBLUE` messages. The archive can have its own quiet count if useful, but it should not dominate the main reading flow.

## Audit/recovery

Every automatic archive decision should record:

```text
message_id
sender
received_at
classifier_version
score
matched_strong_features
matched_state
reason
```

The archive UI needs:

- newest first;
- restore/show in normal inbox;
- `this was correct`;
- `false positive`;
- `allow messages like this`;
- optional bulk purge only by explicit user action.

A false positive should immediately become a negative training example.

## Development stages

1. **Read-only shadow classifier** — run against existing SMS without changing the displayed inbox; collect labels and precision/recall.
2. **Filtered inbox** — hide high-confidence matches from the normal reading list and expose them under `Archived political fundraising`.
3. **Training feedback** — restore/false-positive actions update local labels.
4. **State hold** — preserve allowed-state messages from automatic hiding.
5. **Optional default-SMS integration** — only if later desired for notification/storage control.

## Acceptance tests

The first useful test corpus should contain ordinary personal texts, transactional/OTP texts, delivery/work messages, legitimate local political messages, and a large set of ActBlue-linked fundraising messages.

```text
ordinary personal SMS              -> INBOX
bank/OTP/transactional SMS         -> INBOX
unknown ambiguous political SMS    -> INBOX
high-confidence out-of-state ask   -> ARCHIVE_ACTBLUE
Michigan-relevant political SMS    -> HOLD_ALLOWED_STATE
false positive restored by user    -> becomes KEEP training example
```

Primary success metric:

> How many nuisance messages disappear from the ordinary human reading flow without hiding messages the user actually wanted to see?

Maximum recall is not the goal. A conservative classifier that removes half the ActBlue clutter can already materially improve the inbox.