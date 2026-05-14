# Pi Agent Launcher

[中文](#中文)

One-click [Pi coding agent](https://pi.dev) launcher for JetBrains IDEs — opens a "Pi" tab inside the Terminal tool window and starts `pi` automatically.

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)](https://plugins.jetbrains.com/plugin/31737-pi-agent-launcher)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Features

- **One-click launch** — Click the π button in the toolbar to start Pi
- **Terminal integration** — Pi runs as a tab inside the IDE's Terminal window (alongside Local)
- **Send to Pi** — Select code → Right-click → "Send to Pi" inserts `@path/file.go#L10-25` into Pi's input
- **Auto-open files** — Files modified by Pi automatically open in the editor
- **Completion notifications** — Get notified when Pi finishes
- **Model configuration** — Pick model and thinking level from `~/.pi/agent/models.json`

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Cmd+Esc` / `Ctrl+Esc` | Launch or focus Pi |
| `Cmd+Shift+P` / `Ctrl+Shift+P` | Send selection to Pi |

## Quick Start

1. Install the plugin from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31737-pi-agent-launcher)
2. Ensure `pi` CLI is installed and in your PATH
3. Click the **π** button in the toolbar
4. A "Pi" tab opens in the Terminal window and `pi` starts automatically

## Configuration

**Settings → Tools → Pi Agent**

- **Model** — Select from models defined in `~/.pi/agent/models.json`
- **Custom model id** — Override the dropdown with any model identifier
- **Thinking level** — Default / none / low / medium / high / max
- **Pi command** — Custom path to pi binary
- **Extra arguments** — Additional CLI flags
- **Auto-open files** — Toggle auto-opening modified files
- **Notifications** — Toggle completion notifications

## Supported IDEs

Works with all JetBrains IDEs: IntelliJ IDEA, GoLand, PyCharm, WebStorm, PhpStorm, CLion, Rider, RubyMine, and more.

## Development

```bash
# Build
./gradlew build

# Run sandbox IDE for testing
./gradlew runIde

# Package
./gradlew buildPlugin
```

## Project Structure

```
src/main/kotlin/com/piagent/launcher/
├── actions/
│   ├── LaunchPiAction.kt          # Toolbar button → launch Pi
│   ├── OpenPiAction.kt            # Cmd+Esc → focus Pi
│   └── SendSelectionAction.kt     # Send @file#L reference
├── services/
│   ├── PiTerminalService.kt       # Terminal lifecycle + send text
│   ├── PiDiffWatcher.kt           # File change → diff preview
│   └── PiFileWatcher.kt           # Auto-open + notifications
└── settings/
    ├── PiSettings.kt              # Persistent config
    ├── PiSettingsConfigurable.kt  # Settings UI panel
    └── PiModelLoader.kt           # Load models from models.json
```

## License

MIT

---

## 中文

# Pi Agent Launcher

一键启动 [Pi coding agent](https://pi.dev) 的 JetBrains IDE 插件 — 在 Terminal 窗口中打开 "Pi" tab 并自动启动 `pi`。

## 功能

- **一键启动** — 点击工具栏 π 按钮启动 Pi
- **终端集成** — Pi 作为 IDE Terminal 窗口的一个 tab（和 Local 并列）
- **发送选区** — 选中代码 → 右键 → "Send to Pi"，自动插入 `@path/file.go#L10-25` 到 Pi 输入框
- **自动打开文件** — Pi 修改的文件自动在编辑器中打开
- **完成通知** — Pi 处理完成后弹出通知
- **模型配置** — 从 `~/.pi/agent/models.json` 加载模型列表

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Cmd+Esc` / `Ctrl+Esc` | 启动或聚焦 Pi |
| `Cmd+Shift+P` / `Ctrl+Shift+P` | 发送选区到 Pi |

## 快速开始

1. 从 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31737-pi-agent-launcher) 安装插件
2. 确保 `pi` CLI 已安装且在 PATH 中
3. 点击工具栏 **π** 按钮
4. Terminal 窗口中打开 "Pi" tab，自动启动 `pi`

## 配置

**Settings → Tools → Pi Agent**

- **Model** — 从 `~/.pi/agent/models.json` 中选择模型
- **Custom model id** — 自定义模型标识符
- **Thinking level** — Default / none / low / medium / high / max
- **Pi command** — pi 二进制路径
- **Extra arguments** — 额外 CLI 参数
- **Auto-open files** — 是否自动打开修改的文件
- **Notifications** — 是否显示完成通知

## 支持的 IDE

支持所有 JetBrains IDE：IntelliJ IDEA、GoLand、PyCharm、WebStorm、PhpStorm、CLion、Rider、RubyMine 等。

## 开发

```bash
# 构建
./gradlew build

# 运行沙箱 IDE 测试
./gradlew runIde

# 打包
./gradlew buildPlugin
```

## 许可证

MIT
