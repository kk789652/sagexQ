## Context

Storage (Change 1) already exposes `MessageRepository.conversations: Flow<List<ConversationSummary>>` (DAO-aggregated, ordered by last activity) and `messages(key): Flow<List<CapturedMessage>>` (chronological). This change is a read-only UI layer over those Flows plus navigation between list and detail.

## Goals / Non-Goals

**Goals:**
- Navigation between status/home, conversation list, and conversation detail.
- List grouped into 群聊 / 私聊 sections, ordered by recency, with an empty state.
- Detail view of a conversation's messages, live-updating.
- Keep the type-grouping logic pure and unit-tested.

**Non-Goals:**
- AI summarization (next change), search, filtering, per-message/per-conversation deletion.

## Decisions

**1. Navigation Compose with a small typed-ish route set.**
Three routes: `home`, `conversations`, `conversation/{conversationKey}`. Conversation key (already URL-safe — it is SHA-256 hex) is passed as a path arg; the title is read from the loaded data, not the route. Alternative (single screen with modal) rejected: detail needs its own back stack entry.

**2. Grouping is a pure function over `ConversationSummary`.**
`ConversationGrouping.intoSections(list)` returns ordered, non-empty `ConversationSection`s (GROUP, then DIRECT, then UNKNOWN). Input order (recency) is preserved within a section. Keeping it pure isolates the only real logic from Compose and makes it unit-testable; the screens just render sections.

**3. ViewModels expose `StateFlow` mapped from repository Flows.**
`ConversationListViewModel` maps `repository.conversations` → sections. `ConversationDetailViewModel` reads `conversationKey` from `SavedStateHandle` and exposes `repository.messages(key)`. Both use `stateIn(WhileSubscribed)` for lifecycle-aware collection — live updates fall out of the Flow for free.

## Risks / Trade-offs

- **Conversation title not on the route** → detail derives the title from the first loaded message / summary; if the conversation is empty (shouldn't happen from the list) the title falls back to a placeholder.
- **Large conversations** → `LazyColumn` virtualizes rendering; no pagination needed at this scale for v1.

## Open Questions

- Should detail support jumping to newest vs. oldest on open? (v1: show oldest-to-newest, list auto-scrolls naturally; revisit if conversations get long.)
