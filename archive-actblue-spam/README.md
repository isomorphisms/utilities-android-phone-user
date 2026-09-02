# Archive ActBlue spam

Goal: reduce political-fundraising SMS interruptions by classifying messages locally on the phone and auto-archiving only messages with very high confidence. The first target is ActBlue-linked fundraising traffic. Do not report messages as spam automatically.

## Important Android boundary

There are two materially different implementations.

### 1. Companion/read-only app

A non-default SMS app can read SMS when Android grants the relevant permission, but it cannot write the system SMS provider. It therefore cannot reliably remove/archive a message before the user's normal SMS app receives and notifies on it.

This mode is useful for collecting labeled training data and evaluating the classifier, but it cannot satisfy the final no-interruption requirement by itself.

### 2. Default SMS app

Android delivers `Telephony.Sms.Intents.SMS_DELIVER_ACTION` only to the user's default SMS app. The default SMS app is responsible for writing the incoming message and notifying the user. That gives us the interception point we actually need:

1. receive the SMS PDU;
2. reconstruct the message;
3. classify locally;
4. if ordinary: write to inbox and notify;
5. if allowed-state political message: put in `hold`/normal inbox according to user policy;
6. if high-confidence ActBlue spam: write to our archive store and do not notify;
7. retain enough information to restore/reclassify a false positive.

Official Android references:

- https://developer.android.com/reference/android/provider/Telephony
- https://developer.android.com/reference/android/provider/Telephony.Sms.Intents
- https://developer.android.com/guide/topics/permissions/default-handlers

This probably fits best with the separate simple SMS/text app work: the filtering layer belongs immediately before that app commits an incoming SMS to its visible inbox.

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

A message that otherwise looks like ActBlue fundraising but has credible Michigan relevance is not auto-archived. Initially, err toward `HOLD_ALLOWED_STATE` rather than trying to infer whether every mention of Michigan is sincere/local enough.

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

For safety, training and deployment thresholds are separate. The model can learn from every label while auto-archive remains conservative.

## High-precision decision rule

Do not interpret a raw model score as trustworthy probability without calibration. Start with a conservative score gate.

Suggested initial logic:

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
- restore to inbox;
- `this was correct`;
- `false positive`;
- `allow messages like this`;
- optional bulk purge only by explicit user action.

A false positive should immediately become a negative training example.

## Development stages

1. **Read-only shadow classifier** — run against existing SMS without changing anything; collect labels and precision/recall.
2. **Manual archive suggestion** — show what would have been archived.
3. **High-confidence auto-archive** — only after the observed false-positive rate is acceptable.
4. **Default-SMS interception** — integrate with the simple SMS app so high-confidence messages never generate an inbox notification.
5. **State/candidate relevance refinement** — improve the `allowed_states` hold using accumulated labels.

## Acceptance tests

The first useful test corpus should contain ordinary personal texts, transactional/OTP texts, delivery/work messages, legitimate local political messages, and a large set of ActBlue-linked fundraising messages.

Minimum behavioral checks:

```text
ordinary personal SMS              -> INBOX
bank/OTP/transactional SMS         -> INBOX
unknown ambiguous political SMS    -> INBOX
high-confidence out-of-state ask   -> ARCHIVE_ACTBLUE
Michigan-relevant political SMS    -> HOLD_ALLOWED_STATE
false positive restored by user    -> becomes KEEP training example
```

The success metric is interruption reduction subject to a strict false-positive ceiling, not maximum spam recall.