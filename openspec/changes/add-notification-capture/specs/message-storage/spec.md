## ADDED Requirements

### Requirement: Persist captured messages

The system SHALL persist each parsed message to a local on-device database and SHALL retain messages permanently until the user explicitly clears them.

#### Scenario: Message persisted

- **WHEN** a parsed message is handed to the storage layer
- **THEN** it is written to the local database with all its fields and is retrievable afterward

#### Scenario: Persistence survives restart

- **WHEN** the app process is restarted after messages were stored
- **THEN** the previously stored messages are still present

### Requirement: Conversation identity

The system SHALL assign each message a stable conversation key derived from its source package, conversation type, and conversation title, so that messages from the same chat group under the same key.

#### Scenario: Same conversation groups together

- **WHEN** two messages from the same WeChat conversation are stored
- **THEN** both messages resolve to the same conversation key

#### Scenario: Different conversations stay distinct

- **WHEN** messages from a group chat and from a direct chat with the same display name are stored
- **THEN** they resolve to different conversation keys because their conversation types differ

### Requirement: Message ordering and counts

The system SHALL expose stored messages ordered by timestamp and SHALL expose a total message count for status display.

#### Scenario: Ordered retrieval

- **WHEN** messages are queried for a conversation
- **THEN** they are returned in chronological order by timestamp

#### Scenario: Count reflects stored messages

- **WHEN** N messages have been stored
- **THEN** the exposed total count equals N

### Requirement: Clear stored data

The system SHALL allow the user to permanently delete all stored messages.

#### Scenario: Clear all

- **WHEN** the user confirms "clear all data"
- **THEN** all stored messages are deleted and the total count becomes zero
