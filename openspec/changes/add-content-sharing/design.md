## Context

`add-time-range-delete` provides `TimeRange` + `filterByRange` + `TimeRangeSelector`. The summary flow already formats transcripts internally (`SummaryPromptBuilder`). This change adds outward sharing: export a transcript over a range to the system share sheet, plus share the AI summary. It realizes the `summary-sharing` seam from `design-extensions`.

## Goals / Non-Goals

**Goals:**
- A pure, testable `TranscriptExporter` (range already applied by caller; cap + formatting + optional header).
- A configurable, persisted instruction header.
- One share helper used for both transcript and summary.
- Reuse `TimeRangeSelector`; no new range concept.

**Non-Goals:**
- Direct ChatGPT API calls (the share sheet decouples us from any specific app), file export, rich formats.

## Decisions

**1. Export = instruction header + transcript; share = ACTION_SEND of that text.**
"Export" and "share" are one flow: build text, hand to `Intent.ACTION_SEND` via `Intent.createChooser`. The user picks ChatGPT/any app. No coupling to a specific recipient.

**2. `TranscriptExporter` is pure; the caller pre-filters by range.**
`export(title, messages, instructionHeader, max, zone): String`. The ViewModel applies `filterByRange` first (so export and summary share the same range semantics), then passes the filtered list. Keeps the exporter trivially unit-testable.

**3. Instruction header in its own `ExportSettings` (DataStore), edited in the Settings screen.**
Default: a concise summarize prompt. Separate from `ApiSettings` to keep concerns clean, but surfaced on the same settings screen. Blank header → transcript only.

**4. Reuse the summary's cap rationale.**
Cap most-recent N (default 500 for export — higher than summary's 200 since the recipient model may handle more; still bounded).

**5. Share both raw transcript (detail) and AI summary (summary screen).**
Detail overflow → "导出/分享" → range dialog → share transcript. Summary success → "分享" → share summary text. Same `ShareHelper.shareText`.

## Risks / Trade-offs

- **Very large transcripts** → capped; documented in spec.
- **Privacy** → user-initiated, explicit recipient; consistent with existing summary egress note.
- **Empty range** → caller checks and informs the user; no empty share sheet.

## Open Questions

- Offer Markdown vs plain text? (Plain text only for v1; most chat apps accept it.)
