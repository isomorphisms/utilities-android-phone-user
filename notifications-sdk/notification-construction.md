# Constructing notifications — API 34

`Notification.Builder` is the supported platform construction surface. Most methods mutate builder state and return the builder, so an Idriç binding can model them as pure record updates internally and perform the actual Java/SDK calls only at the Android boundary if that produces a cleaner language interface.

## `Notification.Builder` constructors

### `Notification.Builder(Context context)`
Legacy constructor predating mandatory notification channels. It remains public in API 34 but is deprecated; applications targeting modern Android should normally construct with an explicit channel id.

### `Notification.Builder(Context context, String channelId)`
Creates a builder associated with a notification channel. The channel id is not merely presentation metadata: on modern Android, channel settings govern importance, sound, vibration, lights, badges, and other user-controlled behavior.

## Basic content and identity

### `setSmallIcon(int icon)` / `setSmallIcon(int icon, int level)` / `setSmallIcon(Icon icon)`
Set the required small status icon. The resource overloads are convenient for ordinary APK resources; the `Icon` overload permits other supported icon representations. The level overload is for level-list drawables.

### `setContentTitle(CharSequence title)`
Sets the principal title text.

### `setContentText(CharSequence text)`
Sets the principal body text.

### `setSubText(CharSequence text)`
Adds secondary contextual text. System templates decide exactly where it appears.

### `setContentInfo(CharSequence info)`
Sets supplementary information in templates that expose an information field. Modern templates may give this less prominence than older Android versions.

### `setNumber(int number)`
Associates a number with the notification, historically used as a count and still available to system templates/launchers.

### `setWhen(long timestamp)`
Sets the timestamp associated with the notification.

### `setShowWhen(boolean show)`
Controls whether the system template displays the `when` timestamp.

### `setUsesChronometer(boolean useChronometer)`
Requests a running chronometer based on the notification timestamp instead of a static time display.

### `setChronometerCountDown(boolean countDown)`
When a chronometer is in use, requests countdown rather than count-up behavior.

## Intents and lifetime

### `setContentIntent(PendingIntent intent)`
Sets the action launched when the user taps the notification body.

### `setDeleteIntent(PendingIntent intent)`
Sets an action invoked when the user explicitly dismisses the notification. It is not the same as the content action.

### `setFullScreenIntent(PendingIntent intent, boolean highPriority)`
Associates a full-screen action for exceptional high-urgency cases. API 34 separately exposes `NotificationManager.canUseFullScreenIntent()` because use of this feature is restricted.

### `setAutoCancel(boolean autoCancel)`
Requests automatic cancellation when the user taps the notification.

### `setOngoing(boolean ongoing)`
Marks the notification as associated with an ongoing operation. Ongoing status affects dismissal/presentation semantics but should not be treated as an unconditional guarantee that a notification can never be removed.

### `setTimeoutAfter(long durationMs)`
Requests automatic cancellation after the given duration.

### `setOnlyAlertOnce(boolean onlyAlertOnce)`
When updating an existing notification, suppresses repeated alerting behavior after the initial alert.

## Appearance and template data

### `setLargeIcon(Bitmap icon)` / `setLargeIcon(Icon icon)`
Sets the larger content icon shown by templates that support it.

### `setColor(int color)`
Provides the notification's accent color. Android may apply it selectively according to template and system policy.

### `setColorized(boolean colorize)`
Requests broader colorization of the notification surface. Android restricts when this is honored, particularly around foreground-service or media presentation.

### `setVisibility(int visibility)`
Sets lock-screen visibility policy using the public visibility constants. This expresses sensitivity/presentation intent; the system and user settings remain authoritative.

### `setPublicVersion(Notification notification)`
Supplies an alternate public-safe notification that can be shown when the primary notification's content should be concealed on a secure lock screen.

### `setBadgeIconType(int iconType)`
Controls which icon form is suggested for launcher badging where supported.

### `setSettingsText(CharSequence text)`
Provides text associated with notification settings affordances when the system template displays them.

## Legacy alert controls

### `setSound(Uri sound)` / `setSound(Uri sound, int streamType)` / `setSound(Uri sound, AudioAttributes attributes)`
Set a per-notification sound using legacy notification-level configuration. On channel-based Android versions, channel sound configuration generally controls alert behavior, so these methods are most relevant to compatibility with older behavior.

### `setVibrate(long[] pattern)`
Sets a legacy per-notification vibration pattern. Channel vibration settings take precedence on modern Android.

### `setLights(int argb, int onMs, int offMs)`
Sets legacy notification LED behavior. Channel light settings govern modern channel-based notifications and hardware may not expose an LED at all.

### `setDefaults(int defaults)`
Requests combinations of default sound, vibration, and lights through the legacy notification defaults bitmask.

### `setPriority(int priority)`
Sets legacy pre-channel notification priority. API-26+ alert importance is principally a channel property, but the method remains in the API-34 surface for compatibility.

## Progress

### `setProgress(int max, int progress, boolean indeterminate)`
Adds progress state to system templates. With `indeterminate=false`, `progress/max` expresses bounded progress; with `true`, the values are not interpreted as determinate completion.

## Grouping and sorting

### `setGroup(String groupKey)`
Assigns the notification to a notification group.

### `setGroupSummary(boolean isGroupSummary)`
Marks this notification as the summary for its group.

### `setSortKey(String sortKey)`
Provides a lexical ordering key among notifications within the same group when the system uses application-supplied ordering.

### `setGroupAlertBehavior(int behavior)`
Controls whether alerting is associated with the group summary, group children, or all applicable members.

### `setLocalOnly(boolean localOnly)`
Requests that bridges/companion devices not forward the notification away from the local device.

## Categories and system context

### `setCategory(String category)`
Assigns one of Android's semantic notification categories, such as message, call, alarm, transport, or service. Category may affect system ranking and presentation but does not replace a channel.

### `setShortcutId(String shortcutId)`
Associates the notification with a published shortcut, important for conversation and launcher integration.

### `setLocusId(LocusId locusId)`
Associates the notification with a locus/context identifier used by Android contextual systems.

### `setForegroundServiceBehavior(int behavior)`
Supplies the requested display behavior for a notification associated with a foreground service, within Android's foreground-service policy.

### `setAllowSystemGeneratedContextualActions(boolean allowed)`
Controls whether Android may add contextual actions generated by the system.

## People and conversation context

### `addPerson(String uri)`
Legacy form associating a person URI with the notification.

### `addPerson(Person person)`
Associates a structured `Person` object with the notification. This gives Android richer identity/contact information than the older string URI form.

### `setRemoteInputHistory(CharSequence[] history)`
Supplies recent remote-input replies for system display. It is presentation history rather than a message-store API.

## Actions

### `addAction(int icon, CharSequence title, PendingIntent intent)`
Legacy convenience overload for appending an action from a resource icon, title, and pending intent.

### `addAction(Notification.Action action)`
Appends a fully constructed action, including remote input, semantic action metadata, authentication requirements, and other action properties.

### `setActions(Notification.Action... actions)`
Replaces the builder's action set with the supplied actions.

## Custom layouts

### `setContent(RemoteViews views)`
Legacy custom content-view setter.

### `setCustomContentView(RemoteViews views)`
Supplies a custom collapsed notification layout while retaining Android's notification framework around it.

### `setCustomBigContentView(RemoteViews views)`
Supplies the custom expanded layout.

### `setCustomHeadsUpContentView(RemoteViews views)`
Supplies the custom heads-up layout.

### `createContentView()`
Builds the system-generated collapsed `RemoteViews` for the builder's current state.

### `createBigContentView()`
Builds the system-generated expanded `RemoteViews`, if the current style/state has one.

### `createHeadsUpContentView()`
Builds the system-generated heads-up `RemoteViews`, if applicable.

## Style, bubbles, extras, and extension

### `setStyle(Notification.Style style)`
Attaches a notification style such as big text, messaging, media, call, or inbox. Styles are structured template strategies, not arbitrary rendering hooks.

### `getStyle()`
Returns the style currently attached to the builder.

### `setBubbleMetadata(Notification.BubbleMetadata data)`
Associates bubble launch/display metadata. Whether the notification actually bubbles remains subject to channel/application/user policy.

### `setExtras(Bundle extras)`
Replaces the additional extras bundle carried by the notification.

### `addExtras(Bundle extras)`
Merges additional entries into the builder extras.

### `getExtras()`
Returns the builder's extras bundle for inspection/modification.

### `extend(Notification.Extender extender)`
Lets a public notification extender add its platform-specific metadata to the builder. API 34 includes wearable and car-oriented extenders.

### `setFlag(int mask, boolean value)`
Sets or clears a raw `Notification` flag bit. This exposes lower-level notification flag control beneath convenience methods such as `setOngoing` and `setAutoCancel`.

### `setChannelId(String channelId)`
Changes the channel id associated with the notification under construction. On modern Android this must refer to a channel known to `NotificationManager` before posting.

## Build/recovery

### `build()`
Produces the `Notification` value from the builder's current state.

### `getNotification()`
Deprecated predecessor to `build()`. It remains public in API 34 for compatibility.

### `recoverBuilder(Context context, Notification notification)`
Creates a builder initialized from an existing notification, useful when a framework/client needs to inspect or modify a previously built notification through builder semantics.

## `Notification.Action.Builder`

### Constructors
`Action.Builder(int, CharSequence, PendingIntent)`, `Action.Builder(Icon, CharSequence, PendingIntent)`, and `Action.Builder(Notification.Action)` create an action builder from legacy icon data, a structured `Icon`, or an existing action respectively.

### `addRemoteInput(RemoteInput remoteInput)`
Adds an inline-input specification to the action, enabling reply/input UI where supported.

### `addExtras(Bundle extras)` / `getExtras()`
Add or inspect arbitrary action-level extras.

### `setAllowGeneratedReplies(boolean allow)`
Declares whether system-generated replies may be offered for the action.

### `setSemanticAction(int semanticAction)`
Labels the action with a platform semantic such as reply, mark-as-read, delete, archive, mute, call, or similar defined action constants. This gives Android meaning beyond the displayed title.

### `setContextual(boolean contextual)`
Marks the action as context-dependent. Contextual actions have additional platform constraints, including requiring an executable intent.

### `setAuthenticationRequired(boolean authenticationRequired)`
Requires device authentication before the pending intent associated with the action is launched.

### `extend(Notification.Action.Extender extender)`
Applies action-specific extension metadata.

### `build()`
Produces the immutable `Notification.Action`.

## Notification styles in API 34

### `Notification.BigTextStyle`
Represents a notification whose expanded form contains a larger block of text. Its family includes `bigText`, `setBigContentTitle`, and `setSummaryText`.

### `Notification.BigPictureStyle`
Represents an expanded large-image notification. Its family includes `bigPicture` overloads, `bigLargeIcon` overloads, `setBigContentTitle`, `setSummaryText`, `showBigPictureWhenCollapsed`, and API-31+ content-description/cropping behavior available in the API-34 class.

### `Notification.InboxStyle`
Represents a multi-line inbox-like notification. `addLine` appends lines; `setBigContentTitle` and `setSummaryText` provide expanded-template context.

### `Notification.MessagingStyle`
Represents a structured conversation. It can add `Message` and historic-message objects, set conversation title/group-conversation state, and exposes the associated user and message collections. This is preferable to flattening a conversation into one text blob when Android should understand message semantics.

### `Notification.MediaStyle`
Provides media-session notification integration, including compact-view action selection and a `MediaSession.Token`.

### `Notification.DecoratedCustomViewStyle`
Lets custom `RemoteViews` participate in the standard decorated notification shell rather than replacing the whole notification presentation contract.

### `Notification.DecoratedMediaCustomViewStyle`
Combines decorated custom views with media-style behavior.

### `Notification.CallStyle`
Provides the API-31+ incoming, ongoing, and screening call templates. The static factories `forIncomingCall`, `forOngoingCall`, and `forScreeningCall` define the three supported call states; builder-like methods add verification data and optional answer/decline color hints.

## Bubbles

### `Notification.BubbleMetadata.Builder(PendingIntent, Icon)`
Creates metadata for a bubble launched by a pending intent with a supplied icon.

### `Notification.BubbleMetadata.Builder(String shortcutId)`
Creates bubble metadata associated with a conversation shortcut.

### `setAutoExpandBubble(boolean)`
Requests that the bubble open automatically when posted, subject to system policy.

### `setDeleteIntent(PendingIntent)`
Sets an action for explicit deletion of the bubble.

### `setDesiredHeight(int)` / `setDesiredHeightResId(int)`
Request the bubble's expanded height in pixels or by resource. Android may constrain the resulting geometry.

### `setIcon(Icon)` / `setIntent(PendingIntent)`
Set the bubble icon or launch intent for builder forms where those values are mutable.

### `setSuppressNotification(boolean)`
Requests suppression of the ordinary notification presentation while the bubble represents the content.

### `build()`
Produces `BubbleMetadata`.

## `RemoteInput.Builder`

`RemoteInput` describes user input attached to a notification action. Its builder family includes construction by result key, `setLabel`, `setChoices`, `setAllowFreeFormInput`, `setAllowDataType`, `addExtras`, `getExtras`, `setEditChoicesBeforeSending`, and `build`. Static `RemoteInput` helpers move text/data results between intents and clip data; a backend implementing inline replies needs these helpers as part of the action-result protocol, not just the builder.

## `Person.Builder`

The structured person family includes `setName`, `setIcon`, `setUri`, `setKey`, `setBot`, `setImportant`, and `build`. `Person` is reused elsewhere in Android, but notification conversation ranking and messaging styles are major consumers of this structured identity.
