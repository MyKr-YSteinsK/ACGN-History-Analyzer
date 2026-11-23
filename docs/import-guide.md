# 导入与导出指南

本文覆盖作品库、收藏、正式记录导入与 Bangumi 往返 JSON。日常导入优先走前端导入页，脚本或联调可直接调用 API。写入验证使用独立测试库。

## 推荐顺序

1. 导入作品库。
2. 补录缺失作品的 Bangumi `sourceId`。
3. 导入收藏。
4. 导入正式记录。
5. 检查首页、作品库、记录页和 LLM 分析缓存是否刷新。

## 通用流程

```text
preview -> 排除行/选择歧义候选 -> import -> 后台任务 Toast -> 刷新相关缓存
```

结果码：

- `CAN_IMPORT`：预览可写入。
- `IMPORTED`：正式写入成功。
- `UPDATED`：命中已有数据并更新。
- `AMBIGUOUS`：多候选，需要前端选择或跳过。
- `SKIPPED`：规则性跳过。
- `ERROR`：字段或业务校验失败。

档期值：

- `1`：一月
- `2`：四月
- `3`：七月
- `4`：十月

## 作品库导入

### Bangumi 季度导入

接口：

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

季度导入会拉取 Bangumi 数据、缓存封面、同步标签、检查重复并写入作品库。它使用独立任务状态：`PENDING`、`RUNNING`、`SUCCESS`、`FAILED`。

### Bangumi sourceId 补录

前端当前通过通用 `/api/jobs` 异步执行，后端也保留同步接口：

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

重复作品只更新 `tagNames`、`platformScore`、`episodeCount`。标题、档期、封面、系列等字段需要在作品详情中手动维护。

## 收藏导入

接口：

- `POST /api/collections/import/preview`
- `POST /api/collections/import`

顶层字段为 `collections`：

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

规则：

- `collectionType` 只能是 `WANT` 或 `PAUSED`。
- 已有正式记录的作品会跳过收藏导入。
- 已有收藏的作品会更新清单类型和备注。
- 定位优先级：`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`。

## 正式记录导入

接口：

- `POST /api/records/import/preview`
- `POST /api/records/import`

顶层字段为 `records`：

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

规则：

- 一作品只保留一条正式记录。
- 已有记录时导入会覆盖更新并返回 `UPDATED`。
- 字段校验失败、作品缺失和匹配歧义逐行返回结果。意外写入异常会回滚本批。
- 正式记录写入和对应收藏移除在同一事务中完成。旧数据中的重叠状态可随对应记录重新导入修复。
- 定位优先级：`subjectId > sourceId > subjectTitle(+subjectSeasonIndex/subjectPartIndex)`。

记录及收藏导入在提供高优先级标识时直接按该标识匹配。缺失的高优先级标识不会回退去匹配标题。

## Bangumi 往返导出

导入页的往返导出卡片调用 GET /api/export/bangumi-roundtrip，返回 records JSON 文本和可选的 subjects JSON 文本。恢复时先导入 Bangumi 作品，再导入正式记录。记录中的 Bangumi 作品用 sourceId 定位，其他作品用原库 subjectId。跨库前需核对或映射这些本地 ID。收藏、完整作品字段、封面、任务和分析历史需要通过数据库及文件备份保存。

## 后台提示与页面记忆

- 通用导入任务走 `/api/jobs`，完成、失败或取消后由全局 Toast 提示。
- Bangumi 季度导入由 `bangumiSeasonJobMonitor` 独立轮询，切换页面不会中断。
- 导入页会记住导入目标、模式、草稿文本、Bangumi 年份、季度、Web/日语筛选。
- 作品库、记录库、收藏、系列、TOP、首页和分析页也会记住主要筛选。

## 排障

- 没有 Toast：检查 `BackgroundJobsProvider` 是否包裹了路由，任务是否注册了 UI 信息。
- 导入长时间运行：先确认现有任务的状态。通用任务保存于数据库，季度任务只有在原后端进程仍运行时才能按旧 ID 恢复轮询。
- 记录/收藏匹配不到作品：优先补 `sourceId`。
- 标题匹配歧义：补 `subjectSeasonIndex`、`subjectPartIndex`，或在预览页选择候选。
- 收藏被跳过：检查该作品是否已有正式记录。
- LLM 结果旧：确认数据写入后触发了缓存清理，必要时清理分析页本地缓存。
