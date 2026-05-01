# API Integration Guide

本指南用于前后端联调与二次开发，聚焦当前版本高频接口与行为约束。

## 1. 统一响应

所有接口返回 `ApiResponse<T>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code = 0`：成功
- `code != 0`：业务失败，应直接展示 `message`

## 2. 导入接口

### 2.1 Bangumi 季度导入

- `POST /api/import/bangumi/season/preview`
- `POST /api/import/bangumi/season/import-jobs`
- `GET /api/import/bangumi/season/import-jobs/{jobId}`

请求示例：

```json
{
  "year": 2024,
  "quarter": 1,
  "includeWeb": false,
  "japaneseOnly": true,
  "excludedSourceIds": []
}
```

### 2.2 Bangumi sourceId 补录导入

- `POST /api/import/bangumi/subject/preview`
- `POST /api/import/bangumi/subject/import`

请求示例：

```json
{
  "subjects": [
    { "sourceId": "400602", "releaseYear": 2020, "releaseQuarter": 4 }
  ]
}
```

### 2.3 正式记录导入

- `POST /api/records/import/preview`
- `POST /api/records/import`

请求示例：

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

`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`

关键语义：

- 预览多候选返回 `AMBIGUOUS`
- 正式导入命中已有记录时返回 `UPDATED`（覆盖更新）

### 2.4 收藏导入

- `POST /api/collections/import/preview`
- `POST /api/collections/import`

请求示例：

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

## 3. 导入结果码

- `CAN_IMPORT`
- `IMPORTED`
- `UPDATED`
- `AMBIGUOUS`
- `SKIPPED`
- `ERROR`

前端建议：

- `AMBIGUOUS`：必须先选候选再导入
- `UPDATED`：展示覆盖更新信息
- `ERROR`：展示 message 并允许继续处理其他行

## 4. LLM 接口

### 4.1 状态与配置

- `GET /api/llm/status`
- `POST /api/llm/config`
- `POST /api/llm/test-connection`

配置示例：

```json
{
  "previewMode": false,
  "provider": "deepseek",
  "baseUrl": "https://api.deepseek.com",
  "apiKey": "sk-***",
  "model": "deepseek-v4-flash",
  "timeoutMillis": 60000,
  "temperature": 0.6,
  "maxTokens": 3000
}
```

### 4.2 分析与问答

- 报告：`GET/POST /api/llm/analysis/*`
- 问答：`GET/POST /api/llm/qa`

综合报告请求示例：

```json
{
  "year": 2025,
  "quarter": 1,
  "category": "ANIME"
}
```

## 5. 联调建议顺序

1. `/api/import/overview` 与 `/api/import/templates/*` 对齐字段
2. 先调 preview，处理 `AMBIGUOUS` 与 `ERROR`
3. 再调 import，核对 `IMPORTED/UPDATED/SKIPPED`
4. 最后联调 LLM status/config/test，再接分析和问答

