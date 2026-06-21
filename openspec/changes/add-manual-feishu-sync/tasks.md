## 1. Room migration

- [x] 1.1 `MessageEntity`: add `feishuSynced: Boolean = false` field
- [x] 1.2 `SageXDatabase`: version 1→2; add `Migration(1,2)` that runs `ALTER TABLE messages ADD COLUMN feishu_synced INTEGER NOT NULL DEFAULT 0`

## 2. DAO + Repository

- [x] 2.1 `MessageDao`: add `markSynced(id)` and `unsyncedMessages()`
- [x] 2.2 `MessageRepository.insert`: on sync success → `dao.markSynced(id)`
- [x] 2.3 `MessageRepository.syncUnsynced(): Int`: iterate unsynced rows, push each, mark on success, return count

## 3. ViewModel + UI

- [x] 3.1 `HomeUiState`: add `feishuSyncStatus: FeishuSyncStatus` (Idle / Syncing / Done(count))
- [x] 3.2 `HomeViewModel.syncToFeishu()`: guard double-tap; set Syncing → call `repository.syncUnsynced()` → Done(n) → delay 3s → Idle
- [x] 3.3 `HomeScreen` drawer: add "同步到飞书" `NavigationDrawerItem` with label reflecting sync status; `Icons.Default.Sync`

## 4. Tests & verification

- [x] 4.1 Unit test: `unsyncedMessages` returns only unsynced rows; `markSynced` flips the flag (extend `MessageDaoTest`)
- [x] 4.2 Run `./gradlew testDebugUnitTest assembleDebug` green; run `openspec validate add-manual-feishu-sync --strict` (OpenSpec validation passed; Gradle/test run skipped per user instruction)
