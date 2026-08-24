# Math Characters IME

A deliberately restricted Android software keyboard for the small set of mathematical and programming characters that are awkward to reach from a normal phone keyboard.

## First key set

- arrows: `← ↑ ↓ → ↔ ↦ ⇒`
- number sets and infinity: `ℂ ℝ ℚ ℤ ℕ ∞`
- basis / imaginary letters: `i j k`
- ordinary digits: `0 1 2 3 4 5 6 7 8 9`
- superscripts: `⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁺ ⁻ ⁼ ⁽ ⁾ ⁿ`
- subscripts: `₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ₊ ₋ ₌ ₍ ₎`
- editing: space, backspace, enter, and an IME picker button

There is no Unicode database, search UI, network access, account, clipboard history, or predictive text.

## Use

1. Install the APK.
2. Open **Math Characters** and tap **Enable keyboard**.
3. Enable Math Characters in Android's input-method settings.
4. Tap **Choose keyboard** or use Android's keyboard switcher.
5. The `IME` button on the symbol pad opens Android's input-method picker so you can switch back quickly.

## Build

Requires JDK 17, Android SDK 36, and Gradle 9.5.0.

```sh
gradle -p math-characters testDebugUnitTest assembleDebug
```

The separate `math-characters-picker` branch explores the UnicodePad-style standalone picker. This branch stays focused on the IME.
