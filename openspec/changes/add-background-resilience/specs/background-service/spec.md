## ADDED Requirements

### Requirement: Keep-alive foreground service

The system SHALL provide a user-toggleable foreground service that, while enabled, runs with a persistent notification to keep the app process warm.

#### Scenario: Enable keep-alive

- **WHEN** the user enables keep-alive
- **THEN** the foreground service starts
- **AND** a persistent, low-importance notification is shown indicating capture is running

#### Scenario: Disable keep-alive

- **WHEN** the user disables keep-alive
- **THEN** the foreground service stops
- **AND** its persistent notification is removed

### Requirement: Persisted keep-alive preference

The system SHALL persist the keep-alive preference so the choice survives app and device restarts.

#### Scenario: Preference round-trips

- **WHEN** keep-alive is set to enabled and the value is read back
- **THEN** the stored preference reports enabled

#### Scenario: Default is disabled

- **WHEN** the keep-alive preference has never been set
- **THEN** reading it returns disabled

### Requirement: Restart on boot

The system SHALL start the keep-alive foreground service after device boot when, and only when, keep-alive is enabled.

#### Scenario: Boot with keep-alive enabled

- **WHEN** the device finishes booting and keep-alive is enabled
- **THEN** the foreground service is started

#### Scenario: Boot with keep-alive disabled

- **WHEN** the device finishes booting and keep-alive is disabled
- **THEN** the foreground service is not started

### Requirement: Survive process termination

The system SHALL request that the OS restart the foreground service if it is terminated under memory pressure while keep-alive is enabled.

#### Scenario: Service killed by the OS

- **WHEN** the foreground service is killed by the system while keep-alive is enabled
- **THEN** the service is configured (START_STICKY) so the OS recreates it
