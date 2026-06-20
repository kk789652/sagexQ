## ADDED Requirements

### Requirement: Battery-optimization exemption

The system SHALL report whether the app is exempt from battery optimization and SHALL provide an intent to request the exemption when it is not.

#### Scenario: Not exempt

- **WHEN** the app is not on the battery-optimization whitelist
- **THEN** the UI reports it as not exempt
- **AND** offers an action that opens the system battery-optimization request

#### Scenario: Already exempt

- **WHEN** the app is already exempt from battery optimization
- **THEN** the UI reports it as exempt

### Requirement: Manufacturer autostart guidance

The system SHALL detect the device manufacturer and resolve a best-effort intent to its autostart / background-launch settings, falling back to guidance text when no such screen is available, without crashing.

#### Scenario: Known manufacturer with autostart screen

- **WHEN** the device is from a manufacturer with a known autostart settings screen (e.g. Xiaomi, Huawei, OPPO, vivo)
- **THEN** the helper resolves an intent targeting that settings screen

#### Scenario: Unknown manufacturer or missing screen

- **WHEN** the device manufacturer has no known autostart screen, or the resolved intent cannot be handled
- **THEN** the helper reports that no direct screen is available
- **AND** the app shows fallback guidance instead of crashing
