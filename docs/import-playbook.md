# 导入实战手册（Playbook）

面向“日常导入 + 历史补录”的操作手册。  
如果你只是想稳定把数据导进系统，按这份执行即可。

---

## 1. 推荐顺序（强烈建议）

1. 先导作品库（Bangumi）
2. 再导收藏（WANT/PAUSED）
3. 最后导正式记录（评分/备注）

原因：记录和收藏都依赖已存在的作品主数据。

---

## 2. 作品导入两种方式

### A. Bangumi 新番季度导入（主流程）

- 适用：批量导入某年某档期作品
- 优点：自动补齐字段、封面缓存、结构化标签清洗
- 建议：先 preview 再 import

### B. sourceId JSON 补录导入（补漏用）

- 适用：自动导入没抓到的个别作品
- 每行只填：
  - `sourceId`
  - `releaseYear`
  - `releaseQuarter`
- 后端会按 `sourceId` 去 Bangumi 拉完整作品信息

示例：

```json
{
  "subjects": [
    { "sourceId": "400602", "releaseYear": 2020, "releaseQuarter": 4 }
  ]
}
```

---

## 3. 记录/收藏定位规则

导入时匹配顺序：

1. `subjectId`（最准）
2. `sourceId`
3. `subjectTitle` + 可选 `subjectSeasonIndex/subjectPartIndex`

如果匹配到多个候选，预览会返回 `AMBIGUOUS`，需要前端先选定。

---

## 4. 常见问题

### Q1：为什么重复导入作品没有把标题改掉？

因为系统规则是：重复命中同 `sourceId` 时只同步三项：

- `tagNames`
- `platformScore`
- `episodeCount`

其它字段保持库中原值。

### Q2：为什么收藏导入被跳过？

如果该作品已有正式记录，收藏会被跳过，这是业务规则。

### Q3：为什么有些行显示 ERROR，但其它行仍导入成功？

导入是逐行处理，单行错误不阻断整批。

---

## 5. 失败排查建议

1. 先看 preview 返回中的逐行 message
2. 校验必填字段与类型
3. sourceId 问题优先检查空格、大小写、误填非 Bangumi id
4. 对歧义项先在前端做候选选择再 import

