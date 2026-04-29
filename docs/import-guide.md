# ACGN History Analyzer 数据导入规范

本文档用于批量整理真实 ACGN 历史数据。推荐让 GPT 或 Bangumi 数据整理脚本按本文格式生成 JSON，再通过前端“导入中心”或后端 import 接口导入。

## 推荐顺序

1. 清空旧测试数据，重启后端，让 JPA 自动建表。
2. 如果需要系列，先手动创建少量 `franchise`，或在作品 JSON 中填写可匹配的 `franchiseName`。
3. 导入作品库 `subjects.json`。
4. 导入正式记录 `records.json`。
5. 想看 / 搁置收藏夹后续单独处理。

推荐先调用 preview，再正式 import。

```text
POST /api/subjects/import/preview
POST /api/subjects/import

POST /api/records/import/preview
POST /api/records/import
```

## 月份档期口径

本项目按 1/4/7/10 月档期记录作品和个人记录：

```text
1 = 一月
2 = 四月
3 = 七月
4 = 十月
```

页面展示顺序是：一月、四月、七月、十月。

## 作品库 JSON

顶层必须是对象，字段必须叫 `subjects`。

```json
{
  "subjects": [
    {
      "titleCn": "葬送的芙莉莲",
      "subtitle": null,
      "displayTitle": "葬送的芙莉莲",
      "franchiseName": "葬送的芙莉莲",
      "seasonIndex": 1,
      "partIndex": 1,
      "coverUrl": "https://lain.bgm.tv/pic/cover/l/example.jpg",
      "summary": "勇者一行打倒魔王后，精灵魔法使芙莉莲重新理解人与时间的故事。",
      "releaseYear": 2023,
      "releaseQuarter": 4,
      "category": "ANIME",
      "studioName": "MADHOUSE",
      "platformLink": "https://bgm.tv/subject/400602",
      "platformScore": 8.8,
      "episodeCount": 14,
      "tagNames": ["奇幻", "冒险", "治愈", "成长", "漫画改"],
      "sourcePlatform": "BANGUMI",
      "sourceId": "400602",
      "status": "NORMAL"
    }
  ]
}
```

### 作品字段

| 字段 | 必填 | 格式 | 说明 |
| --- | --- | --- | --- |
| `titleCn` | 是 | 字符串，最长 255 | 作品主标题，参与唯一性判断 |
| `subtitle` | 否 | 字符串或 null，最长 255 | 副标题，参与唯一性判断 |
| `displayTitle` | 是 | 字符串，最长 500 | 页面展示标题 |
| `franchiseId` | 否 | 正整数或 null | 已有系列 id。与 `franchiseName` 同时存在时优先使用 |
| `franchiseName` | 否 | 字符串或 null，最长 200 | 用系列名称匹配已有系列 |
| `seasonIndex` | 否 | 正整数或 null | 第几季，参与唯一性判断 |
| `partIndex` | 否 | 正整数或 null | 第几 Part，参与唯一性判断。半年番按月份档期切割时可用 P1/P2 |
| `coverUrl` | 否 | URL 或 `/api/assets/covers/...` | 远程 URL 会在正式导入时尝试缓存到本地 |
| `summary` | 否 | 字符串或 null，最长 2000 | 简介 |
| `releaseYear` | 是 | 1900-2100 | 发行年份 |
| `releaseQuarter` | 是 | 1/2/3/4 | 1 一月，2 四月，3 七月，4 十月 |
| `category` | 是 | `ANIME`/`COMIC`/`GAME`/`NOVEL` | ACGN 类型 |
| `studioName` | 否 | 字符串或 null，最长 255 | 制作方、出版社、开发商等 |
| `platformLink` | 否 | 字符串或 null，最长 500 | Bangumi 等外部链接 |
| `platformScore` | 否 | 0.00-10.00 或 null | 平台评分 |
| `episodeCount` | 否 | >=0 或 null | 集数、话数、章节数 |
| `tagNames` | 否 | 字符串数组，最多 12 个 | 结构化标签，推荐 3-7 个 |
| `sourcePlatform` | 否 | 字符串或 null，最长 64 | 推荐 `BANGUMI` |
| `sourceId` | 否 | 字符串或 null，最长 128 | 推荐 Bangumi subject id |
| `status` | 是 | `NORMAL`/`HIDDEN` | 通常写 `NORMAL` |

### 作品唯一性

作品重复判断使用：

```text
titleCn + subtitle + seasonIndex + partIndex + category
```

这些字段全部相同，就视为同一个作品。`displayTitle`、`releaseYear`、`releaseQuarter` 不参与当前唯一性判断。

### 标签

只使用 `tagNames`，不要再生成旧的 `tags` 字符串。

推荐标签数量：

```text
3-7 个，最多 12 个
```

后端会自动：

1. 查找已有标签。
2. 没有则创建 `CUSTOM` 标签。
3. 写入 `subject_tag` 关联表。

### 封面

可以直接填 Bangumi 图片 URL：

```json
"coverUrl": "https://lain.bgm.tv/pic/cover/l/example.jpg",
"sourcePlatform": "BANGUMI",
"sourceId": "400602"
```

正式导入时后端会尝试下载到：

```text
backend/uploads/covers/
```

并把数据库中的 `cover_url` 改成本地访问路径：

```text
/api/assets/covers/bangumi-400602.jpg
```

如果你手动放图片，文件放到：

```text
backend/uploads/covers/
```

JSON 写：

```json
"coverUrl": "/api/assets/covers/your-file-name.jpg"
```

## 正式记录 JSON

顶层必须是对象，字段必须叫 `records`。

```json
{
  "records": [
    {
      "subjectTitleCn": "葬送的芙莉莲",
      "subjectSubtitle": null,
      "subjectSeasonIndex": 1,
      "subjectPartIndex": 1,
      "subjectCategory": "ANIME",
      "scoreValue": 48,
      "recordYear": 2024,
      "recordQuarter": 1,
      "note": "节奏舒缓但后劲很强，人物情绪非常细。"
    }
  ]
}
```

### 记录字段

| 字段 | 必填 | 格式 | 说明 |
| --- | --- | --- | --- |
| `subjectTitleCn` | 是 | 字符串 | 用于定位作品 |
| `subjectSubtitle` | 否 | 字符串或 null | 用于定位作品 |
| `subjectSeasonIndex` | 否 | 正整数或 null | 用于定位作品 |
| `subjectPartIndex` | 否 | 正整数或 null | 用于定位作品 |
| `subjectCategory` | 是 | `ANIME`/`COMIC`/`GAME`/`NOVEL` | 用于定位作品 |
| `scoreValue` | 否 | 0-50 或 null | 个人评分 |
| `recordYear` | 是 | 1900-2100 | 记录年份，不一定等于发行年份 |
| `recordQuarter` | 是 | 1/2/3/4 | 记录月份档期：1 一月，2 四月，3 七月，4 十月 |
| `note` | 否 | 字符串或 null，最长 500 | 评价/备注 |

记录导入使用作品唯一键匹配作品：

```text
subjectTitleCn + subjectSubtitle + subjectSeasonIndex + subjectPartIndex + subjectCategory
```

如果找不到作品，记录会跳过。一个作品只能有一条正式记录。

## 半年番和跨档期作品

如果你习惯按月份档期统计，可以人为切成 P1/P2：

```json
{
  "titleCn": "某半年番",
  "subtitle": null,
  "displayTitle": "某半年番 Part 1",
  "seasonIndex": 1,
  "partIndex": 1,
  "releaseYear": 2025,
  "releaseQuarter": 2,
  "category": "ANIME"
}
```

```json
{
  "titleCn": "某半年番",
  "subtitle": null,
  "displayTitle": "某半年番 Part 2",
  "seasonIndex": 1,
  "partIndex": 2,
  "releaseYear": 2025,
  "releaseQuarter": 3,
  "category": "ANIME"
}
```

这样它们在统计中是两个作品切片，但可以通过同一个 `franchiseName` 串回同一系列。

## 给 GPT / Bangumi 整理器的提示词

```text
请根据 Bangumi 信息生成 ACGN History Analyzer 导入 JSON。

输出两个 JSON 文件：
1. subjects.json
2. records.json

作品 category 只能是 ANIME / COMIC / GAME / NOVEL。
月份档期规则：1 一月，2 四月，3 七月，4 十月。
作品唯一键是 titleCn + subtitle + seasonIndex + partIndex + category。
不要生成 tags 字符串，只生成 tagNames 数组。
tagNames 使用中文标签，推荐 3-7 个，最多 12 个。
coverUrl 填 Bangumi 封面 URL。
sourcePlatform 固定为 BANGUMI。
sourceId 填 Bangumi subject id。
status 固定为 NORMAL。
记录 JSON 必须用 subjectTitleCn、subjectSubtitle、subjectSeasonIndex、subjectPartIndex、subjectCategory 匹配作品。
```
