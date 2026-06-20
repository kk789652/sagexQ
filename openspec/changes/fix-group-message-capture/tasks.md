## 1. Parser fix

- [x] 1.1 Add `stripCountPrefix(text)` to `WeChatNotificationParser` removing a leading `[N条]` / `【N条】` (ASCII/fullwidth) prefix
- [x] 1.2 Apply stripping before classification; treat as `ContentUnavailable` only when the stripped remainder is empty or a pure count summary
- [x] 1.3 Keep pure-summary detection (`N条新消息`, `N new messages`) working on the stripped text

## 2. Diagnostics

- [x] 2.1 Add debug-only (`BuildConfig.DEBUG`) logging in `SageXNotificationListenerService` dumping WeChat notification fields (title, text, text-lines, sub-text, has-messages) to logcat

## 3. Tests & verification

- [x] 3.1 Parser tests: count-prefixed group (`[3条]张三: 你好`), count-prefixed direct (`[2条]在吗`), fullwidth bracket (`【5条】王五: 看一下`), pure summary (`2条新消息`) stays content-unavailable
- [x] 3.2 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` green; run `openspec validate fix-group-message-capture --strict`
- [ ] 3.3 Manual: install on a real device with WeChat, trigger a group message, confirm it is captured (and/or read the debug log to confirm format)
