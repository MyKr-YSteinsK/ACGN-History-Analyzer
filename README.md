# ACGN History Analyzer

个人 ACGN 历史管理与复盘工具。作品资料、完成记录、想看与搁置清单分别管理。系统支持用 Bangumi 补充作品资料、迁移旧记录，并按年份、档期、系列或标签回顾个人历史。

- **记录与统计**　作品库、正式记录、收藏、系列、档期统计和个人榜单。
- **数据迁移**　Bangumi 季度与 sourceId 导入、记录和收藏的 JSON／CSV 导入、Bangumi 往返 JSON 导出。记录和收藏可先预览匹配结果及歧义。
- **辅助复盘**　以库内数据构造报告输入，可使用本地 Mock 或配置 DeepSeek。打开导入或分析页发起任务后，可在其他页面接收完成提示。

前端使用 React、TypeScript、Vite、Tailwind CSS 和 TanStack Query。后端使用 Java 17、Spring Boot、Spring Data JPA 和 MySQL。当前使用方式为个人本地运行。Bangumi 往返导出覆盖正式记录及其关联的 Bangumi 作品标识，整库备份需要单独备份 MySQL。

## 本地启动

准备 JDK 17、Node.js 24 和 MySQL 8。先创建专用数据库并设置数据库连接环境变量。首次演示可用本地 Mock。环境变量与排障见[运行手册](docs/项目源文件/RUNBOOK.md)。

~~~powershell
cd backend
$env:APP_LLM_PROVIDER = 'mock'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
~~~

另开终端，在项目根目录运行。

~~~powershell
cd frontend
npm.cmd ci
npm.cmd run dev
~~~

打开终端输出的 Vite 地址，默认是 http://localhost:5173。Vite 将 /api 请求代理到 8080 端口。

## 维护入口

- 项目源文件、粘贴用指令和 Codex 入口见[接手与创建 GPT Project](docs/项目源文件/00-START_HERE.md)。
- 现行数据契约、维护权限、基线和验证见[项目目标与约定](docs/项目源文件/PROJECT.md)、[当前状态](docs/项目源文件/STATUS.md)。
- 实现和排障可查阅[架构与接口](docs/architecture-overview.md)、[数据库](docs/database-schema.md)、[导入导出](docs/import-guide.md)和 [LLM](docs/llm-guide.md)。
- 待确认的重构方向和简历定位见[升级审查](docs/项目源文件/升级路线与技术审查.md)。毕设阶段的界面与演进见[历史摘要](docs/archive/README.md)。
