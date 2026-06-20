## ADDED Requirements

### Requirement: Notification access permission gating

The system SHALL detect whether the user has granted notification-listener access and SHALL provide a path to the system settings screen where it can be granted. The system MUST NOT attempt to capture notifications when access has not been granted.

#### Scenario: Access not yet granted

- **WHEN** the app starts and notification-listener access is not enabled
- **THEN** the home screen reports capture as inactive
- **AND** offers an action that opens the system "Notification access" settings screen

#### Scenario: Access granted

- **WHEN** notification-listener access is enabled for SageX
- **THEN** the home screen reports capture as active

#### Scenario: Access revoked while running

- **WHEN** the user revokes notification access in system settings
- **THEN** the next status check reports capture as inactive without crashing

### Requirement: WeChat notification filtering

The system SHALL process only notifications posted by the WeChat package (`com.tencent.mm`) and SHALL ignore notifications from all other packages.

#### Scenario: WeChat notification received

- **WHEN** a notification posted by `com.tencent.mm` is received
- **THEN** the system passes it to the parser

#### Scenario: Non-WeChat notification received

- **WHEN** a notification from any package other than `com.tencent.mm` is received
- **THEN** the system ignores it and stores nothing

### Requirement: Notification parsing into structured messages

The system SHALL parse a WeChat notification into a structured message containing: conversation title, conversation type (group or direct), sender name when distinguishable, body text, source package, and the notification post timestamp.

#### Scenario: Direct (person) message

- **WHEN** a WeChat notification with title `<name>` and text `<content>`, where `<content>` has no `<sender>:` prefix, is parsed
- **THEN** the message records conversation type `DIRECT`, conversation title `<name>`, sender `<name>`, and body `<content>`

#### Scenario: Group message

- **WHEN** a WeChat notification with title `<group-title>` and text of the form `<sender>: <content>` is parsed
- **THEN** the message records conversation type `GROUP`, conversation title `<group-title>`, sender `<sender>`, and body `<content>`

#### Scenario: Aggregated / collapsed notification

- **WHEN** a WeChat notification whose text indicates a count rather than content (e.g. "[2条]" / "2 new messages") is parsed
- **THEN** the system does not fabricate message content
- **AND** either skips persistence or records the message flagged as content-unavailable, without crashing

#### Scenario: Unexpected or empty notification format

- **WHEN** a WeChat notification with missing title or text, or an unrecognized format, is parsed
- **THEN** the parser returns no message (or a content-unavailable marker) and does not throw
