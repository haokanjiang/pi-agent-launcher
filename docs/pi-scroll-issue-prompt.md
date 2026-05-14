# Pi TUI 滚动问题分析 Prompt

## 问题描述

在 JetBrains IDE 内嵌终端（JediTerm）中运行 pi 时，触控板滚动存在两个问题：

1. **输入框跟随滚动** — 使用触控板上下滑动时，pi 底部的输入框也会跟着上下移动（不应该动）
2. **弹回问题** — 滚动到底部时，画面会弹到底部上方而不是停在最底部

## 技术背景

- pi 是 TUI 应用，使用 alternate screen buffer 渲染
- JediTerm 是 IntelliJ 的终端模拟器
- JediTerm 发送 `ESC[?2026h` / `ESC[?2026l`（synchronized output）但 pi 的 powerline-footer 每秒刷新也在发这个序列
- JediTerm 报错 `Attempt to get line out of bounds: 49 >= 22`，说明 pi 认为终端有 50 行但实际只有 22 行

## 需要分析的方向

1. **pi 如何处理鼠标滚轮事件？**
   - 是否启用了 mouse reporting（`ESC[?1000h` 等）？
   - 滚轮事件是被 pi 消费还是透传给终端模拟器？

2. **pi 的 TUI 渲染机制**
   - 是否使用 alternate screen buffer（`ESC[?1049h`）？
   - 输入框是固定在底部的吗？用什么方式固定（cursor positioning vs scroll region）？
   - 是否设置了 scroll region（`ESC[top;bottom r`）来限制滚动区域？

3. **终端尺寸同步**
   - pi 如何获取终端尺寸（SIGWINCH / ioctl TIOCGWINSZ）？
   - 是否正确响应终端 resize？
   - 为什么 pi 认为有 50 行但实际只有 22 行？

4. **synchronized output（ESC[?2026h/l）**
   - pi 或其扩展（powerline-footer）是否在使用这个特性？
   - 每秒发送是否导致 JediTerm 状态混乱？

## 可能的修复方向

- 如果 pi 没有启用 mouse reporting，JediTerm 会把滚轮事件当成 scroll buffer 操作，导致 alternate screen 内容被滚动
- 如果 pi 启用了 mouse reporting 但没正确处理滚轮事件，也会出问题
- scroll region 设置不正确可能导致输入框不固定
- 终端尺寸不同步导致渲染越界

## 关键源码位置（需要确认）

- pi 的 TUI 渲染入口
- 终端初始化代码（设置 alternate screen、mouse mode、scroll region）
- 输入框/footer 的渲染逻辑
- SIGWINCH 处理
