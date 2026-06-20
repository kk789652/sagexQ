## ADDED Requirements

### Requirement: Reusable time range with presets and custom span

The system SHALL provide a time range with presets (all, today, last 7 days, last 30 days) and a custom start–end span, and SHALL filter messages to a range deterministically relative to a supplied "now".

#### Scenario: Preset range filters messages

- **WHEN** a preset range (e.g. last 7 days) is applied to a set of messages
- **THEN** only messages whose timestamp falls within that range relative to now are kept

#### Scenario: Custom range filters by explicit bounds

- **WHEN** a custom range with an explicit start and end is applied
- **THEN** only messages whose timestamp falls within `[start, end)` are kept

#### Scenario: All range keeps everything

- **WHEN** the "all" range is applied
- **THEN** every message is kept regardless of timestamp

### Requirement: Time-range selector UI

The system SHALL present a selector for choosing a preset range or a custom start–end span, reusable across features that operate over a range.

#### Scenario: Choose a preset

- **WHEN** the user selects a preset in the selector
- **THEN** the selected range is that preset

#### Scenario: Choose a custom span

- **WHEN** the user picks a custom start and end date
- **THEN** the selected range is a custom span covering those dates
