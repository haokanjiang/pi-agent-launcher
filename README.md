# Pi JetBrains Plugin

在 JetBrains IDE 中集成 Pi coding agent CLI。

## 功能

- **内嵌终端**：在 IDE 底部 Tool Window 中运行 Pi agent
- **发送选区**：选中代码右键 → "Send to Pi" 子菜单，支持多种模式
- **自定义提问**：`Cmd+Shift+P` 弹出输入框，输入任意问题
- **Diff 预览**：Pi 修改文件后自动弹出 IDE 原生 diff 视图
- **快速切换**：`Cmd+Esc` 打开/隐藏 Pi 面板

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Cmd+Esc` | 打开/隐藏 Pi Agent 面板 |
| `Cmd+Shift+P` | 发送选中代码 + 自定义提问 |

## 右键菜单

选中代码后右键 → **Send to Pi**：
- Explain This Code — 解释代码
- Refactor This Code — 重构代码
- Fix Bug in This Code — 修 bug
- Ask Pi About This... — 自定义问题

## 开发

```bash
# 构建
./gradlew build

# 运行 IDE 沙箱测试
./gradlew runIde

# 打包
./gradlew buildPlugin
```

## 项目结构

```
src/main/kotlin/com/piagent/launcher/
├── PiToolWindowFactory.kt          # Tool Window 工厂
├── services/
│   ├── PiTerminalService.kt        # 终端生命周期 + 发送文本
│   └── PiDiffWatcher.kt            # 文件变更监听 + diff 展示
└── actions/
    ├── OpenPiAction.kt              # 打开/隐藏面板
    ├── BaseSendAction.kt            # 发送选区基类
    ├── SendModeActions.kt           # 解释/重构/修bug
    └── SendCustomAction.kt          # 自定义提问（弹窗）
```

## 架构

```
┌─────────────────────────────────────────────┐
│  JetBrains IDE                              │
│  ┌───────────────────────────────────────┐  │
│  │  Editor                               │  │
│  │  [选中代码] → Right Click → Send to Pi│  │
│  └───────────────────────────────────────┘  │
│                    │                         │
│                    ▼                         │
│  ┌───────────────────────────────────────┐  │
│  │  PiTerminalService                    │  │
│  │  - 管理终端生命周期                     │  │
│  │  - 格式化并发送选区                     │  │
│  └───────────────────────────────────────┘  │
│                    │                         │
│                    ▼                         │
│  ┌───────────────────────────────────────┐  │
│  │  Pi Agent Terminal (Tool Window)      │  │
│  │  - 内嵌 PTY 终端运行 `pi` CLI         │  │
│  │  - 交互式对话                          │  │
│  └───────────────────────────────────────┘  │
│                    │                         │
│                    ▼                         │
│  ┌───────────────────────────────────────┐  │
│  │  PiDiffWatcher                        │  │
│  │  - 监听文件变更                        │  │
│  │  - 自动弹出 IDE Diff 视图             │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## 依赖

- IntelliJ Platform 2024.3+
- Terminal plugin (bundled)
- Pi CLI (系统已安装 `pi` 命令)

## TODO

- [ ] 适配 Reworked Terminal API (2025.3+)
- [ ] 支持 accept/reject diff changes
- [ ] 设置面板（自定义 pi 启动参数、快捷键）
- [ ] 支持多 Pi 会话

## License

MIT
