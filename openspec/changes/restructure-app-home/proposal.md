## Why

The app currently opens to a status/controls home with conversations one tap away and config scattered as buttons. The user wants an app-centric structure: the home lists source apps (WeChat, …); tapping one shows that app's conversations; all configuration moves into a navigation drawer. This scales to multiple sources and matches familiar messaging-app navigation.

## What Changes

- Home becomes an **app list**: one row per source app that has captured messages (dynamic, derived from stored data), showing app name, conversation/message counts, and last-activity time. Tapping a row opens that app's conversation list.
- Add a **navigation drawer** (hamburger on the home top bar) consolidating all configuration: notification-access/capture status, background keep-alive + battery/autostart, API 设置, 删除消息 (global, range), and 关于 (about + author credit).
- The conversation list is now **scoped to one app** (by source package passed via navigation).
- Remove the unread-count badge from conversation rows (per the new direction).
- Add an **About** screen showing app name, version/build, and author credit (Tis Wu) per project conventions.

Out of scope (later changes): long-press delete (`add-long-press-delete`), per-app prompt (`add-per-app-prompt`), summary message selection.

## Capabilities

### New Capabilities
- `app-list`: The home lists source apps derived from captured messages and drills into a single app's conversations.
- `config-drawer`: All configuration and an about/author screen are reached from a navigation drawer.

### Modified Capabilities
<!-- conversation-browsing is now app-scoped and drops the count badge; no new behavioral requirement beyond the app filter. -->

## Impact

- **Data**: new app-summary query (distinct source package with counts + last activity); conversation query gains a source-package filter.
- **Code**: `AppSummary` model + DAO/repo; app display-name util; `HomeScreen` rewritten as app list + drawer; `ConversationListViewModel` takes a source package; new `CaptureSettingsScreen` (status + background), `AboutScreen`; nav routes updated.
- **Risk**: moderate (navigation rewrite); behavior of capture/summary/export unchanged underneath.
