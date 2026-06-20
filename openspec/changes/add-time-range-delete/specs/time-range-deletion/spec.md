## ADDED Requirements

### Requirement: Delete messages within a time range

The system SHALL allow permanently deleting captured messages whose timestamp falls within a chosen time range, and SHALL require explicit confirmation before deleting.

#### Scenario: Global delete within a range

- **WHEN** the user chooses a range and confirms a global delete
- **THEN** all messages across all conversations within that range are deleted, and messages outside the range remain

#### Scenario: Delete all

- **WHEN** the user chooses the "all" range and confirms
- **THEN** every captured message is deleted

#### Scenario: Confirmation required

- **WHEN** the user initiates a delete
- **THEN** nothing is deleted until the user confirms

### Requirement: Delete a single conversation within a time range

The system SHALL allow deleting only one conversation's messages within a chosen time range.

#### Scenario: Per-conversation range delete

- **WHEN** the user chooses a range and confirms deletion for a specific conversation
- **THEN** only that conversation's messages within the range are deleted, and other conversations are unaffected
