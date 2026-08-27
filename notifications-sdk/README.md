# Android notifications SDK target

This branch records the **public Android 14 / API level 34 platform notification contract** as a possible Idriç Android backend target. It deliberately distinguishes that supported SDK contract from both hidden framework Binder interfaces and the AndroidX `NotificationCompat` compatibility library.

The goal is architectural completeness rather than a minimal tutorial. The files in this directory enumerate the public call families that an application can use to construct, post, inspect, cancel, organize, listen to, and control interruption behavior for notifications on API 34.

## Boundary

Included here are the app-facing call surfaces centered on:

- `android.app.Notification` and its builders, actions, styles, bubble metadata, and extenders;
- `android.app.NotificationManager`;
- `android.app.NotificationChannel` and `NotificationChannelGroup`;
- `android.app.AutomaticZenRule` and notification policy / interruption-filter operations;
- `android.service.notification.NotificationListenerService`;
- `android.app.RemoteInput`, `android.app.Person`, and the notification-specific operations on their builders;
- notification listener result objects such as `StatusBarNotification` and ranking objects where they are part of the callback contract.

Not included in the meaning of "public SDK" are `@SystemApi`, hidden framework methods, private Binder interfaces, vendor extensions, or post-API-34 additions. AndroidX is useful and may deserve a separate target, but it is not the Android platform ABI being pinned here.

## Files

- [`notification-manager.md`](notification-manager.md) — posting, cancellation, inspection, delegation, channels, Do Not Disturb, automatic Zen rules, bubbles, and full-screen-intent capability.
- [`notification-construction.md`](notification-construction.md) — every API-34 `Notification.Builder` operation, actions, styles, bubbles, remote input, and people.
- [`channels.md`](channels.md) — notification channels and channel groups.
- [`listener-service.md`](listener-service.md) — notification-listener callbacks and control operations.
- [`inventory.md`](inventory.md) — compact name-only inventory useful when implementing bindings or checking coverage.

## Backend rule

A device-independent Idriç interface should describe semantic operations such as `post`, `cancel`, `channel`, or `observe`. The API-34 backend may then lower those operations to this SDK. A separate Binder target can coexist with it. Having both is useful: the SDK target gives us Android's supported compatibility contract while the Binder target exposes the machinery underneath it.
