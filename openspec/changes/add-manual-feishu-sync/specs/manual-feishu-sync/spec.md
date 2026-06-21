## ADDED Requirements

### Requirement: Track per-message Feishu sync state

The system SHALL record whether each stored message has been successfully synced to Feishu Bitable.

#### Scenario: Successful auto-sync marks the row as synced

- **WHEN** a message is inserted and the Feishu API call succeeds
- **THEN** the message is marked feishuSynced = true in Room

#### Scenario: Failed auto-sync leaves the row unsynced

- **WHEN** a message is inserted but the Feishu API call fails
- **THEN** the message remains feishuSynced = false

### Requirement: Manual sync pushes unsynced messages

The system SHALL provide a manual sync action that pushes all feishuSynced=false messages to Feishu and marks each as synced on success.

#### Scenario: Manual sync pushes only unsynced rows

- **WHEN** the user triggers manual sync and some messages are unsynced
- **THEN** only feishuSynced=false messages are sent to Feishu; already-synced messages are not re-sent

#### Scenario: Manual sync marks success individually

- **WHEN** manual sync runs and a message is pushed successfully
- **THEN** that message is immediately marked feishuSynced = true, independently of other messages

### Requirement: Drawer shows sync status

The system SHALL show a sync status in the drawer (idle / syncing / result count).

#### Scenario: Status updates during sync

- **WHEN** manual sync is in progress
- **THEN** the drawer item shows a syncing indicator; when done, it shows how many messages were pushed
