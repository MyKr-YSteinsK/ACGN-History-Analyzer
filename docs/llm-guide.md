# ACGN History Analyzer LLM 模块指南

LLM 模块用于把结构化历史数据转成可解释分析结论。它不是通用聊天入口，而是面向个人 ACGN 历史数据的分析引擎。

## 1. 后端调用链

```text
LlmAnalysisController
  -> LlmAnalysisService
      -> LlmAnalysisResultCacheService
      -> LlmContextBuilder
      -> LlmPromptService
      -> RuntimeSwitchingLlmClient
          -> MockLlmClient / RealLlmClient
```

`AnalysisPage` 也可以通过通用 `/api/jobs` 以 `LLM_ANALYSIS` 后台任务方式生成报告。

## 2. 能力范围

接口前缀：`/api/llm`

- 状态：`GET /status`
- 配置：`POST /config`
- 连接测试：`POST /test-connection`
- 分析报告：`GET/POST /analysis/*`
- 问答：`GET/POST /qa`

分析类型：

- `yearly`
- `quarterly`
- `type-preference`
- `score-structure`
- `tag-preference`
- `recommendations`
- `collection-backlog`
- `taste-profile`
- `preference-contradictions`
- `interest-trend`
- `franchise-completion`
- `comprehensive-report`
- `home-insight`

## 3. 上下文构建

`LlmContextBuilder` 会聚合：

- 正式记录
- 作品信息
- 收藏清单
- 标签
- 系列
- 评分分布
- 年度/季度趋势
- 推荐候选
- evidence 文本

目标是先用代码形成可推理结构，再让模型负责表达和总结，降低幻觉。

## 4. 返回契约

报告返回 `LlmAnalysisResponse`：

- `analysisType`
- `title`
- `summary`
- `keywords`
- `sections`
- `recommendations`
- `evidence`
- `warnings`
- `scope`
- `mockMode`
- `modelName`
- `generatedAt`

问答返回 `LlmQuestionAnswerResponse`：

- `question`
- `answer`
- `mockMode`
- `modelName`
- `generatedAt`

## 5. 配置

启动配置来源：`backend/src/main/resources/application.properties`

常见配置：

- `app.llm.enabled`
- `app.llm.provider`
- `app.llm.base-url`
- `app.llm.api-key`
- `app.llm.model`
- `app.llm.timeout-seconds`
- `app.llm.temperature`
- `app.llm.max-tokens`
- `app.llm.cache-content-version`
- `app.llm.result-cache-days`

运行时通过 `POST /api/llm/config` 切换：

```json
{
  "previewMode": true,
  "baseUrl": "https://api.deepseek.com",
  "apiKey": "sk-***",
  "model": "deepseek-v4-flash",
  "timeoutSeconds": 30,
  "temperature": 0.4,
  "maxTokens": 3000
}
```

`previewMode=true` 强制使用 mock。运行时配置是内存态，重启后回到启动配置。

## 6. 缓存与失效

LLM 结果缓存位于数据库表对应的 `LlmAnalysisResultCacheEntity`。缓存 key 由结果类型、逻辑 key、模型、mock/real 模式、内容版本构成。

数据写入后，`CacheInvalidationService` 会清理 LLM 结果缓存。修改 prompt 逻辑或上下文结构时，可以提高 `app.llm.cache-content-version` 让旧缓存自然失效。

## 7. 失败策略

- real client 会校验 provider/baseUrl/apiKey/model
- real 调用遇到限流或 5xx 会重试
- 模型返回 JSON 不规整时会尝试清洗和文本 fallback
- real 失败时可降级为 mock，并在 `warnings` 中说明

## 8. 前端消费

`AnalysisPage` 负责：

- 选择分析类型、年份、季度、分类
- 发起后台 LLM 任务
- 展示生成进度
- 缓存报告和问答历史到 localStorage
- 展示 evidence、warnings、recommendations
- 任务完成后通过全局 Toast 提示
