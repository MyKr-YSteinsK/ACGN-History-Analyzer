# API Integration Guide

本指南面向前后端协作和二次开发，聚焦最常用、最容易踩坑的接口：

- 导入链路（Bangumi 季度、sourceId 补录、记录/收藏导入）
- LLM 链路（状态、配置、分析、问答）
- 统一响应与错误处理建议

---

## 1. 统一响应格式

系统返回统一 `ApiResponse<T>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code = 0` 表示成功
- 非 0 表示业务失败（前端应显示 `message`）

---

## 2. 导入接口

## 2.1 Bangumi 新番季度预览

`POST /api/import/bangumi/season/preview`

请求：

```json
{
  "year": 2024,
  "quarter": 1,
  "includeWeb": false,
  "japaneseOnly": true,
  "excludedSourceIds": ["400602"]
}
```

用途：只做预览，不写库。

---

## 2.2 Bangumi 新番异步导入（推荐）

1) 启动任务  
`POST /api/import/bangumi/season/import-jobs`

请求同预览。

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "jobId": "9a7f6a4f-...."
  }
}
```

2) 查询进度  
`GET /api/import/bangumi/season/import-jobs/{jobId}`

返回（示例）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "jobId": "9a7f6a4f-....",
    "status": "RUNNING",
    "stage": "FETCHING",
    "message": "正在获取 Bangumi TV 动画列表",
    "percent": 34,
    "result": null,
    "errorMessage": null
  }
}
```

---

## 2.3 作品库 sourceId JSON 补录（你当前新增）

### 预览

`POST /api/import/bangumi/subject/preview`

请求：

```json
{
  "subjects": [
    { "sourceId": "400602", "releaseYear": 2020, "releaseQuarter": 4 },
    { "sourceId": "414767", "releaseYear": 2024, "releaseQuarter": 1 }
  ]
}
```

### 正式导入

`POST /api/import/bangumi/subject/import`

请求体同上。

关键逻辑：

- 后端按 `sourceId` 去 Bangumi 拉完整作品信息
- `releaseYear/releaseQuarter` 使用请求值覆盖
- 命中已有作品时仅更新：`tagNames/platformScore/episodeCount`

---

## 2.4 记录导入（preview/import）

- `POST /api/records/import/preview`
- `POST /api/records/import`

定位优先级：

`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`

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

---

## 2.5 收藏导入（preview/import）

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

---

## 3. 导入结果码语义

常见 `item.result`：

- `CAN_IMPORT`: 预览可导入
- `IMPORTED`: 正式导入成功
- `UPDATED`: 命中已有并执行更新
- `SKIPPED`: 跳过（重复、冲突或规则限制）
- `ERROR`: 参数或数据错误
- `AMBIGUOUS`: 多候选需先人工选择

---

## 4. LLM 接口

## 4.1 状态与配置

- `GET /api/llm/status`
- `POST /api/llm/config`
- `POST /api/llm/test-connection`

配置请求示例：

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

---

## 4.2 分析接口（以综合报告为例）

- `GET /api/llm/analysis/comprehensive-report?year=2025&quarter=1&category=ANIME`
- `POST /api/llm/analysis/comprehensive-report`

POST 请求示例：

```json
{
  "year": 2025,
  "quarter": 1,
  "category": "ANIME"
}
```

返回核心结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "analysisType": "COMPREHENSIVE_REPORT",
    "title": "2025 一季度观影复盘",
    "summary": "...",
    "keywords": ["高分集中", "剧情向"],
    "sections": [],
    "recommendations": [],
    "evidence": [],
    "warnings": [],
    "scope": {},
    "mockMode": false,
    "modelName": "deepseek-v4-flash",
    "generatedAt": "2026-05-02T01:20:00"
  }
}
```

---

## 4.3 问答接口

- `GET /api/llm/qa?question=2025年我有哪些高分动画`
- `POST /api/llm/qa`

POST 请求：

```json
{
  "question": "2025年我有哪些高分动画？"
}
```

---

## 5. 常见错误与前端处理建议

- 参数校验失败：直接展示后端 `message`
- LLM 服务不可用：提示用户切换 preview(mock) 模式重试
- 导入 `AMBIGUOUS`：引导用户在预览候选中手动选定后再 import
- 导入 `UPDATED`：前端展示 `changeSummary`（旧值 -> 新值）

---

## 6. 联调建议顺序

1. 调 `/api/import/overview` 和 `/api/import/templates/*` 对齐字段。
2. 先跑 preview，确认 result 分布与 message。
3. 再跑 import，核对写入条数和跳过原因。
4. 最后联调 LLM status/config/test，再接分析与问答页。

