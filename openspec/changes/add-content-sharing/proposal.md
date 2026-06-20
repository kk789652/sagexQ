## Why

The user wants to export a conversation's content over a time range and send it to ChatGPT (or any app) to summarize externally, in addition to the built-in API summary. The cleanest, decoupled way is the Android system share sheet (`ACTION_SEND`) — this realizes the sharing extension point reserved in `design-extensions`, built on the `TimeRange` foundation from `add-time-range-delete`.

## What Changes

- Add a pure `TranscriptExporter` that renders a conversation's messages (over a chosen range, capped) into plain text, optionally prefixed with a configurable instruction header (default: a summarize prompt) so the recipient app can act on it directly.
- Add a configurable export instruction header in settings (editable; sensible default).
- Add a share helper that sends text via the system share sheet (`ACTION_SEND`, `text/plain`).
- Conversation detail: add "导出/分享" (pick a time range → share the transcript). Reuses the existing `TimeRangeSelector`.
- Summary screen: add a "分享" action that shares the produced AI summary text.

Out of scope: file/attachment export, sharing images, direct ChatGPT API integration (handled by the share sheet instead).

## Capabilities

### New Capabilities
- `content-export-sharing`: Exporting a conversation's transcript over a time range and sharing it (and sharing an AI summary) via the system share sheet, with a configurable instruction header.

### Modified Capabilities
<!-- None. Reuses TimeRange (add-time-range-delete) and message-storage queries. -->

## Impact

- **New code**: `TranscriptExporter` (pure); `ExportSettings` (DataStore) + settings field; `ShareHelper`; conversation-detail export entry; summary share button.
- **Dependencies**: none new (ACTION_SEND is platform).
- **Privacy**: sharing sends the selected content to whichever app the user picks — explicit, user-initiated; consistent with the existing AI-summary egress note.
- **Risk**: low; additive UI + a pure exporter.
