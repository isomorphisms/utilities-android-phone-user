# Math Characters

A deliberately restricted Android utility for the small set of mathematical and programming characters that are awkward to reach from a normal phone keyboard.

## Two interfaces

Keep both interaction models:

1. **Software keyboard / IME** — switch to Math Characters from another app and type symbols directly into the current text field.
2. **Character pad / picker app** — open Math Characters as a small UnicodePad-like utility with an editable text buffer, tap symbols into it, then copy/share/return the text.

The IME is the first implemented slice. The picker should reuse the same character table rather than maintain a second list.

## First key set

- arrows: `← ↑ ↓ → ↔ ↦ ⇒`
- number sets and infinity: `ℂ ℝ ℚ ℤ ℕ ∞`
- basis / imaginary letters: `i j k`
- ordinary digits: `0 1 2 3 4 5 6 7 8 9`
- superscripts: `⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁺ ⁻ ⁼ ⁽ ⁾ ⁿ`
- subscripts: `₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ₊ ₋ ₌ ₍ ₎`
- editing: space, backspace, enter, and an IME picker button

There is no Unicode database, search UI, network access, account, clipboard history, or predictive text.

## IME use

1. Install the APK.
2. Open **Math Characters** and tap **Enable keyboard**.
3. Enable Math Characters in Android's input-method settings.
4. Tap **Choose keyboard** or use Android's keyboard switcher.
5. The `IME` button on the symbol pad opens Android's input-method picker so you can switch back quickly.

## Picker direction

The picker should stay small: an editable text buffer, the same fixed character grid, backspace/clear, copy, share, and Android `PROCESS_TEXT` support. Long-press character details can be added later without importing UnicodePad's full metadata database.

## Build

Requires JDK 17, Android SDK 36, and Gradle 9.5.0.

```sh
gradle -p math-characters testDebugUnitTest assembleDebug
```

## UnicodePad relationship

[Ryosuke839/UnicodePad](https://github.com/Ryosuke839/UnicodePad) is an Apache-2.0 open-source Android Unicode input application and was useful as a reference point for the picker model. This implementation is intentionally much smaller and does not copy UnicodePad source or its multi-megabyte Unicode name database.
