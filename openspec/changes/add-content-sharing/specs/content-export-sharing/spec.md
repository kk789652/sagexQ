## ADDED Requirements

### Requirement: Export a conversation transcript over a time range

The system SHALL render a conversation's messages within a chosen time range into plain text, each line showing time, sender, and body, bounded to a maximum number of most-recent messages.

#### Scenario: Transcript content

- **WHEN** a conversation with messages in the selected range is exported
- **THEN** the output contains one line per included message with its time, sender, and body

#### Scenario: Bounded export

- **WHEN** the conversation has more messages in range than the cap
- **THEN** only the most recent messages up to the cap are included

#### Scenario: Empty range

- **WHEN** the selected range contains no messages
- **THEN** the user is informed there is nothing to export and no share sheet is shown

### Requirement: Configurable instruction header

The system SHALL prefix the exported transcript with a user-configurable instruction header (defaulting to a summarize prompt), and SHALL allow the header to be edited and persisted.

#### Scenario: Default header

- **WHEN** the instruction header has not been customized
- **THEN** the export is prefixed with the default summarize prompt

#### Scenario: Edited header persists

- **WHEN** the user edits the instruction header and saves it
- **THEN** subsequent exports use the edited header and it survives restart

### Requirement: Share via system share sheet

The system SHALL share exported text and AI summaries through the Android system share sheet as plain text.

#### Scenario: Share a transcript

- **WHEN** the user confirms export for a range
- **THEN** the system share sheet opens with the exported transcript as `text/plain`

#### Scenario: Share an AI summary

- **WHEN** a summary has been generated and the user taps share
- **THEN** the system share sheet opens with the summary text as `text/plain`
