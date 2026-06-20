## Context

Conversations and their messages are available via `MessageRepository`. This change adds outbound AI: configurable endpoint + a chat client + a summary flow. OkHttp and kotlinx.serialization are already on the classpath. This is the first feature to send message content off-device, so configuration gating and explicit user initiation matter.

## Goals / Non-Goals

**Goals:**
- Persisted, validated OpenAI-compatible API settings.
- A thin chat client (`/chat/completions`) with typed DTOs and graceful error handling.
- A manual summary flow with range selection, bounded prompt, and clear loading/success/error states.
- Keep prompt building and range filtering pure and unit-tested.

**Non-Goals:**
- Streaming, system-share of summaries (next change), scheduled/auto summaries, multi-conversation summaries, on-device models.

## Decisions

**1. OpenAI Chat Completions wire format.**
Target `POST {baseUrl}/chat/completions` with `{ model, messages: [{role, content}], temperature }` and `Authorization: Bearer {key}`, parsing `choices[0].message.content`. This is the de-facto standard supported by OpenAI, and most compatible providers (vLLM, Ollama, LM Studio, DeepSeek, etc.). `baseUrl` is stored without a trailing slash; the client appends `/chat/completions`.

**2. Summary input = system prompt + one user message containing the transcript.**
Rather than mapping each captured message to a chat turn (roles would be meaningless — they're all "received"), we render the transcript into a single user message ("Summarize the following chat...") with a system instruction. `SummaryPromptBuilder` (pure) produces the `messages` list from `CapturedMessage`s, formatting each line as `[time] sender: body`. Pure → unit-testable without network.

**3. Range filtering is a pure function over a supplied `now`.**
`MessageRange` ∈ {ALL, TODAY, LAST_7_DAYS}; `filterByRange(messages, range, now)` returns the in-range subset. Passing `now` keeps it deterministic and testable.

**4. Bounded prompt.**
Cap at the most recent N messages (e.g. 200) within range to avoid oversized/expensive requests; the builder takes the cap and trims to the most recent. Documented limit, surfaced in the spec.

**5. Settings stored in DataStore alongside keep-alive.**
Reuse the existing `DataStore<Preferences>`. `ApiSettings` exposes a `Flow<ApiConfig>` and `isConfigured` (all three fields non-blank). API key unencrypted for v1 (deferred; noted).

**6. Client returns a typed result, not exceptions, to the ViewModel.**
`OpenAiClient.summarize(...)` runs on `Dispatchers.IO` and returns `Result<String>` (or a sealed outcome); the ViewModel maps it to loading/success/error UI state. Keeps error handling explicit and the UI crash-free.

## Risks / Trade-offs

- **Provider format variance** → stick to the OpenAI standard; tolerate missing optional fields in the response; treat any non-2xx as a readable error.
- **HTTPS assumption** → cleartext/local endpoints (http://LAN) are not supported in v1 without a network-security-config change; documented as a follow-up.
- **Privacy: content leaves the device** → only on explicit user action, only the selected range, only to the user's own configured endpoint; stated in the proposal.
- **API key at rest** → unencrypted in DataStore for v1; encryption-at-rest deferred (same open question as message storage).

## Open Questions

- Encrypt the API key (and stored messages) at rest now or in a later hardening change? (Deferred.)
- Support cleartext/local endpoints via a user-opt-in network-security exception? (Deferred to follow-up.)
