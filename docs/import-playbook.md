# 导入执行手册（Playbook）

这份手册强调“怎么最快、最稳地完成导入”，用于日常维护和历史补录。

## 1. 标准顺序

1. 导入作品库（Bangumi）
2. 导入收藏（WANT / PAUSED）
3. 导入正式记录（评分 / 备注）

## 2. 每一步怎么做

### 步骤 A：作品库

优先使用 Bangumi 季度导入：

- 先 `preview`
- 检查结果分层（可导入 / 更新 / 错误）
- 再确认写入

若有漏抓作品，再用 sourceId 补录导入：

```json
{
  "subjects": [
    { "sourceId": "400602", "releaseYear": 2020, "releaseQuarter": 4 }
  ]
}
```

### 步骤 B：收藏

使用 `collections` 顶层字段，`collectionType` 只能是 `WANT` / `PAUSED`。

### 步骤 C：正式记录

使用 `records` 顶层字段。  
如果同一作品已有记录，正式导入会执行覆盖更新并返回 `UPDATED`。

## 3. 预览阶段重点关注

- `AMBIGUOUS`：必须先人工选候选，再导入
- `ERROR`：直接看 message 修字段
- `UPDATED`：确认是你期望覆盖的数据

## 4. 常见问题速查

### 为什么重复作品没更新标题？

作品重复导入只更新三项：`tagNames`、`platformScore`、`episodeCount`。

### 为什么收藏被跳过？

该作品已有正式记录时，收藏会被规则性跳过。

### 为什么部分行报错但其余行成功？

导入按行处理，单行失败不阻断整批。

## 5. 快速排障清单

1. 先看 preview 的分层结果和逐行 message
2. 确认必填字段与类型
3. 检查 `sourceId` 是否有空格或非 Bangumi id
4. 先处理 `AMBIGUOUS` 候选选择，再执行 import

