## Why

The summary prompt is hardcoded and the export instruction is a single global string. The user wants the prompt to be manually configurable and customizable per app, so different sources can be summarized/exported with tailored instructions. One editable prompt per app, shared by AI summary and export.

## What Changes

- Add per-app prompt storage (keyed by source package) with a sensible default.
- Add a "Prompt 配置" screen (reached from the drawer) listing apps with captured messages and letting the user edit each app's prompt.
- AI summary uses the conversation's app prompt as the system instruction (instead of the hardcoded one); `SummaryPromptBuilder` takes the system prompt as a parameter.
- Export/share uses the same app prompt as the instruction header (replacing the global `ExportSettings`).
- Remove the global export-instruction field from API settings (superseded by per-app prompts).

## Capabilities

### New Capabilities
- `per-app-prompt`: A configurable prompt per source app, used as the system instruction for AI summary and as the header for export/share.

### Modified Capabilities
<!-- ai-summary / content-export-sharing now source their prompt per app; no new external behavior beyond the prompt source. -->

## Impact

- **Data**: new `PromptSettings` (DataStore, key per package) with `promptFor(pkg): Flow<String>`, `setPrompt(pkg, value)`, default constant; removes `ExportSettings`.
- **Code**: `SummaryPromptBuilder.build(...)` gains a `systemPrompt` param; `SummaryViewModel` resolves the conversation's app prompt; `ConversationDetailViewModel.export` uses the app prompt; new `PromptConfigScreen` + ViewModel + drawer entry + route; `SettingsScreen` drops the instruction field.
- **Risk**: moderate (touches summary/export prompt sourcing); behavior otherwise unchanged. Default prompt preserves current summarize behavior.
