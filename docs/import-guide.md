# ACGN History Analyzer 导入规范

本文档是当前系统导入行为的主规范，覆盖作品库、收藏、正式记录三条导入链路。

## 1. 导入边界

- 作品库：推荐仅通过 Bangumi 导入
- 收藏夹：支持 JSON / CSV 手动导入
- 正式记录：支持 JSON / CSV 手动导入

说明：作品主数据是全系统的数据底座，常规流程不建议手工拼完整 `subjects` 内容。

## 2. 推荐顺序

1. 先导入作品库（Bangumi 季度导入）
2. 再导入收藏（WANT / PAUSED）
3. 最后导入正式记录（评分 / 备注）

## 3. 统一流程与结果语义

所有导入都遵循 `preview -> import`。  
单行异常不会阻塞整批。

常见结果码：

- `CAN_IMPORT`：预览可导入
- `IMPORTED`：正式导入成功
- `UPDATED`：命中已有数据并执行更新
- `AMBIGUOUS`：多候选，需要前端先选定
- `SKIPPED`：规则性跳过
- `ERROR`：字段或业务校验失败

## 4. 月份档期口径

系统统一使用季度索引而非自然月：

- `1` = 一月
- `2` = 四月
- `3` = 七月
- `4` = 十月

## 5. 作品库导入

### 5.1 Bangumi 季度导入（主流程）

接口：

- `POST /api/import/bangumi/season/preview`
- `POST /api/import/bangumi/season/import-jobs`
- `GET /api/import/bangumi/season/import-jobs/{jobId}`

说明：

- 支持 `includeWeb`、`japaneseOnly`、`excludedSourceIds`
- 正式导入时会走封面缓存、标签同步、重复检查等完整流程

### 5.2 Bangumi sourceId 补录导入（补漏）

接口：

- `POST /api/import/bangumi/subject/preview`
- `POST /api/import/bangumi/subject/import`

请求体最小字段：

```json
{
  "subjects": [
    { "sourceId": "400602", "releaseYear": 2020, "releaseQuarter": 4 }
  ]
}
```

行为：

- 后端按 `sourceId` 去 Bangumi 拉完整作品数据
- `releaseYear` / `releaseQuarter` 以请求值为准并覆盖

## 6. 作品唯一性与更新边界

作品唯一键：`sourceId`。  
若命中同 `sourceId`，重复导入只更新：

- `tagNames`
- `platformScore`
- `episodeCount`

其他字段（标题、系列、档期、封面等）保持原值。

## 7. 正式记录导入

接口：

- `POST /api/records/import/preview`
- `POST /api/records/import`

顶层字段：`records`。

定位优先级：

`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`

关键规则：

- `subjectId` / `sourceId` / `subjectTitle` 至少提供一个
- 标题多命中返回 `AMBIGUOUS`，需前端先选候选
- 一作品仅保留一条正式记录
- 若记录已存在，正式导入执行覆盖更新并返回 `UPDATED`（不是跳过）

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

## 8. 收藏导入

接口：

- `POST /api/collections/import/preview`
- `POST /api/collections/import`

顶层字段：`collections`。

定位优先级与正式记录一致：

`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`

关键规则：

- `collectionType` 必填，取值 `WANT` / `PAUSED`
- 若作品已有正式记录，收藏导入会被跳过
- 若已有收藏记录，正式导入会更新类型和备注

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

## 9. 旧接口说明

`/api/subjects/import*` 仍可用于开发/脚本场景，但不是导入中心推荐路径。  
日常使用请优先走 `/api/import/bangumi/*`。
