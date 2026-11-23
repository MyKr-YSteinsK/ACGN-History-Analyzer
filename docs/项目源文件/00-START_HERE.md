# 接手 ACGN History Analyzer

- **接入类型**　已有项目升级。
- **当前范围**　恢复可开发基线、归档旧资料和小范围兼容整理。升级候选待用户确认。
- **维护方式**　按孵化器的 Standard 层级保留状态和运行入口，支持 GPT Project Main、Codex Native，以及确有多来源任务时的 Codex Hub。
- **仓库与分支**　[MyKr-YSteinsK/ACGN-History-Analyzer 的 main](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/tree/main)。

## 创建 GPT Project

1. 创建名为 ACGN History Analyzer 的 GPT Project。
2. 将 [PROJECT_INSTRUCTIONS.md](PROJECT_INSTRUCTIONS.md) 正文粘贴到该 Project 的「项目指令」。文件里已经包含角色、权限、阅读顺序、正式交接与评审规则。
3. 上传 [PROJECT.md](PROJECT.md)、[STATUS.md](STATUS.md)、[RUNBOOK.md](RUNBOOK.md) 和 [升级路线与技术审查.md](升级路线与技术审查.md)。这四份是项目理解、运行恢复和决策所需的资料。需要细查实现时从 GitHub 读取，或按问题补传 README 链接中的对应技术文档。
4. 在首条消息写明这些附件对应的 Git 提交，并提出这次要判断的问题。仓库源文件有实质更新时，替换对应附件，告知 Main 新提交；同名材料以用户指定版本为准。

可复制的首条消息：

> 这是我的 ACGN History Analyzer 维护项目。请先读取 PROJECT.md、STATUS.md、RUNBOOK.md 和《升级路线与技术审查》。以我指定的仓库提交作为代码基线，告诉我这个项目当前可维护的范围、尚待我决定的升级分叉，以及导入往返闭环是否值得作为补充项目。请先审查方案，本次无需创建任务或改动仓库。

## 在 Codex 接手

根 [AGENTS.md](../../AGENTS.md) 和这组项目文档已落地在目标工作区，路径沿用当前仓库。打开目标仓库后指定本次任务，从根 AGENTS.md 读目标、状态和适用的运行入口。当前首轮维护结束后，可以先由用户审查升级候选，也可单独授权兼容维护。

普通任务用短指令即可。需要正式交接时，在有权使用的目标 GitHub Issue 写完整 Markdown，随后只向 Codex 转交任务目标、Issue URL、工作区和必要权限。公开 Issue 仅使用可公开内容。具体约定在 [PROJECT.md](PROJECT.md)。
