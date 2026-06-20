## Why

The summary page currently summarizes everything in the chosen time range. The user wants finer control: after narrowing by time range, pick exactly which messages to include before generating. This makes summaries precise (drop noise, focus on specific messages).

## What Changes

- The summary page shows the in-range messages as a checklist (each message selectable), defaulting to all selected.
- Changing the time range refreshes the checklist (re-defaults to all selected).
- "生成总结" summarizes only the selected messages; if none are selected (or none in range), it does not call the API and informs the user.
- Add select-all / clear-all convenience for the checklist.

## Capabilities

### New Capabilities
- `summary-selection`: On the summary page the user selects which in-range messages to include before generating the summary.

### Modified Capabilities
<!-- ai-summary now summarizes the selected subset rather than the whole range. -->

## Impact

- **Code**: `SummaryViewModel` exposes the in-range candidate messages + a selected-id set (toggle, select-all, clear-all); `generate` uses the selected subset. `SummaryScreen` renders the checklist (range selector + per-message checkboxes + generate + result).
- **Risk**: low–moderate (summary screen rework); summarization/prompt path unchanged underneath.
