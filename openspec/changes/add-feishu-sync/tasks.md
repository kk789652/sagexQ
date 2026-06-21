## 1. FeishuSyncClient

- [x] 1.1 `data/sync/FeishuSyncClient.kt`: `getToken()` with in-memory cache + expiry; `syncMessage(CapturedMessage)` POST to Bitable; no-op when `FEISHU_APP_ID` blank
- [x] 1.2 `di/SyncModule.kt`: `@Singleton` provision of `FeishuSyncClient`

## 2. Wire into MessageRepository

- [x] 2.1 Inject `FeishuSyncClient` + application `CoroutineScope` into `MessageRepository`; after `dao.insert`, launch fire-and-forget sync wrapped in `runCatching`

## 3. Tests & verification

- [x] 3.1 Unit test: `FeishuSyncClient` no-op when APP_ID blank; token cache returns cached value before expiry
- [x] 3.2 Run `./gradlew testDebugUnitTest assembleDebug` green
- [x] 3.3 Run `openspec validate add-feishu-sync --strict`
