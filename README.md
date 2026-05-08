# ACGN History Analyzer

ACGN History Analyzer 是一个用于整理个人动画、漫画、游戏、小说历史数据的全栈系统。系统以“作品库”为数据底座，围绕“正式记录、收藏状态、系列关系、统计榜单、LLM 分析”构建复盘闭环。

## 当前状态

- Frontend: React 19 + TypeScript + Vite + Tailwind CSS v4 + React Query
- Backend: Java 17 + Spring Boot 3.5 + Spring Data JPA
- Database: MySQL
- Integration: Bangumi API, DeepSeek/Mock LLM
- Assets: Bangumi 封面会缓存到本地 `uploads/covers`

## 核心功能

- 作品库：作品主数据、标签、系列、封面、平台评分、状态流转
- 正式记录：一作品一条记录，保存个人评分、记录年月档期、备注
- 收藏夹：`WANT` / `PAUSED` 清单，与正式记录互斥
- 导入中心：Bangumi 季度导入、Bangumi sourceId 补录、记录/收藏 JSON 或 CSV 导入
- 后台任务：通用 `/api/jobs` 支持 sourceId 补录、记录/收藏导入、LLM 分析；Bangumi 季度导入有独立任务接口
- 页面记忆：导入中心、作品库、记录库、收藏、系列、TOP、首页和分析页会记住上次筛选/输入
- 首页与榜单：季度仪表盘、搜索、年度高分、TOP 视图
- 智能分析：结构化报告、问答、推荐、趋势、系列补完分析

## 关键业务规则

- 作品唯一来源键为 `sourceId`，当前主要对应 Bangumi subject id
- 作品重复导入只更新 `tagNames`、`platformScore`、`episodeCount`
- 正式记录一作品最多一条；重复导入执行覆盖更新并返回 `UPDATED`
- 收藏一作品最多一条；作品已有正式记录时收藏导入会跳过
- 记录/收藏导入定位优先级：`subjectId > sourceId > subjectTitle(+season/part)`
- 导入遵循 `preview -> import`；歧义匹配返回 `AMBIGUOUS`，需要前端选择候选
- 数据写入后会清理读缓存和 LLM 结果缓存

## 项目结构

```text
backend/
  src/main/java/com/mykr/acgnhistoryanalyzer/
    controller/        REST API
    service/           核心业务逻辑
    repository/        JPA 查询
    entity/            JPA 实体
    request|response/  DTO
    job/               通用后台任务
    llm/               LLM 上下文、Prompt、客户端、结果缓存
    config/            缓存、静态资源、请求 ID 等

frontend/
  src/
    api/         API DTO 与请求封装
    pages/       页面编排
    components/  复用组件
    jobs/        后台任务轮询与全局 Toast
    hooks/       页面状态持久化等复用 Hook
    query/       React Query keys/cache
    theme/       ACGN 分类、档期、状态视觉主题
    utils|hooks|types|mappers/

docs/            开发和导入说明
sql/             手动维护脚本
uploads/         本地封面缓存
```

## 前端设计语言

整体是浅色玻璃拟态：柔和多色背景、半透明白色面板、大圆角卡片、轻阴影、按 ACGN 分类映射颜色。主题集中在 `frontend/src/theme/acgnTheme.ts`，全局背景和进入动画在 `frontend/src/index.css`。

## API 分组

- `/api/subjects/*`
- `/api/records/*`
- `/api/collections/*`
- `/api/franchises/*`
- `/api/tags/*`
- `/api/import/*`
- `/api/import/bangumi/*`
- `/api/jobs/*`
- `/api/home/*`
- `/api/top/*`
- `/api/options/*`
- `/api/export/*`
- `/api/llm/*`

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

构建与检查：

```powershell
cd frontend
npm run build

cd ..\backend
.\mvnw.cmd test
```

## 配套文档

- `docs/architecture-overview.md`：架构速查
- `docs/api-integration-guide.md`：接口对接
- `docs/import-guide.md`：导入规则
- `docs/import-playbook.md`：导入执行手册
- `docs/llm-guide.md`：LLM 模块说明
