# Notification channels and groups — API 34

Channels are persistent user-visible notification policy objects. Their central design fact is that an application proposes channel behavior, but after creation Android and the user own important parts of that behavior. A backend should therefore read back actual channel state rather than assume repeated setters can override user decisions.

## `NotificationChannel`

### `NotificationChannel(String id, CharSequence name, int importance)`
Creates a channel definition with a stable id, user-visible name, and initial importance. The id is the durable identity used by posted notifications; changing the name does not create a new channel.

### `getId()`
Returns the immutable channel id.

### `getName()` / `setName(CharSequence)`
Read or propose the user-visible channel name. Name is presentation metadata and may be updated without changing the channel identity.

### `getDescription()` / `setDescription(String)`
Read or set explanatory text shown in channel settings.

### `getImportance()` / `setImportance(int)`
Read or propose channel importance. Before channel creation the setter defines initial policy; once installed, user/system ownership means applications cannot assume a later call will override the effective importance.

### `getOriginalImportance()`
Returns the channel's original application-supplied importance, distinct from its possibly user-modified effective state.

### `hasUserSetImportance()`
Reports whether the user has explicitly changed importance. This lets an application distinguish its initial choice from user policy.

### `getSound()` / `setSound(Uri, AudioAttributes)`
Read or propose the channel sound and audio attributes. On modern Android, sound belongs to the channel rather than to each posted notification.

### `getAudioAttributes()`
Returns the audio attributes associated with the channel sound.

### `hasUserSetSound()`
Reports whether the user has explicitly chosen the sound setting.

### `enableVibration(boolean)` / `shouldVibrate()`
Enable or query channel vibration policy.

### `setVibrationPattern(long[])` / `getVibrationPattern()`
Set or retrieve the channel's vibration pattern. The system/user may constrain actual device behavior.

### `enableLights(boolean)` / `shouldShowLights()`
Enable or query notification-light behavior for hardware that exposes such a light.

### `setLightColor(int)` / `getLightColor()`
Set or retrieve the requested notification-light color.

### `setShowBadge(boolean)` / `canShowBadge()`
Set or query whether notifications from the channel may contribute launcher badges.

### `setBypassDnd(boolean)` / `canBypassDnd()`
Set or query whether the channel may bypass Do Not Disturb. This is subject to notification-policy authority and user/system policy; it is not an unconditional application privilege.

### `setLockscreenVisibility(int)` / `getLockscreenVisibility()`
Set or retrieve channel-level lock-screen visibility behavior.

### `setGroup(String)` / `getGroup()`
Associate the channel with a channel-group id or retrieve that association. The group organizes settings; notifications still target the channel id.

### `setAllowBubbles(boolean)` / `canBubble()`
Propose or query whether notifications from this channel may bubble. Application-level and user-level bubble settings also participate in the final decision.

### `setConversationId(String parentChannelId, String conversationId)`
Marks this channel as a conversation-specific child of a parent channel and associates a stable conversation id.

### `isConversation()`
Reports whether the channel is a conversation channel.

### `getParentChannelId()` / `getConversationId()`
Return the parent channel and conversation identifiers for a conversation channel.

### `isImportantConversation()`
Reports whether Android/user policy marks this conversation as important.

### `isDemoted()`
Reports whether the conversation has been demoted from conversation treatment.

### `setBlockable(boolean)` / `isBlockable()`
Control/query whether a channel associated with a fixed or otherwise special notification context may be blocked by the user where Android exposes that distinction.

### `describeContents()` / `writeToParcel(Parcel,int)`
Parcelable machinery required when a channel crosses process boundaries. An Idriç SDK binding normally lets the framework handle this rather than implementing the Parcel format itself; the direct Binder target is where exact parceling becomes relevant.

### `equals(Object)` / `hashCode()` / `toString()`
Ordinary Java value/object operations. They are public members and therefore part of the class surface, although they do not perform notification-service operations.

## `NotificationChannelGroup`

Channel groups organize channels in settings and can themselves be blocked. The API-34 public family consists of construction plus identity/name/description access, channel inspection, and blocked-state inspection.

### `NotificationChannelGroup(String id, CharSequence name)`
Creates a group definition with a stable id and user-visible name.

### `getId()`
Returns the group id.

### `getName()` / `setName(CharSequence)`
Read or update the user-visible group name.

### `getDescription()` / `setDescription(String)`
Read or update descriptive settings text for the group.

### `getChannels()`
Returns channels associated with the group in the returned system representation. Channel membership itself is assigned through `NotificationChannel.setGroup` before registration.

### `isBlocked()`
Reports whether the user/system has blocked the group. A blocked group prevents its channels from producing visible notifications regardless of an individual channel's otherwise permissive settings.

### `describeContents()` / `writeToParcel(Parcel,int)` / `equals(Object)` / `hashCode()` / `toString()`
Parcelable and ordinary Java object operations. They complete the public class surface but are not separate service requests.

## Lifecycle through `NotificationManager`

Channels and groups become system state only through `NotificationManager.createNotificationChannel(s)` and `createNotificationChannelGroup(s)`. Deletion also occurs through `NotificationManager`. Constructing or mutating a Java channel object by itself does not alter installed Android notification policy.
