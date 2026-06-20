## ADDED Requirements

### Requirement: Count-prefixed notifications are parsed, not discarded

The system SHALL strip a leading bracketed unread-count prefix (`[N条]` or fullwidth `【N条】`) from a WeChat notification before classification, and SHALL parse the remaining text as a normal message. A bare `N条` without brackets SHALL NOT be stripped (it is ambiguous with real message content such as "3条建议"). A notification SHALL be treated as content-unavailable only when no real content remains after stripping (a pure count or multi-chat summary).

#### Scenario: Count-prefixed group message

- **WHEN** a WeChat notification has title `开发群` and text `[3条]张三: 你好`
- **THEN** the message records conversation type `GROUP`, title `开发群`, sender `张三`, body `你好`

#### Scenario: Count-prefixed direct message

- **WHEN** a WeChat notification has title `李四` and text `[2条]在吗`
- **THEN** the message records conversation type `DIRECT`, title `李四`, sender `李四`, body `在吗`

#### Scenario: Fullwidth-bracket count prefix

- **WHEN** a WeChat notification text is `【5条】王五: 看一下`
- **THEN** the count prefix is stripped and the message is parsed as a group message from `王五` with body `看一下`

#### Scenario: Pure count summary remains content-unavailable

- **WHEN** a WeChat notification text is `2条新消息` with no content after the count
- **THEN** the result is content-unavailable and nothing is persisted

#### Scenario: Multi-chat summary remains content-unavailable

- **WHEN** a WeChat notification text is `[3条]来自2个聊天` (a multi-chat summary, not a single message)
- **THEN** after stripping the prefix the remainder is recognized as a summary and the result is content-unavailable

#### Scenario: Bare count-like content is preserved

- **WHEN** a WeChat notification has title `李四` and text `3条建议给你` (no brackets — real content that merely starts with a number)
- **THEN** the text is NOT treated as a count prefix and the message body is `3条建议给你`
