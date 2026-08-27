# `NotificationManager` — API 34 public calls

`NotificationManager` is the main application-facing service for posting and withdrawing notifications and for administering notification channels and interruption policy. A direct SDK target should preserve the distinction between operations that affect one posted notification, operations that alter persistent channel configuration, and privileged/policy operations that Android may reject unless the application has the appropriate user-granted access.

## Posting and cancellation

### `notify(int id, Notification notification)`
Posts a notification under the application's package and integer id. Posting another notification with the same identity replaces the existing one. This is the ordinary terminal operation for a constructed `Notification`.

### `notify(String tag, int id, Notification notification)`
Posts a notification using the pair `(tag,id)` as its package-local identity. The tag is useful when one integer namespace is inconvenient or when a logical object already has a stable textual key.

### `notifyAsPackage(String targetPackage, String tag, int id, Notification notification)`
Posts as another package when that package has delegated notification posting to the caller. This is not a general impersonation mechanism: Android checks the notification-delegate relationship.

### `cancel(int id)`
Cancels the notification posted by this package with the given integer id and no tag.

### `cancel(String tag, int id)`
Cancels the package notification identified by the exact `(tag,id)` pair.

### `cancelAsPackage(String targetPackage, String tag, int id)`
Cancels a notification previously posted on behalf of a package through notification delegation. The same delegation rules that constrain `notifyAsPackage` apply.

### `cancelAll()`
Cancels all notifications owned by the calling package. It is intentionally broad and should not be used as the default implementation of a more precise semantic cancellation operation.

## Inspecting notification state

### `getActiveNotifications()`
Returns the package's currently active `StatusBarNotification` records. This is useful for reconciling application state with what Android is actually displaying rather than assuming every earlier `notify` remains active.

### `areNotificationsEnabled()`
Reports whether the package as a whole is presently allowed to post notifications. A successful call to `notify` does not itself guarantee that the user will see a notification if package notifications are disabled.

### `areNotificationsPaused()`
Reports whether notifications for the package are temporarily paused by the system, for example because of package suspension or related system policy.

### `getImportance()`
Returns the package-level notification importance assigned by Android. Channel importance remains separately relevant on channel-based Android versions.

### `shouldHideSilentStatusBarIcons()`
Reports the system's current policy for hiding status-bar icons for silent notifications. This is display-policy information rather than permission to alter the policy.

## Delegation

### `setNotificationDelegate(String delegate)`
Names another package that may post notifications on behalf of this package. Passing `null` removes the delegation. Delegation is a supported SDK relationship and is narrower than direct access to another package's notifications.

### `getNotificationDelegate()`
Returns the package currently authorized as the caller's notification delegate, or no delegate when none is configured.

### `canNotifyAsPackage(String pkg)`
Reports whether the caller is currently permitted to post notifications on behalf of the named package.

## Channels and channel groups

### `createNotificationChannel(NotificationChannel channel)`
Creates a channel or submits an updated definition for an existing channel with the same id. Android deliberately gives the user ownership over several channel choices after creation, so recreating a channel is not equivalent to resetting it.

### `createNotificationChannels(List<NotificationChannel> channels)`
Batch form of channel creation/update. It is semantically the same channel operation but reduces repeated service calls when an application defines several channels together.

### `deleteNotificationChannel(String channelId)`
Deletes the named channel. Recreating a deleted channel may restore user-visible historical behavior/settings according to Android policy; a backend should not treat delete/recreate as a reliable way to overwrite user choices.

### `getNotificationChannel(String channelId)`
Returns this package's channel with the given id, or `null` when it does not exist.

### `getNotificationChannel(String channelId, String conversationId)`
Returns the channel associated with a particular conversation where conversation-channel semantics are in use.

### `getNotificationChannels()`
Returns all notification channels belonging to the package.

### `createNotificationChannelGroup(NotificationChannelGroup group)`
Creates or updates a channel group. Groups primarily organize channels in notification settings; they do not replace channels as the unit used by a posted notification.

### `createNotificationChannelGroups(List<NotificationChannelGroup> groups)`
Batch form of group creation/update.

### `deleteNotificationChannelGroup(String groupId)`
Deletes a channel group. Android also removes channels belonging to that group, so this is materially broader than a display-only grouping operation.

### `getNotificationChannelGroup(String groupId)`
Returns the named channel group.

### `getNotificationChannelGroups()`
Returns all channel groups belonging to the package.

## Bubbles

### `areBubblesEnabled()`
Reports whether bubbles are enabled for the calling application under current system/user policy.

### `areBubblesAllowed()`
Deprecated predecessor to `areBubblesEnabled()`. It remains part of the API-34 public binary/source surface and therefore belongs in an exhaustive binding even though new code should normally prefer the newer query.

### `getBubblePreference()`
Returns the application's current bubble preference, allowing a caller to distinguish broader bubble policy states rather than reducing them to a Boolean.

## Full-screen intents

### `canUseFullScreenIntent()`
API-34 query reporting whether the application currently has authority to use full-screen notification intents. Full-screen intents are deliberately restricted because they can take over the user's display and are intended for high-priority use cases such as calls or alarms.

## Do Not Disturb / notification policy

### `isNotificationPolicyAccessGranted()`
Reports whether the user/system has granted this application notification-policy access. Policy-mutating operations should be gated on this capability rather than assumed to work.

### `getNotificationPolicy()`
Returns the current `NotificationManager.Policy` describing priority-category and related Do Not Disturb behavior visible to the caller.

### `setNotificationPolicy(NotificationManager.Policy policy)`
Requests a new notification policy. It requires notification-policy access and should be modeled as a privileged mutation rather than an ordinary notification-posting call.

### `getCurrentInterruptionFilter()`
Returns the current coarse interruption-filter mode such as all, priority, none, or alarms.

### `setInterruptionFilter(int interruptionFilter)`
Requests a change to the current interruption filter. Permission/policy access is required; the integer is one of `NotificationManager`'s defined interruption-filter constants.

### `getConsolidatedNotificationPolicy()`
Returns the effective consolidated policy after Android combines the policy contributions that determine current Do Not Disturb behavior.

### `matchesCallFilter(Uri uri)`
Asks Android whether a call from the supplied contact/person URI would be allowed through the current call-filtering policy. It lets a caller use Android's policy decision instead of reimplementing contact affinity and DND rules.

### `isNotificationListenerAccessGranted(ComponentName listener)`
Reports whether a particular notification-listener component has been granted listener access by the user/system.

## Automatic Zen rules

### `addAutomaticZenRule(AutomaticZenRule automaticZenRule)`
Adds an automatic Do Not Disturb rule and returns its system-assigned rule id. The rule describes its condition source, interruption filter, metadata, and enabled state; Android remains the authority over the installed rule.

### `getAutomaticZenRule(String id)`
Returns the automatic Zen rule with the supplied id if visible to the caller.

### `getAutomaticZenRules()`
Returns the caller-visible automatic Zen rules keyed by id.

### `updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule)`
Replaces the mutable definition of an existing automatic Zen rule. It does not create a new id.

### `removeAutomaticZenRule(String id)`
Deletes an automatic Zen rule owned/managed as permitted for the caller.

### `setAutomaticZenRuleState(String id, Condition condition)`
Supplies a current condition state for an automatic rule. This is the bridge between a condition-producing application and Android's Zen-rule state machine; it is not equivalent to globally setting the interruption filter.

## Constants are part of the binding too

An implementation also needs the public constant families used by these calls: importance values, interruption-filter values, policy priority categories/senders/conversation senders, bubble-preference values, and the fields/constants on `NotificationManager.Policy`. They are values rather than callable operations, so they are recorded conceptually here rather than mislabelled as methods.
