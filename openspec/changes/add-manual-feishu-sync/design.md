## Context

`MessageEntity` has no sync-state field; `MessageRepository.insert` fires a `runCatching` sync and discards the result. We need to close the feedback loop: on success, mark the row; on failure, leave it unsynced for manual retry.

## Decisions

**1. Room migration v1→v2.**
Add column: `ALTER TABLE messages ADD COLUMN feishuSynced INTEGER NOT NULL DEFAULT 0`.
`SageXDatabase`: `version = 2`, `Migration(1, 2)` object. All existing rows default to `feishuSynced = false` — they'll be eligible for the first manual sync.

**2. `MessageEntity.feishuSynced: Boolean = false`.**
Mapped column. `toDomain()` does not expose it (sync state is internal to data layer).

**3. Two new DAO methods.**
```kotlin
@Query("UPDATE messages SET feishuSynced = 1 WHERE id = :id")
suspend fun markSynced(id: Long)

@Query("SELECT * FROM messages WHERE feishuSynced = 0 ORDER BY postedAt ASC")
suspend fun unsyncedMessages(): List<MessageEntity>
```

**4. `MessageRepository.insert` closes the loop.**
```kotlin
val id = dao.insert(entity)
applicationScope.launch {
    runCatching { feishuSync.syncMessage(entity.copy(id=id).toDomain()) }
        .onSuccess { dao.markSynced(id) }
        .onFailure { Log.w(TAG, "Feishu sync failed", it) }
}
```

**5. `MessageRepository.syncUnsynced(): Int`.**
```kotlin
suspend fun syncUnsynced(): Int {
    val rows = dao.unsyncedMessages()
    var count = 0
    rows.forEach { entity ->
        runCatching { feishuSync.syncMessage(entity.toDomain()) }
            .onSuccess { dao.markSynced(entity.id); count++ }
            .onFailure { Log.w(TAG, "Manual sync failed for ${entity.id}", it) }
    }
    return count   // number successfully pushed
}
```

**6. `HomeViewModel` sync state.**
```kotlin
sealed interface FeishuSyncStatus { object Idle; object Syncing; data class Done(val count: Int) }
private val _syncStatus = MutableStateFlow<FeishuSyncStatus>(Idle)
// exposed in HomeUiState
fun syncToFeishu() {
    if (_syncStatus.value is Syncing) return
    viewModelScope.launch {
        _syncStatus.value = Syncing
        val n = repository.syncUnsynced()
        _syncStatus.value = Done(n)
        delay(3000); _syncStatus.value = Idle  // auto-reset after 3s
    }
}
```

**7. Drawer item label reflects status.**
- Idle: "同步到飞书"
- Syncing: "同步中…"
- Done(n): "已同步 $n 条"
