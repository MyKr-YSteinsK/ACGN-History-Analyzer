# 毕设阶段历史摘要

当前接入基线是原 GitHub main 的 e9c8c2656e934fd1d0ccbe7ce694586483d97f42。该提交之前已有 145 个可达提交，历史记录中的开始日期为 2026-04-13，基线提交日期为 2026-05-26。以下是从仓库能追溯的实施记录。

| 阶段 | 可追溯成果 |
| --- | --- |
| 2026 年 4 月中下旬 | Spring Boot 服务、MySQL/JPA、个人记录与作品库。逐步加入组合筛选、档期模型和 HTTP 请求样例。参见 [0631861](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/0631861)、[c1d9108](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/c1d9108)。 |
| 2026 年 5 月初 | Bangumi 季度与 sourceId 导入、导入预览与重复更新、读缓存、LLM 持久结果和后台任务。参见 [35de69d](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/35de69d)、[431d396](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/431d396)、[1a7a1ad](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/1a7a1ad)。 |
| 2026 年 5 月后续 | 跨路由任务通知、筛选记忆和分析页布局。旧导入说明整合进维护文档；原展示提交保存了首页、导入、记录、系列与分析等截图。参见 [f540fb3](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/f540fb3)、[87c6d05](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/commit/87c6d05)、[旧展示 README 与截图](https://github.com/MyKr-YSteinsK/ACGN-History-Analyzer/tree/e9c8c2656e934fd1d0ccbe7ce694586483d97f42)。 |

## 本地待清理原件

| docs/delete 下的位置 | 内容与来源 |
| --- | --- |
| README.original-local.md | 接入前工作区的简版说明。有效入口已整合至新的根 README 和项目源文件。 |
| picture/ | 从远端基线回收的 16 张毕设阶段截图。线上历史仍可按基线提交访问。 |
| frontend/ | 远端基线中的 Vite 模板 README、未引用的 public/icons.svg，以及从本地源码移出的 React、Vite 模板图标。站点图标 favicon.svg 和首页实际使用的图片留在源码。 |
| backend/ | 独立线程练习，以及旧工作区中未跟踪的 MySQL 手动索引脚本。脚本中的索引候选保留在数据库资料中。 |

合计 23 个文件，约 14.7 MB。docs/delete 在 Git 忽略范围内，物理清理由用户执行。提交历史中的旧版本继续可追溯。本次初始工作区实查未发现要迁出的私有数据库快照、封面缓存、依赖树或打包目录，数据库里的数据也没有作为仓库文件处理。

技术参考保留四份活跃文档，旧 UI 与操作约定以当前实现和[项目目标与约定](../项目源文件/PROJECT.md)为准。接手前根目录缺 Git 元数据，部分 wrapper、lint、图标文件原本缺失，接入时按已确认的同名远端恢复。路径 fronted 与远端 frontend 的业务文件逐一核对一致，工作区归回 frontend。完整升级建议见[技术审查](../项目源文件/升级路线与技术审查.md)。
