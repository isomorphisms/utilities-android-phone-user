# utilities-android-phone-user

Small Android utilities intended for direct use on a phone.

Each utility lives in its own folder and should remain independently buildable where practical.

[`math-characters`](math-characters) is **Programmer's Unicode Picker**: an Idriç-owned, standalone copy/paste picker whose Android APK remains native, DEX-free, and separate from the system keyboard.

[`text-pad`](text-pad) records the product direction and phone user stories for a deliberately small plain-text editor. It is a design note rather than an implementation today.

[`jni`](jni) records the complete Android JNI 1.6 function-table surface, its ART-specific rules, and the native document-picker/share, clipboard, and display routes needed by IB and the phone utilities.

[`file-picker`](file-picker) records the Android picker stack and a semantic-search design based on rebuildable metadata/OCR/embedding indexes rather than forcing meaning into filenames or folders.
