## ADDED Requirements

### Requirement: Capture from additional source apps

The system SHALL support capturing notifications from source apps beyond WeChat by adding new `NotificationParser` implementations, without changing the listener service or storage. This is a design-only contract; implementation per source is deferred to future changes.

#### Scenario: A new source is added

- **WHEN** a new `NotificationParser` implementation for some source app is registered (bound `@IntoSet`)
- **THEN** notifications from that source are parsed and stored using the same pipeline as WeChat, keyed by their own `sourcePackage`

#### Scenario: Source isolation

- **WHEN** multiple source parsers are registered
- **THEN** each notification is handled only by the parser whose `supports(sourcePackage)` returns true, and conversations from different sources never collide (the conversation key includes `sourcePackage`)
