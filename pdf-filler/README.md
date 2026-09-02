# PDF Filler

A deliberately small Android utility for the ordinary task: open a PDF on the phone, put information into it, and save a usable PDF.

The product goal is not a general PDF editor. It is a free, local, no-ads form filler that handles the PDFs people actually get sent.

## Product promise

Version 1 should do these things well:

1. Open a local PDF through Android's document picker.
2. Display it with normal pan/zoom.
3. If the PDF contains ordinary AcroForm fields, let the user fill text fields, checkboxes, radio buttons, combo boxes, and list boxes.
4. If the PDF is flat or scanned, let the user tap a location and place text there.
5. Let the user place an X/checkmark on a flat form.
6. Save the edited document as a new PDF through Android's document picker.
7. Work entirely on-device.

Explicitly not required for v1:

- full PDF editing
- page rearrangement
- OCR-driven automatic form reconstruction
- cloud storage accounts
- Adobe accounts
- digital certificate signatures
- XFA compatibility
- collaboration
- subscriptions
- ads
- analytics/tracking

A handwritten signature may be added if it falls out cheaply from the chosen library, but it is not a release blocker.

## Why this is now tractable

As of 2026-08-26, AndroidX PDF 1.0.0-beta01 contains an editable PDF viewer with form filling, annotations, and write support. Android's platform PDF APIs also expose form widgets and PDF write operations on sufficiently recent SDK-extension levels.

The important limitation is compatibility: `EditablePdfViewerFragment` currently requires Android 12+ with SDK extension 18. Viewing has been backported more broadly, but editing has not. Therefore AndroidX PDF is the first implementation spike, not an irreversible engine decision.

## First engine choice: AndroidX PDF

Start with:

```text
androidx.pdf:pdf-viewer-fragment:1.0.0-beta01
androidx.pdf:pdf-ink:1.0.0-beta01
```

Use `EditablePdfViewerFragment` for the first executable version.

Reasons:

- it already supplies the PDF viewer UI;
- it already understands real PDF form widgets;
- it already supports text fields, drop-downs, checkboxes, and radio buttons;
- it already has an edit/apply/save workflow through `PdfWriteHandle`;
- Android's current PDF stack supports free-text/stamp annotations on sufficiently recent SDK extensions;
- it is Apache-2.0 code, so it does not force the app to be GPL/AGPL;
- it minimizes the amount of PDF-specific code we must own.

This route uses the ordinary Android/Kotlin/Gradle stack. That is a deliberate trade: for this utility, getting a correct PDF writer quickly matters more than avoiding DEX.

## Compatibility spike

Before building the complete UI, make one tiny executable that answers these questions on the actual target phones:

```text
OPEN      can we open and render a PDF?
FORM      can we enumerate/fill an AcroForm text field?
SAVE      does the saved file reopen with the value visible?
FLAT_TEXT can we place typed text on a non-form PDF and persist it?
CHECK     can we place a check/X and persist it?
```

Record each as PASS / FAIL / SKIP with Android version and SDK extension level.

This spike is the engine gate.

### If AndroidX editing works on the target devices

Keep AndroidX PDF for v1.

### If AndroidX editing is unavailable on too many target devices

Move to a bundled PDFium path rather than trying to reimplement PDF.

The most useful reference implementation is MJ PDF. Current MJ PDF v3 is GPLv3, uses its own AndroidPdfViewer + PdfiumAndroid stack, fills PDF forms, saves annotations/signatures into PDFs, has no ads/tracking, and builds PDFium from source. We can study it or reuse GPL code if we deliberately make this utility GPLv3.

PDFium itself is the underlying Chromium PDF engine and supports builds with JavaScript/XFA options. A direct PDFium/JNI path gives broad OS compatibility and more control, but costs substantially more integration work than AndroidX.

MuPDF is not the default fallback because its AGPL/commercial licensing is a needless complication for this app.

## PDF cases

The app should classify the document internally into practical cases rather than pretending every blank is a form field.

### A. AcroForm

Use the existing field widgets.

Expected v1 support:

- text
- checkbox
- radio button
- combo/dropdown
- list

Save the field values and verify their visible appearances in at least two independent PDF viewers.

### B. Flat/vector PDF

There is no field to fill.

Interaction:

```text
tap page
-> text cursor / small text box
-> type
-> drag if necessary
-> resize/font-size if necessary
-> save
```

Internally, store placement in page coordinates:

```text
page
x
y
width
height
text
font_size
alignment
```

Persist this using a free-text/stamp annotation or equivalent engine-supported PDF object. Do not rely on a screen-only overlay that disappears when the PDF is opened elsewhere.

### C. Scanned PDF

Treat it exactly like a flat PDF for v1. The page image is just the background.

OCR is optional assistance later; it is not necessary for a person to tap where text belongs.

### D. XFA / unsupported interactive PDF

Detect it and say clearly that this form type is unsupported. Do not silently corrupt it.

## UX

Keep the first UI small.

```text
Open PDF

[page viewer]

Fill mode:
- automatic real form fields when present
- Text
- Check / X
- optional Signature

Undo
Save a copy
```

No library screen, document management system, login, tutorial carousel, AI, or cloud sync in v1.

The important user story is:

> Someone emails me a PDF form. I open it on my phone, fill the blanks, save it, and send it back without creating an account or fighting Adobe software.

## Privacy and permissions

Default policy:

- no `INTERNET` permission;
- no advertising SDK;
- no analytics SDK;
- no crash-reporting network SDK;
- no account;
- no storage-wide permission when the Storage Access Framework is sufficient;
- source repository public;
- privacy policy can truthfully state that documents are processed locally and are not collected by the app.

## Acceptance corpus

Keep small public/test PDFs in a test-fixtures directory where licensing permits, or generate fixtures during tests.

Minimum corpus:

1. one simple text-field AcroForm;
2. checkbox + radio form;
3. dropdown/list form;
4. multipage AcroForm;
5. flat vector form;
6. scanned/image-only form;
7. password-protected PDF;
8. XFA sample for correct refusal;
9. malformed-but-viewable PDF;
10. a PDF filled by the app and reopened in another viewer.

The crucial acceptance test is not merely that our own viewer displays the edit. The saved output must reopen correctly in another independent PDF implementation.

## Build sequence

### Milestone 0 — engine receipt

Build the five-test compatibility spike: OPEN / FORM / SAVE / FLAT_TEXT / CHECK.

Do this before polishing UI.

### Milestone 1 — useful APK

- document picker
- editable viewer
- real form filling
- save-as
- clear unsupported-file errors

At this point the app is already useful for genuine AcroForms.

### Milestone 2 — the feature that makes it broadly useful

- tap-to-place typed text on flat/scanned PDFs
- move/resize text
- X/checkmark placement
- undo/remove placement

This matters more than sophisticated AcroForm features because many forms that visually look fillable are not interactive PDFs.

### Milestone 3 — release hardening

- filename/save behavior
- rotation/process-death testing
- large PDF sanity check
- corrupted PDF failure behavior
- accessibility pass
- test on low-memory phone
- verify no network permission or trackers
- build release AAB
- store listing/privacy text/screenshots

### Later, not v1

- remembered signature
- OCR-assisted blank detection
- automatic field detection for flat forms
- flatten edits into page content
- encrypted PDF editing
- better XFA handling
- desktop/web ports

## September / October 2026 target

A functional prototype during the first implementation week is realistic if AndroidX PDF passes the compatibility spike. A production-quality release by October is also realistic as a narrow utility, but it is not guaranteed: the main technical risk is AndroidX editing availability on the devices we want to support, followed by weird real-world PDFs.

For Google Play submissions after 2026-08-31, the app must target Android 16 / API 36. If the developer account is a personal account created after 2023-11-13, Google currently also requires a closed test with at least 12 continuously opted-in testers for 14 days before applying for production access. Those store requirements can determine the public release date independently of whether the APK is finished.

## Decision rule

Do not spend a week debating engines before running the spike.

```text
AndroidX passes target devices
    -> build v1 on AndroidX

AndroidX fails because SDK extensions exclude important devices
    -> evaluate MJ PDF/PDFium as bundled fallback

PDFium integration becomes larger than the app
    -> ship AndroidX-compatible v1 first, state minimum requirements clearly,
       then add the compatibility backend separately
```

The first engineering task is therefore not "build a PDF editor." It is:

> Produce an APK that opens one AcroForm and one flat PDF, makes one persistent edit to each, saves both, and proves the files reopen correctly elsewhere.

## Primary references

- AndroidX PDF releases: https://developer.android.com/jetpack/androidx/releases/pdf
- EditablePdfViewerFragment: https://developer.android.com/reference/androidx/pdf/ink/EditablePdfViewerFragment
- Android platform PdfRenderer.Page form APIs: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer.Page
- Android FreeTextAnnotation: https://developer.android.com/reference/android/graphics/pdf/component/FreeTextAnnotation
- PDFium: https://github.com/chromium/pdfium
- MJ PDF: https://github.com/mudlej/mj_pdf
- Google Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Google Play new personal-account testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465
