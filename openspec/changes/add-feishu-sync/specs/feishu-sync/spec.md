## ADDED Requirements

### Requirement: Sync captured messages to Feishu Bitable

The system SHALL push each captured message to the Feishu Bitable messages table after persisting it locally.

#### Scenario: Message synced after insert

- **WHEN** a message is successfully inserted into Room
- **THEN** the same message is asynchronously sent to Feishu Bitable with fields: app, conversation_title, sender, body, posted_at, processed=false

#### Scenario: Sync failure does not affect local storage

- **WHEN** the Feishu API call fails (network error, auth error)
- **THEN** the local Room record is unaffected and the error is logged only

### Requirement: Tenant access token is cached

The system SHALL cache the tenant_access_token and refresh it only when expired, to avoid redundant auth calls.

#### Scenario: Cached token is reused

- **WHEN** a valid token is already cached
- **THEN** no new auth request is made and the cached token is returned

#### Scenario: Expired token is refreshed

- **WHEN** the cached token has expired
- **THEN** a new auth request is made and the new token replaces the old one
