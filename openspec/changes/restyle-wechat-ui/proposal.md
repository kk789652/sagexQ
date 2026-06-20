## Why

The current UI uses default Material 3 dynamic colors and plain list/text layouts. The user wants a familiar WeChat look so the app feels native to the content it captures. Adopting WeChat's visual language (light gray background, white cells, brand green, avatars, chat bubbles, message previews) makes conversations far more scannable.

## What Changes

- Apply a fixed WeChat-style light theme: brand green `#07C160`, app background `#EDEDED`, white surfaces/cells, light nav bars, red unread accent `#FA5151`. Disable dynamic color so the look is consistent.
- Add an `Avatar` component: a rounded square showing the conversation/sender initial on a deterministic per-name color.
- Redesign the conversation list as WeChat chat cells: avatar + title + **last-message preview** + time, white rows on a gray background with left-inset dividers. This requires surfacing each conversation's last message in the data layer.
- Redesign the conversation detail as WeChat message bubbles: received-style white bubbles with the sender's avatar, sender name shown for group messages, on a gray background.

Out of scope: bottom-tab navigation restructure, sent-message bubbles (all captured messages are "received"), animations.

## Capabilities

### New Capabilities
- `conversation-preview`: The conversation list surfaces each conversation's most recent message as a preview, with a deterministic avatar.

### Modified Capabilities
<!-- Visual restyle of existing conversation-browsing screens; no behavioral requirement changes there. -->

## Impact

- **Code**: `Theme.kt` (WeChat palette, no dynamic color); new `Avatar` composable + deterministic color util; `ConversationListScreen` + `ConversationDetailScreen` redesign; `MessageDao` conversation query + `ConversationSummary` gain `lastMessage`/`lastSender`.
- **Tests**: DAO returns last message per conversation; avatar color is deterministic.
- **Risk**: low; visual + one additive query column. No data migration.
