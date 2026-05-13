# JetBrains Marketplace 发布准备

## 插件信息

- **Name**: Pi Agent
- **Tagline**: AI coding assistant powered by Pi — right inside your JetBrains IDE
- **Category**: AI Assistant / Code Tools
- **Tags**: ai, coding-assistant, terminal, pi, code-review, refactoring

## 需要准备的截图（4 张）

上传前需要截图，在 `./gradlew runIde` 沙箱 IDE 中操作并截图：

### 截图 1：Pi Agent 面板
- 打开 Pi Agent 面板（Cmd+Esc）
- 展示底部终端中 pi 正在运行
- 截图尺寸建议：1280x800

### 截图 2：右键菜单
- 选中一段代码
- 右键展开 "Send to Pi" 子菜单
- 显示 4 个选项：Explain / Refactor / Fix Bug / Ask Pi About This...

### 截图 3：自定义提问弹窗
- 选中代码后按 Cmd+Shift+P
- 展示输入框弹窗

### 截图 4：Diff 预览
- Pi 修改文件后弹出的 IDE 原生 diff 视图
- 左边 "Before Pi" 右边 "After Pi"

## 截图命名

```
screenshots/
├── 01-pi-terminal-panel.png
├── 02-context-menu.png
├── 03-custom-question-dialog.png
└── 04-diff-preview.png
```

## Marketplace 描述（纯文本版，用于填表）

Pi Agent brings the Pi coding agent CLI directly into your JetBrains IDE. 
Select code, right-click, and choose from multiple AI-powered actions — 
explain, refactor, fix bugs, or ask custom questions. Pi's file changes 
are shown in the IDE's native diff viewer for easy review.

No API keys needed — just install the Pi CLI and start coding with AI.

## 发布 Checklist

- [ ] `./gradlew runIde` 测试所有功能正常
- [ ] 截 4 张图
- [ ] `./gradlew buildPlugin` 打包
- [ ] `./gradlew verifyPlugin` 验证通过
- [ ] 登录 plugins.jetbrains.com 上传
- [ ] 填写描述、截图、分类
- [ ] 提交审核
