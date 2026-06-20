## 1. Domain model & parser (pure, testable)

- [x] 1.1 Define `ConversationType` enum (`GROUP`, `DIRECT`, `UNKNOWN`) and a `CapturedMessage` domain model (sourcePackage, conversationKey, conversationType, conversationTitle, sender, body, postedAt)
- [x] 1.2 Add `conversationKey(sourcePackage, type, title)` helper (stable hash) used by both parser and storage
- [x] 1.3 Define `NotificationParser` interface returning a sealed `ParseResult` (`Parsed`, `ContentUnavailable`, `Ignored`)
- [x] 1.4 Implement `WeChatNotificationParser` covering: zh group (`群名` / `发送者: 内容`), zh direct (`人名` / `内容`), en formats, aggregated counts → `ContentUnavailable`, empty/unknown → `Ignored`, with `EXTRA_LINES`/`tickerText` fallback

## 2. Storage layer (Room)

- [x] 2.1 Create `MessageEntity` mapped to a `messages` table and a mapper to/from `CapturedMessage`
- [x] 2.2 Create `MessageDao` with insert, count, ordered query by conversation, distinct-conversation query, and `clearAll`
- [x] 2.3 Create `SageXDatabase` (Room, version 1, `exportSchema = true`) and a Hilt module providing the DB and DAO
- [x] 2.4 Create `MessageRepository` exposing suspend `save(message)`, `Flow<Int>` total count, ordered queries, and `clearAll`, running DB work on `Dispatchers.IO`

## 3. Notification capture service

- [x] 3.1 Implement `SageXNotificationListenerService` extending `NotificationListenerService`, filtering to `com.tencent.mm`
- [x] 3.2 In `onNotificationPosted`, extract notification extras synchronously, run the parser, and dispatch non-ignored results to the repository on a coroutine scope
- [x] 3.3 Register the service in `AndroidManifest.xml` with `BIND_NOTIFICATION_LISTENER_SERVICE` and the `NotificationListenerService` intent filter

## 4. Permission gating & status UI

- [x] 4.1 Add `NotificationAccessHelper` to check whether listener access is granted and to build the settings deep-link intent
- [x] 4.2 Build a home-screen status section (Compose + ViewModel) showing access state (active/inactive), total message count, and last-captured time
- [x] 4.3 Add an "Enable capture" action that opens the system Notification-access settings screen, and a "Clear all data" action with confirmation

## 5. Tests & verification

- [x] 5.1 Unit tests for `WeChatNotificationParser` covering every scenario in the notification-capture spec (group, direct, aggregated, empty/unknown)
- [x] 5.2 Unit tests for `conversationKey` (same conversation groups; group vs direct with same title stays distinct)
- [x] 5.3 Room DAO tests (in-memory DB): persist+retrieve, ordering by timestamp, count reflects inserts, clearAll empties the table
- [x] 5.4 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-notification-capture --strict`
