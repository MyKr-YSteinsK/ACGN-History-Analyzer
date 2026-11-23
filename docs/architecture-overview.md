# 架构与接口概览

本文提供当前代码的架构和接口索引。接手、运行和维护任务见[项目源文件](项目源文件/00-START_HERE.md)。

## 技术栈

- 前端：React 19、TypeScript、Vite、Tailwind CSS v4、React Query
- 后端：Java 17、Spring Boot 3.5、Spring Data JPA
- 数据库：MySQL，Hibernate `ddl-auto=update`
- 集成：Bangumi API、DeepSeek / Mock LLM
- 本地资源：Bangumi 封面缓存到后端工作目录下的 `uploads/covers`

## 代码分层

```text
frontend/src
  routes        路由和懒加载
  pages         页面级状态与业务编排
  components    通用 UI、作品卡、详情抽屉、分析面板
  api           前端 DTO 与请求函数
  jobs          后台任务轮询与全局 Toast
  query         React Query key 与缓存失效
  hooks         页面状态持久化等复用 Hook
  theme         分类、档期、状态、评分视觉 token
  utils         localStorage cache、报告格式化、展示辅助

backend/src/main/java/com/mykr/acgnhistoryanalyzer
  controller    REST 入口
  service       核心业务规则
  repository    JPA Repository / JPQL
  entity        JPA 表映射
  request       请求 DTO
  response      响应 DTO
  job           通用后台任务
  llm           LLM 上下文、Prompt、客户端、结果缓存
  config        缓存、静态资源、请求追踪、运行时配置
```

## 主要页面

- 首页：季度概览、高分记录、分类占比、首页智能洞察。
- 我的记录：按年份、档期、分类、关键词和排序查看正式记录。
- 作品库：作品基础信息、状态、标签、封面、详情与记录操作。
- TOP 榜单：年度高分和个人神作视图。
- 收藏夹：`WANT` / `PAUSED` 清单，与正式记录互斥。
- 导入：Bangumi 季度导入、sourceId 补录、收藏/记录 JSON 或 CSV 导入。
- 系列：跨季、跨媒介系列整理。
- 智能分析：年度、季度、偏好、推荐、问答等 LLM 报告。

## 核心业务规则

- 作品库是记录、收藏、统计、导入和 LLM 分析的底座。
- 作品当前主要以 Bangumi `sourceId` 作为来源唯一键；唯一性由服务层检查。
- 作品重复导入只更新标签、平台评分、集数，不覆盖标题、档期、封面、系列。
- 正式记录一作品最多一条，重复导入会覆盖更新。
- 收藏一作品最多一条。创建、更新或重新导入正式记录时移除对应收藏，有正式记录时跳过收藏导入。
- 记录/收藏导入定位优先级：`subjectId > sourceId > subjectTitle(+season/part)`。
- 写入作品、记录、收藏、系列或标签后，需要清理读缓存和 LLM 结果缓存。

## 后台任务

通用任务走 `/api/jobs`，实体表为 `background_job`。

- 创建：`POST /api/jobs`
- 查询：`GET /api/jobs/{jobId}`
- 取消：`DELETE /api/jobs/{jobId}`
- 类型：`BANGUMI_SUBJECT_PREVIEW`、`BANGUMI_SUBJECT_IMPORT`、`COLLECTION_IMPORT_PREVIEW`、`COLLECTION_IMPORT`、`RECORD_IMPORT_PREVIEW`、`RECORD_IMPORT`、`LLM_ANALYSIS`
- 状态：`PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELLED`

前端通过 `registerBackgroundJobUi` 注册任务界面信息并启动共享轮询，`BackgroundJobsProvider` 负责显示全局提示。Bangumi 季度导入使用独立接口和 `bangumiSeasonJobMonitor`，同样支持跨路由轮询。通用任务保存在 MySQL，季度任务状态仅驻留在运行进程中。

## API 分组

业务 API 的常用响应结构

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

常用接口组：

- 健康与选项：`GET /api/hello`、`GET /api/health`、`GET /api/options/*`
- 作品：`/api/subjects/*`
- 记录：`/api/records/*`
- 收藏：`/api/collections/*`
- 系列：`/api/franchises/*`
- 标签：`/api/tags/*`
- 导入说明与模板：`/api/import/overview`、`/api/import/templates/*`
- Bangumi 导入：`/api/import/bangumi/*`
- 后台任务：`/api/jobs/*`
- 首页与榜单：`/api/home/*`、`/api/top/*`
- 导出：`GET /api/export/bangumi-roundtrip`，包含记录 JSON 与可选的关联 Bangumi 作品 JSON
- LLM：`/api/llm/*`

## 前端实现入口

- 全局视觉 token 在 `frontend/src/theme/acgnTheme.ts`，基础样式在 `frontend/src/index.css`。
- 移动端导航在顶部显示，桌面端为左侧侧栏。
- 导入、作品库、记录库、收藏、系列、TOP、首页和分析页使用 `usePersistentState` 记住主要筛选和输入。

## 相关文档

- `docs/database-schema.md`：数据库表、字段、约束、索引。
- `docs/import-guide.md`：导入流程、请求格式、结果码和排障。
- `docs/llm-guide.md`：LLM 模块、缓存、配置和前端消费。
