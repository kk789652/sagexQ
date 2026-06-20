## 1. Export logic & settings

- [x] 1.1 Add pure `TranscriptExporter.export(title, messages, instructionHeader, max, zone)` — optional header + `[time] sender: body` lines, capped to most-recent N
- [x] 1.2 Add `ExportSettings` (DataStore) exposing `Flow<String>` instruction header (default summarize prompt) + suspend `setInstruction`/`current`
- [x] 1.3 Surface the instruction header field on the Settings screen (edit + save)

## 2. Share helper

- [x] 2.1 Add `ShareHelper.shareText(context, text)` using `ACTION_SEND` + `Intent.createChooser`

## 3. Entry points

- [x] 3.1 Conversation detail: overflow "导出/分享" → range selector → build transcript (filtered by range + header) → share; inform if range is empty
- [x] 3.2 Summary screen: add a "分享" action on a successful summary that shares the summary text

## 4. Tests & verification

- [x] 4.1 Unit tests for `TranscriptExporter` (header included/omitted; cap trims to most recent; line format; empty list)
- [x] 4.2 Unit tests for `ExportSettings` default + round-trip (temp DataStore)
- [x] 4.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-content-sharing --strict`
