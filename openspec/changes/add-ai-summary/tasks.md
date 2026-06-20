## 1. API settings (DataStore)

- [x] 1.1 Add `ApiConfig(baseUrl, apiKey, model)` with an `isConfigured` (all non-blank) check
- [x] 1.2 Add `ApiSettings` backed by the existing `DataStore<Preferences>` exposing `Flow<ApiConfig>` and a suspend `save(config)`
- [x] 1.3 Add `SettingsScreen` + `SettingsViewModel` (fields for base URL / key / model, save), reachable via a new `settings` route and a home entry

## 2. Pure summary logic

- [x] 2.1 Add `MessageRange` (ALL / TODAY / LAST_7_DAYS) and a pure `filterByRange(messages, range, now)`
- [x] 2.2 Add `SummaryPromptBuilder` producing the system + user chat messages from `CapturedMessage`s, capped to the most recent N, formatting each line as `[time] sender: body`

## 3. OpenAI-compatible client

- [x] 3.1 Add serializable DTOs: `ChatCompletionRequest`/`ChatMessage`/`ChatCompletionResponse`
- [x] 3.2 Add `OpenAiClient.summarize(config, messages)` calling `POST {baseUrl}/chat/completions` on `Dispatchers.IO`, returning a typed result; non-2xx and malformed responses become readable errors
- [x] 3.3 Provide OkHttp + Json + client via Hilt

## 4. Summary flow UI

- [x] 4.1 Add `SummaryViewModel`: reads conversation messages, applies range filter + prompt builder, calls the client, exposes loading/success/error state; blocks when not configured
- [x] 4.2 Add `SummaryScreen`: range selector, generate button, loading/result/error display, copy action; "not configured" routes to settings
- [x] 4.3 Add a `summary/{conversationKey}` route and a "总结" action in the conversation detail top bar

## 5. Tests & verification

- [x] 5.1 Unit tests for `filterByRange` (ALL/TODAY/LAST_7_DAYS boundaries with a fixed `now`)
- [x] 5.2 Unit tests for `SummaryPromptBuilder` (cap trims to most recent N; line formatting; empty input)
- [x] 5.3 Unit tests for `ApiConfig.isConfigured` and `ChatCompletionResponse` parsing (content extraction; missing choices)
- [x] 5.4 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-ai-summary --strict`
