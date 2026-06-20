## ADDED Requirements

### Requirement: Long-press a conversation to delete it

The system SHALL let the user long-press a conversation in the list to delete that conversation's messages, after confirmation.

#### Scenario: Delete a conversation via long-press

- **WHEN** the user long-presses a conversation row and confirms delete
- **THEN** all of that conversation's messages are deleted and other conversations are unaffected

#### Scenario: Confirmation required

- **WHEN** the user long-presses a conversation and dismisses the confirmation
- **THEN** nothing is deleted

### Requirement: Long-press a message to delete it

The system SHALL let the user long-press a single message in the detail to delete only that message, after confirmation.

#### Scenario: Delete a single message via long-press

- **WHEN** the user long-presses a message bubble and confirms delete
- **THEN** only that message is removed and the rest of the conversation remains

### Requirement: Addressable message identity

The system SHALL expose a stable identifier for each stored message so a single message can be deleted.

#### Scenario: Stored message has an id

- **WHEN** a message has been persisted and is read back
- **THEN** it carries the stable row id assigned by storage

#### Scenario: Delete by id removes one row

- **WHEN** `deleteById` is called with a stored message's id
- **THEN** only that row is removed
