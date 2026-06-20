## ADDED Requirements

### Requirement: Configuration in a navigation drawer

The system SHALL provide a navigation drawer, opened from the home screen, that gives access to: capture/notification-access status and background controls, API settings, global range-based deletion, and an about screen. The home content itself SHALL NOT show these controls inline.

#### Scenario: Open the drawer

- **WHEN** the user opens the drawer from the home screen
- **THEN** entries for capture/background settings, API settings, delete, and about are shown

#### Scenario: Capture status reachable

- **WHEN** notification access is not granted
- **THEN** the drawer (or its capture-settings entry) surfaces that capture is inactive and offers to enable it

### Requirement: About screen with author credit

The system SHALL provide an about screen showing the app name, version and build number, and the author credit "Tis Wu".

#### Scenario: About content

- **WHEN** the user opens the about screen
- **THEN** it displays the app name with version/build and the author credit "Tis Wu"
