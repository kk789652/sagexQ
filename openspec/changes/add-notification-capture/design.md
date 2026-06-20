## Context

SageX must observe WeChat conversations, but Android exposes no API to read chat history. The only sanctioned source is the system notification stream consumed via `NotificationListenerService` (NLS). The NLS is bound and kept alive by the system once the user grants "Notification access" — so for *receiving* notifications no foreground service is required (process-persistence hardening is a separate later change). This change builds the capture pipeline and storage layer that everything else depends on.

Constraints:
- Notification access cannot be requested via a runtime permission dialog; the user must enable it manually in system settings.
- WeChat notification content varies by app version and locale, and WeChat aggregates rapid messages ("2 new messages"), which carry no usable content.
- Muted conversations produce no notifications, so capture is inherently incomplete — by design, not a bug.

## Goals / Non-Goals

**Goals:**
- A working NLS that filters to `com.tencent.mm` and parses notifications into structured messages.
- A Room-backed storage layer with stable conversation identity, ordered queries, counts, and clear-all.
- An onboarding/status surface that detects access and deep-links to settings.
- A parser that degrades gracefully on aggregated/empty/unknown formats and is unit-testable in isolation.

**Non-Goals:**
- Keeping the app process alive / surviving task-kill (→ `add-background-resilience`).
- Conversation list & detail UI beyond a minimal status readout (→ `add-conversation-list`).
- Any AI / network behavior (→ `add-ai-summary`).
- Multi-app sources and share-export (→ `design-extensions`).

## Decisions

**1. Capture via `NotificationListenerService`, not AccessibilityService.**
NLS is the purpose-built, lower-risk API for reading notifications. AccessibilityService could scrape on-screen chat text but is invasive, fragile, and risks Play policy rejection. Trade-off accepted: NLS only sees notifications (no history, no muted chats).

**2. Parse off the binder thread; persist via repository + coroutines.**
`onNotificationPosted` runs on the main/binder thread. We extract the needed fields synchronously into a plain DTO, then hand off to a repository that writes via Room on `Dispatchers.IO`. Keeps the callback fast and avoids ANRs.

**3. Source-agnostic domain model from day one (cheap future-proofing).**
The stored entity carries a `sourcePackage` field and a `NotificationParser` interface, even though WeChat is the only implementation now. This reserves the multi-app extension point without building it. Alternative (hard-code WeChat everywhere) rejected: it would force a schema migration later.

**4. Conversation identity = hash of (sourcePackage, conversationType, conversationTitle).**
Notifications don't give a stable chat ID, so we derive a key from package + type + title. Including `conversationType` disambiguates a group and a direct chat that share a display name (spec requirement). Trade-off: a renamed group becomes a "new" conversation — acceptable for v1.

**5. Parsing strategy: title/text heuristics with a typed result.**
The parser returns a sealed result: `Parsed(message)`, `ContentUnavailable(conversation)`, or `Ignored`. WeChat group notifications typically render as title=`群名` and text=`发送者: 内容`; direct as title=`人名` and text=`内容`. The `EXTRA_TITLE` / `EXTRA_TEXT` (and `EXTRA_LINES`) of the notification drive classification. Aggregated counts (regex on patterns like `^\[?\d+\s*条`, `\d+ new messages`) map to `ContentUnavailable`. This keeps all locale/format brittleness in one pure, testable function.

**6. The listener service is declared `android:exported="true"`.**
Notification access is granted to the app, but the bind comes from the **system process** (a different UID). An `exported="false"` service cannot be bound externally, so capture would silently never start on targetSdk 31+. Safety is preserved by `android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"`, which only the system holds — exporting does not open the service to other apps. (This is a manifest/runtime concern not catchable by unit tests; verified by code review.)

**7. Room schema kept minimal and additive.**
Single `messages` table now; `conversations` is a derived query (DISTINCT on conversation key) rather than a separate table, to avoid premature normalization. `exportSchema = true` so future migrations are tracked from the first version.

## Risks / Trade-offs

- **WeChat changes notification format across versions/locales** → isolate all format logic in `WeChatNotificationParser` behind the `NotificationParser` interface; cover known formats (zh group, zh direct, en, aggregated, empty) with unit tests so regressions surface fast.
- **Aggregated notifications lose content** → represent as `ContentUnavailable` and (decision) skip persistence to avoid polluting data; documented limitation, not a silent drop — surfaced in status if needed later.
- **System may not deliver `EXTRA_TEXT` reliably for some messages** → fall back to `EXTRA_LINES`/`tickerText`; if all empty → `Ignored`.
- **Privacy: plaintext chat content stored on device** → in-scope storage is local-only with no network egress this change; encryption-at-rest deferred and noted as an open question.

## Migration Plan

Additive only — new service, new DB (version 1), new UI status. No existing data to migrate. Rollback = remove the service registration; no destructive changes.

## Open Questions

- Should aggregated (`ContentUnavailable`) notifications be persisted as markers for completeness, or skipped entirely? (Current decision: skip; revisit if users want gap visibility.)
- Encryption-at-rest for stored messages — defer to a later hardening change or adopt SQLCipher now? (Deferred.)
