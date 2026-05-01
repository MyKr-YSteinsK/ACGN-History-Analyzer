# ACGN History Analyzer

ACGN History Analyzer 是一个围绕「作品库 -> 收藏/记录 -> 统计 -> LLM 分析」的全栈系统。  
它用于持续整理个人 ACGN 历史数据，并把行为数据转成可解释的结论：你喜欢什么、评分结构如何、兴趣如何变化、下一步补什么最合适。

---

## 目录

- [项目定位](#项目定位)
- [功能总览](#功能总览)
- [系统架构](#系统架构)
- [前端结构](#前端结构)
- [后端结构](#后端结构)
- [核心数据模型与规则](#核心数据模型与规则)
- [导入体系](#导入体系)
- [LLM 模块（重点）](#llm-模块重点)
- [API 分组速览](#api-分组速览)
- [运行与配置](#运行与配置)
- [典型使用路径](#典型使用路径)
- [设计边界与注意事项](#设计边界与注意事项)
- [配套文档](#配套文档)

---

## 项目定位

项目重点不是“纯记录”，而是“结构化复盘”：

1. 用作品库管理作品本体信息（标题、季度、标签、评分、系列）。
2. 用收藏夹与正式记录管理你的行为状态（想看/搁置/已完成）。
3. 基于结构化数据提供首页统计、TOP 榜单、系列视图。
4. 把数据喂给 LLM，生成可追溯、可解释、可复盘的分析结论。

---

## 功能总览

### 1) 作品库（Library）

- 多条件筛选：类别、年份、月份档期、状态、标签、关键词、排序。
- 卡片展示平台评分 + 个人评分（有正式记录时展示）。
- 支持详情查看、添加/编辑正式记录、状态切换、删除（受引用保护）。

### 2) 正式记录（Records）

- 一作品一正式记录（评分、记录年份、记录档期、备注）。
- 支持记录分页与筛选。
- 支持导入预览与批量导入。

### 3) 收藏夹（Collections）

- 管理 WANT / PAUSED。
- 与正式记录互斥：已正式记录的作品不再保留收藏状态。
- 支持导入预览与批量导入。

### 4) 系列（Franchises）

- 系列列表、详情、系列内作品。
- Bangumi 导入与正式记录后触发轻量自动关联系列。

### 5) 导入中心（Import）

- 统一流程：选择对象 -> 准备内容 -> 预览校验 -> 确认写入。
- 作品库导入支持：
  - Bangumi 新番季度导入（异步任务）
  - Bangumi sourceId JSON 导入（按 sourceId 拉完整作品，手填年份/档期）
- 记录/收藏导入支持 JSON/CSV 与候选选择。

### 6) 首页与榜单（Home / Top）

- 首页季度仪表盘与搜索。
- TOP 榜单（年度高分、神作）。

### 7) 智能分析（Analysis）

- 一键综合报告、年度/季度总结、类型偏好、评分结构、标签偏好等。
- 自然语言问答。
- 推荐、补坑、趋势、偏好矛盾、系列补完等专题分析。

---

## 系统架构

### 技术栈

- Frontend: React 19 + TypeScript + Vite + TailwindCSS
- Backend: Java 17 + Spring Boot 3 + Spring Data JPA
- DB: MySQL
- Integration: Bangumi API, LLM Provider (Mock / DeepSeek)

### 高层数据流

1. 前端页面通过 `api/*` 发送请求。
2. 后端 Controller 接收后交给 Service 编排。
3. Service 聚合 Repository 数据并执行业务规则。
4. 分析页调用 `/api/llm/*`，由 LLM 服务读取结构化上下文并生成结果。

---

## 前端结构

`frontend/src` 主要目录职责：

- `pages/`: 页面级业务编排  
  - `HomePage`, `LibraryPage`, `RecordsPage`, `CollectionsPage`, `ImportPage`, `FranchisesPage`, `AnalysisPage`, `TopPage`
- `components/`: 复用 UI 与业务组件
- `api/`: 接口封装与 DTO 类型
- `mappers/`: 后端响应到视图模型映射
- `theme/`: 统一色板与视觉 token
- `utils/`, `hooks/`, `constants/`, `types/`: 工具、hooks、常量、全局类型

特点：

- 前端统一消费后端 `ApiResponse`，在请求层统一解包和错误转换。
- 页面状态以本地 state 为主，配合 `useMemo/useCallback` 保持性能和可读性。
- 导入页与分析页都有明确的“状态阶段”组织方式，便于复杂交互落地。

---

## 后端结构

`backend/src/main/java/com/mykr/acgnhistoryanalyzer` 主要目录职责：

- `controller/`: API 入口
- `service/`: 业务规则与编排
- `repository/`: JPA 数据访问
- `entity/`: 数据表模型
- `request/`, `response/`: 入参与出参 DTO
- `llm/`: LLM 客户端、配置与运行时切换
- `common/`: 统一响应、错误码、异常处理
- `config/`: 静态资源、启动诊断等

---

## 核心数据模型与规则

### Subject（作品本体）

- 业务唯一键：`sourceId`（Bangumi subject id）
- 重复导入命中同 `sourceId` 时，仅同步：
  - `tagNames`
  - `platformScore`
  - `episodeCount`

### UserSubjectRecord（正式记录）

- 一作品最多一条正式记录（`subject_id` 唯一）。
- 表示“已完成记录”，不是收藏状态。

### UserSubjectCollection（收藏）

- 一作品最多一个收藏状态（WANT / PAUSED）。
- 若已有正式记录，收藏导入或新增会被跳过/禁止。

### Franchise（系列）

- 用于聚合同标题多季/Part/跨媒介作品。
- 系列可手动管理，也可自动检测关联。

### Tag（结构化标签）

- 使用白名单与归一映射，过滤无效/噪声标签。
- 提供白名单清理接口。

---

## 导入体系

### 导入原则

- 所有导入都先 `preview` 再 `import`。
- 单行错误不阻断整批处理。
- 导入结果明确区分：可导入、更新、跳过、错误、歧义候选。

### 作品库导入

#### A. Bangumi 新番季度导入

- 按年 + 档期（1/2/3/4）抓取作品列表。
- 支持异步任务与进度查询（前端默认使用任务模式）。

#### B. Bangumi sourceId JSON 导入

- 适合补录季度抓取漏掉的作品。
- 每行只需：
  - `sourceId`
  - `releaseYear`
  - `releaseQuarter`
- 后端会按 `sourceId` 去 Bangumi 拉完整信息，并用你的年份/档期覆盖写入。

### 记录/收藏导入作品定位优先级

- `subjectId` > `sourceId` > `subjectTitle(+subjectSeasonIndex/subjectPartIndex)`
- 多候选时返回候选列表，前端选定后再正式导入。

---

## LLM 模块（重点）

LLM 模块是项目的核心增值能力：把结构化数据转成可执行洞察。

### 1) 能力清单

基础能力：

- LLM 状态查询
- 运行时配置切换
- 连接测试

分析能力（结构化 JSON 输出）：

- yearly / quarterly
- type-preference / score-structure / tag-preference
- recommendations / collection-backlog
- taste-profile / preference-contradictions / interest-trend
- franchise-completion
- comprehensive-report
- home-insight

交互能力：

- 自然语言问答（QA）

### 2) 上下文构建流程

分析不是直接把数据库原始数据喂给模型，而是先经过上下文构建器：

1. 参数归一化（年、季度、类别）。
2. 聚合记录、收藏、作品、系列数据。
3. 计算统计特征（均分、分布、标签桶、趋势信号等）。
4. 组装 evidence（证据片段）用于可追溯解释。
5. 交给 Prompt 服务按分析类型构造输入。

这样做的价值：

- 降低幻觉（模型基于明确结构化证据回答）。
- 输出可解释（报告中有证据字段）。
- 便于后续扩展分析类型。

### 3) Prompt 与返回契约

- 分析类 Prompt 要求输出严格 JSON，固定字段结构：
  - `analysisType`, `title`, `summary`, `keywords`, `sections`, `recommendations`, `evidence`, `warnings`, `scope`
- 问答类 Prompt 输出自然语言，但仍强调基于个人数据上下文回答。

前端消费结构：

- 报告分段展示
- 推荐列表展示
- 证据与警告展示
- 历史与缓存恢复

### 4) 运行模式与切换机制

支持两种模式：

- `mock`：本地模拟输出，用于开发和兜底
- `real`：真实 Provider（默认 DeepSeek）

运行时由 `RuntimeSwitchingLlmClient` 按配置选择客户端。  
配置可通过接口动态调整（不改文件，不落库，重启恢复默认配置）。

### 5) 故障处理与降级策略

- real 模式网络失败/限流会重试。
- real 模式异常时可降级 mock 输出，保证页面可用性。
- JSON 解析失败有清洗与回退逻辑，尽量给出可展示结果。
- 统一异常映射到业务错误响应，便于前端提示。

### 6) 前端分析页体验策略

- 支持模式化分析与问答。
- 支持历史恢复与本地缓存回退。
- 支持配置测试、缓存清理、失败提示。

这让 LLM 模块在“稳定可用”和“结果可读”之间取得平衡。

---

## API 分组速览

- `/api/subjects/*`：作品库
- `/api/records/*`：正式记录 + 统计
- `/api/collections/*`：收藏
- `/api/franchises/*`：系列
- `/api/tags/*`：标签
- `/api/import/overview`：导入总览
- `/api/import/templates/*`：导入模板
- `/api/import/bangumi/*`：Bangumi 季度导入 + sourceId 导入 + import jobs
- `/api/home/*`：首页统计/搜索
- `/api/top/*`：榜单
- `/api/options/*`：枚举选项
- `/api/llm/*`：智能分析与问答

---

## 运行与配置

### 后端

在 `backend` 目录运行：

- Windows: `mvnw.cmd spring-boot:run`
- Unix-like: `./mvnw spring-boot:run`

关键配置来源：`backend/src/main/resources/application.properties`

- DB：`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- LLM：`app.llm.*` 与 `DEEPSEEK_API_KEY`
- 资源：封面目录映射 `/api/assets/covers/**`

### 前端

在 `frontend` 目录运行：

- 开发：`npm install && npm run dev`
- 构建：`npm run build`

---

## 典型使用路径

### 冷启动整理历史

1. 先用 Bangumi 季度导入构建作品库。
2. 对漏抓作品用 sourceId JSON 模式补录。
3. 导入收藏（WANT/PAUSED）。
4. 导入正式记录（评分/备注）。
5. 进入分析页生成综合报告并做问答复盘。

### 日常维护

1. 新季度导入作品。
2. 持续从收藏转为正式记录。
3. 定期看首页/榜单。
4. 用 LLM 看兴趣趋势与补坑建议。

---

## 设计边界与注意事项

- 作品重复导入不会全量覆盖字段（仅更新标签/平台评分/集数）。
- 一作品一正式记录，避免统计口径混乱。
- 收藏与正式记录互斥，保证状态语义清晰。
- LLM 结果是“辅助决策”，建议结合原始数据与个人判断。

---

## 配套文档

- `docs/llm-guide.md`：LLM 全链路设计、配置、降级与输出契约。
- `docs/import-guide.md`：导入字段与规则规范（主规范）。
- `docs/import-playbook.md`：导入实战手册（按步骤执行版）。
- `docs/architecture-overview.md`：前后端架构速查。
- `docs/api-integration-guide.md`：接口对接指南（请求/响应样例 + 错误处理建议）。

