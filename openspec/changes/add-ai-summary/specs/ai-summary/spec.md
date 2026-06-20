## ADDED Requirements

### Requirement: Summarize a conversation over a time range

The system SHALL produce an AI summary of a single conversation's messages within a user-selected time range (all / today / last 7 days) by calling the configured OpenAI-compatible API.

#### Scenario: Successful summary

- **WHEN** the API is configured and the user generates a summary for a conversation and range that contains messages
- **THEN** the selected messages are sent to the API and the returned summary text is shown

#### Scenario: Range filtering

- **WHEN** a time range is selected
- **THEN** only messages whose timestamp falls within that range are included in the request

#### Scenario: No messages in range

- **WHEN** the selected range contains no messages
- **THEN** the system does not call the API and informs the user there is nothing to summarize

### Requirement: Gate on configuration

The system SHALL require a complete API configuration before summarizing, and SHALL direct the user to settings when it is missing.

#### Scenario: Not configured

- **WHEN** the user attempts to summarize while the API is not configured
- **THEN** no request is made
- **AND** the user is prompted to open settings to configure the API

### Requirement: Request state and errors

The system SHALL reflect loading, success, and error states for a summary request, and SHALL surface a readable message on failure without crashing.

#### Scenario: Loading

- **WHEN** a summary request is in flight
- **THEN** a loading state is shown

#### Scenario: API error

- **WHEN** the API returns a non-success response or the call fails
- **THEN** an error message is shown and the app does not crash

### Requirement: Prompt is bounded

The system SHALL bound the number/size of messages included in a single summary request to avoid oversized API calls.

#### Scenario: Large conversation

- **WHEN** a conversation in range has more messages than the configured cap
- **THEN** the request includes at most the cap (most recent messages) rather than all of them
