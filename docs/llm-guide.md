# LLM 模块指南

LLM 模块把结构化历史数据转成可解释报告。它不是通用聊天系统，核心目标是围绕个人 ACGN 记录做复盘、推荐和趋势分析。

## 后端链路

```text
LlmAnalysisController
  -> LlmAnalysisService
      -> LlmAnalysisResultCacheService
      -> LlmContextBuilder
      -> LlmPromptService
      -> RuntimeSwitchingLlmClient
          -> MockLlmClient / RealLlmClient
```

前端分析页优先通过通用 `/api/jobs` 创建 `LLM_ANALYSIS` 后台任务。首页智能洞察也复用同一任务类型。

## 能力范围

接口前缀：`/api/llm`

- 状态：`GET /status`
- 配置：`POST /config`
- 连接测试：`POST /test-connection`
- 报告：`GET/POST /analysis/*`
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

## 上下文

`LlmContextBuilder` 聚合：

- 正式记录
- 作品基础信息
- 收藏清单
- 标签
- 系列
- 评分分布
- 年度/季度趋势
- 推荐候选
- evidence 文本

原则是先用后端代码形成稳定结构，再让模型负责表达和总结，减少幻觉。

## 返回契约

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

## 配置

启动配置位于 `backend/src/main/resources/application.properties`：

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

运行时配置请求：

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

`previewMode=true` 强制使用 mock。运行时配置保存在内存中，重启后回到启动配置。

## 缓存

- 后端结果缓存表：`llm_analysis_result_cache`
- 缓存 key 由结果类型、业务 scope、模型、mock/real 模式、内容版本组成。
- 数据写入后，`CacheInvalidationService` 会清理 LLM 结果缓存。
- Prompt 或上下文结构变更时，提高 `app.llm.cache-content-version` 可以让旧缓存自然失效。
- 前端分析页和首页还会在 localStorage 保存最近报告，用于快速恢复和失败兜底。

## 前端消费

`AnalysisPage` 负责：

- 选择分析类型、年份、季度、分类和问题。
- 记住上次选择与输入。
- 通过后台任务生成报告。
- 展示生成进度、队列、错误和历史。
- 缓存报告和问答历史。
- 展示 evidence、warnings、recommendations。
- 任务完成后显示全局 Toast；切换页面不会中断任务轮询。

## 失败策略

- real client 会校验 provider、baseUrl、apiKey、model。
- real 调用遇到限流或 5xx 会重试。
- 模型返回 JSON 不规整时会尝试清洗和文本 fallback。
- real 失败时可降级为 mock，并在 `warnings` 中说明。
