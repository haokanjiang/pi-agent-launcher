# Pi Agent Launcher

一键启动 [Pi coding agent](https://pi.dev) 的 JetBrains IDE 插件 — 在 Terminal 窗口中打开 "Pi" tab 并自动启动 `pi`。

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)](https://plugins.jetbrains.com/plugin/31737-pi-agent-launcher)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## 功能

- **一键启动** — 点击工具栏 π 按钮启动 Pi
- **终端集成** — Pi 作为 IDE Terminal 窗口的一个 tab（和 Local 并列）
- **发送选区** — 选中代码 → 右键 → "Send to Pi"，自动插入 `@path/file.go#L10-25` 到 Pi 输入框
- **完成通知** — Pi 处理完成后弹出通知
- **模型配置** — 从 `~/.pi/agent/models.json` 加载模型列表

### 默认安静

Pi 一次 session 经常改写几十个文件。插件**不弹 diff**、**不弹逐文件通知**、**不上报退出**——把 100 个改动文件变成 100 个标签页是无法使用的。想看改动请用 `git diff`。

唯一会弹的通知：**你点启动但终端创建失败**时的错误提示。

如果你确实希望把改动文件开成普通编辑器标签，可在 **Settings → Tools → Pi Agent → Auto-open files** 手动开启（默认关闭）。

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
- **Auto-open files** — 默认关闭。开启后把改动文件开成普通编辑器标签（不会是 diff），带防抖与数量上限

## 常见问题：IDE 卡死

部分 GoLand 2026.2 会因平台级读写锁死锁而卡死（见 [GO-20886](https://youtrack.jetbrains.com/issue/GO-20886) 和 [IJPL-252277](https://youtrack.jetbrains.com/issue/IJPL-252277)）。插件早期版本会参与其中：VFS 变更通知在后台线程处理，每次写入（包括 `go build`、`gofmt` 产生的写入）都会打开编辑器或 diff。

自 0.1.5 起，插件**不再弹任何 diff**；只有在手动开启 *Auto-open files* 时才会碰编辑器，且改在 EDT 上执行并带防抖与过滤。如果 IDE 仍然自行卡死，请升级或回退 GoLand。

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
