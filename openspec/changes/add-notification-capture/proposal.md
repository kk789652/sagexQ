## Why

SageX's entire value depends on observing WeChat conversations, yet Android gives third-party apps no API to read chat history. The only sanctioned channel is the system notification stream via `NotificationListenerService`. Without a reliable capture-and-store foundation, no downstream feature (classification, AI summary, sharing) can exist. This change establishes that foundation: listen to WeChat notifications, parse them into structured messages, and persist them locally.

## What Changes

- Add a `NotificationListenerService` that receives posted notifications and filters to WeChat (`com.tencent.mm`).
- Parse each WeChat notification into a structured message: conversation key, conversation type (group vs. direct), conversation title, sender name, body text, timestamp.
- Persist parsed messages to a local Room database (permanent retention, per product decision).
- Add an in-app onboarding flow that detects whether notification access is granted and deep-links the user to the system "Notification access" settings screen to grant it.
- Surface capture status (access granted / denied, message count, last captured time) on the home screen so the user can confirm it is working.
- Gracefully handle the known limits of the notification channel (collapsed/aggregated notifications, muted chats) by recording what is available and skipping/marking what is not — never crash on unexpected formats.

Out of scope for this change (handled by later changes): keeping the process alive in the background (`add-background-resilience`), the conversation list/detail UI (`add-conversation-list`), and AI summarization (`add-ai-summary`).

## Capabilities

### New Capabilities
- `notification-capture`: Receiving WeChat system notifications, gating on the notification-access permission, and parsing raw notifications into structured messages.
- `message-storage`: Local persistence layer for captured messages, including the schema, conversation identity/keys, and basic query/count access.

### Modified Capabilities
<!-- None: this is the first feature change; no existing specs. -->

## Impact

- **New code**: `NotificationListenerService` implementation; notification parser; Room database, entities, DAOs; repository layer; permission-status helper; home-screen status UI.
- **Manifest**: declare the listener service with `BIND_NOTIFICATION_LISTENER_SERVICE` permission and the `service.notification.NotificationListenerService` intent filter.
- **Dependencies**: Room (already in the build), Hilt modules for DB/repository wiring (already present).
- **Permissions**: requires the user to manually grant "Notification access" (cannot be requested via a runtime dialog).
- **Privacy**: message content is stored unencrypted on-device only; no network egress in this change.
- **Risk**: WeChat notification formats vary by locale/version and can be aggregated; the parser must degrade gracefully and is the main area to test.
