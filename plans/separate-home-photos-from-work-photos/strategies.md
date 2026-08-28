# Separation strategies

The user-visible operation is one separator:

```text
photo -> work_candidate | home_candidate | unknown
```

How that result is computed is replaceable. This document records the first two strategies considered; it is intentionally not an exhaustive survey of every possible classifier.

## Strategy A — Local model on the phone

Run a small vision model on the phone, initially MobileNet V3 Small.

### Baseline model

Google's MediaPipe image-embedder examples use a MobileNet V3 Small TFLite model at 224×224 input. That is a useful baseline because an embedding can feed a very small application-specific separator rather than requiring the original model's labels to equal `work` and `home`.

The repository records the model source in `models/README.md` and `models/fetch-mobilenet-v3-small.sh`.

Possible local separator layers on top of the embedding include:

- a handful of labeled prototype images and nearest-prototype similarity;
- two or three centroids learned from representative work/home images;
- a tiny linear or logistic classifier;
- explicit mappings or rules for strong work-like/object-like and person/family-like signals;
- combinations of visual classification with source album, capture context, or later user corrections.

The first experiment should prefer high precision and allow `unknown` freely.

### Dependencies and costs

Local inference removes the runtime network dependency, but it is not free. Its notable requirements/costs are:

- model bytes stored on the phone;
- initial APK size if bundled, or one-time download time if installed separately;
- RAM while the model is loaded;
- CPU/GPU/NPU time;
- battery use;
- time required to scan an existing photo library.

These are mostly deployment/resource costs, not side effects in the Haskell sense.

### Privacy and failure behavior

- Photo pixels need not leave the phone.
- No connection is required after the model is installed.
- A failed classification becomes `unknown`; it must never damage or delete the original photo.
- A model update should invalidate/recompute derived classifications without changing original media.

### Bundled versus downloaded local model

These are themselves two lower-level choices:

**Bundled:** simplest runtime; application install is larger.

**Downloaded once:** smaller initial application; first classification waits for the model download and needs a network connection once. The model should be versioned and checksummed. GitHub Releases could distribute model bytes, but GitHub would be serving a static file, not executing inference.

## Strategy B — Network/remote classifier

Send a reduced image, full image, or derived representation to a remote inference service and receive a separation result.

This permits a much larger model without storing it on the phone. It also introduces a real runtime effect/dependency:

```text
classify_remote : photo -> network I/O -> separation result
```

### Advantages

- larger model can be changed server-side without an application update;
- little or no model storage on the phone;
- potentially more accurate semantic classification;
- compute burden is moved off a cheap phone.

### Costs and effects

- network availability;
- request and response latency;
- server availability and maintenance;
- bandwidth;
- possible monetary inference cost;
- privacy consequences because pixels or derived information leave the phone;
- authentication and abuse-control work if the service is public.

GitHub can host/download model files, but a static model stored on GitHub does **not** perform remote inference. A remote strategy needs some executing service. GitHub Actions is not an appropriate per-photo interactive inference API.

### Failure behavior

- offline or failed requests become `unknown` or fall back to a local separator;
- remote failure must never block ordinary access to the photo library;
- the application should say what leaves the phone before remote classification is enabled.

## Signals, not a brittle definition

A useful first separator can exploit the fact that many work photos in these occupations strongly feature inorganic structures:

- machinery;
- rust and exposed metal;
- tools;
- automotive components;
- conveyor and factory hardware;
- roofing/building material;
- wiring and control cabinets;
- gauges, labels and displays.

Many home/family photos strongly feature people, faces, children, pets, food and gatherings.

But `inorganic -> work` and `organic -> home` are not universal laws. The conservative design is:

```text
strong work evidence -> work_candidate
strong personal evidence -> home_candidate
otherwise -> unknown
```

The product wins by removing easy clutter, not by manufacturing certainty.

## Other strategies not evaluated yet

The design space is open. Examples that might later be worth testing include:

- on-device object detection rather than global image classification;
- image segmentation or simple estimates of person/foreground coverage;
- face/person detection plus machine/tool/object detection;
- classical color/texture/edge features;
- EXIF/time/location/source-folder rules;
- user-trained prototypes;
- a cascade: very cheap rules first, small model second, larger model only for leftovers;
- another small pretrained vision model that benchmarks better than MobileNet for this corpus.

This `other` case is deliberate. The two strategies above are the two investigated leaves, not a claim that all possible algorithms have been exhausted.
