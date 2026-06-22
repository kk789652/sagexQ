# SageX

[![CI](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> 一款本地优先的 Android 应用：监听微信通知、按「群聊 / 个人」归类会话，并通过任意
> OpenAI 兼容大模型生成聊天摘要。可选地把消息同步到飞书多维表格做进一步处理。

SageX 把零散的微信通知整理成结构化的会话记录，让你能快速回顾「今天群里聊了什么」、
导出某段时间的聊天发给 ChatGPT 总结，或交给应用内置的 AI 摘要功能直接出结论。

> ⚠️ **说明**：本项目仅通过系统「通知访问」读取通知内容，**不接入、不读取微信内部聊天数据库**，
> 也不上传你的聊天记录到任何第三方服务器（飞书同步为可选项，且推送到你自己的多维表格）。

## ✨ 功能特性

- **通知采集**：通过 `NotificationListenerService` 捕获微信通知，解析出群名 / 联系人、
  发送者与正文，去除「[N条]」未读前缀和纯数量汇总等噪音。
- **会话归类**：按 `群聊 / 个人` 区分，同名的群和私聊互不混淆。
- **会话详情**：聊天式时间线，**自动记住上次阅读位置**，新进入直接定位到最新一条，并提供
  「跳到最新」悬浮按钮。
- **导出 / 分享**：按时间范围（今天 / 近 7 天 / 近 30 天 / 自定义日期）把聊天导出为纯文本，
  通过系统分享发给 ChatGPT 等任意应用。
- **AI 摘要**：配置任意 OpenAI 兼容接口（base URL + API key + 模型），一键总结所选时间范围的会话。
- **飞书同步（可选）**：把消息写入飞书多维表格，便于做每日总结、归档或自动化流水线。
- **后台保活**：前台服务 + 开机自启，尽量保证通知不漏接。

## 🧱 技术栈

- Kotlin 2.0 · Jetpack Compose（Material 3）
- Hilt（依赖注入）· Room（本地存储）· DataStore（配置）
- Coroutines / Flow · OkHttp · kotlinx.serialization
- `minSdk 26`（Android 8.0+）· `targetSdk 35`

## 📦 下载安装

- **正式版本**：前往 [Releases](../../releases/latest) 下载最新的 `sagex-*.apk`。
- **最新构建**：在 [Actions](../../actions/workflows/ci.yml) 里选择一次成功的运行，从
  「Artifacts」下载 `sagex-release-apk`。

> Release 默认使用 Android 调试密钥签名，安装时系统可能提示「未知来源」，按提示允许即可。
> 如需用你自己的密钥签名，见下文「发布签名」。

## 🔧 从源码构建

需要 **JDK 21**（推荐 Android Studio 自带的 JBR；系统 JDK 可能过新导致 AGP 不兼容）。

```bash
# 1. 准备本地配置
cp local.properties.example local.properties
#    编辑 local.properties，至少填好 sdk.dir

# 2. 构建 / 测试
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug          # 调试包
./gradlew assembleRelease        # 发布包（默认用调试密钥签名）
./gradlew testDebugUnitTest      # 单元测试（含 Robolectric DAO 测试）
```

## ⚙️ 配置

### AI 摘要（应用内设置）

打开应用 → 设置，填入 OpenAI 兼容服务的 `Base URL`、`API Key` 和模型名即可。
这些信息保存在设备本地（DataStore），不会进入代码仓库。

### 飞书同步（可选，编译期注入）

在 `local.properties` 中填入以下字段后重新编译，应用即会把消息同步到飞书多维表格；
留空则完全不启用同步，应用照常离线工作：

```properties
FEISHU_APP_ID=
FEISHU_APP_SECRET=
FEISHU_MESSAGES_TABLE_ID=
FEISHU_APP_TOKEN=
```

> 这些值通过 `BuildConfig` 注入，**绝不要**写进任何被 Git 跟踪的文件。
> `local.properties` 已在 `.gitignore` 中。

### 发布签名（可选）

不配置时，`assembleRelease` 会回退使用调试密钥，产物可直接安装。若要用自己的密钥：

- 本地：在 `local.properties` 设置 `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` /
  `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD`。
- CI：在仓库 Secrets 配置 `RELEASE_STORE_BASE64`（keystore 的 base64）及上述三个密码/别名，
  `Release` 工作流会自动解码并签名。

## 🔐 权限说明

- **通知访问（Notification Access）**：核心权限，用于读取微信通知内容。首次启动会引导开启。
- **前台服务 / 开机自启 / 电池白名单**：用于后台保活，减少漏接通知。

## ⚠️ 已知限制

- 只能读到「以通知形式出现」的消息：被微信折叠、聚合或关闭通知的会话无法采集，这是
  基于通知监听方案的固有限制。
- 通知文案格式随微信版本变化，解析为启发式实现，个别情况可能解析不完整。

## 🛠️ 开发

项目采用 **OpenSpec** 规范驱动开发，所有功能先有变更提案再实现，详见 `openspec/` 与
[`CLAUDE.md`](CLAUDE.md)。CI 会在每次 push / PR 上跑单元测试并构建发布包。

## 📄 许可证

[MIT](LICENSE) © 2026 Tis Wu
