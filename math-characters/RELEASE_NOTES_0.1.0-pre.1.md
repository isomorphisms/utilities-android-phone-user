# Math Characters 0.1.0 prerelease

This freezes the small Android software keyboard from the last green `math-characters-ime` build.

The keyboard is intentionally named **Math Characters** so it is easy to understand and easy to find. It provides a compact set of mathematical and programming characters that are awkward to enter from an ordinary phone keyboard, including arrows, number sets, superscripts, subscripts, and script letters.

## Frozen source

Application source is frozen from commit `0135ea3349aec18bf7ffd5d5ed5b13109cb1fd3f` (the successful Math Characters IME build). Release metadata and the prerelease workflow are the only additions on the freeze branch.

## Prerelease artifacts

- `math-characters-0.1.0-pre.1-debug.apk` — debug-signed, installable APK for testing and sideloading.
- `math-characters-0.1.0-pre.1-debug.aab` — debug Android App Bundle for archival/testing.
- `math-characters-0.1.0-pre.1-release-unsigned.apk` — unsigned release APK preserved for later signing.
- `math-characters-0.1.0-pre.1-release-unsigned.aab` — unsigned release App Bundle preserved for later signing.
- `SHA256SUMS.txt` — checksums for all packaged binaries.

The unsigned release artifacts are intentionally not presented as installable/store-ready binaries. A future Google Play release still needs a proper release signing key and store metadata.

## Current key set

Arrows, `ℂ ℝ ℚ ℤ ℕ ∞`, ordinary digits, superscript digits/operators and `ⁱ ʲ ᵏ ˡ ⁿ`, subscript digits/operators and `ᵢ ⱼ ₖ ₗ`, plus space, backspace, enter, and the Android IME picker.
