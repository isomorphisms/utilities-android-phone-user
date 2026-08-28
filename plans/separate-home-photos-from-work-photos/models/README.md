# Models

## Baseline: MobileNet V3 Small

The first local experiment uses MobileNet V3 Small.

Google's current MediaPipe image-embedder sample downloads this model:

```text
https://storage.googleapis.com/mediapipe-models/image_embedder/mobilenet_v3_small/float32/1/mobilenet_v3_small.tflite
```

The sample uses it as an image embedder, which is a better fit for this experiment than pretending ImageNet class names directly equal `work` and `home`. The application-specific separator can be tiny and sit on top of the embedding.

`fetch-mobilenet-v3-small.sh` records the exact versioned source URL and downloads it into this directory as `mobilenet_v3_small.tflite`.

This model is a baseline, not a permanent architectural dependency. The utility should expose a separator boundary so MobileNet can later be replaced by a smaller model, an object detector, a cascade, a remote classifier, or another approach without changing the user workflow.

## First experiment

Use a small labeled corpus containing examples such as:

- roof faults/materials versus people/family scenes;
- automotive parts, leaks, wiring and under-hood images versus personal scenes;
- conveyors, motors, bearings, sensors, cabinets, tooling and fault displays versus personal scenes.

Evaluate a conservative separator that is permitted to return `unknown`.

Useful measurements are:

- precision of `work_candidate`;
- precision of `home_candidate`;
- fraction left `unknown`;
- fraction of irrelevant thumbnails removed from a work-focused gallery;
- scan time and peak memory on the target phone;
- model bytes stored on the phone.

The product does not require 100% coverage. A separator that confidently removes only a useful minority of clutter can still be successful.
