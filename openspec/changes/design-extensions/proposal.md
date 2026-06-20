## Why

Two capabilities were called out from day one as future directions: (1) sharing a summary through the system share sheet, and (2) extending capture beyond WeChat to other apps. We do not want to build them yet, but we do want their contracts and seams designed now, so later implementation is a small, low-risk change rather than a refactor. This change is **design-only**: it records the intended capabilities and the extension points, and adds no production behavior.

## What Changes

- Document the **summary sharing** capability: how a generated summary will be sent through Android's `ACTION_SEND` share sheet, and the seam where it plugs into the existing summary screen.
- Document the **multi-source capture** capability: how additional source apps will be added behind the existing `NotificationParser` abstraction, what stays source-agnostic, and what each new source must provide.
- Confirm and record the seams already reserved in code (the `NotificationParser` `@IntoSet` binding and the `sourcePackage` field) so future work knows what is already in place.
- No new screens, services, or logic are implemented in this change.

## Capabilities

### New Capabilities
- `summary-sharing`: Sharing a produced summary to other apps via the system share sheet. **Design only — not implemented in this change.**
- `multi-source-capture`: Capturing notifications from source apps beyond WeChat through the existing parser abstraction. **Design only — not implemented in this change.**

### Modified Capabilities
<!-- None. No existing behavior changes. -->

## Impact

- **Code**: none in this change (design only). Implementation will land in dedicated future changes (`add-summary-sharing`, `add-source-<app>`).
- **Docs**: `design.md` captures the interfaces and integration points; `specs/` records the future behavioral contracts so they are ready to implement.
- **Risk**: none — no runtime change. The value is reduced future risk by validating the seams now.
