# Pi Agent Launcher

[中文文档](README_CN.md)

One-click [Pi coding agent](https://pi.dev) launcher for JetBrains IDEs — opens a "Pi" tab inside the Terminal tool window and starts `pi` automatically.

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange)](https://plugins.jetbrains.com/plugin/31737-pi-agent-launcher)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Features

- **One-click launch** — Click the π button in the toolbar to start Pi
- **Terminal integration** — Pi runs as a tab inside the IDE's Terminal window (alongside Local)
- **Send to Pi** — Select code → Right-click → "Send to Pi" inserts `@path/file.go#L10-25` into Pi's input
- **No notifications** — the plugin stays quiet: no diff tabs, no "Pi finished" balloons, no exit reports
- **Model configuration** — Pick model and thinking level from `~/.pi/agent/models.json`

### Silent by design

Pi sessions routinely rewrite dozens of files. The plugin deliberately opens **no**
diff editor, shows **no** per-file notification and reports **no** exit — surfacing
100 modified files as 100 editor tabs is unusable. Review the work with `git diff`.

The only notification it ever raises is an error when *you* click launch and the
terminal cannot be created.

If you do want modified files opened as ordinary editor tabs, opt in via
**Settings → Tools → Pi Agent → Auto-open files** (off by default).

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
- **Auto-open files** — Off by default. When enabled, opens modified files as ordinary editor tabs (never diffs), debounced and capped.

## Troubleshooting: IDE freeze

Some GoLand 2026.2 builds freeze because of a platform-level read/write-lock deadlock
(see [GO-20886](https://youtrack.jetbrains.com/issue/GO-20886) and
[IJPL-252277](https://youtrack.jetbrains.com/issue/IJPL-252277)). Earlier versions of
this plugin contributed to it: VFS change notifications were handled on a background
thread, opening an editor or a diff on every single write — including writes made by
`go build` or `gofmt`.

Since 0.1.5 the plugin no longer opens diffs at all, only touches the editor when you
explicitly enable *Auto-open files*, and does that on the EDT with debouncing and
filtering. If the IDE still freezes, update or roll back GoLand.

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
├── domain/                        # Pure logic, no IDE types, unit tested
│   ├── PiLaunchOptions.kt         # Inputs that determine how Pi starts
│   ├── PiCommandLine.kt           # Renders options into the CLI command
│   ├── PiFileReference.kt         # @path#L10-25 references
│   └── PiModel.kt                 # Model advertised by the Pi CLI
├── infrastructure/                # Adapters to the IDE and the filesystem
│   ├── PiTerminal.kt              # Terminal interfaces (fakeable)
│   ├── IdeTerminalProvider.kt     # Reflective Terminal tool window access
│   ├── PiModelRepository.kt       # Reads ~/.pi/agent/models.json
│   └── PiNotifier.kt              # Notification interface + IDE impl
├── util/
│   └── PiChangeDebouncer.kt       # Coalesces write bursts, IDE-free
├── services/
│   ├── PiSessionService.kt        # Terminal session lifecycle
│   ├── PiSessionState.kt          # Observable status (topic)
│   ├── PiCommandDispatcher.kt     # Sends the start command
│   ├── PiFileWatcher.kt           # Optional auto-open (off by default)
│   ├── PiStatusWidget.kt          # Status bar entry
│   └── PiVfsUtils.kt              # VFS change filtering
├── actions/                       # Extract input, delegate to a service
│   ├── LaunchPiAction.kt
│   ├── OpenPiAction.kt
│   ├── SendSelectionAction.kt
│   ├── SendFileToPiAction.kt
│   └── PiPrompt.kt                # Shared reference insertion
└── settings/
    ├── PiSettings.kt              # Persisted state + domain mapping
    ├── PiSettingsConfigurable.kt  # Settings UI shell
    ├── PiSettingsForm.kt          # Swing form and state mapping
    └── SettingsModelCatalog.kt    # Swappable model source
```

The `domain` and `util` packages have no IntelliJ dependencies, so the argument
handling, reference formatting and debouncing are covered by plain unit tests
(`./gradlew test`).

## License

MIT
