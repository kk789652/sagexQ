## Context

`SummaryPromptBuilder` hardcodes a system prompt; `ExportSettings` holds one global instruction header (edited in API settings). The user wants one configurable prompt per source app, shared by summary and export. Each conversation/message carries `sourcePackage`, so the app is known at summary/export time.

## Goals / Non-Goals

**Goals:**
- One editable prompt per app (DataStore), default preserves current behavior.
- A drawer "Prompt 配置" screen to edit per-app prompts.
- Summary + export both source the prompt from the conversation's app.

**Non-Goals:**
- Per-conversation prompts; prompt templating/variables.

## Decisions

**1. `PromptSettings` replaces `ExportSettings`.**
DataStore keyed `prompt_<package>`; `promptFor(pkg): Flow<String>` (default `DEFAULT_PROMPT`), `currentPrompt(pkg)`, `setPrompt(pkg, value)`. `DEFAULT_PROMPT` = the previous summarize instruction so existing behavior is unchanged. `ExportSettings` and its API-settings field are removed.

**2. `SummaryPromptBuilder.build(conversationTitle, messages, systemPrompt, max, zone)`.**
The system prompt becomes a parameter. `SummaryViewModel.generate` resolves the conversation's app prompt: read the first message's `sourcePackage`, `promptSettings.currentPrompt(pkg)`, pass it in.

**3. Export uses the app prompt as header.**
`TranscriptExporter.export(...)` already takes `instructionHeader`; `ConversationDetailViewModel.export` now reads `promptSettings.currentPrompt(sourcePackage)` (from the loaded messages) instead of `ExportSettings`.

**4. Prompt config screen lists apps.**
`PromptConfigViewModel` exposes `repository.apps` (AppSummary list) plus current prompt per app; the screen renders one editable field per app with save. Route `prompt-config`; drawer item added.

## Risks / Trade-offs

- **Default migration** → default prompt equals the old hardcoded one, so summaries are unchanged until customized.
- **Empty conversation (no sourcePackage)** → fall back to the default prompt.
- **Removing ExportSettings** → update all references (SettingsScreen field, ConversationDetailViewModel, tests).

## Open Questions

- Show a global/default prompt editor in addition to per-app? (Per-app only for now; default is a constant.)
