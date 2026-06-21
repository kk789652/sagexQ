## Context

`MessageRepository.insert(entity)` writes to Room via `dao.insert`. We need to fire a Feishu Bitable record-create call after that, without blocking the notification pipeline.

## Decisions

**1. `FeishuSyncClient` — standalone class, OkHttp.**
- `getToken()`: POST `https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal` with `{app_id, app_secret}`; cache result with expiry (`expire` field in response, subtract 60s buffer). Thread-safe via `@Synchronized`.
- `syncMessage(message: CapturedMessage)`: POST to `https://open.feishu.cn/open-apis/bitable/v1/apps/{APP_TOKEN}/tables/{TABLE_ID}/records` with fields:
  - `app` → message.sourcePackage
  - `conversation_title` → message.conversationTitle
  - `sender` → message.sender ?: ""
  - `body` → message.body
  - `posted_at` → message.postedAt (epoch millis, Feishu date field expects ms)
  - `processed` → false
- All calls on `@IoDispatcher`.

**2. Fire-and-forget in `MessageRepository.insert`.**
```kotlin
suspend fun insert(entity: MessageEntity) {
    dao.insert(entity)
    coroutineScope.launch { runCatching { feishuSync.syncMessage(entity.toDomain()) } }
}
```
Uses a `CoroutineScope` injected via Hilt (application-scoped).

**3. `SyncModule` provides `FeishuSyncClient` as `@Singleton`.**
Reads `BuildConfig.FEISHU_APP_ID` etc. Guarded: if `APP_ID` is blank, `syncMessage` is a no-op.

## Risks

- **Token expiry during burst** → mutex on `getToken()` prevents parallel refresh.
- **Feishu API unavailable** → `runCatching` swallows; local write already done.
