## Why

Change 1 captures and stores messages and already exposes them grouped by conversation, but the app only shows aggregate status — there is no way to actually read what was captured. Users need to browse captured conversations, separated into groups vs. people, and drill into a conversation to read its messages. This is also the selection surface that the upcoming AI-summary feature will build on.

## What Changes

- Add navigation (Navigation Compose) with three destinations: home/status, conversation list, conversation detail.
- Add a conversation list screen that shows captured conversations split into sections by type (群聊 / 私聊), each entry showing title, message count, and last-activity time, ordered by most recent activity.
- Add a conversation detail screen that shows a conversation's messages in chronological order, each with sender (for groups) and body + time.
- Both screens update live as new messages are captured (backed by existing repository Flows).
- Add an empty state when nothing has been captured yet.
- Add a pure grouping helper that splits a flat conversation list into ordered, non-empty type sections (unit-tested).

Out of scope: AI summarization (`add-ai-summary`), search/filter, message deletion per-conversation.

## Capabilities

### New Capabilities
- `conversation-browsing`: Browsing captured conversations grouped by type (group/person) and viewing a single conversation's messages chronologically.

### Modified Capabilities
<!-- None. Reuses message-storage queries from add-notification-capture without changing them. -->

## Impact

- **New code**: navigation host + routes; `ConversationListViewModel` + screen; `ConversationDetailViewModel` + screen; pure `ConversationGrouping` helper; small changes to `MainActivity`/`HomeScreen` to add navigation entry.
- **Dependencies**: Navigation Compose (already in the build).
- **Data**: read-only; reuses `MessageRepository.conversations` and `MessageRepository.messages(key)`.
- **Risk**: low — additive UI over existing, tested storage.
