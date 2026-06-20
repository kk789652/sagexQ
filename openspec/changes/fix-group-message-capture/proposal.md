## Why

On a real device, group-chat messages are not captured while direct-chat messages are. Root cause: WeChat prefixes a conversation's notification text with a count `[N条]` (e.g. `[3条]张三: 你好`) whenever there is more than one unread message. Group chats are usually busier, so almost every group notification carries this prefix — while single chats often have just one unread and no prefix. The current parser treats any `[N条]` / `N条`-leading text as an aggregated, content-less notification and **skips it**, dropping nearly all group messages.

## What Changes

- Strip a leading count prefix (`[N条]` / `【N条】` / `N条`) from the notification text before classification, then parse the remaining `sender: content`.
- Only treat a notification as content-unavailable when, after stripping, there is no real content left (a pure summary such as `2条新消息` / `N位联系人...`).
- Add debug-only diagnostic logging that dumps the raw WeChat notification fields (title, text, text-lines, message extras) to logcat, so any other device/version format can be confirmed and handled precisely.
- Add parser tests for count-prefixed group and direct messages.

## Capabilities

### Modified Capabilities
- `notification-capture`: parsing now recovers content from count-prefixed notifications instead of discarding them.

## Impact

- **Code**: `WeChatNotificationParser` (prefix stripping); `SageXNotificationListenerService` (debug diagnostic dump); parser tests.
- **Behavior**: group messages (and multi-unread direct messages) are now captured. No data migration.
- **Risk**: low; isolated to parsing. Diagnostic logging is gated to debug builds and logs only to logcat (no new egress).
