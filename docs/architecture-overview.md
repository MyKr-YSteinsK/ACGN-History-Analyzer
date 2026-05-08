# ACGN History Analyzer 架构速查

本文按当前代码实现整理，旧文档如与代码冲突，以代码为准。

## 1. 总体分层

```text
React + TypeScript frontend
  pages -> components -> api -> query/jobs/theme/utils
        |
        v
Spring Boot backend
  controller -> service -> repository -> entity
        |
        +-> job/* 通用后台任务
        +-> llm/* 智能分析
        +-> config/* 缓存、静态资源、请求追踪
```

## 2. 后端模块职责

- `controller`：薄控制器，负责参数入口、基础业务错误返回、调用 service
- `service`：核心业务规则，包括导入、统计、系列自动关联、封面缓存、LLM 编排
- `repository`：JPA Repository 与少量 JPQL 查询
- `entity`：`Subject`、`UserSubjectRecord`、`UserSubjectCollection`、`Franchise`、`Tag`、`BackgroundJob`、LLM cache entity
- `job`：通用后台任务，支持 sourceId 作品导入、记录/收藏导入、LLM 分析
- `llm`：上下文构建、Prompt、mock/real client、运行时配置、DB 结果缓存
- `config`：Caffeine 读缓存清理、本地封面静态资源、请求 ID filter

## 3. 前端模块职责

- `pages`：页面级状态和业务编排；大页面包括 `ImportPage`、`AnalysisPage`、`HomePage`
- `api`：接口 DTO 与请求函数；`request.ts` 统一拆包 `ApiResponse<T>`
- `jobs`：后台任务轮询、全局完成 Toast、事件中心
- `query`：React Query client、query keys、写入后缓存失效
- `components`：通用 UI、分析面板、作品卡片、详情抽屉、记录弹窗
- `theme`：分类、档期、状态、评分的视觉 token

## 4. 数据模型核心

- `Subject`：作品底座，主要来源唯一键是 `sourceId`
- `Tag`：结构化标签，通过 `subject_tag` 与作品多对多关联
- `UserSubjectRecord`：正式记录，数据库约束保证一个 `subject_id` 一条记录
- `UserSubjectCollection`：收藏状态，数据库约束保证一个 `subject_id` 一条收藏
- `Franchise`：系列信息，`Subject.franchiseId` 保存关联

## 5. 关键链路

### 导入

```text
前端解析 JSON/CSV
  -> preview
  -> 处理 AMBIGUOUS 候选
  -> import
  -> invalidateLibraryRelatedQueries
  -> 全局 Toast 提示任务结果
```

Bangumi 季度导入走 `/api/import/bangumi/season/import-jobs`，状态为 `PENDING/RUNNING/SUCCESS/FAILED`。通用后台任务走 `/api/jobs`，状态为 `PENDING/RUNNING/SUCCEEDED/FAILED/CANCELLED`。

### LLM

```text
LlmAnalysisController
  -> LlmAnalysisService
      -> LlmAnalysisResultCacheService
      -> LlmContextBuilder
      -> LlmPromptService
      -> RuntimeSwitchingLlmClient
          -> MockLlmClient / RealLlmClient
```

### 缓存

读缓存使用 Spring Cache + Caffeine。作品、记录、收藏、系列、标签等写入后会调用 `CacheInvalidationService.invalidateAfterDataMutation()`，同时清理 LLM 结果缓存，避免报告基于旧数据。

## 6. 视觉架构

前端使用浅色玻璃拟态：固定多色柔光背景、半透明白色面板、大圆角、分类色渐变。样式主要来自 Tailwind class，主题 token 位于 `frontend/src/theme/acgnTheme.ts`。
