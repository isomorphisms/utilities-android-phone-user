# utilities-android-phone-user

Small Android utilities intended for direct use on a phone.

Each utility lives in its own folder and should remain independently buildable where practical.

[`math-characters`](math-characters) is **Programmer's Unicode Picker**: an Idriç-owned, standalone copy/paste picker whose Android APK remains native, DEX-free, and separate from the system keyboard.

[`text-pad`](text-pad) records the product direction and phone user stories for a deliberately small plain-text editor. It is a design note rather than an implementation today.

[`codex-meter`](codex-meter) is a Termux/tmux-friendly Codex quota meter. Its installer puts a tiny command wrapper in the Termux prefix and points that wrapper back at the repository checkout so normal `git pull` updates change the live meter immediately.
