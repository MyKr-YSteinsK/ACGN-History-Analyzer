# 导入执行手册

这份手册面向日常维护和历史补录，强调怎么稳妥完成导入。

## 1. 标准流程

1. 用 Bangumi 导入作品库
2. 补录缺失作品的 `sourceId`
3. 导入收藏清单
4. 导入正式记录
5. 检查首页、作品库、记录页和 LLM 分析是否刷新

## 2. 作品库

优先使用 Bangumi 季度导入：

- 先预览
- 排除不想写入的条目
- 确认写入
- 等待右上角 Toast 或页面内任务状态提示

漏抓作品用 sourceId 补录：

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

重复作品只更新标签、平台评分和集数。需要改标题、档期、封面、系列时，请在作品详情里手动维护。

## 3. 收藏

收藏 JSON 顶层字段是 `collections`：

```json
{
  "collections": [
    {
      "sourceId": "400602",
      "collectionType": "WANT",
      "note": "之后补"
    }
  ]
}
```

注意：

- `collectionType` 只能是 `WANT` 或 `PAUSED`
- 已有正式记录的作品不会再进入收藏
- 已有收藏的作品会更新类型和备注

## 4. 正式记录

记录 JSON 顶层字段是 `records`：

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

注意：

- 一作品只保留一条正式记录
- 已有记录时导入会覆盖更新并返回 `UPDATED`
- 评分范围由后端 DTO 校验控制，错误行不会阻塞其他行

## 5. 处理预览结果

- `CAN_IMPORT`：可直接导入
- `UPDATED`：会覆盖已有数据，确认后再导入
- `AMBIGUOUS`：先选择候选或跳过该行
- `SKIPPED`：规则性跳过，通常无需重试
- `ERROR`：根据 message 修正字段

## 6. 排障清单

- 没有 Toast：确认前端构建包含 `BackgroundJobsProvider` 和 `backgroundJobEvents.ts`
- 导入一直 running：刷新导入页，看 URL 中的 `jobId` 是否能继续轮询
- 记录/收藏匹配不到作品：优先补 `sourceId`
- 标题匹配歧义：补 `subjectSeasonIndex`、`subjectPartIndex` 或直接选候选
- 收藏被跳过：检查该作品是否已有正式记录
- LLM 结果旧：确认数据写入接口已触发缓存清理
