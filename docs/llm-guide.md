# ACGN History Analyzer LLM 模块指南

本指南专门说明项目中的 LLM 子系统：它做什么、怎么做、如何配置、出现问题怎么降级。

---

## 1. 模块定位

LLM 模块并不是“纯聊天”功能，而是把系统已有结构化数据（作品、记录、收藏、标签、系列）转换成可读结论，服务于：

- 年度/季度复盘
- 偏好画像
- 推荐与补坑
- 趋势观察
- 自然语言问答

核心原则：**先结构化聚合，再交给模型生成表达层结果**。

---

## 2. 功能矩阵

接口统一前缀：`/api/llm`

- 配置与状态
  - `GET /status`
  - `POST /config`
  - `POST /test-connection`
- 报告分析（GET/POST 双入口）
  - `yearly`, `quarterly`, `type-preference`, `score-structure`, `tag-preference`
  - `recommendations`, `collection-backlog`
  - `taste-profile`, `preference-contradictions`, `interest-trend`
  - `franchise-completion`, `comprehensive-report`, `home-insight`
- 问答
  - `GET /qa`
  - `POST /qa`

---

## 3. 后端调用链

```text
LlmAnalysisController
  -> LlmAnalysisService
      -> LlmContextBuilder (聚合上下文)
      -> LlmPromptService  (构建 prompt)
      -> RuntimeSwitchingLlmClient
           -> MockLlmClient 或 RealLlmClient
```

### 关键职责

- `LlmContextBuilder`: 把原始业务数据转成“可推理上下文”
- `LlmPromptService`: 把上下文 + 分析目标转换成指令
- `RuntimeSwitchingLlmClient`: 运行时选择 mock/real
- `RealLlmClient`: 调外部模型（当前默认 DeepSeek）

---

## 4. 上下文构建策略

构建过程并不是简单拼文本，而是按分析目标做字段聚合：

1. 参数归一化（year/quarter/category）
2. 拉取记录、作品、收藏、系列
3. 统计衍生（均分、评分带、类别分布、标签分桶、趋势信号）
4. 生成 evidence（证据列表）
5. 交给 prompt 层统一描述

这样能显著降低“模型随口编造”的风险，并保证输出可解释。

---

## 5. 输出契约

### 报告类

输出统一映射到 `LlmAnalysisResponse`，关键字段：

- `analysisType`
- `title`, `summary`
- `keywords`
- `sections`
- `recommendations`
- `evidence`
- `warnings`
- `scope`
- `mockMode`, `modelName`, `generatedAt`

### 问答类

输出映射到 `LlmQuestionAnswerResponse`：

- `question`
- `answer`
- `mockMode`
- `modelName`
- `generatedAt`

---

## 6. 配置与模式切换

### 启动配置

来源：`backend/src/main/resources/application.properties`

典型项：

- `app.llm.enabled`
- `app.llm.provider`
- `app.llm.base-url`
- `app.llm.api-key`（可由 `DEEPSEEK_API_KEY` 注入）
- `app.llm.model`
- `app.llm.timeout-millis`
- `app.llm.temperature`
- `app.llm.max-tokens`

### 运行时切换

通过 `POST /api/llm/config` 可在运行时切换：

- `previewMode=true` -> 强制 mock
- 否则走 real（需完整 provider/baseUrl/apiKey/model）

注意：运行时配置为内存态，重启后恢复到启动配置。

---

## 7. 异常与降级

- real 调用层有重试（限流/5xx）
- 返回内容 JSON 异常时有清洗与回退策略
- real 模式失败可降级 mock，尽量保证前端可用
- 统一异常由全局异常处理器映射为可读业务错误

---

## 8. 前端分析页策略

`AnalysisPage` 主要策略：

- 模式化分析 + 问答
- 本地缓存（结果与历史）
- 失败时优先回退缓存
- 配置测试与状态感知

这保证“可用性优先 + 结果持续可追溯”。

