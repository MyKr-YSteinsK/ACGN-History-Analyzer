# ACGN History Analyzer 数据导入规范

本文档用于批量整理真实 ACGN 历史数据。

当前系统的导入边界很明确：

```text
作品库：只允许 Bangumi 新番自动导入
收藏夹：允许 JSON / CSV 手动导入
正式记录：允许 JSON / CSV 手动导入
```

这样做是为了保护作品库。作品本体一旦被乱写，后面的记录、收藏、系列、标签和 AI 分析都会跟着变脏；所以作品库统一从 Bangumi 拉取，手动导入只负责“我的行为数据”。

## 推荐顺序

1. 清空旧测试数据，重启后端，让 JPA 自动建表。
2. 在导入中心使用 Bangumi 新番导入，按年份和一月 / 四月 / 七月 / 十月档期导入作品库。
3. 如需保留想看 / 搁置，导入收藏夹 `collections.json`。
4. 导入正式记录 `records.json`。

Bangumi 导入和正式记录导入后，后端会按作品主标题做一次轻量系列检测：同名多季、Part 或跨媒介作品会自动创建/复用系列并关联。这个检测不会把所有未记录作品都强行塞进系列，只围绕已有标题关系和多季 / Part 特征做收束。

推荐先调用 preview，再正式 import。

导入接口按行处理数据：某一行字段不合法会返回 `ERROR`，重复或无法匹配会返回 `SKIPPED`，但不会阻塞其他合法行继续预览或写入。

```text
POST /api/import/bangumi/season/preview
POST /api/import/bangumi/season/import

POST /api/collections/import/preview
POST /api/collections/import

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

## 作品库来源：Bangumi 自动导入

导入页的“Bangumi 新番”模式可以直接选择年份和月份档期。后端会调用 Bangumi 官方 `/v0/subjects` 浏览接口，按 `type=2`、`cat=1`、`year`、`month` 拉取 TV 动画，再转换成系统内部的作品写入请求。

可选项：

- 优先日本动画：使用 Bangumi 的“日本”标签做轻量过滤。
- 包含 WEB 动画：额外拉取 Bangumi `cat=5` 的 WEB 动画。

正式写入时仍然复用作品导入逻辑，所以重复判断、结构化标签、封面缓存都会按原规则执行。预览后可以排除不想写入的条目，再确认导入。

### 作品字段

| 字段 | 必填 | 格式 | 说明 |
| --- | --- | --- | --- |
| `titleCn` | 是 | 字符串，最长 255 | 作品主标题，参与唯一性判断 |
| `subtitle` | 否 | 字符串或 null，最长 255 | 副标题，参与唯一性判断 |
| `displayTitle` | 是 | 字符串，最长 500 | 页面展示标题 |
| `franchiseId` | 否 | 正整数或 null | 内部字段。前端不再提供手动系列导入 |
| `franchiseName` | 否 | 字符串或 null，最长 200 | 内部字段。Bangumi 导入后由自动检测决定是否建系列 |
| `seasonIndex` | 否 | 正整数或 null | 第几季，参与唯一性判断 |
| `partIndex` | 否 | 正整数或 null | 第几 Part，参与唯一性判断。Bangumi 导入时会尽量从标题中推断 |
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

作品库的标签会被控制在常见标签集合内。Bangumi 上奇怪的个人化标签、声优梗、短句吐槽、重复外文标签都会被忽略，避免标签系统失控。

当前保留的常见标签大致包括：

```text
原创、漫画改、小说改、游戏改、奇幻、战斗、冒险、科幻、悬疑、
校园、青春、恋爱、喜剧、日常、治愈、致郁、运动、音乐、偶像、
机战、异世界、转生、后宫、百合、乙女、女性向、历史、战争、
职场、美食、家庭、社团、短片、3D、少儿向、少年向、少女向
```

后端会自动：

1. 查找已有标签。
2. 没有则创建结构化标签。
3. 写入 `subject_tag` 关联表。

推荐每部作品最后保留 3-7 个标签。标签是后续 AI 分析个人偏好、兴趣聚类和推荐理由的重要输入，所以宁可少而准，不要多而杂。

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

## 一个作品一条记录

当前系统约定：一个作品只保留一条正式记录。

如果一部半年番跨档期播放，不再为了统计把它人为拆成两条记录。作品的发行档期来自 Bangumi 和作品库；个人记录只表示你在哪一年、哪个月份档期完成它，以及你的评分和备注。

## 收藏夹 JSON

顶层必须是对象，字段必须叫 `collections`。收藏夹只表示想看 / 搁置，不表示正式完成记录。

```json
{
  "collections": [
    {
      "subjectId": null,
      "subjectTitleCn": "葬送的芙莉莲",
      "subjectSubtitle": null,
      "subjectSeasonIndex": 1,
      "subjectPartIndex": 1,
      "subjectCategory": "ANIME",
      "collectionType": "WANT",
      "note": "之后想补完。"
    }
  ]
}
```

### 收藏字段

| 字段 | 必填 | 格式 | 说明 |
| --- | --- | --- | --- |
| `subjectId` | 否 | 正整数或 null | 如果知道数据库作品 id，可直接定位作品 |
| `subjectTitleCn` | 否 | 字符串 | `subjectId` 为空时，用于定位作品 |
| `subjectSubtitle` | 否 | 字符串或 null | 用于定位作品 |
| `subjectSeasonIndex` | 否 | 正整数或 null | 用于定位作品 |
| `subjectPartIndex` | 否 | 正整数或 null | 用于定位作品 |
| `subjectCategory` | 否 | `ANIME`/`COMIC`/`GAME`/`NOVEL` | `subjectId` 为空时必填 |
| `collectionType` | 是 | `WANT`/`PAUSED` | WANT 想看，PAUSED 搁置 |
| `note` | 否 | 字符串或 null，最长 500 | 收藏备注 |

如果作品已有正式记录，收藏导入会跳过。  
如果作品已经在收藏夹中，正式导入会更新为新的 `collectionType` 和 `note`。

## 给 GPT / Bangumi 整理器的提示词

```text
请根据 Bangumi 信息生成 ACGN History Analyzer 导入 JSON。

先通过系统导入中心的 Bangumi 新番导入作品库。

然后根据我提供的旧数据，生成两个 JSON 文件：
1. collections.json（如需要想看/搁置）
2. records.json

作品 category 只能是 ANIME / COMIC / GAME / NOVEL。
月份档期规则：1 一月，2 四月，3 七月，4 十月。
作品唯一键是 titleCn + subtitle + seasonIndex + partIndex + category。
记录 JSON 必须用 subjectTitleCn、subjectSubtitle、subjectSeasonIndex、subjectPartIndex、subjectCategory 匹配作品。
收藏 JSON 使用 collections 顶层字段，collectionType 只能是 WANT 或 PAUSED；已有正式记录的作品不要再生成收藏。
不要生成 subjects.json，不要手动写入作品库。
```
