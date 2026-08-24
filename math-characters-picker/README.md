# Math Characters Picker

A small standalone Android character pad modeled on the useful interaction pattern in UnicodePad: open the app, tap characters into an editable text buffer, then copy, share, or return the text to the invoking app.

This branch is separate from the `math-characters-ime` keyboard path in PR #1. It is not an Android software keyboard.

## First character set

- arrows: `← ↑ ↓ → ↔ ↦ ⇒`
- number sets and infinity: `ℂ ℝ ℚ ℤ ℕ ∞`
- basis / imaginary letters: `i j k`
- ordinary digits: `0 1 2 3 4 5 6 7 8 9`
- superscripts: `⁰ ¹ ² ³ ⁴ ⁵ ⁶ ⁷ ⁸ ⁹ ⁺ ⁻ ⁼ ⁽ ⁾ ⁿ`
- subscripts: `₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ₊ ₋ ₌ ₍ ₎`

## Small first slice

- editable text buffer
- fixed character grid
- tap inserts at the cursor
- backspace and clear
- copy
- share
- Android `PROCESS_TEXT` input/output

No full Unicode database, search index, emoji catalog, ads, network access, or predictive text.

UnicodePad by Ryosuke839 is Apache-2.0 and is the reference for the picker interaction model, but this branch should remain a much smaller implementation.
