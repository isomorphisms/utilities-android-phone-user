# Programmer's Unicode Picker 0.3.0 prerelease

This is the standalone Unicode-pad-style picker, not the Android keyboard/IME.

Open the app, tap characters into its text buffer, tap **COPY**, then paste into the target app. It does not ask to enable or select a keyboard and does not alter the system keyboard configuration.

## Included pages

- Unicode
- Math
- Punctuation
- Programming
- Regular Expressions
- Concept Separation
- Incantation Assistance
- Several Pastebins

The picker includes the current heavy programming assignment arrow `⟵`, while ordinary `←` remains available on the Unicode and Math pages.

## Android/runtime boundary

- native C APK using Android's platform `NativeActivity`
- no app Java or Kotlin
- no `classes.dex`
- no requested Android permissions
- arm64-v8a, armeabi-v7a, and x86_64 native libraries
- versionName 0.3.0 / versionCode 3

The Idriç source defines the picker pages and allowed key actions and generates the checked-in C layout snapshot; the mutable editing state and Android boundary remain C in this release.

## Verification

The release workflow checks the Idriç layout contract, runs the host C behavior/touch tests, builds and inspects the APK, and exercises the picker in an Android emulator before publishing the release.

The attached APK is debug-signed for sideload testing. It is not a Google Play production-signed artifact.
