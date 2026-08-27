# Android telephony SDK target

This branch records the **public Android 14 / API level 34 platform telephony contract** as an Android backend target. It is intentionally separate from the direct Binder target: this branch treats the public SDK as the supported contract Android promises applications, while a Binder backend may separately target the service protocols underneath it.

## Scope

The operational inventory covers the public API-34 application-facing call families centered on:

- `android.telephony.TelephonyManager`;
- `android.telephony.SmsManager`;
- `android.telephony.SubscriptionManager`;
- `android.telephony.TelephonyCallback` and legacy `PhoneStateListener`;
- network/cell scanning and callback interfaces returned or consumed by those managers;
- public operational telephony subpackages where they represent independent services/managers rather than passive records.

The `android.telephony` namespace also contains a large set of Parcelable/value classes (`CellInfo*`, `CellIdentity*`, `SignalStrength`, `ServiceState`, `NetworkRegistrationInfo`, `PhysicalChannelConfig`, `BarringInfo`, and many others). Their getters describe returned data but are not independent system-service/API calls. They are catalogued as value families rather than mixed into the operation list.

Hidden framework interfaces, `@SystemApi`, carrier/system-only calls, vendor extensions, and methods introduced after API 34 are outside this branch's public-SDK boundary.

## Files

- [`sms-manager.md`](sms-manager.md) — complete public API-34 SMS/MMS operation family and the non-destructive test boundary.
- [`telephony-manager.md`](telephony-manager.md) — public `TelephonyManager` operational families.
- [`subscriptions.md`](subscriptions.md) — `SubscriptionManager` operations.
- [`callbacks.md`](callbacks.md) — `TelephonyCallback`, legacy listener, cell-info, scan, and asynchronous result families.
- [`package-map.md`](package-map.md) — value/data families and related telephony subpackages.
- [`safety.md`](safety.md) — privacy and irreversible-operation rules for a public repository.
- [`inventory.md`](inventory.md) — compact implementation checklist.

## Implementation stance

A backend should not turn every telephony method into one undifferentiated primitive. There are useful semantic boundaries: read device/network state, observe state, inspect subscriptions/SIMs, scan networks, issue carrier/UICC operations, prepare/send SMS, and perform privileged mutations. Keeping those families explicit makes it possible to support harmless read-only work before enabling irreversible or privacy-sensitive operations.
