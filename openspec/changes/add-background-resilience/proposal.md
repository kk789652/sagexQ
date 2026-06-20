## Why

Capture is only useful if it keeps running. Android OEMs (Xiaomi, Huawei, OPPO, vivo, Samsung) aggressively kill background apps, and Doze/App Standby can suspend the process — silently stopping notification capture. The user requires background persistence and whitelist setup "from the start". This change makes SageX hard to kill and gives the user the controls (battery whitelist, autostart) needed to keep capture alive.

## What Changes

- Add a user-toggleable foreground service (`SageXForegroundService`) that runs with a persistent, low-importance notification, keeping the app process warm so it is far less likely to be killed.
- Start the service automatically on device boot via a `BootReceiver` (`RECEIVE_BOOT_COMPLETED`), when keep-alive is enabled.
- Use `START_STICKY` and self-restart semantics so the OS restarts the service if it is killed under memory pressure.
- Persist the keep-alive preference (DataStore) so the choice survives restarts.
- Add a battery-optimization helper: detect whether the app is exempt and provide the system request/settings intent to add it to the whitelist.
- Add manufacturer-aware autostart guidance: detect the OEM and resolve a best-effort intent to its autostart/background settings screen, with fallback guidance text.
- Surface all of the above in the UI: a "keep capture alive" toggle, battery-whitelist status + action, and an autostart-guidance action.

Out of scope: the notification capture pipeline itself (`add-notification-capture`, done), conversation UI, and AI.

## Capabilities

### New Capabilities
- `background-service`: Foreground service lifecycle, persistent notification + channel, boot restart, and the keep-alive preference.
- `process-persistence`: User-facing OS-level survival controls — battery-optimization exemption and manufacturer autostart guidance.

### Modified Capabilities
<!-- None. notification-capture is unchanged; this change runs alongside it. -->

## Impact

- **New code**: `SageXForegroundService`, `BootReceiver`, `KeepAlivePreferences` (DataStore), `BatteryOptimizationHelper`, `AutostartHelper` (+ OEM mapping), UI controls + ViewModel wiring.
- **Manifest**: declare the foreground service (`foregroundServiceType="specialUse"` + subtype property) and the boot receiver; permissions `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
- **Dependencies**: DataStore (already in build).
- **Behavior**: a persistent notification will be visible while keep-alive is on (required by Android for foreground services).
- **Risk**: foreground-service-type rules (Android 14+) and OEM autostart intents are device-specific; autostart deep-links may not exist on every device and must fail gracefully to guidance.
