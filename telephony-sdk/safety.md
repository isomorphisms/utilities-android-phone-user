# Telephony safety and privacy boundary

This repository is public. No personal telephone number belongs in source, fixtures, expected output, screenshots, issue text generated from tests, CI variables printed to logs, captured SDK traces, Binder/Parcel dumps, or example commands.

## SMS transmission

Ordinary automated tests stop before radio transmission. A useful test may:

- construct a send request;
- exercise message division/encoding;
- marshal the public-SDK arguments;
- inject a fake `SmsManager`-shaped transport in application code;
- send the text to a local notification/clipboard/paste flow instead;
- verify callbacks using a fake sender;
- compare generated protocol data using synthetic numbers reserved for documentation rather than a real personal number.

A real SMS destination has **no repository default**. If an operator deliberately tests transmission, the number must be supplied at runtime from outside Git (for example interactive input or an ignored local secret) and the program must require an explicit irreversible final action. CI never transmits SMS.

The program must not attempt to discover the device's own line number and silently use it as a test destination. Besides the privacy problem, Android/carrier provisioning does not make the device's own dialable number a reliable universal application value.

## SMS receive/store work

Read-only does not mean non-sensitive. Real SMS rows can contain phone numbers, message bodies, timestamps, OTPs, account data, and private correspondence. Repository tests should use synthetic rows or deliberately redacted local captures. No production message database snapshot belongs in Git.

## Other telephony identifiers

IMEI, MEID, IMSI/subscriber id, ICCID/SIM serial number, line number, voicemail number, carrier identifiers tied to a subscription, cell observations, and detailed network state may be identifying or permission-restricted. Diagnostic output should default to structural facts rather than raw identifiers. Tests should use synthetic values.

## Mutations

The same caution applies to network-selection changes, data enablement, SIM/UICC operations, voicemail configuration, modem reboot/configuration, premium-capability purchase, and other state-changing calls. The SDK inventory documents them because they are part of the contract; documentation is not a decision to make them part of the first executable backend.

A reasonable implementation order is read-only query → callback observation → local/fake request construction → reversible settings with explicit user action → irreversible/network-billable operations last.
