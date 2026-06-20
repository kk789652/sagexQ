## Context

Screens currently use Material 3 dynamic color and plain rows. WeChat's visual language is light gray background, white cells, brand green accent, rounded-square avatars, message previews in the list, and chat bubbles in the detail. The conversation list lacks a last-message preview, which requires a small data-layer addition.

## Goals / Non-Goals

**Goals:**
- A consistent WeChat-style light theme (fixed palette, no dynamic color).
- Reusable deterministic `Avatar`.
- WeChat-style conversation list (avatar + preview + time) and detail (bubbles).

**Non-Goals:**
- Bottom-tab navigation; sent-message bubbles; per-pixel WeChat cloning; dark-mode polish.

## Decisions

**1. Fixed WeChat palette, dynamic color off.**
```
brand green   #07C160   (primary)
sent bubble   #95EC69   (reserved; we only render received)
app background#EDEDED
surface/cell  #FFFFFF
divider       #E5E5E5
unread/red    #FA5151
text primary  #191919
text secondary#888888
```
Dynamic color is disabled so the brand identity is stable across devices. Dark theme keeps reasonable equivalents but is not a focus.

**2. Deterministic avatar = initial + color from name hash.**
`avatarColor(name): Color` picks from a fixed palette by `name.hashCode()`; `avatarInitial(name)` takes the first non-blank character (fallback "?"). Pure → unit-testable; stable across recompositions and restarts.

**3. Surface last message in the conversation query.**
Extend the aggregate query with correlated subqueries for the latest body and sender per conversation; add `lastMessage`/`lastSender` to `ConversationSummaryRow` and `ConversationSummary`. Keeps a single query (no N+1) and updates live via the existing Flow.

**4. List = WeChat cells; detail = received bubbles.**
List row: rounded-square avatar, title (top), preview (gray, one line ellipsized), time (top-right gray), on white with a left-inset divider, gray page background. Detail: each message = avatar + (sender for groups) + white rounded bubble, gray page background, comfortable spacing.

## Risks / Trade-offs

- **Correlated subqueries cost** → fine at this scale; conversation count is small. Revisit with window functions if needed.
- **Hash-based color collisions** → acceptable; purely cosmetic.
- **Light-only focus** → dark mode remains functional but unpolished (documented non-goal).

## Open Questions

- Adopt a WeChat-like bottom tab bar (会话 / 我) later? (Deferred; keep current navigation now.)
