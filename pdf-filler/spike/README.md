# AndroidX PDF engine gate

This is the smallest executable compatibility spike for the five questions in the parent PDF
filler plan. It is a gate, not the editor.

```text
OPEN      AndroidX reports its first visible page bitmap for a generated PDF
FORM      a generated AcroForm text field is enumerated, filled, saved, and read after reopen
SAVE      another installed PDF viewer visibly shows the saved AcroForm value
FLAT_TEXT another installed PDF viewer visibly shows typed text converted to vector outlines
CHECK     another installed PDF viewer visibly shows a vector X
```

The last three gates do not become `PASS` merely because AndroidX can reopen its own output. The
spike records that internal check as evidence but leaves the gate at `SKIP` until the operator opens
the result in another viewer and records what was actually visible.

`FLAT_TEXT` is deliberately narrow. AndroidX PDF 1.0.0-beta01 does not expose a free-text
annotation. The spike turns the typed marker into glyph outlines and stores those paths in a stamp
annotation. A pass proves a persistent visible mark, not searchable text or a text object that can
later be edited character-by-character.

## Build

Install JDK 17, Android SDK platform `android-36-ext19`, and Gradle 8.13, then run:

```sh
cd pdf-filler/spike
gradle --no-daemon :app:assembleDebug
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`. The repository workflow performs the same
build and uploads the APK as an artifact.

The AndroidX beta AAR requires compile SDK extension 19. That is a build-time requirement; the
editable fragment's runtime requirement remains Android 12 plus S extension 18. The app targets
API 36, has `minSdk 28`, and requests no permissions. It contains no network, analytics,
advertising, OCR, or document-management code.

## Device run

1. Install the debug APK on the target phone.
2. Run **FORM**. The app creates its own tiny AcroForm, fills the marker, saves it, and reopens it.
3. Choose **View form result**, use a different PDF viewer, return, and record `SAVE` as `PASS` or
   `FAIL`.
4. Run **FLAT_TEXT + CHECK**. The app creates a flat PDF and writes two stamp annotations.
5. Choose **View flat result**, inspect it in a different viewer, then record `FLAT_TEXT` and
   `CHECK` separately.
6. Export the JSON receipt and commit it under `receipts/` without editing its outcomes.

On devices below Android 12 or below SDK extension 18, the app still uses the backported read-only
viewer to exercise `OPEN`. It records the four editing gates as `SKIP` with the reported API and
extension level. A missing extension is not a failed PDF operation; the operation was unavailable
and was not run.

## Decision rule

- Keep AndroidX only if the important target phones can run the editable fragment and the exported
  receipts show all five gates passing.
- Treat `FORM=PASS` with `SAVE=SKIP` as incomplete: AndroidX read its own output, but no independent
  implementation has confirmed it.
- Treat `FLAT_TEXT=PASS` as acceptance of outlined glyph stamps for v1. If v1 requires native,
  searchable, re-editable free-text annotations, this spike cannot pass that stronger requirement.
- An unavailable editable fragment on an important phone is evidence for the bundled PDFium
  fallback, even though its editing gates correctly say `SKIP` rather than `FAIL`.

## Receipt contract

Every exported receipt contains the Android release, API level, S extension level, manufacturer,
model, ABIs, exact AndroidX version, timestamp, and one `PASS`, `FAIL`, or `SKIP` record for each
gate. Each record has a reason and concrete evidence field. `PASS` never means more than its stated
evidence.
