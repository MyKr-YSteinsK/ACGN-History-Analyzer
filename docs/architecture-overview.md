# ACGN History Analyzer 架构速查

这是一份给开发者的“最短上手索引”，用于快速确认模块职责与关键约束。

## 1. 分层结构

```text
Frontend (React + TS)
  pages / components / api / mappers / hooks / utils
        |
        v
Backend (Spring Boot)
  controller -> service -> repository -> entity
        |
        +-> llm/*
        +-> import/*
```

## 2. 模块映射

- `subjects`：作品库查询、详情、分页、导入逻辑
- `records`：正式记录 CRUD、导入、统计
- `collections`：收藏状态管理与导入
- `franchises`：系列管理与自动关联
- `tags`：标签列表和白名单清理
- `import`：导入总览、模板、Bangumi 导入入口
- `llm`：分析、问答、配置、降级
- `home/top/options`：首页、榜单、枚举选项

## 3. 当前核心约束

- 作品唯一键：`sourceId`
- 一作品只保留一条正式记录
- 一作品最多一个收藏状态
- 作品重复导入仅更新：`tagNames/platformScore/episodeCount`
- 记录/收藏导入定位优先级：`subjectId > sourceId > subjectTitle(+season/part)`
- 记录导入重复命中执行覆盖更新（`UPDATED`）

## 4. 关键链路

### 导入链路

1. 前端先调 preview
2. 若有 `AMBIGUOUS`，前端选择候选
3. 再执行 import
4. 返回分层结果（可导入/更新/歧义/跳过/错误）

### LLM 链路

```text
Controller
  -> AnalysisService
      -> ContextBuilder
      -> PromptService
      -> RuntimeSwitchingClient (mock/real)
```

详细说明见 `docs/llm-guide.md`。

