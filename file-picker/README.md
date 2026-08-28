# File picker: Android surface and semantic search

Issue: #27.

The immediate failure case is simple: an image has an opaque filename, while the remembered descriptions are things like **hermit cabin**, **rusty farm shed**, and **dilapidated lean-to**. A picker that searches only names and ordinary metadata cannot recover the file from those associations.

## The namespace does not need to become the index

Putting vectors literally *in the filesystem* is not required. The cleaner model is:

```text
canonical file / content URI
        │
        ├── ordinary metadata index
        ├── OCR / extracted-text index
        ├── caption / tag index
        ├── embedding index from model A
        └── embedding index from model B
```

All derived indexes point back to the same stable file identity. They are disposable and rebuildable. Changing an embedding model should not rename the file, move it, or destroy the old metadata. Multiple models can coexist because an embedding is meaningful only together with the model that produced it.

So yes: **one filesystem plus several indexes is the useful equivalent of a vector-aware filesystem**, and is probably better because the namespace stays boring while retrieval can change aggressively.

For Android, do not make a pathname the canonical identity. The platform often gives an app a `content://` URI rather than a path. Keep an internal opaque item id plus provenance such as URI/provider/document id, MediaStore volume/id, or an imported local copy.

## What Android's picker is actually made of

The existing `jni` branch already has the native call route in [`jni/ib-file-picker-route.md`](../jni/ib-file-picker-route.md). JNI is only the bridge; the picker itself is ordinary Android framework machinery.

### Storage Access Framework

An app launches an intent such as `ACTION_OPEN_DOCUMENT`, `ACTION_CREATE_DOCUMENT`, or `ACTION_OPEN_DOCUMENT_TREE`. Android shows a system picker and returns narrow URI grants for the item the user selected. The app then reads the returned `content://` URI through `ContentResolver`.

Reference: <https://developer.android.com/training/data-storage/shared/documents-files>

### DocumentsUI

`com.android.documentsui` is the system UI that presents document roots and files. It is not the storage database. It is a front end over document providers.

AOSP overview: <https://source.android.com/docs/core/ota/modular-system/documentsui>

AOSP source: <https://android.googlesource.com/platform/packages/apps/DocumentsUI/+/refs/heads/main/src/com/android/documentsui/>

### DocumentsProvider / DocumentsContract

A provider exposes roots, directory children, metadata, file descriptors, recent files, and search results. This is how local storage and cloud-storage apps can participate in the system picker.

The useful hook for us is `DocumentsProvider.querySearchDocuments(...)`. Android explicitly leaves *how the query is matched* to the provider and asks providers to return results sorted by relevance. A provider advertises `Root.FLAG_SUPPORTS_SEARCH`.

That means semantic search can be inserted **behind the normal Android picker**: expose a semantic document root, accept the user's free-text query, run our hybrid/vector search, and return matching documents in relevance order.

Reference: <https://developer.android.com/reference/android/provider/DocumentsProvider>

This does **not** grant access to every other provider. A normal app cannot simply enumerate arbitrary private document-provider contents and replace DocumentsUI wholesale. The semantic provider can expose files it owns, MediaStore material it is permitted to read, imported copies, and external URIs for which it has retained grants.

### MediaStore

For local photos, screenshots, videos, audio, and many ordinary shared-storage files, MediaStore is already an indexed catalog. `MediaStore.Images` is the natural starting point for the motivating image search; `MediaStore.Files` gives a broader view.

Reference: <https://developer.android.com/reference/android/provider/MediaStore>

MediaStore is useful as the **catalog of candidate files**. It is not, by itself, the semantic index we want.

### Photo Picker

For a photo/video-only selection UX, Android also has the Photo Picker (`ACTION_PICK_IMAGES`). Current AOSP implements it with MediaProvider/PhotoPicker machinery rather than the generic DocumentsUI route.

Reference: <https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES>

AOSP source: <https://android.googlesource.com/platform/packages/providers/MediaProvider/+/refs/heads/main/photopicker/>

## Search stack for this utility

A first implementation should be hybrid rather than betting everything on one model.

For every candidate file, keep as much of this as is cheaply available:

```text
item_id
source URI / provider identity
MIME type
filename / display name
size + timestamps
MediaStore metadata
EXIF
OCR text
optional caption / tags
embedding(model_signature, values)
```

At query time:

1. exact filename/path/display-name match;
2. substring/prefix/text search over metadata, OCR, captions, and tags;
3. semantic similarity where a compatible embedding exists;
4. combine the signals and rank;
5. show thumbnails and, ideally, a short reason such as `caption`, `OCR`, `filename`, or `semantic similarity`.

Unsupported or not-yet-indexed files should be marked unknown, not treated as semantic nonmatches.

## Embeddings on Android

There are two separate jobs: **produce** embeddings and **search** them.

Google's MediaPipe Tasks exposes Android text and image embedders, and LiteRT can run compatible models directly on CPU/GPU/NPU. These are useful model-execution mechanisms, but an image-only similarity model does not automatically make a text query such as `rusty farm shed` comparable with an image vector.

For text-to-image retrieval we need one of:

- a multimodal model whose text and image encoders share one embedding space; or
- an image caption/tag stage followed by the same text embedder used for the query.

References:

- <https://developers.google.com/edge/mediapipe/solutions/text/text_embedder/android>
- <https://developers.google.com/edge/mediapipe/solutions/vision/image_embedder/android>
- <https://developers.google.com/edge/litert/android>

### AppSearch is now relevant, with a compatibility catch

Current AppSearch APIs have `EmbeddingVector` plus semantic-search expressions and a model signature. AndroidX AppSearch can also use an app-local native search library.

However, this repo's JNI target is Android 14 / API 34 and prefers a native, DEX-free route. The framework embedding property was added at API 36 (also T Extensions 16), so it must not be assumed present on the API-34 target. AndroidX LocalStorage increases compatibility but also means packaging AndroidX-managed code, which is a separate tradeoff from the current native-only stance.

Therefore the file picker should put a narrow interface in front of vector search:

```text
put(item_id, model_signature, vector)
search(model_signature, query_vector, k)
delete(item_id)
rebuild(model_signature)
```

Then the backend may be platform AppSearch when available, AndroidX if we deliberately accept it, or a tiny native index otherwise.

References:

- <https://developer.android.com/reference/android/app/appsearch/EmbeddingVector>
- <https://developer.android.com/reference/androidx/appsearch/app/EmbeddingVector>
- <https://developer.android.com/reference/androidx/appsearch/localstorage/LocalStorage>

For a personal phone collection, start by measuring a plain quantized-vector scan before importing a large approximate-nearest-neighbor system. Android's own current platform embedding index documents a linear exact search mode; a few thousand or tens of thousands of vectors may not require anything more complicated.

## Other levers worth pulling

**Thumbnails first.** The screenshots motivating this issue show why a list of `Screenshot_20260828-153354.png` names is barely a picker at all. Image results should render thumbnails immediately.

**OCR is unusually valuable for screenshots.** It is cheap evidence for screenshots of webpages, messages, code, receipts, and UI. It complements rather than replaces visual semantics.

**Progressive indexing.** Filename/metadata and thumbnails can appear immediately. OCR, captions, and embeddings can be filled in later. Search results should say which layers are ready.

**Work while idle.** Expensive image analysis should be incremental and resumable, with charging/idle execution as an optimization rather than a correctness requirement.

**Virtual semantic provider.** A `DocumentsProvider` backed by our index is a serious option, not merely an implementation detail. It could make a semantic root available to ordinary Android `ACTION_OPEN_DOCUMENT` callers while leaving the system picker and URI-permission model intact.

**Standalone picker remains useful.** A custom picker can provide richer intersection filters, tags, model-specific diagnostics, and ranking explanations than DocumentsUI exposes. The provider route and the standalone UI can share the same catalog and indexes.

## First acceptance target

Do not start with every file type or a large UI. Use a small image fixture set containing one opaque-named cabin/shed image and unrelated images.

The branch is useful when:

- thumbnails render;
- ordinary filename search still works;
- `hermit cabin`, `rusty farm shed`, and `dilapidated lean-to` retrieve the target from semantic evidence;
- the result records which model/evidence produced the hit;
- deleting the derived index and rebuilding it produces the same file identities;
- swapping models requires rebuilding only that model's index, not reorganizing storage.
