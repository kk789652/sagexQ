## Context

SageX's MVP (capture → store → browse → summarize) is in place. Two future capabilities were planned from the start and should have their seams designed now so later work is additive:

1. **Summary sharing** via the system share sheet.
2. **Multi-source capture** beyond WeChat.

This document records the intended interfaces and integration points. No production code is written in this change.

## Goals / Non-Goals

**Goals:**
- Define the share interface and exactly where it plugs into the summary screen.
- Define what a new capture source must implement and confirm the seams already present.
- Make future implementation a small, well-scoped change.

**Non-Goals:**
- Implementing either capability now. Each gets its own change later (`add-summary-sharing`, `add-source-<app>`).

## Decisions

### Summary sharing

**Seam:** `SummaryStatus.Success` already holds the summary text. Sharing is a UI-only addition — no ViewModel/data changes needed.

**Planned interface** (future):
```kotlin
// android intent, built where the success state is rendered
fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null))
}
```
- Add a "分享" action in `SummaryScreen` next to "复制", enabled only for `SummaryStatus.Success`.
- No new permission (ACTION_SEND needs none).
- Optional later: a generic `ShareExporter` so other artifacts (e.g. a conversation transcript) can be shared too — defer until a second caller exists (YAGNI).

### Multi-source capture

**Seams already in place (verified in code):**
- `NotificationParser` interface with `supports(sourcePackage)` + `parse(RawNotification)`.
- Parsers bound `@IntoSet` (`ParserModule`) — `SageXNotificationListenerService` already iterates the set and picks the first parser that `supports` the package.
- `RawNotification.sourcePackage` and `CapturedMessage.sourcePackage` are source-agnostic.
- `ConversationKey = SHA-256(sourcePackage | type | title)` already includes the source, so conversations from different apps cannot collide.

**What a new source must provide (future):**
1. A `NotificationParser` implementation for its package (the only brittle, app-specific part — its notification format heuristics), with unit tests mirroring `WeChatNotificationParserTest`.
2. An `@IntoSet` binding in `ParserModule`.
3. Nothing else: storage, listener, browsing, and summary are already source-agnostic.

**Open design choices deferred to implementation:**
- Per-source enable/disable toggle (likely a `Set<String>` of enabled packages in settings).
- Per-source display label/icon in the conversation list (currently the conversation title only).
- The listener currently filters implicitly via `supports`; if many sources are added, consider an explicit allow-list to avoid parsing every app's notifications.

## Risks / Trade-offs

- **Designing too far ahead** → kept to interfaces + integration points only; no speculative code (e.g. generic `ShareExporter` deferred until a second caller exists).
- **Source format brittleness** → isolated per-parser, exactly like WeChat today; each new source is independently testable and cannot destabilize others.

## Migration Plan

None — design only. Each capability ships later as its own additive change.

## Open Questions

- Should multi-source include a settings UI to toggle sources, or start with code-only registration? (Defer to the first concrete second source.)
- Should sharing offer formats beyond plain text (e.g. Markdown)? (Defer; plain text covers the stated requirement.)
