# 运行手册

## 环境与启动

在 Windows PowerShell 中，本轮使用 JDK 17.0.18、仓库 Maven Wrapper 3.9.11、Node.js 24.14.1、npm 11.11.0、MySQL 8.0.45。依赖以 backend/pom.xml、Wrapper 配置和 frontend/package-lock.json 为准。新环境先核对 Java 和 Node 的版本。

在本机 MySQL 创建独立数据库，例如 acgn_history_analyzer，并为它配置专用账户。验证写入流程时另建测试库，命令中的连接改为测试库。运行后端前，在同一 PowerShell 会话设置：

~~~powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/acgn_history_analyzer'
$env:DB_USERNAME = Read-Host 'MySQL 用户'
$dbPassword = Read-Host 'MySQL 密码' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPassword).Password
$env:APP_LLM_PROVIDER = 'mock'

cd backend
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
~~~

从项目根目录另开终端：

~~~powershell
cd frontend
npm.cmd ci
npm.cmd run dev
~~~

打开 http://localhost:5173。Vite 默认将 /api 代理到 http://localhost:8080。切换端口时同步检查 vite.config.ts。通过 GET /api/health 检查后端，通过 GET /api/llm/status 确认 mockMode。使用 DeepSeek 时按需配置 DEEPSEEK_API_KEY、模型和服务地址，并先确认允许发送的记录范围。配置字段见 [LLM 指南](../llm-guide.md)。

在 Linux 或 macOS 上可从 backend 使用 ./mvnw，前端使用 npm。当前验证使用 Windows。MySQL 的表初始化使用 Hibernate ddl-auto=update，旧库验证使用备份恢复出的副本。

## 数据地图

| 数据 | 位置与处理 |
| --- | --- |
| 作品、正式记录、收藏、系列、标签 | 由 DB_URL 指定的 MySQL。备份和验证恢复时同时保留实际结构及数据。记录含个人评分及备注。 |
| Bangumi 作品、评分、标签和封面 | 外部来源。导入结果持久化到作品库，封面默认按后端工作目录存放于 uploads/covers。对外展示前核对素材使用权限。 |
| 导入导出文件 | 用户自行提供 JSON／CSV。当前 Bangumi 往返 JSON 不包含整个 MySQL 数据集，恢复方法及限制见[导入导出指南](../import-guide.md)。 |
| 通用任务、分析结果 | MySQL 的 background_job 与 llm_analysis_result_cache。载荷与结果可能含记录、问句或报告内容，按个人数据处理。新建样例库可验证启动。 |
| 季度导入任务、读缓存 | 季度任务状态和 Caffeine 读缓存驻留在服务进程。季度任务重启后无法按旧 ID 查询。 |
| 页面筛选、草稿和部分报告 | 浏览器 localStorage，按站点地址区分。测试请用独立浏览器配置或测试端口。 |
| 待清理原件 | 本地 docs/delete，由用户手动清除。来源和保留证据见[历史摘要](../archive/README.md)。 |

## 验证

~~~powershell
# 在 backend 目录运行
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp package

# 在 frontend 目录运行
npm.cmd ci
npm.cmd run lint
npm.cmd run build
~~~

HTTP 样例在 backend/src/test/http，可按需在 IDE 的 HTTP 客户端运行。先把 baseUrl 指向连接独立库的后端；需要样例数据时运行 00-seed-data.http。样例包括写入和删除请求，部分外部接口会发起网络请求。Maven 单元测试与 HTTP 样例各自执行。

后端启动失败时先核对 DB_URL、MySQL 账户和数据库是否已创建。能打开页面但 API 不通时，检查后端 8080、Vite 代理及后端日志。通用任务通过 GET /api/jobs/{jobId} 查询，Bangumi 季度任务使用专用状态接口。已有运行中任务的进程中断后，先核对实际数据和任务状态，再决定是否重跑导入。跨数据或接口升级的验证范围与当前结果见[当前状态](STATUS.md)。
