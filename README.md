# ACGN History Analyzer

ACGN History Analyzer 是一个个人 ACGN 历史数据管理系统。它以作品库为底座，串联正式记录、收藏、系列、榜单、导入导出和 LLM 分析。

## 技术栈

- Frontend：React 19、TypeScript、Vite、Tailwind CSS v4、React Query
- Backend：Java 17、Spring Boot 3.5、Spring Data JPA
- Database：MySQL
- Integration：Bangumi API、DeepSeek / Mock LLM
- Assets：Bangumi 封面缓存到 `uploads/covers`

## 功能

- 作品库：作品主数据、封面、标签、系列、平台评分和状态。
- 正式记录：一作品一条记录，保存个人评分、年月档期和备注。
- 收藏夹：`WANT` / `PAUSED`，与正式记录互斥。
- 导入导出：Bangumi 季度导入、sourceId 补录、记录/收藏 JSON 或 CSV 导入、Bangumi 往返 JSON 导出。
- 后台任务：通用 `/api/jobs` 处理异步导入和 LLM 分析，完成后显示全局 Toast。
- 页面记忆：主要页面会记住筛选条件和输入。
- 统计与榜单：首页季度仪表盘、TOP 榜单、高分记录。
- 智能分析：年度、季度、偏好、推荐、趋势、系列补完和自由问答。

## 业务规则

- 作品当前主要以 Bangumi `sourceId` 作为来源唯一键。
- 作品重复导入只更新标签、平台评分、集数。
- 正式记录一作品最多一条；重复导入覆盖更新。
- 收藏一作品最多一条；作品已有正式记录时收藏导入跳过。
- 记录/收藏导入定位优先级：`subjectId > sourceId > subjectTitle(+season/part)`。
- 数据写入后会清理读缓存和 LLM 结果缓存。

## 目录

```text
backend/
  src/main/java/com/mykr/acgnhistoryanalyzer/
    controller/        REST API
    service/           核心业务逻辑
    repository/        JPA 查询
    entity/            JPA 实体
    request|response/  DTO
    job/               通用后台任务
    llm/               LLM 上下文、Prompt、客户端、缓存
    config/            缓存、静态资源、请求追踪

frontend/
  src/
    routes/      路由
    pages/       页面编排
    components/  复用组件
    api/         请求封装和 DTO
    jobs/        后台任务轮询与 Toast
    hooks/       状态持久化等 Hook
    query/       React Query keys/cache
    theme/       视觉 token
    utils/       工具函数

docs/            架构、数据库、导入、LLM 文档
sql/             手动维护脚本
uploads/         本地封面缓存
```

## 本地运行

后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

前端：

```powershell
cd frontend
npm install
npm run dev
```

验证：

```powershell
cd frontend
npm run lint
npm run build

cd ..\backend
.\mvnw.cmd test
```

## 文档

- `docs/architecture-overview.md`：架构、页面、接口、任务和前端体验约定。
- `docs/database-schema.md`：数据库表、字段、约束、索引。
- `docs/import-guide.md`：导入流程、请求格式、结果码和排障。
- `docs/llm-guide.md`：LLM 链路、配置、缓存和前端消费。
