## 1. Per-app prompt storage

- [x] 1.1 Add `PromptSettings` (DataStore, key `prompt_<package>`): `promptFor(pkg): Flow<String>`, suspend `currentPrompt(pkg)`, suspend `setPrompt(pkg, value)`, `DEFAULT_PROMPT` (= the previous summarize instruction)
- [x] 1.2 Remove `ExportSettings`; update all references

## 2. Wire summary + export to the app prompt

- [x] 2.1 `SummaryPromptBuilder.build(...)` takes a `systemPrompt: String` param (no hardcoded prompt)
- [x] 2.2 `SummaryViewModel.generate` resolves the conversation's app prompt (first message's sourcePackage → `PromptSettings.currentPrompt`, fallback default) and passes it
- [x] 2.3 `ConversationDetailViewModel.export` uses `PromptSettings.currentPrompt(sourcePackage)` as the header (drop `ExportSettings`)
- [x] 2.4 Remove the export-instruction field from `SettingsScreen`/`SettingsViewModel`

## 3. Prompt config screen

- [x] 3.1 Add `PromptConfigViewModel` exposing the app list (`repository.apps`) and per-app current prompt
- [x] 3.2 Add `PromptConfigScreen`: one editable prompt field per app with save; route `prompt-config`; drawer item "Prompt 配置"

## 4. Tests & verification

- [x] 4.1 Unit tests: `PromptSettings` default + per-app set/round-trip (temp DataStore); `SummaryPromptBuilder` uses the passed system prompt
- [x] 4.2 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-per-app-prompt --strict`
