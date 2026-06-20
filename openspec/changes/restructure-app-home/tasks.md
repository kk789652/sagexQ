## 1. App-list data

- [x] 1.1 Add pure `appDisplayName(sourcePackage)` (`com.tencent.mm` → `微信`, else the package)
- [x] 1.2 Add `AppSummary` domain model + `AppSummaryRow` + mapper (displayName via `appDisplayName`)
- [x] 1.3 Add `MessageDao.observeApps()` aggregate (sourcePackage → conversationCount, messageCount, lastPostedAt) and expose `MessageRepository.apps: Flow<List<AppSummary>>`
- [x] 1.4 Add `MessageDao.observeConversations(sourcePackage)` (existing query + source filter) and `MessageRepository.conversations(sourcePackage)`

## 2. Per-app conversation list

- [x] 2.1 `ConversationListViewModel` reads a `sourcePackage` nav arg and uses the scoped conversations query
- [x] 2.2 Route `conversations/{sourcePackage}`; conversation list top bar shows the app display name
- [x] 2.3 Remove the unread-count badge from conversation rows (keep avatar + title + preview + time)

## 3. Home = app list + drawer

- [x] 3.1 Rewrite `HomeScreen` as an app list (avatar + name + counts + last activity), tapping a row → `conversations/{sourcePackage}`; empty state when no apps; capture-inactive banner when access not granted
- [x] 3.2 Wrap home in `ModalNavigationDrawer` with a hamburger top-bar action; drawer items → capture settings, API 设置, 删除消息 (range dialog, global), 关于

## 4. Migrated/new screens

- [x] 4.1 Add `CaptureSettingsScreen` (+ ViewModel) holding the old home's capture status + enable-access + keep-alive toggle + battery exemption + autostart; route `capture-settings`
- [x] 4.2 Add `AboutScreen` showing app name + `BuildConfig.VERSION_NAME`/`VERSION_CODE` + "© <year> Tis Wu"; route `about`
- [x] 4.3 Update `SageXNavHost` for new routes; keep conversation detail / summary / settings routes working

## 5. Tests & verification

- [x] 5.1 Unit tests for `appDisplayName` (wechat → 微信; unknown → package)
- [x] 5.2 DAO tests: `observeApps` aggregates per source package; `observeConversations(sourcePackage)` returns only that app's conversations
- [x] 5.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate restructure-app-home --strict`
