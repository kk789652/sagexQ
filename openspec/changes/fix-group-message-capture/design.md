## Context

`WeChatNotificationParser` currently flags any text matching `^\[?\s*\d+\s*条` as aggregated → `ContentUnavailable` → skipped. But WeChat uses `[N条]<content>` for multi-unread conversations, so this discards the very messages we want. Direct chats with a single unread have no prefix, which is why they still work.

## Goals / Non-Goals

**Goals:**
- Recover content from `[N条]`-prefixed notifications.
- Keep genuine pure-summary notifications (`N条新消息`) as content-unavailable.
- Get ground-truth on the exact on-device format via debug logging.

**Non-Goals:**
- MessagingStyle `EXTRA_MESSAGES` parsing — only add it if diagnostics show WeChat uses it on the target device (kept as a follow-up to avoid speculative complexity).

## Decisions

**1. Strip the count prefix before classification.**
Add `stripCountPrefix(text)` removing a leading `[N条]` / `【N条】` (ASCII or fullwidth brackets) and an optional following space. Applied to the resolved body first; classification (`sender: content` → group, else direct) then runs on the remainder.

**2. Redefine "content-unavailable".**
After stripping, a notification is content-unavailable only if the remainder is empty or still matches a pure-summary pattern (`^\d+\s*条(新)?消息`, `N位联系人...`, `N new messages`). A bracketed prefix followed by real content is NOT a summary.

**3. Debug-only diagnostic dump.**
In `onNotificationPosted`, when `BuildConfig.DEBUG` and the package is WeChat, log `EXTRA_TITLE`, `EXTRA_TEXT`, `EXTRA_TEXT_LINES`, `EXTRA_SUB_TEXT`, and the presence of `EXTRA_MESSAGES`. This confirms the real format and reveals if a device uses InboxStyle/MessagingStyle so we can extend precisely. Debug-gated; logcat only.

## Risks / Trade-offs

- **Other WeChat versions may differ** → the diagnostic dump gives ground truth; the prefix fix covers the dominant format now. → Mitigation: ship both together.
- **Over-stripping** → the prefix regex is anchored and specific (`^[\[【]\s*\d+\s*条\s*[\]】]`), so normal content like "3条建议" (no brackets) is not stripped.

## Open Questions

- Does the target device's WeChat use MessagingStyle (`EXTRA_MESSAGES`)? Pending the diagnostic log; if so, add a follow-up to read the newest message entry.
