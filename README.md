# ACGN History Analyzer

ACGN History Analyzer 是一个用于管理个人 ACGN 历史数据的全栈系统。  
系统以“作品库”为数据底座，围绕“收藏/正式记录/统计/LLM 分析”构建完整闭环，目标是把历史行为数据转成可解释、可追溯、可执行的复盘结论。

## 功能概览

- 作品库：作品主数据管理、状态流转、详情查看、标签与系列关联
- 正式记录：一作品一条记录，保存评分、记录时间和备注
- 收藏夹：WANT / PAUSED 清单管理，和正式记录语义分离
- 导入中心：支持 Bangumi 季度导入、sourceId 补录导入、记录/收藏 JSON/CSV 导入
- 首页与榜单：季度仪表盘、历史搜索、TOP 视图
- 智能分析：结构化报告 + 问答 + 推荐 + 趋势分析

## 当前关键行为（2026-05）

- 作品唯一键为 `sourceId`（Bangumi subject id）
- 作品重复导入只更新三项：`tagNames`、`platformScore`、`episodeCount`
- 记录/收藏导入定位优先级：`subjectId > sourceId > subjectTitle(+season/part)`
- 记录导入遇到已存在记录时执行覆盖更新（`UPDATED`），不再跳过
- 导入预览/结果支持分层状态展示：可导入、更新、歧义待选、跳过、错误
- 作品库与记录库的关键词搜索是页面级全局搜索（输入关键词后不再受年/月/类型限制）

## 技术栈

- Frontend: React 19 + TypeScript + Vite + TailwindCSS
- Backend: Java 17 + Spring Boot 3 + Spring Data JPA
- Database: MySQL
- Integration: Bangumi API, LLM Provider（Mock / Real）

## 架构总览

前端通过 `api/*` 调用后端；后端采用 `controller -> service -> repository -> entity` 分层。  
LLM 相关能力由独立 `llm/*` 模块负责：上下文构建、Prompt 组装、客户端路由、降级策略。

### 前端主要目录

- `frontend/src/pages`: 页面编排（Library/Records/Collections/Import/Analysis 等）
- `frontend/src/components`: 复用组件
- `frontend/src/api`: 请求封装与 DTO
- `frontend/src/mappers`: 后端响应映射
- `frontend/src/theme`: 视觉主题
- `frontend/src/utils|hooks|constants|types`: 工具与基础层

### 后端主要目录

- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/controller`
- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/service`
- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/repository`
- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/entity`
- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/request|response`
- `backend/src/main/java/com/mykr/acgnhistoryanalyzer/llm`

## 导入体系

统一流程：`preview -> import`。  
单行错误不会阻塞整批。

### 作品库导入

1. Bangumi 季度导入（支持异步任务）
2. Bangumi sourceId JSON 补录导入（每行只需 `sourceId + releaseYear + releaseQuarter`）

### 记录/收藏导入

- 支持 JSON / CSV
- 支持歧义候选返回与人工选择
- 记录重复项在正式导入阶段执行覆盖更新

## LLM 模块（重点）

LLM 模块不是“通用聊天”，而是“结构化复盘引擎”：

1. 从作品/记录/收藏/标签/系列聚合上下文
2. 计算统计特征并生成证据（evidence）
3. 按分析类型构建 Prompt
4. 调用运行时客户端（mock/real）
5. 返回结构化结果供前端渲染与追溯

### 支持能力

- 状态与配置：`/api/llm/status`, `/api/llm/config`, `/api/llm/test-connection`
- 报告分析：yearly、quarterly、type-preference、score-structure、tag-preference、recommendations、collection-backlog、taste-profile、preference-contradictions、interest-trend、franchise-completion、comprehensive-report、home-insight
- 问答：`GET/POST /api/llm/qa`

### 设计原则

- 先结构化聚合，再让模型生成表达层文本
- 尽量减少幻觉，增强结果可解释性
- real 模式失败可降级 mock，保障可用性
- 运行时配置为内存态，重启后回到启动配置

## API 分组

- `/api/subjects/*`
- `/api/records/*`
- `/api/collections/*`
- `/api/franchises/*`
- `/api/tags/*`
- `/api/import/overview`
- `/api/import/templates/*`
- `/api/import/bangumi/*`
- `/api/home/*`
- `/api/top/*`
- `/api/options/*`
- `/api/llm/*`

## 本地运行

### 后端

- Windows: `backend/mvnw.cmd spring-boot:run`
- Unix-like: `backend/mvnw spring-boot:run`

常用配置来源：`backend/src/main/resources/application.properties`

### 前端

- `cd frontend && npm install`
- `npm run dev`
- `npm run build`

## 配套文档

- `docs/import-guide.md`：导入规则主规范
- `docs/import-playbook.md`：导入执行手册
- `docs/api-integration-guide.md`：接口对接与示例
- `docs/llm-guide.md`：LLM 模块详解
- `docs/architecture-overview.md`：架构速查

