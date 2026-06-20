## ADDED Requirements

### Requirement: Select which messages to summarize

The system SHALL show the in-range messages on the summary page as a selectable checklist, defaulting to all selected, and SHALL summarize only the selected messages.

#### Scenario: Default all selected

- **WHEN** the summary page shows messages for the chosen range
- **THEN** every in-range message is selected by default

#### Scenario: Summarize only selected

- **WHEN** the user deselects some messages and generates a summary
- **THEN** only the still-selected messages are sent to the API

#### Scenario: Range change refreshes selection

- **WHEN** the user changes the time range
- **THEN** the checklist updates to the new range's messages with all selected

### Requirement: Guard empty selection

The system SHALL NOT call the API when no messages are selected, and SHALL inform the user there is nothing to summarize.

#### Scenario: Nothing selected

- **WHEN** the user clears all selections and taps generate
- **THEN** no request is made and the user is told there is nothing to summarize

### Requirement: Select-all and clear-all

The system SHALL provide select-all and clear-all actions for the checklist.

#### Scenario: Clear then select all

- **WHEN** the user taps clear-all then select-all
- **THEN** no messages are selected after clear-all, and all in-range messages are selected after select-all
