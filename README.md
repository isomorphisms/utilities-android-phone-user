# utilities-android-phone-user

Small Android utilities intended for direct use on a phone.

Each utility lives in its own folder and should remain independently buildable where practical.

[`math-characters`](math-characters) is **Programmer's Unicode Picker**: an Idriç-owned, standalone copy/paste picker whose Android APK remains native, DEX-free, and separate from the system keyboard.

[`text-pad`](text-pad) records the product direction and phone user stories for a deliberately small plain-text editor. It is a design note rather than an implementation today.

[`llm-training`](llm-training) is a draft phone-first collector for pairwise preferences, ideal responses, critique-and-rewrite examples, and response rankings, with explicit trainer usernames and append-only GitHub-backed records.
