# ACGN History Analyzer 导入规范

本文是导入行为的主规范，覆盖作品库、收藏、正式记录三条链路。

## 1. 推荐顺序

1. 先导入作品库
2. 再导入收藏
3. 最后导入正式记录

作品库是记录、收藏、统计、LLM 分析的底座。记录和收藏导入都需要先能定位到作品。

## 2. 统一流程

导入遵循：

```text
preview -> 处理歧义/排除行 -> import -> 刷新缓存
```

单行失败不会阻塞整批。常见结果码：

- `CAN_IMPORT`
- `IMPORTED`
- `UPDATED`
- `AMBIGUOUS`
- `SKIPPED`
- `ERROR`

## 3. 档期口径

系统使用季度索引表示 ACGN 档期：

- `1` = 一月
- `2` = 四月
- `3` = 七月
- `4` = 十月

## 4. 作品库导入

### 4.1 Bangumi 季度导入

接口：

- `POST /api/import/bangumi/season/preview`
- `POST /api/import/bangumi/season/import-jobs`
- `GET /api/import/bangumi/season/import-jobs/{jobId}`

支持字段：

- `year`
- `quarter`
- `includeWeb`
- `japaneseOnly`
- `excludedSourceIds`

正式导入会拉取 Bangumi 数据、缓存封面、同步标签、检查重复并写入作品库。

### 4.2 Bangumi sourceId 补录

接口：

- `POST /api/import/bangumi/subject/preview`
- `POST /api/import/bangumi/subject/import`

前端当前优先通过通用 `/api/jobs` 异步执行补录预览和写入。

最小请求：

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

## 5. 作品唯一性与重复更新

作品唯一键按 `sourceId` 处理。命中已有作品时，重复导入只更新：

- `tagNames`
- `platformScore`
- `episodeCount`

标题、档期、封面、系列等字段不会被重复导入覆盖。

## 6. 正式记录导入

接口：

- `POST /api/records/import/preview`
- `POST /api/records/import`

顶层字段：`records`

定位优先级：

```text
subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)
```

规则：

- `subjectId`、`sourceId`、`subjectTitle` 至少提供一个
- 标题多命中返回 `AMBIGUOUS`
- 一作品只保留一条正式记录
- 已有记录时正式导入覆盖更新并返回 `UPDATED`

示例：

```json
{
  "records": [
    {
      "sourceId": "414767",
      "scoreValue": 48,
      "recordYear": 2024,
      "recordQuarter": 1,
      "note": "节奏舒缓但后劲很强。"
    }
  ]
}
```

## 7. 收藏导入

接口：

- `POST /api/collections/import/preview`
- `POST /api/collections/import`

顶层字段：`collections`

定位优先级与正式记录一致：

```text
subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)
```

规则：

- `collectionType` 必填，取值 `WANT` 或 `PAUSED`
- 作品已有正式记录时跳过收藏导入
- 作品已有收藏时更新清单类型和备注

示例：

```json
{
  "collections": [
    {
      "sourceId": "414767",
      "collectionType": "WANT",
      "note": "之后想补完。"
    }
  ]
}
```

## 8. 后台提示

前端导入中心使用后台任务轮询。任务完成、失败或超时时会通过 `frontend/src/jobs/backgroundJobEvents.ts` 发出全局 Toast，展示在页面右上角。

## 9. 旧接口说明

`/api/subjects/import*` 仍可用于开发或脚本场景，但日常作品导入推荐走 `/api/import/bangumi/*`。
