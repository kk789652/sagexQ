## Context

Current `HomeScreen` mixes capture status, background controls, "查看会话", API settings, and delete. Conversations are global. The user wants an app-centric home + a config drawer. Storage already has `sourcePackage` on every message, so an app list is a straightforward aggregate.

## Goals / Non-Goals

**Goals:**
- App-list home (dynamic from captured data) → per-app conversation list.
- Navigation drawer holding all configuration + an about screen with author credit.
- Minimal disruption to capture/summary/export internals.

**Non-Goals:**
- Long-press delete, per-app prompt, summary selection (separate changes).
- Multi-source capture itself (still WeChat-only parser).

## Decisions

**1. App list is a DAO aggregate over `sourcePackage`.**
```sql
SELECT sourcePackage,
       COUNT(DISTINCT conversationKey) AS conversationCount,
       COUNT(*) AS messageCount,
       MAX(postedAt) AS lastPostedAt
FROM messages GROUP BY sourcePackage ORDER BY lastPostedAt DESC
```
→ `AppSummaryRow` → `AppSummary(sourcePackage, displayName, conversationCount, messageCount, lastPostedAt)`. `displayName` via a pure `appDisplayName(pkg)` (`com.tencent.mm` → `微信`, else the package). Exposed as `MessageRepository.apps: Flow<List<AppSummary>>`.

**2. Conversation list scoped by source package.**
Add `observeConversations(sourcePackage)` (existing query + `WHERE m.sourcePackage = :pkg`). `ConversationListViewModel` reads a `sourcePackage` nav arg (`SavedStateHandle`) and uses the scoped query. Route becomes `conversations/{sourcePackage}`.

**3. Navigation drawer on Home only.**
Wrap the Home `Scaffold` in `ModalNavigationDrawer`; a hamburger in the top bar opens it. Drawer items navigate to: `capture-settings` (notification access status + keep-alive + battery + autostart, migrated from the old home), `settings` (API), a global delete dialog (range), and `about`. Non-home screens keep back navigation; the drawer is not repeated there.

**4. New screens.**
- `CaptureSettingsScreen`: the old home's status card + background card (access enable, keep-alive toggle, battery exemption, autostart).
- `AboutScreen`: app name + `BuildConfig.VERSION_NAME` (`versionCode`) + "© {year} Tis Wu" (use `String()` around the year to avoid SwiftUI-style/locale grouping — here just plain Kotlin string).
- `HomeScreen`: app list + drawer; the capture-inactive banner shown if access not granted.

**5. Remove the unread count badge** from conversation rows (kept the count in the data; just not rendered as a badge).

## Risks / Trade-offs

- **Navigation rewrite risk** → keep routes additive where possible; conversation detail/summary/settings routes unchanged except the new `conversations/{sourcePackage}`.
- **Empty app list vs loading** → show empty state when the apps flow is empty.

## Open Questions

- Per-app icons (real launcher icons) vs initial avatars? (Use the deterministic avatar for now.)
