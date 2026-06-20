## 1. Grouping logic (pure)

- [x] 1.1 Add `ConversationSection(type, items)` and `ConversationGrouping.intoSections(list)` returning ordered, non-empty sections (GROUP, DIRECT, UNKNOWN) preserving input order within each

## 2. Navigation

- [x] 2.1 Add a `SageXNavHost` (Navigation Compose) with routes `home`, `conversations`, `conversation/{conversationKey}`
- [x] 2.2 Wire `MainActivity` to host the nav graph; add a "查看会话" entry on the home screen that navigates to the list

## 3. Conversation list

- [x] 3.1 Add `ConversationListViewModel` mapping `repository.conversations` to sections via `ConversationGrouping`, exposed as `StateFlow`
- [x] 3.2 Add `ConversationListScreen`: sectioned `LazyColumn` (群聊 / 私聊), each row showing title, count, last-activity time; tapping a row navigates to detail; empty state when nothing captured

## 4. Conversation detail

- [x] 4.1 Add `ConversationDetailViewModel` reading `conversationKey` from `SavedStateHandle` and exposing `repository.messages(key)` as `StateFlow`
- [x] 4.2 Add `ConversationDetailScreen`: chronological message list, each showing body + time and sender for group messages; top bar shows the conversation title with back navigation

## 5. Tests & verification

- [x] 5.1 Unit tests for `ConversationGrouping.intoSections` (separates types, drops empty sections, preserves recency order, handles empty input)
- [x] 5.2 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-conversation-list --strict`
