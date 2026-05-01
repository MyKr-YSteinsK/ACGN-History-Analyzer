# ACGN History Analyzer LLM 模块指南

本文档描述 LLM 子系统的真实职责、调用链、输入输出契约和故障策略。

## 1. 定位

LLM 模块用于把结构化行为数据（作品、记录、收藏、标签、系列）转成可解释分析结论。  
它不是通用闲聊模块，而是“面向个人历史数据的分析引擎”。

## 2. 能力范围

接口前缀：`/api/llm`

- 状态与配置：`/status`, `/config`, `/test-connection`
- 分析报告：`/analysis/*`
- 问答：`GET/POST /qa`

分析类型包含：

- yearly, quarterly
- type-preference, score-structure, tag-preference
- recommendations, collection-backlog
- taste-profile, preference-contradictions, interest-trend
- franchise-completion, comprehensive-report, home-insight

## 3. 后端调用链

```text
LlmAnalysisController
  -> LlmAnalysisService
      -> LlmContextBuilder
      -> LlmPromptService
      -> RuntimeSwitchingLlmClient
           -> MockLlmClient / RealLlmClient
```

职责：

- `LlmContextBuilder`：生成可推理上下文和证据
- `LlmPromptService`：按分析类型组织提示词与约束
- `RuntimeSwitchingLlmClient`：运行时路由 mock / real

## 4. 上下文构建策略

核心流程：

1. 参数归一化（年、档期、类型）
2. 聚合业务数据（记录、作品、收藏、系列、标签）
3. 计算统计信号（评分分布、偏好权重、趋势特征）
4. 生成 evidence（供前端展示与追溯）
5. 输出给 Prompt 层

目标：减少幻觉，增强可解释性和稳定性。

## 5. 返回契约

报告类输出（`LlmAnalysisResponse`）关键字段：

- `analysisType`
- `title`, `summary`
- `keywords`
- `sections`
- `recommendations`
- `evidence`
- `warnings`
- `scope`
- `mockMode`, `modelName`, `generatedAt`

问答类输出（`LlmQuestionAnswerResponse`）关键字段：

- `question`, `answer`
- `mockMode`, `modelName`, `generatedAt`

## 6. 配置与运行模式

启动配置来源：`backend/src/main/resources/application.properties`

常见配置：

- `app.llm.enabled`
- `app.llm.provider`
- `app.llm.base-url`
- `app.llm.api-key`（可由 `DEEPSEEK_API_KEY` 注入）
- `app.llm.model`
- `app.llm.timeout-millis`
- `app.llm.temperature`
- `app.llm.max-tokens`

运行时通过 `POST /api/llm/config` 切换：

- `previewMode=true`：强制 mock
- `previewMode=false`：走 real（需有效 provider/baseUrl/apiKey/model）

运行时配置是内存态，重启后回到启动配置。

## 7. 失败与降级

- real 调用支持重试（典型限流/5xx场景）
- 返回 JSON 不规整时会做清洗/回退
- real 失败可降级到 mock（保证功能可用）
- 异常统一映射为标准业务错误响应

## 8. 前端消费策略（AnalysisPage）

- 分析报告与问答分流
- 本地缓存报告与历史问答
- 请求失败时优先回退缓存
- UI 展示 evidence/warnings，保证结果可追溯

