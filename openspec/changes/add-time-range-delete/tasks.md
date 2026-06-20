## 1. TimeRange foundation

- [x] 1.1 Add sealed `TimeRange` (All / Today / Last7Days / Last30Days / Custom(start,end)) with a `bounds(now, zone)` returning [startInclusive, endExclusive)
- [x] 1.2 Add pure `filterByRange(messages, range, now, zone)` using `bounds`; remove the old `MessageRange` enum
- [x] 1.3 Migrate summary (`SummaryViewModel`/`SummaryScreen`) to `TimeRange`

## 2. Range-based deletion (data layer)

- [x] 2.1 Add `MessageDao.deleteInRange(start, end)` and `deleteInRange(conversationKey, start, end)`
- [x] 2.2 Add `MessageRepository.deleteByRange(range, now)` and `deleteByRange(conversationKey, range, now)` computing bounds

## 3. Reusable selector UI

- [x] 3.1 Add a `TimeRangeSelector` composable: preset chips + a custom date-range picker emitting a `TimeRange`

## 4. Delete UX

- [x] 4.1 Home: replace "清空所有数据" with a range-aware delete (selector + confirmation naming the range) calling global `deleteByRange`
- [x] 4.2 Conversation detail: add an overflow "删除本会话" with range selector + confirmation calling per-conversation `deleteByRange`

## 5. Tests & verification

- [x] 5.1 Unit tests for `TimeRange.bounds` + `filterByRange` (presets with fixed now; custom [start,end) inclusivity)
- [x] 5.2 Room DAO tests for `deleteInRange` (global keeps out-of-range; per-conversation leaves other conversations)
- [x] 5.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-time-range-delete --strict`
