## 1. Message identity + deletes (data)

- [x] 1.1 Add `id: Long = 0` to `CapturedMessage`; map it in `MessageEntity.toDomain()` (toEntity keeps default 0)
- [x] 1.2 Add `MessageDao.deleteById(id)`; `MessageRepository.deleteMessage(id)` and `deleteConversation(conversationKey)` on IO

## 2. Long-press: conversation

- [x] 2.1 Conversation row uses `combinedClickable` (tap opens; long-press shows a delete confirmation); confirm → `ConversationListViewModel.deleteConversation(key)`

## 3. Long-press: message

- [x] 3.1 Message bubble uses `combinedClickable` (long-press shows a delete confirmation); confirm → `ConversationDetailViewModel.deleteMessage(id)`
- [x] 3.2 Use the real message `id` as the detail LazyColumn item key (replace the index workaround)

## 4. Tests & verification

- [x] 4.1 DAO test: `deleteById` removes exactly one row; `toDomain` exposes the row id
- [x] 4.2 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-long-press-delete --strict`
