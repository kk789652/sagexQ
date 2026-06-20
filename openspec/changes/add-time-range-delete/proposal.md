## Why

Summary already filters by a fixed set of ranges, but the user wants richer time-range control (including custom start/end) and wants the same time-range concept reused for deletion — currently only an all-or-nothing "clear all" exists. A reusable time range, plus range-based deletion (global and per-conversation), is the foundation; export/sharing over a range builds on it next.

## What Changes

- Introduce a reusable `TimeRange` (presets All / Today / Last 7 days / Last 30 days, plus a Custom start–end) and a pure `filterByRange`, replacing the previous fixed `MessageRange` enum.
- Add a reusable Compose time-range selector (preset chips + a custom date-range picker).
- Migrate the AI summary flow to the new `TimeRange` (so summaries gain custom ranges).
- Add range-based deletion in the data layer (delete messages within a range, globally or for one conversation).
- Replace the home "clear all" with a range-aware delete (choose a range, then confirm), and add a per-conversation range delete in the conversation detail screen.

Out of scope: export/sharing of content (next change, `add-content-sharing`), which will reuse the same `TimeRange` and selector.

## Capabilities

### New Capabilities
- `time-range-selection`: A reusable time range (presets + custom start/end) selectable in the UI and applied consistently across features.
- `time-range-deletion`: Deleting captured messages within a chosen time range, either globally or for a single conversation.

### Modified Capabilities
<!-- ai-summary keeps its requirements; it simply adopts the richer TimeRange (implementation detail). -->

## Impact

- **New/changed code**: `TimeRange` + `filterByRange` (replaces `MessageRange`); `TimeRangeSelector` composable; `MessageDao`/`MessageRepository` range-delete queries; home delete UX; conversation-detail delete; summary screen/VM migrated to `TimeRange`.
- **Tests**: `filterByRange` (incl. custom), range-delete DAO tests; existing range tests updated.
- **Data**: deletion is permanent (consistent with existing clear-all); no migration.
- **Risk**: low–moderate; deletion is destructive, so it is always behind an explicit range + confirmation dialog.
