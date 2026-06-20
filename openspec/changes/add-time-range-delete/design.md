## Context

`MessageRange` (enum: ALL/TODAY/LAST_7_DAYS) + `filterByRange(now)` exist and are used by the summary flow. The user wants custom start/end ranges and the same range concept reused for deletion. Deletion today is only `MessageRepository.clearAll()`.

## Goals / Non-Goals

**Goals:**
- One reusable `TimeRange` (presets + custom) with a pure, testable filter and bounds.
- A reusable Compose selector (chips + custom date-range picker).
- Range-based deletion: global and per-conversation, behind confirmation.
- Migrate summary to the new range with minimal churn.

**Non-Goals:**
- Export/sharing (next change). Undo for deletes (deletes remain permanent, as today).

## Decisions

**1. Replace the `MessageRange` enum with a sealed `TimeRange`.**
```kotlin
sealed interface TimeRange {
    data object All; data object Today; data object Last7Days; data object Last30Days
    data class Custom(val startMillis: Long, val endMillis: Long) : TimeRange // [start, end)
}
fun TimeRange.bounds(now: Long, zone: ZoneId): Pair<Long, Long> // (startInclusive, endExclusive)
fun filterByRange(messages, range, now, zone): List<CapturedMessage>
```
Bounds drive both filtering and SQL deletion, so they stay consistent. `All` → `(0, Long.MAX_VALUE)`; `Today` → `[startOfDay, now+1)`; lastN → `[now - N*day, now+1)`; `Custom` → its bounds (end made exclusive at end-of-day). Pure + deterministic via injected `now`/`zone`.

**2. Deletion uses the same bounds as filtering.**
`MessageDao.deleteInRange(start, end)` and `deleteInRange(conversationKey, start, end)`; `MessageRepository.deleteByRange(range, now)` / `deleteByRange(key, range, now)` compute bounds and delegate. This guarantees "what you see filtered is what you delete".

**3. Reusable `TimeRangeSelector` composable.**
Preset chips + a "自定义" chip that opens a Material3 `DateRangePicker`. Emits the chosen `TimeRange`. Used by summary (now), delete dialogs (now), and export (next change).

**4. Delete is always confirmed and range-scoped.**
Home "清空数据" → range selector + confirm → global `deleteByRange`. Conversation detail overflow → "删除本会话" → range selector + confirm → per-conversation `deleteByRange`. Confirmation dialog states the range.

## Risks / Trade-offs

- **Destructive** → always behind explicit range + confirmation; copy names the range.
- **Custom end boundary off-by-one** → end is normalized to end-of-day exclusive; covered by unit tests on bounds/filter.
- **Migration churn** → summary VM/screen switch from `MessageRange` to `TimeRange`; tests updated alongside.

## Open Questions

- Should a global "delete older than N days" maintenance option be offered? (Deferred; presets + custom cover it.)
