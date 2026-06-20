## Context

`SummaryViewModel` currently reads `repository.messages(key).first()` at generate time, filters by range, and summarizes all of it. Messages now carry a stable `id` (from `add-long-press-delete`), so individual selection is addressable. The summary screen needs a checklist between the range selector and the generate button.

## Goals / Non-Goals

**Goals:**
- In-range messages shown as a checklist (default all selected); summarize only selected.
- Range change re-defaults selection to all; select-all / clear-all actions.
- Reuse the existing prompt/range/summary plumbing.

**Non-Goals:**
- Selection persistence across navigation; search/filter within the checklist.

## Decisions

**1. ViewModel holds messages reactively + a selected-id set.**
```kotlin
private val allMessages = repository.messages(key).stateIn(...)      // live
private val range = MutableStateFlow<TimeRange>(All)
val candidates = combine(allMessages, range){ a,r -> filterByRange(a,r,now()) }.stateIn(...)
private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
```
A collector resets `selectedIds` to all candidate ids whenever `candidates` changes (range change / new messages) so the default is "all selected". `toggle(id)`, `selectAll()`, `clearAll()` mutate `selectedIds`. `uiState` combines range + candidates + selectedIds + status.

**2. generate() summarizes the selected subset.**
`val selected = candidates.value.filter { it.id in selectedIds.value }`; if empty → `Empty` status (no API call); else resolve the app prompt (as today) and summarize `selected`.

**3. Screen layout.**
Range selector → checklist (LazyColumn of message rows with a leading `Checkbox`, body + time; keyed by id) with a select-all/clear-all row → generate button → status/result. Keep the not-configured / loading / error / success handling.

## Risks / Trade-offs

- **Large conversations** → checklist is a `LazyColumn` (virtualized); the existing prompt cap still applies on the selected set.
- **Selection reset on range change** → intentional (range defines the candidate set); documented.

## Open Questions

- Persist manual selection when toggling range back and forth? (No for v1 — range change resets to all.)
