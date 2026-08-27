# Telephony safety and privacy boundary

This repository is public. No personal telephone number belongs in source, fixtures, expected output, screenshots, issue text generated from tests, CI variables printed to logs, captured SDK traces, Binder/Parcel dumps, or example commands.

## SMS transmission

Ordinary automated tests should not transmit over a real carrier network. A useful test may:

- construct a send request;
- exercise message division/encoding;
- marshal the public-SDK arguments;
- inject a fake `SmsManager`-shaped transport in application code;
- send the text to a local notification/clipboard/paste flow instead;
- verify callbacks using a fake sender;
- compare generated protocol data using synthetic numbers reserved for documentation rather than a real personal number.

## Emulator loopback / local telephony oracle

The Android Emulator gives us a substantially better acceptance oracle than a real personal phone number.

### Injected incoming SMS

The emulator console command

```text
adb emu sms send 5551234 hello
```

generates an emulated incoming SMS from an arbitrary synthetic numeric sender and passes it through the emulator's Android telephony framework to the SMS application/receivers. This is useful for receive/store testing. It does **not** exercise our outgoing `SmsManager.sendTextMessage` path.

### Emulator-to-emulator SMS

When two Android Emulator instances are running, each instance's console port acts as its simulated telephone number (commonly `5554`, `5556`, and so on). An SMS sent by one emulator to the other emulator's console-port number is automatically routed to the target emulator by the emulator telephony layer.

This is the preferred end-to-end send acceptance path because it can exercise the application's ordinary Android SMS send stack without a carrier, billable message, or real phone number. The first generated-send test should therefore target a second emulator rather than a physical handset.

The oracle should record:

1. source and target emulator serials/console ports;
2. synthetic message text;
3. sender-side `sentIntent` result;
4. target-side receipt through the SMS framework/provider or receiver;
5. no dependency on any real telephone number or carrier service.

A single-emulator `adb emu sms send` test remains useful for the receive side; the two-emulator route is the stronger send-side test.

## Real-network send

A real SMS destination has **no repository default**. If an operator deliberately tests transmission on physical hardware, the number must be supplied at runtime from outside Git (for example interactive input or an ignored local secret) and the program must require an explicit irreversible final action. CI never transmits a real carrier SMS.

The program must not attempt to discover the device's own line number and silently use it as a test destination. Besides the privacy problem, Android/carrier provisioning does not make the device's own dialable number a reliable universal application value.

## SMS receive/store work

Read-only does not mean non-sensitive. Real SMS rows can contain phone numbers, message bodies, timestamps, OTPs, account data, and private correspondence. Repository tests should use synthetic rows or deliberately redacted local captures. No production message database snapshot belongs in Git.

## Other telephony identifiers

IMEI, MEID, IMSI/subscriber id, ICCID/SIM serial number, line number, voicemail number, carrier identifiers tied to a subscription, cell observations, and detailed network state may be identifying or permission-restricted. Diagnostic output should default to structural facts rather than raw identifiers. Tests should use synthetic values.

## Mutations

The same caution applies to network-selection changes, data enablement, SIM/UICC operations, voicemail configuration, modem reboot/configuration, premium-capability purchase, and other state-changing calls. The SDK inventory documents them because they are part of the contract; documentation is not a decision to make them part of the first executable backend.

A reasonable implementation order is read-only query → callback observation → local/fake request construction → emulator telephony acceptance → reversible settings with explicit user action → real-network or otherwise irreversible/network-billable operations last.
