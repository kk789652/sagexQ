## ADDED Requirements

### Requirement: Home lists source apps with captured messages

The system SHALL present, on the home screen, one entry per source app that has at least one captured message, showing the app's display name, its conversation and message counts, and its last-activity time, ordered by most recent activity.

#### Scenario: App appears after capture

- **WHEN** messages from a source app have been captured
- **THEN** that app appears in the home list with its counts and last-activity time

#### Scenario: No apps yet

- **WHEN** no messages have been captured
- **THEN** the home shows an empty state instead of an app list

#### Scenario: Known app display name

- **WHEN** the source app is `com.tencent.mm`
- **THEN** its display name is shown as `微信`

#### Scenario: Unknown app display name

- **WHEN** the source app has no known mapping
- **THEN** its package name is used as the display name

### Requirement: Drill into an app's conversations

The system SHALL open a conversation list scoped to a single source app when its home entry is tapped, showing only that app's conversations.

#### Scenario: Open an app

- **WHEN** the user taps an app entry
- **THEN** the conversation list shows only conversations belonging to that source app
