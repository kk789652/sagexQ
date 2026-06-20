## 1. ViewModel: candidates + selection

- [x] 1.1 `SummaryViewModel` holds live conversation messages; derive `candidates` = filterByRange(messages, range, now) as StateFlow
- [x] 1.2 Maintain a `selectedIds: Set<Long>` that resets to all candidate ids when candidates change; add `toggle(id)`, `selectAll()`, `clearAll()`
- [x] 1.3 `uiState` exposes range, candidates, selectedIds, status
- [x] 1.4 `generate()` summarizes only candidates whose id ∈ selectedIds; empty selection → `Empty` (no API call), keeping app-prompt resolution

## 2. Screen: checklist

- [x] 2.1 `SummaryScreen`: range selector → message checklist (LazyColumn, leading `Checkbox`, body + time, keyed by id) → select-all/clear-all row → generate → status/result
- [x] 2.2 Preserve not-configured / loading / error / success + copy/share

## 3. Tests & verification

- [x] 3.1 Unit tests (where pure-testable): selection toggle/select-all/clear-all logic; generate uses only selected ids (extract a pure helper if needed for the subset selection)
- [x] 3.2 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-summary-selection --strict` (skipped per instruction: do not run gradle/build/tests)
