## ADDED Requirements

### Requirement: Per-app configurable prompt

The system SHALL store one editable prompt per source app (keyed by package) with a default, and SHALL persist edits across restarts.

#### Scenario: Default when unset

- **WHEN** an app's prompt has never been customized
- **THEN** reading its prompt returns the default prompt

#### Scenario: Edit persists per app

- **WHEN** the user sets a custom prompt for an app and reads it back
- **THEN** the stored prompt for that app is the custom value, and other apps are unaffected

### Requirement: Prompt configuration screen

The system SHALL provide a screen, reachable from the drawer, listing apps with captured messages and allowing each app's prompt to be edited and saved.

#### Scenario: Edit an app's prompt

- **WHEN** the user edits an app's prompt on the configuration screen and saves
- **THEN** that app's prompt is updated and used by subsequent summaries/exports for that app

### Requirement: Summary and export use the app prompt

The system SHALL use a conversation's app prompt as the AI-summary system instruction and as the export/share header.

#### Scenario: Summary uses the app prompt

- **WHEN** a conversation belonging to app A is summarized
- **THEN** app A's configured prompt is used as the system instruction

#### Scenario: Export uses the app prompt

- **WHEN** a conversation belonging to app A is exported/shared
- **THEN** app A's configured prompt is used as the instruction header
