# 数据库表结构

本文按当前 JPA 实体和 application.properties 整理。MySQL 表结构主要由 Hibernate 的 `ddl-auto=update` 根据实体维护，既有实例的实际索引和约束须查询数据库确认。

## 说明

- Java `Long` 主键在 MySQL 下按 `BIGINT` 自增理解。
- Java `Integer` 按 `INT` 理解。
- Java `String` 且声明 `length = n` 按 `VARCHAR(n)` 理解。
- `LocalDateTime` / `Instant` 在 MySQL 下通常映射为 `DATETIME(6)` 或等价时间类型，具体取决于 Hibernate MySQL dialect。
- 未写 `nullable = false` 的字段默认为可空。
- `@CreationTimestamp` / `@UpdateTimestamp` 是 Hibernate 写入时自动填充，不等于数据库层 `DEFAULT CURRENT_TIMESTAMP`。
- `subject.franchise_id`、`user_subject_record.subject_id`、`user_subject_collection.subject_id` 是业务关联字段，当前实体没有声明 `@ManyToOne` 外键。
- `subject_tag` 来自 `Subject.structuredTags` 的 `@ManyToMany`，是明确的 JPA 关联表。

## 表清单

| 表名 | 来源 | 用途 |
|---|---|---|
| `subject` | `Subject` | 作品主数据 |
| `tag` | `Tag` | 结构化标签 |
| `subject_tag` | `Subject.structuredTags` | 作品与标签多对多关联 |
| `user_subject_record` | `UserSubjectRecord` | 正式观看/游玩/阅读记录 |
| `user_subject_collection` | `UserSubjectCollection` | 想看、搁置收藏 |
| `franchise` | `Franchise` | 系列信息 |
| `background_job` | `BackgroundJob` | 通用后台任务 |
| `llm_analysis_result_cache` | `LlmAnalysisResultCacheEntity` | LLM 结果缓存 |

## `subject`

作品库主表。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto increment, not null | 主键 |
| `title_cn` | `String` | `VARCHAR(255)` | not null | 中文/主标题 |
| `subtitle` | `String` | `VARCHAR(255)` | nullable | 副标题 |
| `display_title` | `String` | `VARCHAR(500)` | not null | 展示标题 |
| `season_index` | `Integer` | `INT` | nullable | 季序号 |
| `part_index` | `Integer` | `INT` | nullable | 分割放送/部分序号 |
| `franchise_id` | `Long` | `BIGINT` | nullable | 系列 ID，业务关联 `franchise.id`，未声明数据库外键 |
| `cover_url` | `String` | `VARCHAR(500)` | nullable | 封面 URL 或本地缓存地址 |
| `summary` | `String` | `VARCHAR(2000)` | nullable | 简介 |
| `release_year` | `Integer` | `INT` | not null | 首播/发行年份 |
| `release_quarter` | `Integer` | `INT` | not null | 档期：`1` 一月、`2` 四月、`3` 七月、`4` 十月 |
| `category` | `String` | `VARCHAR(32)` | not null | 分类，业务值：`ANIME`、`COMIC`、`GAME`、`NOVEL` |
| `studio_name` | `String` | `VARCHAR(255)` | nullable | 制作公司/厂牌 |
| `platform_link` | `String` | `VARCHAR(500)` | nullable | 来源平台链接 |
| `platform_score` | `BigDecimal` | `DECIMAL(4,2)` | nullable | 平台评分 |
| `episode_count` | `Integer` | `INT` | nullable | 集数/章节数 |
| `source_platform` | `String` | `VARCHAR(64)` | nullable | 来源平台，当前主要为 Bangumi |
| `source_id` | `String` | `VARCHAR(128)` | nullable | 来源 ID，当前主要为 Bangumi subject id |
| `status` | `String` | `VARCHAR(32)` | not null | 作品状态，当前业务主要使用 `NORMAL` |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | nullable, insert auto-filled, updatable=false | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | nullable, update auto-filled | 更新时间 |

约束与索引：

- 主键：`id`
- 代码层唯一规则：`source_id` 归一化后应唯一；当前不是数据库唯一约束，由 `SubjectService` 检查。
- 手工索引：`idx_subject_source (source_platform, source_id(64))`
- 手工索引：`idx_subject_franchise_id (franchise_id)`

## `tag`

结构化标签表。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto increment, not null | 主键 |
| `name` | `String` | `VARCHAR(64)` | not null | 标签展示名 |
| `normalized_name` | `String` | `VARCHAR(64)` | not null, unique | 归一化标签名 |
| `tag_type` | `String` | `VARCHAR(32)` | not null | 标签类型，如 `GENRE`、`ELEMENT`、`MOOD`、`STYLE`、`CUSTOM` |
| `color_key` | `String` | `VARCHAR(32)` | not null | 前端颜色 key |
| `builtin` | `Boolean` | `BOOLEAN` | not null | 是否内置标签 |
| `status` | `String` | `VARCHAR(32)` | not null | 标签状态，当前业务主要使用 `NORMAL` |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | nullable, insert auto-filled, updatable=false | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | nullable, update auto-filled | 更新时间 |

约束与索引：

- 主键：`id`
- 唯一约束：`uk_tag_normalized_name (normalized_name)`

## `subject_tag`

作品与标签的多对多中间表，由 `Subject.structuredTags` 生成。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `subject_id` | `Long` | `BIGINT` | JPA join column | 关联 `subject.id` |
| `tag_id` | `Long` | `BIGINT` | JPA inverse join column | 关联 `tag.id` |

约束与索引：

- 唯一约束：`uk_subject_tag (subject_id, tag_id)`
- JPA 多对多关系会为 `subject_id`、`tag_id` 建立关联约束；实际外键名由 Hibernate 生成。
- 代码没有显式声明主键字段；唯一约束承担去重作用。

## `user_subject_record`

正式记录表，一部作品最多一条正式记录。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto increment, not null | 主键 |
| `subject_id` | `Long` | `BIGINT` | not null, unique | 作品 ID，业务关联 `subject.id`，未声明数据库外键 |
| `subject_title` | `String` | `VARCHAR(255)` | not null | 记录时冗余保存的作品标题 |
| `score_value` | `Integer` | `INT` | nullable | 个人评分，写入接口接受 0 至 50 |
| `record_year` | `Integer` | `INT` | not null | 记录年份 |
| `record_quarter` | `Integer` | `INT` | not null | 记录档期：`1` 一月、`2` 四月、`3` 七月、`4` 十月 |
| `note` | `String` | `VARCHAR(500)` | nullable | 备注 |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | nullable, insert auto-filled, updatable=false | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | nullable, update auto-filled | 更新时间 |

约束与索引：

- 主键：`id`
- 唯一约束：`uk_user_subject_record_subject_id (subject_id)`
- 手工索引：`idx_user_subject_record_subject_id (subject_id)`；注意该索引与唯一约束在功能上有重叠。
- 手工索引：`idx_user_subject_record_year_quarter (record_year, record_quarter)`

## `user_subject_collection`

收藏表，保存想看和搁置；已有正式记录的作品按业务规则不再保留收藏。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto increment, not null | 主键 |
| `subject_id` | `Long` | `BIGINT` | not null, unique | 作品 ID，业务关联 `subject.id`，未声明数据库外键 |
| `subject_title` | `String` | `VARCHAR(255)` | not null | 收藏时冗余保存的作品标题 |
| `collection_type` | `CollectionType` | `VARCHAR(32)` | not null, enum string | 收藏类型：`WANT`、`PAUSED` |
| `note` | `String` | `VARCHAR(500)` | nullable | 备注 |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | nullable, insert auto-filled, updatable=false | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | nullable, update auto-filled | 更新时间 |

约束与索引：

- 主键：`id`
- 唯一约束：`uk_user_subject_collection_subject_id (subject_id)`

## `franchise`

系列表。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` | PK, auto increment, not null | 主键 |
| `name` | `String` | `VARCHAR(200)` | not null | 系列原名/主名称 |
| `name_cn` | `String` | `VARCHAR(200)` | nullable | 系列中文名 |
| `description` | `String` | `VARCHAR(1000)` | nullable | 描述 |
| `status` | `String` | `VARCHAR(32)` | not null | 系列状态，当前业务主要使用 `NORMAL` |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | not null, pre-persist auto-filled | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | not null, pre-update auto-filled | 更新时间 |

约束与索引：

- 主键：`id`
- 代码层唯一规则：`name`、`name_cn` 会按 `lower(trim(...))` 做重复检查；当前不是数据库唯一约束。

## `background_job`

通用后台任务表。字段未显式写 `name` 的列由 Spring Boot / Hibernate 默认物理命名策略从 camelCase 推导为 snake_case。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | `String` | `VARCHAR(40)` | PK, not null | 任务 ID |
| `job_type` | `BackgroundJobType` | `VARCHAR(48)` | not null, enum string | 任务类型 |
| `status` | `BackgroundJobStatus` | `VARCHAR(24)` | not null, enum string, app default `PENDING` | 任务状态 |
| `payload_json` | `String` | `LONGTEXT` | nullable | 任务输入 JSON |
| `result_json` | `String` | `LONGTEXT` | nullable | 任务结果 JSON |
| `progress` | `Integer` | `INT` | not null, app default `0` | 进度百分比 |
| `message` | `String` | `VARCHAR(2000)` | nullable | 进度/状态消息 |
| `error_message` | `String` | `VARCHAR(4000)` | nullable | 失败信息 |
| `created_at` | `LocalDateTime` | `DATETIME(6)` | not null, pre-persist auto-filled | 创建时间 |
| `updated_at` | `LocalDateTime` | `DATETIME(6)` | not null, pre-update auto-filled | 更新时间 |

枚举值：

- `job_type`：`BANGUMI_SUBJECT_PREVIEW`、`BANGUMI_SUBJECT_IMPORT`、`COLLECTION_IMPORT_PREVIEW`、`COLLECTION_IMPORT`、`RECORD_IMPORT_PREVIEW`、`RECORD_IMPORT`、`LLM_ANALYSIS`
- `status`：`PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELLED`

约束与索引：

- 主键：`id`
- 手工索引：`idx_background_job_status_created (status, created_at)`

## `llm_analysis_result_cache`

LLM 结果缓存表。字段未显式写 `name` 的列由默认命名策略推导为 snake_case。

| 字段 | Java 类型 | MySQL 类型 | 约束 | 说明 |
|---|---|---|---|---|
| `cache_key` | `String` | `VARCHAR(128)` | PK, not null | 缓存 key |
| `result_kind` | `LlmResultKind` | `VARCHAR(32)` | not null, enum string | 缓存结果类型 |
| `response_json` | `String` | `LONGTEXT` | not null | LLM 响应 JSON |
| `expires_at` | `Instant` | `DATETIME(6)` | not null | 过期时间 |

枚举值：

- `result_kind`：`ANALYSIS`、`QUESTION_ANSWER`

约束与索引：

- 主键：`cache_key`

## 历史手工索引

旧工作区索引脚本已移入本地 docs/delete。候选涉及记录的 subject_id 与 (record_year, record_quarter)、作品的 (source_platform, source_id 前 64 字符) 与 franchise_id，以及通用任务的 (status, created_at)。旧脚本未纳入版本化迁移。重新采用时按实际 SHOW INDEX、查询形态及 EXPLAIN 决定，记录表已有 subject_id 唯一索引声明。

## 关系概览

```text
subject 1 --- 0..1 user_subject_record      业务关联：user_subject_record.subject_id
subject 1 --- 0..1 user_subject_collection  业务关联：user_subject_collection.subject_id
franchise 1 --- n subject                业务关联：subject.franchise_id
subject n --- n tag                      JPA 多对多：subject_tag(subject_id, tag_id)
```

当前只有 `subject_tag` 通过 JPA 多对多显式建模为关联表；其他 `*_id` 字段在实体中以普通 ID 保存，依赖服务层校验和查询维护一致性。
