# `NotificationListenerService` — API 34 public calls

A notification listener is qualitatively different from an application posting its own notifications. It is a bound service granted special user-controlled access to observe and, within the supported contract, manipulate notifications belonging to other applications. The service must be declared with the notification-listener binding permission and should treat `onListenerConnected()` / `onListenerDisconnected()` as the boundary around most usable operations.

System-only methods are deliberately excluded below even when they are visible in AOSP source. For example, the `snoozeNotification(String,String)` criterion overload is `@SystemApi`; the public SDK exposes the duration-based overload.

## Binding and lifecycle

### `onBind(Intent intent)`
Returns the framework Binder used by Android to connect to the listener service. Applications normally inherit the framework implementation rather than replacing the Binder protocol themselves.

### `onListenerConnected()`
Called when the listener has become connected and the notification-listener operations are safe to use. This is the normal point to query initial state.

### `onListenerDisconnected()`
Called when the listener loses its connection. Most listener methods should no longer be used until a later connection callback.

### `onDestroy()`
Normal Android service destruction callback. A binding should release application-side resources here but should not confuse service destruction with individual notification removal.

### `requestRebind(ComponentName componentName)`
Static request asking Android to rebind a listener component that was previously disconnected/unbound. The AOSP contract specifically allows this request outside the normal connected interval.

### `requestUnbind()`
Requests that this listener be unbound. This is a deliberate lifecycle transition rather than a temporary failure.

## Posted/removed/ranking callbacks

### `onNotificationPosted(StatusBarNotification sbn)`
Called when a notification becomes visible to this listener. `StatusBarNotification` contains both the original `Notification` and identifying/source information such as package, tag, id, user, and notification key.

### `onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap)`
Richer posting callback including a ranking snapshot. Prefer this form when the consumer needs importance/ranking/channel/conversation information at the moment the notification is delivered.

### `onNotificationRemoved(StatusBarNotification sbn)`
Called when a previously visible notification is removed.

### `onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap)`
Removal callback with the current ranking map.

### `onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason)`
Removal callback that also supplies Android's cancellation reason. API 34 exposes reason constants covering user clicks/dismissals, app cancellation, listener cancellation, channel/package changes, snoozing, timeout, lockdown, and related system causes. A backend should preserve this reason rather than reduce every removal to a Boolean disappearance.

### `onNotificationRankingUpdate(RankingMap rankingMap)`
Called when ranking information changes independently of a new post/removal. A listener that exposes ranking to higher layers must therefore treat ranking as mutable state rather than immutable metadata captured at post time.

## Listener connection policy callbacks

### `onListenerHintsChanged(int hints)`
Reports changes in listener hints concerning suppression of host notification/call effects. Hints do not themselves change the global interruption filter.

### `onInterruptionFilterChanged(int interruptionFilter)`
Reports changes to the current interruption filter (all, priority, none, alarms, etc.).

### `onSilentStatusBarIconsVisibilityChanged(boolean hideSilentStatusIcons)`
Reports changes to the policy for hiding silent-notification status-bar icons.

## Channel/group callbacks

### `onNotificationChannelModified(String pkg, UserHandle user, NotificationChannel channel, int modificationType)`
Called when a channel visible to the listener is added, updated, or deleted. The modification-type constants preserve the difference between those three events.

### `onNotificationChannelGroupModified(String pkg, UserHandle user, NotificationChannelGroup group, int modificationType)`
Equivalent callback for channel-group additions, updates, and deletions.

## Querying visible notifications

### `getActiveNotifications()`
Returns all currently active notifications available to the listener.

### `getActiveNotifications(String[] keys)`
Returns the active notifications identified by the supplied notification keys. Keys, unlike `(package,tag,id)`, are system-generated identities suitable for listener operations.

### `getSnoozedNotifications()`
Returns notifications currently snoozed and visible to the listener.

### `getCurrentRanking()`
Returns the current `RankingMap`. Ranking is meaningful only in relation to notification keys; consumers ask the map to populate/read a `Ranking` object for each key.

### `getCurrentListenerHints()`
Returns the listener hints currently in effect.

### `getCurrentInterruptionFilter()`
Returns the current interruption-filter value seen by this listener.

### `getNotificationChannels(String pkg, UserHandle user)`
Returns notification channels for the named package/user that the listener is allowed to inspect.

### `getNotificationChannelGroups(String pkg, UserHandle user)`
Returns notification channel groups for the named package/user.

## Cancelling and snoozing

### `cancelNotification(String key)`
Requests cancellation of the active notification identified by its listener key.

### `cancelNotifications(String[] keys)`
Requests cancellation of several keyed notifications in one operation. Passing `null` has broad semantics in some platform versions; a binding should not use it as an accidental stand-in for an explicit cancel-all operation.

### `cancelNotification(String pkg, String tag, int id)`
Deprecated older cancellation API using package/tag/id rather than the globally meaningful listener key. It remains in the API-34 public surface for compatibility.

### `cancelAllNotifications()`
Requests cancellation of all notifications the listener is permitted to cancel. This is intentionally broad and should have an explicit semantic name at higher layers.

### `snoozeNotification(String key, long durationMs)`
Snoozes the keyed notification for a duration. The notification may later return through the ordinary posted callback.

## Reporting notification visibility

### `setNotificationsShown(String[] keys)`
Tells Android that the supplied notifications have been shown to the user by the listener/companion surface. This is reporting state back to the notification service, not changing the notification's content.

## Requesting host policy

### `requestListenerHints(int hints)`
Requests listener hints such as suppression of notification or call effects. The effective state can later be read through `getCurrentListenerHints()` and observed with `onListenerHintsChanged`.

### `clearRequestedListenerHints()`
Clears hints previously requested by this listener.

### `requestInterruptionFilter(int interruptionFilter)`
Requests a global interruption-filter mode from the listener context. Android policy and access remain authoritative.

## Conversation-channel operations

### `createConversationNotificationChannelForPackage(String pkg, UserHandle user, String parentChannelId, String conversationId)`
Creates a conversation-specific channel for the supplied package/user from a parent channel where the listener has the required authority. This is a specialized cross-package operation associated with notification-listener privileges.

### `deleteConversationNotificationChannel(String pkg, UserHandle user, String channelId)`
Deletes a conversation channel through the listener service where permitted.

## Ranking objects

`NotificationListenerService.RankingMap` and `Ranking` are value/query objects rather than system-service endpoints, but they are part of the listener call contract. A binding should expose at least ordered keys plus `getRanking(key, outRanking)` and the API-34 `Ranking` getters for rank, importance, explanation, suppressed visual effects, channel, snooze criteria, user sentiment, conversation/shortcut state, smart actions/replies, bubble capability, suspension state, and related ranking metadata. They are intentionally kept subordinate to the listener service here instead of being mistaken for independent Android services.
