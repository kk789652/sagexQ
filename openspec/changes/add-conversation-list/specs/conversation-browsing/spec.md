## ADDED Requirements

### Requirement: Conversation list grouped by type

The system SHALL present captured conversations split into sections by conversation type (group and direct), showing for each conversation its title, message count, and last-activity time. Empty sections SHALL NOT be shown.

#### Scenario: Groups and people are separated

- **WHEN** captured conversations include both group chats and direct chats
- **THEN** the list shows a group section and a direct section, each containing only its matching conversations

#### Scenario: Only one type present

- **WHEN** all captured conversations are of a single type
- **THEN** only that type's section is shown

#### Scenario: Conversation entry contents

- **WHEN** a conversation is shown in the list
- **THEN** its entry displays the conversation title, its message count, and its last-activity time

### Requirement: Conversation list ordering

The system SHALL order conversations by most recent activity first.

#### Scenario: Most recent first

- **WHEN** the conversation list is shown
- **THEN** conversations are ordered by last-activity time, newest first

### Requirement: Empty state

The system SHALL show an empty state when no messages have been captured.

#### Scenario: Nothing captured

- **WHEN** no messages have been captured
- **THEN** the conversation list shows an empty state instead of an empty list

### Requirement: Conversation detail

The system SHALL show a single conversation's messages in chronological order when the user opens it, each message displaying its body and time, and the sender for group conversations.

#### Scenario: Open a conversation

- **WHEN** the user taps a conversation in the list
- **THEN** the detail screen shows that conversation's messages ordered oldest-to-newest

#### Scenario: Group message shows sender

- **WHEN** a message in a group conversation is shown
- **THEN** its sender name is displayed alongside the body

### Requirement: Live updates

The system SHALL reflect newly captured messages in the list and detail without requiring a manual refresh.

#### Scenario: New message arrives

- **WHEN** a new message is captured for a conversation while the list is visible
- **THEN** that conversation's count and last-activity update, and its ordering is re-evaluated
