## ADDED Requirements

### Requirement: Configure OpenAI-compatible endpoint

The system SHALL let the user set and persist a base URL, API key, and model name for an OpenAI-compatible API, and SHALL retain these across restarts.

#### Scenario: Save settings

- **WHEN** the user enters a base URL, API key, and model and saves
- **THEN** the values are persisted and shown when the settings screen is reopened

#### Scenario: Defaults when unset

- **WHEN** no API settings have ever been saved
- **THEN** the configuration reports as not configured

### Requirement: Configuration completeness

The system SHALL treat the API as configured only when base URL, API key, and model are all non-blank.

#### Scenario: Incomplete configuration

- **WHEN** any of base URL, API key, or model is blank
- **THEN** the configuration reports as not configured

#### Scenario: Complete configuration

- **WHEN** base URL, API key, and model are all non-blank
- **THEN** the configuration reports as configured
