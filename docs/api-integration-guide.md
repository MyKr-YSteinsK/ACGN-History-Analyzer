# API Integration Guide

本指南用于前后端联调和二次开发，按当前代码中的接口与 DTO 整理。

## 1. 统一响应

后端接口统一返回：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

- `code = 0`：成功，前端使用 `data`
- `code != 0`：业务失败，前端展示 `message`
- 传输层错误由前端 `frontend/src/api/request.ts` 转换为 `ApiRequestError`

## 2. 通用后台任务

接口：

- `POST /api/jobs`
- `GET /api/jobs/{jobId}`
- `DELETE /api/jobs/{jobId}`

任务类型：

- `BANGUMI_SUBJECT_PREVIEW`
- `BANGUMI_SUBJECT_IMPORT`
- `COLLECTION_IMPORT_PREVIEW`
- `COLLECTION_IMPORT`
- `RECORD_IMPORT_PREVIEW`
- `RECORD_IMPORT`
- `LLM_ANALYSIS`

状态：

- `PENDING`
- `RUNNING`
- `SUCCEEDED`
- `FAILED`
- `CANCELLED`

前端通过 `waitForBackgroundJob(jobId)` 轮询，并使用 `jobs/backgroundJobEvents.ts` 触发全局完成 Toast。

## 3. 导入接口

### 3.1 Bangumi 季度导入

- `POST /api/import/bangumi/season/preview`
- `POST /api/import/bangumi/season/import-jobs`
- `GET /api/import/bangumi/season/import-jobs/{jobId}`

请求：

```json
{
  "year": 2024,
  "quarter": 1,
  "includeWeb": false,
  "japaneseOnly": true,
  "excludedSourceIds": []
}
```

季度任务状态使用 `PENDING/RUNNING/SUCCESS/FAILED`。这是 Bangumi 季度导入的独立任务服务，与通用 `/api/jobs` 的 `SUCCEEDED` 命名不同。

### 3.2 Bangumi sourceId 补录

前端当前通过通用 `/api/jobs` 执行，后端也保留同步接口：

- `POST /api/import/bangumi/subject/preview`
- `POST /api/import/bangumi/subject/import`

请求：

```json
{
  "subjects": [
    {
      "sourceId": "400602",
      "releaseYear": 2020,
      "releaseQuarter": 4
    }
  ]
}
```

后端会按 `sourceId` 拉取 Bangumi 完整信息，`releaseYear/releaseQuarter` 以请求值为准。

### 3.3 正式记录导入

- `POST /api/records/import/preview`
- `POST /api/records/import`

请求：

```json
{
  "records": [
    {
      "sourceId": "400602",
      "recordYear": 2024,
      "recordQuarter": 1,
      "scoreValue": 47,
      "note": "补完"
    }
  ]
}
```

定位优先级：

```text
subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)
```

已有正式记录时，正式导入覆盖更新并返回 `UPDATED`。

### 3.4 收藏导入

- `POST /api/collections/import/preview`
- `POST /api/collections/import`

请求：

```json
{
  "collections": [
    {
      "sourceId": "400602",
      "collectionType": "WANT",
      "note": "准备补"
    }
  ]
}
```

`collectionType` 只能是 `WANT` 或 `PAUSED`。作品已有正式记录时，收藏导入会跳过。

## 4. 导入结果码

- `CAN_IMPORT`：预览可写入
- `IMPORTED`：正式导入成功
- `UPDATED`：命中已有数据并更新
- `AMBIGUOUS`：多候选，需要前端选择
- `SKIPPED`：规则性跳过
- `ERROR`：字段或业务校验失败

## 5. 常用查询接口

- `GET /api/subjects/page`
- `GET /api/records/page`
- `GET /api/collections`
- `GET /api/franchises/overview`
- `GET /api/tags`
- `GET /api/home/quarter-dashboard`
- `GET /api/home/search`
- `GET /api/top/year-high-score`
- `GET /api/top/masterpieces`
- `GET /api/export/bangumi-roundtrip`

## 6. LLM 接口

状态与配置：

- `GET /api/llm/status`
- `POST /api/llm/config`
- `POST /api/llm/test-connection`

配置请求：

```json
{
  "previewMode": false,
  "baseUrl": "https://api.deepseek.com",
  "apiKey": "sk-***",
  "model": "deepseek-v4-flash",
  "timeoutSeconds": 30,
  "temperature": 0.4,
  "maxTokens": 3000
}
```

分析与问答：

- `GET/POST /api/llm/analysis/yearly`
- `GET/POST /api/llm/analysis/quarterly`
- `GET/POST /api/llm/analysis/type-preference`
- `GET/POST /api/llm/analysis/score-structure`
- `GET/POST /api/llm/analysis/tag-preference`
- `GET/POST /api/llm/analysis/recommendations`
- `GET/POST /api/llm/analysis/collection-backlog`
- `GET/POST /api/llm/analysis/taste-profile`
- `GET/POST /api/llm/analysis/preference-contradictions`
- `GET/POST /api/llm/analysis/interest-trend`
- `GET/POST /api/llm/analysis/franchise-completion`
- `GET/POST /api/llm/analysis/comprehensive-report`
- `GET/POST /api/llm/analysis/home-insight`
- `GET/POST /api/llm/qa`
