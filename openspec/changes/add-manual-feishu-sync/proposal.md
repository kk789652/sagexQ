## Why

The current Feishu sync is fire-and-forget on insert; failures are silently dropped and there's no way to retry or verify what actually reached Feishu. Adding a `feishuSynced` flag per message enables tracking sync state and lets the user trigger a manual sync from the drawer to push any missed messages.

## What Changes

- `MessageEntity` gains `feishuSynced: Boolean` (default false); Room migrates to version 2.
- After a successful Feishu sync in `MessageRepository.insert`, the row is marked `feishuSynced = true`.
- A "同步到飞书" action in the drawer triggers `syncUnsynced()`: fetches all unsynced rows and pushes them to Feishu one by one, marking each as synced on success.
- The drawer shows a brief status (syncing / done / count).

## Capabilities

### New Capabilities
- `manual-feishu-sync`: User can manually trigger a Feishu sync from the drawer that pushes any locally stored but unsynced messages.

### Modified Capabilities
<!-- feishu-sync: auto-sync now marks the row as synced on success. -->

## Impact

- **Data**: Room version 1→2; migration adds `feishuSynced INTEGER NOT NULL DEFAULT 0`; existing rows treated as unsynced (will be pushed on first manual sync).
- **Code**: `MessageEntity` + `MessageDao.markSynced(id)` + `MessageDao.unsyncedMessages()` + `MessageRepository.syncUnsynced()`; `HomeViewModel` exposes sync action + status; drawer wired up.
- **Risk**: low — Room migration is additive; sync is idempotent on the Feishu side (may create duplicate records if the user syncs twice before Feishu processes, but acceptable for now).
