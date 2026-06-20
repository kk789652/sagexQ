## 1. Design documentation (this change)

- [x] 1.1 Document the summary-sharing interface and its integration point in the summary screen (design.md)
- [x] 1.2 Document the multi-source capture contract and confirm the seams already present in code (design.md)
- [x] 1.3 Capture future behavioral contracts as specs (`summary-sharing`, `multi-source-capture`)
- [x] 1.4 Validate: `openspec validate design-extensions --strict`

## 2. Deferred implementation (future changes — NOT in this change)

- [ ] 2.1 `add-summary-sharing`: add a share action to `SummaryScreen` using `ACTION_SEND`
- [ ] 2.2 `add-source-<app>`: add a `NotificationParser` + `@IntoSet` binding for a new source app, with parser unit tests

> Section 2 is intentionally left unchecked: it scopes future changes and is out of scope for this design-only change.
