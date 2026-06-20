## ADDED Requirements

### Requirement: Share a summary via the system share sheet

The system SHALL allow a produced summary to be shared to other apps via the Android system share sheet (`ACTION_SEND`, `text/plain`). This is a design-only contract; implementation is deferred to a future change.

#### Scenario: Share a generated summary

- **WHEN** a summary has been generated and the user invokes share
- **THEN** the system presents the Android share sheet with the summary text as `text/plain`

#### Scenario: Share unavailable without a summary

- **WHEN** no summary has been generated yet
- **THEN** the share action is not offered
