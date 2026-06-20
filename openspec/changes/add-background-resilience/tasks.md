## 1. Keep-alive preference (DataStore)

- [x] 1.1 Create `KeepAlivePreferences` backed by Preferences DataStore exposing `Flow<Boolean>` and a suspend `setEnabled(Boolean)`, default `false`
- [x] 1.2 Provide the DataStore and `KeepAlivePreferences` via a Hilt module

## 2. Foreground service

- [x] 2.1 Create a notification channel + builder for the low-importance persistent "capture running" notification
- [x] 2.2 Implement `SageXForegroundService` (LifecycleService) that calls `startForeground`, returns `START_STICKY`, and stops self when keep-alive is disabled
- [x] 2.3 Add `ServiceController` helpers to start/stop the foreground service respecting the persisted preference
- [x] 2.4 Declare the service in `AndroidManifest.xml` with `android:foregroundServiceType="specialUse"` (+ subtype property); `specialUse` is required because Android 14/15 forbid starting a `dataSync` FGS from `BOOT_COMPLETED`

## 3. Boot restart

- [x] 3.1 Implement `BootReceiver` that, on `BOOT_COMPLETED`, starts the foreground service only when keep-alive is enabled
- [x] 3.2 Register `BootReceiver` in `AndroidManifest.xml` with the `RECEIVE_BOOT_COMPLETED` intent filter

## 4. OS-level persistence controls

- [x] 4.1 Create `BatteryOptimizationHelper` to check `isIgnoringBatteryOptimizations` and build the request intent
- [x] 4.2 Create `AutostartHelper` with a pure `Build.MANUFACTURER` → candidate-intents mapping and a runtime `resolveActivity` check returning a typed result (resolved intent vs. none)

## 5. UI wiring

- [x] 5.1 Extend the home/settings UI + ViewModel with a keep-alive toggle, battery-exemption status + request action, and an autostart-guidance action
- [x] 5.2 Re-check battery-exemption status on screen resume

## 6. Tests & verification

- [x] 6.1 Unit tests for `AutostartHelper` manufacturer mapping (known OEMs map to candidates; unknown returns none)
- [x] 6.2 Tests for `KeepAlivePreferences` (default false; set/round-trip via Flow) using an in-memory/temp DataStore
- [x] 6.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate add-background-resilience --strict`
