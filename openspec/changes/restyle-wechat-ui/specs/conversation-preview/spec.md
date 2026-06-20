## ADDED Requirements

### Requirement: Conversation list shows a last-message preview

The system SHALL show, for each conversation in the list, a preview of its most recent message alongside the title, message count, and last-activity time.

#### Scenario: Preview reflects the latest message

- **WHEN** a conversation's most recent message has body `B`
- **THEN** the conversation's list entry exposes `B` as its preview

#### Scenario: Preview updates on new message

- **WHEN** a newer message arrives in a conversation
- **THEN** the conversation's preview becomes that newer message's body

### Requirement: Deterministic avatar

The system SHALL render an avatar for a conversation/sender derived from its display name: the same name SHALL always produce the same initial and background color.

#### Scenario: Stable avatar for a name

- **WHEN** an avatar is rendered for a given name twice
- **THEN** both renders use the same initial and the same background color

#### Scenario: Empty name is handled

- **WHEN** an avatar is requested for a blank name
- **THEN** a stable fallback initial and color are used without error

### Requirement: Group message bubbles show sender

The system SHALL display group conversation messages as bubbles labeled with the sender, while direct conversation messages SHALL NOT show a redundant sender label.

#### Scenario: Group bubble

- **WHEN** a message in a group conversation is displayed
- **THEN** its bubble shows the sender's name (and avatar)

#### Scenario: Direct bubble

- **WHEN** a message in a direct conversation is displayed
- **THEN** no sender label is shown above the bubble
