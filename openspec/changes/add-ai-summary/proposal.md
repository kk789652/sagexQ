## Why

The whole point of SageX is to turn captured chatter into something digestible. Capture (Change 1), persistence, and browsing (Change 3) are in place; now we add the headline capability: AI summarization of a conversation over a chosen time range, using a user-configured OpenAI-compatible API. This is the feature users actually open the app for.

## What Changes

- Add API settings: a settings screen to configure an OpenAI-compatible endpoint — base URL, API key, and model name — persisted locally (DataStore).
- Add an OpenAI-compatible chat client that calls `POST {baseUrl}/chat/completions` and returns the assistant's text, with typed request/response models and error handling.
- Add a summary flow reachable from a conversation: the user picks a time range (全部 / 今天 / 近 7 天), taps generate, and sees the produced summary.
- Build the summarization prompt from the conversation's messages in the selected range (pure, testable), bounded to avoid oversized requests.
- Gate summarization on configuration: if the API is not configured, route the user to settings instead of failing silently.
- Surface request state (loading / success / error) and allow copying the summary text.

Out of scope: streaming responses, sharing the summary via the system share sheet (`design-extensions`), automatic/scheduled summaries, multi-conversation summaries.

## Capabilities

### New Capabilities
- `api-settings`: Configuring and persisting an OpenAI-compatible endpoint (base URL, API key, model).
- `ai-summary`: Producing an AI summary of a conversation over a selected time range via the configured API.

### Modified Capabilities
<!-- None. Reads conversation messages via existing message-storage queries. -->

## Impact

- **New code**: `ApiSettings` (DataStore) + settings screen/ViewModel; `OpenAiClient` + DTOs; `SummaryPromptBuilder` + `MessageRange` (pure); summary screen/ViewModel; nav routes + entry points.
- **Dependencies**: OkHttp + kotlinx.serialization (already in the build).
- **Permissions**: `INTERNET` (already declared).
- **Privacy**: when the user runs a summary, the selected messages are sent to the user's configured endpoint — this is the first network egress of message content; it is explicit and user-initiated, and the destination is the user's own API.
- **Security**: API key stored locally in DataStore (unencrypted for v1; encryption-at-rest deferred).
- **Risk**: endpoint/format variance across OpenAI-compatible providers; the client must handle non-2xx and malformed responses gracefully. HTTPS assumed (cleartext/local endpoints noted as a follow-up).
