## Why

Deletion today is only via dialogs (global on home, per-conversation in detail). The user wants quick, direct deletion: long-press a conversation in the list to delete it, and long-press a single message in the detail to delete just that message. This needs message identity exposed to the UI.

## What Changes

- Long-press a conversation row → context popup with "删除会话" → confirm → delete that conversation's messages.
- Long-press a message bubble → context popup with "删除消息" → confirm → delete just that message.
- Expose the stored row `id` on the domain `CapturedMessage` so a single message can be addressed; add a `deleteById` path.

## Capabilities

### New Capabilities
- `long-press-delete`: Long-pressing a conversation or a message opens a delete action, removing that conversation or that single message after confirmation.

### Modified Capabilities
<!-- message-storage gains a delete-by-id; no requirement change to existing queries. -->

## Impact

- **Data**: `CapturedMessage` gains `id` (mapped from `MessageEntity.id`; default 0 for parser-created); `MessageDao.deleteById(id)` + `MessageRepository.deleteMessage(id)`; `MessageRepository.deleteConversation(conversationKey)`.
- **UI**: long-press (`combinedClickable`) on conversation rows and message bubbles, each opening a small confirm (dropdown or dialog).
- **Risk**: low; deletes are confirmed; id is additive.
