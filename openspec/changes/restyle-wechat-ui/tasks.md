## 1. Theme

- [x] 1.1 Rework `Theme.kt` to a fixed WeChat light palette (brand green, gray background, white surface, red accent); disable dynamic color

## 2. Avatar

- [x] 2.1 Add pure `avatarInitial(name)` + `avatarColor(name)` (deterministic from name; blank-safe)
- [x] 2.2 Add an `Avatar` composable (rounded square, initial on the deterministic color)

## 3. Last-message preview (data)

- [x] 3.1 Extend the conversation aggregate query with latest `lastMessage`/`lastSender` per conversation
- [x] 3.2 Add `lastMessage`/`lastSender` to `ConversationSummaryRow` and `ConversationSummary` (+ mapper)

## 4. Conversation list restyle

- [x] 4.1 Redesign `ConversationListScreen` rows: avatar + title + last-message preview + time, white cells on gray background, left-inset dividers

## 5. Conversation detail restyle

- [x] 5.1 Redesign `ConversationDetailScreen` messages as WeChat received bubbles: avatar + (sender for groups) + white bubble on gray background

## 6. Tests & verification

- [x] 6.1 Unit tests for `avatarInitial`/`avatarColor` determinism + blank handling
- [x] 6.2 DAO test: conversation query returns the latest message body/sender per conversation
- [x] 6.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate restyle-wechat-ui --strict`
