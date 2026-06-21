## Why

SageX currently stores messages only in local Room DB, requiring manual in-app interaction. Adding a Feishu Bitable sync layer lets an external agent (codex) read messages and auto-generate digests without touching the phone.

## What Changes

- After each successful Room insert, asynchronously POST the message to Feishu Bitable (fire-and-forget; local storage is not affected by sync failures).
- Credentials come from BuildConfig (injected from local.properties at build time).
- A `FeishuSyncClient` handles tenant_access_token caching and the record create call.

## Capabilities

### New Capabilities
- `feishu-sync`: Every captured message is synced to the Feishu Bitable `messages` table, making it available to external agents.

## Impact

- **Code**: new `data/sync/FeishuSyncClient.kt` (OkHttp, token cache); new `di/SyncModule.kt`; `MessageRepository.insert` fires sync after Room write.
- **Risk**: low — sync is fire-and-forget, isolated from local storage path.
