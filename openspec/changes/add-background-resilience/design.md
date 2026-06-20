## Context

The `NotificationListenerService` is bound by the system while access is granted, but the app process can still be killed by Doze, App Standby, low-memory, or aggressive OEM task-killers — which stops capture until the user reopens the app. The user wants the app to be hard to kill and to expose the OS-level controls that actually matter (battery-optimization exemption, OEM autostart). This change adds a foreground service plus those controls.

Constraints:
- Android 14+ requires every foreground service to declare a `foregroundServiceType`.
- A foreground service must show a persistent notification — this is unavoidable and user-visible.
- OEM autostart screens are undocumented, vary by brand/version, and may not exist; deep-links must be best-effort with graceful fallback.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` direct-prompt intent is allowed but Play-policy-sensitive; acceptable for this app's use case.

## Goals / Non-Goals

**Goals:**
- A toggleable foreground service that keeps the process warm, restarts on boot, and is `START_STICKY`.
- Persisted keep-alive preference (DataStore).
- Battery-optimization status + request action.
- Manufacturer-aware autostart guidance with safe fallback.
- Pure, unit-testable logic for OEM detection and the keep-alive preference.

**Non-Goals:**
- Guaranteeing the app can never be killed — impossible on Android; we maximize survival, not promise it.
- Re-implementing capture (done in `add-notification-capture`).
- A full settings screen redesign (controls are added to the existing home/settings surface).

## Decisions

**1. Foreground service with `foregroundServiceType="specialUse"`.**
The service keeps the app process warm so the notification listener survives — no standard FGS type truly matches this, making `specialUse` the honest choice (declared with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`). Critically, `specialUse` is also one of the few types that Android 14/15 still permit a `BOOT_COMPLETED` receiver to start; `dataSync` (the initial pick) is explicitly **disallowed** from boot on Android 15 and throws `ForegroundServiceStartNotAllowedException`, which would break restart-on-boot. Trade-off: `specialUse` is Play-review-sensitive and must be justified at submission — acceptable for this app's purpose. `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` exists from API 34, so the typed `startForeground` overload is used only on API 34+; older versions use the untyped overload.

**2. Keep-alive is opt-in and persisted in DataStore.**
The persistent notification is intrusive, so keep-alive defaults to disabled and is user-controlled. DataStore (Preferences) chosen over SharedPreferences for coroutine/Flow ergonomics consistent with the rest of the app. The boot receiver reads this preference before starting anything.

**3. `START_STICKY` + boot receiver for restart coverage.**
`START_STICKY` covers OS-initiated kills; `BootReceiver` covers reboots. Together they cover the common death paths. The receiver is gated on the persisted preference so a user who disabled keep-alive is not surprised by a service after reboot.

**4. OEM autostart handled by a pure mapping + runtime resolvability check.**
`AutostartHelper` maps `Build.MANUFACTURER` to a list of candidate component intents (Xiaomi/Huawei/OPPO/vivo/etc.). The mapping itself is a pure function (testable without a device); resolvability (`PackageManager.resolveActivity`) is checked at call time. If nothing resolves → return a "no direct screen" result and show guidance. This isolates the brittle, device-specific knowledge in one tested place.

**5. Battery-optimization check wraps `PowerManager.isIgnoringBatteryOptimizations`.**
Thin helper; the request uses `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`. Status is re-checked on screen resume (same pattern as notification access in Change 1).

## Risks / Trade-offs

- **OEM autostart intents break across versions / don't exist** → pure mapping + runtime `resolveActivity` guard + guidance fallback; never launch an unresolved intent.
- **Foreground-service-type policy (Android 14+) / Play review** → declare `dataSync`; documented escape hatch to `specialUse`.
- **Persistent notification annoys users** → keep it low-importance (silent), opt-in, and clearly labeled.
- **Battery-exemption prompt is policy-sensitive** → gated behind an explicit user action, not auto-triggered.

## Migration Plan

Additive: new service, receiver, preference store, helpers, UI controls. No schema or data migration. Rollback = disable keep-alive / remove the service registration; capture (Change 1) is unaffected either way.

## Open Questions

- Should keep-alive be auto-suggested on first launch, or remain purely opt-in? (Current: opt-in.)
- Is `dataSync` durable against future Play policy, or should we pre-emptively adopt `specialUse`? (Deferred; revisit before public release.)
