# API 34 notification call inventory

This is the compact checklist corresponding to the explanatory files. It is intended for binding/codegen coverage checks. Overloads are shown where they materially differ. Constructors and ordinary Parcelable/object methods are included where they are part of the public class surface discussed by this target.

## `NotificationManager`

- `notify(int, Notification)`
- `notify(String, int, Notification)`
- `notifyAsPackage(String, String, int, Notification)`
- `cancel(int)`
- `cancel(String, int)`
- `cancelAsPackage(String, String, int)`
- `cancelAll()`
- `getActiveNotifications()`
- `areNotificationsEnabled()`
- `areNotificationsPaused()`
- `getImportance()`
- `shouldHideSilentStatusBarIcons()`
- `setNotificationDelegate(String)`
- `getNotificationDelegate()`
- `canNotifyAsPackage(String)`
- `createNotificationChannel(NotificationChannel)`
- `createNotificationChannels(List<NotificationChannel>)`
- `deleteNotificationChannel(String)`
- `getNotificationChannel(String)`
- `getNotificationChannel(String, String)`
- `getNotificationChannels()`
- `createNotificationChannelGroup(NotificationChannelGroup)`
- `createNotificationChannelGroups(List<NotificationChannelGroup>)`
- `deleteNotificationChannelGroup(String)`
- `getNotificationChannelGroup(String)`
- `getNotificationChannelGroups()`
- `areBubblesAllowed()` (deprecated)
- `areBubblesEnabled()`
- `getBubblePreference()`
- `canUseFullScreenIntent()`
- `isNotificationPolicyAccessGranted()`
- `getNotificationPolicy()`
- `setNotificationPolicy(Policy)`
- `getCurrentInterruptionFilter()`
- `setInterruptionFilter(int)`
- `getConsolidatedNotificationPolicy()`
- `matchesCallFilter(Uri)`
- `isNotificationListenerAccessGranted(ComponentName)`
- `addAutomaticZenRule(AutomaticZenRule)`
- `getAutomaticZenRule(String)`
- `getAutomaticZenRules()`
- `updateAutomaticZenRule(String, AutomaticZenRule)`
- `removeAutomaticZenRule(String)`
- `setAutomaticZenRuleState(String, Condition)`

## `Notification.Builder`

- `Builder(Context)` (deprecated)
- `Builder(Context, String)`
- `addAction(int, CharSequence, PendingIntent)`
- `addAction(Notification.Action)`
- `addExtras(Bundle)`
- `addPerson(String)`
- `addPerson(Person)`
- `build()`
- `createBigContentView()`
- `createContentView()`
- `createHeadsUpContentView()`
- `extend(Notification.Extender)`
- `getExtras()`
- `getNotification()` (deprecated)
- `getStyle()`
- `recoverBuilder(Context, Notification)`
- `setActions(Notification.Action...)`
- `setAllowSystemGeneratedContextualActions(boolean)`
- `setAutoCancel(boolean)`
- `setBadgeIconType(int)`
- `setBubbleMetadata(Notification.BubbleMetadata)`
- `setCategory(String)`
- `setChannelId(String)`
- `setChronometerCountDown(boolean)`
- `setColor(int)`
- `setColorized(boolean)`
- `setContent(RemoteViews)`
- `setContentInfo(CharSequence)`
- `setContentIntent(PendingIntent)`
- `setContentText(CharSequence)`
- `setContentTitle(CharSequence)`
- `setCustomBigContentView(RemoteViews)`
- `setCustomContentView(RemoteViews)`
- `setCustomHeadsUpContentView(RemoteViews)`
- `setDefaults(int)`
- `setDeleteIntent(PendingIntent)`
- `setExtras(Bundle)`
- `setFlag(int, boolean)`
- `setForegroundServiceBehavior(int)`
- `setFullScreenIntent(PendingIntent, boolean)`
- `setGroup(String)`
- `setGroupAlertBehavior(int)`
- `setGroupSummary(boolean)`
- `setLargeIcon(Bitmap)`
- `setLargeIcon(Icon)`
- `setLights(int, int, int)`
- `setLocalOnly(boolean)`
- `setLocusId(LocusId)`
- `setNumber(int)`
- `setOngoing(boolean)`
- `setOnlyAlertOnce(boolean)`
- `setPriority(int)`
- `setProgress(int, int, boolean)`
- `setPublicVersion(Notification)`
- `setRemoteInputHistory(CharSequence[])`
- `setSettingsText(CharSequence)`
- `setShortcutId(String)`
- `setShowWhen(boolean)`
- `setSmallIcon(int)`
- `setSmallIcon(int, int)`
- `setSmallIcon(Icon)`
- `setSortKey(String)`
- `setSound(Uri)`
- `setSound(Uri, int)`
- `setSound(Uri, AudioAttributes)`
- `setStyle(Notification.Style)`
- `setSubText(CharSequence)`
- `setTicker(CharSequence)`
- `setTicker(CharSequence, RemoteViews)`
- `setTimeoutAfter(long)`
- `setUsesChronometer(boolean)`
- `setVibrate(long[])`
- `setVisibility(int)`
- `setWhen(long)`

## `Notification.Action.Builder`

- `Builder(Notification.Action)`
- `Builder(Icon, CharSequence, PendingIntent)`
- `Builder(int, CharSequence, PendingIntent)`
- `addExtras(Bundle)`
- `addRemoteInput(RemoteInput)`
- `build()`
- `extend(Notification.Action.Extender)`
- `getExtras()`
- `setAllowGeneratedReplies(boolean)`
- `setAuthenticationRequired(boolean)`
- `setContextual(boolean)`
- `setSemanticAction(int)`

## Style families

- `Notification.BigTextStyle`
- `Notification.BigPictureStyle`
- `Notification.InboxStyle`
- `Notification.MessagingStyle`
- `Notification.MessagingStyle.Message`
- `Notification.MediaStyle`
- `Notification.DecoratedCustomViewStyle`
- `Notification.DecoratedMediaCustomViewStyle`
- `Notification.CallStyle`
- `Notification.BubbleMetadata.Builder`
- `Notification.WearableExtender`
- `Notification.CarExtender`
- `RemoteInput.Builder`
- `Person.Builder`

Their operational members are described by family in `notification-construction.md`; their public getters/Parcelable methods are value-object operations rather than notification-service calls.

## `NotificationChannel`

- `NotificationChannel(String, CharSequence, int)`
- `canBubble()`
- `canBypassDnd()`
- `canShowBadge()`
- `describeContents()`
- `enableLights(boolean)`
- `enableVibration(boolean)`
- `equals(Object)`
- `getAudioAttributes()`
- `getConversationId()`
- `getDescription()`
- `getGroup()`
- `getId()`
- `getImportance()`
- `getLightColor()`
- `getLockscreenVisibility()`
- `getName()`
- `getOriginalImportance()`
- `getParentChannelId()`
- `getSound()`
- `getVibrationPattern()`
- `hasUserSetImportance()`
- `hasUserSetSound()`
- `hashCode()`
- `isBlockable()`
- `isConversation()`
- `isDemoted()`
- `isImportantConversation()`
- `setAllowBubbles(boolean)`
- `setBlockable(boolean)`
- `setBypassDnd(boolean)`
- `setConversationId(String, String)`
- `setDescription(String)`
- `setGroup(String)`
- `setImportance(int)`
- `setLightColor(int)`
- `setLockscreenVisibility(int)`
- `setName(CharSequence)`
- `setShowBadge(boolean)`
- `setSound(Uri, AudioAttributes)`
- `setVibrationPattern(long[])`
- `shouldShowLights()`
- `shouldVibrate()`
- `toString()`
- `writeToParcel(Parcel, int)`

## `NotificationChannelGroup`

- `NotificationChannelGroup(String, CharSequence)`
- `getId()`
- `getName()`
- `setName(CharSequence)`
- `getDescription()`
- `setDescription(String)`
- `getChannels()`
- `isBlocked()`
- `describeContents()`
- `writeToParcel(Parcel, int)`
- `equals(Object)`
- `hashCode()`
- `toString()`

## `NotificationListenerService`

- `onBind(Intent)`
- `onDestroy()`
- `onListenerConnected()`
- `onListenerDisconnected()`
- `onNotificationPosted(StatusBarNotification)`
- `onNotificationPosted(StatusBarNotification, RankingMap)`
- `onNotificationRemoved(StatusBarNotification)`
- `onNotificationRemoved(StatusBarNotification, RankingMap)`
- `onNotificationRemoved(StatusBarNotification, RankingMap, int)`
- `onNotificationRankingUpdate(RankingMap)`
- `onListenerHintsChanged(int)`
- `onInterruptionFilterChanged(int)`
- `onSilentStatusBarIconsVisibilityChanged(boolean)`
- `onNotificationChannelModified(String, UserHandle, NotificationChannel, int)`
- `onNotificationChannelGroupModified(String, UserHandle, NotificationChannelGroup, int)`
- `getActiveNotifications()`
- `getActiveNotifications(String[])`
- `getSnoozedNotifications()`
- `getCurrentRanking()`
- `getCurrentListenerHints()`
- `getCurrentInterruptionFilter()`
- `getNotificationChannels(String, UserHandle)`
- `getNotificationChannelGroups(String, UserHandle)`
- `cancelNotification(String)`
- `cancelNotification(String, String, int)` (deprecated)
- `cancelNotifications(String[])`
- `cancelAllNotifications()`
- `snoozeNotification(String, long)`
- `setNotificationsShown(String[])`
- `requestListenerHints(int)`
- `clearRequestedListenerHints()`
- `requestInterruptionFilter(int)`
- `createConversationNotificationChannelForPackage(String, UserHandle, String, String)`
- `deleteConversationNotificationChannel(String, UserHandle, String)`
- `requestRebind(ComponentName)`
- `requestUnbind()`

## Deliberate exclusions

This checklist excludes hidden methods, `@SystemApi`-only operations, vendor additions, AndroidX `NotificationCompat`, and APIs introduced after level 34. It also does not inflate the service-call list with every getter on passive Parcelable data classes; those are value schemas and can be inventoried independently if/when an Idriç representation for the corresponding value type is implemented.
