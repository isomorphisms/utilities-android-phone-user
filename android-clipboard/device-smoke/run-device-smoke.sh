#!/bin/sh
set -eu

root=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)
adb=${ADB:-adb}
package=com.isomorphisms.clipboardsmoke
component="$package/android.app.NativeActivity"
apk=${1:-}

if [ -z "$apk" ]; then
    apk=$(sh "$root/build-apk.sh")
fi

"$adb" get-state >/dev/null
"$adb" uninstall "$package" >/dev/null 2>&1 || true
"$adb" logcat -c
"$adb" install "$apk" >/dev/null
"$adb" shell am start -W -n "$component" >/dev/null
sleep 1

receipt=$($adb logcat -d -s ClipboardSmoke:I '*:S')
printf '%s\n' "$receipt"
printf '%s\n' "$receipt" | grep -F 'CLIPBOARD_SMOKE PASS' >/dev/null
printf '%s\n' 'PASS: foreground Android ClipboardManager smoke receipt'
