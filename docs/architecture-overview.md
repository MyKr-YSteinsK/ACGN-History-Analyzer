# ACGN History Analyzer 架构速查

这是一份面向开发者的快速架构索引，帮助你在不通读全仓库的前提下快速定位模块职责。

---

## 1. 分层总览

```text
Frontend (React + TS)
  pages / components / api / mappers / theme
        |
        v
Backend (Spring Boot)
  controller -> service -> repository -> entity
        |
        +-> llm/*
        +-> import/bangumi*
```

---

## 2. 业务模块映射

- `subjects`: 作品库主数据与导入
- `records`: 正式记录与统计
- `collections`: 想看/搁置
- `franchises`: 系列管理与自动关联
- `tags`: 标签体系与白名单清理
- `import`: 导入总览、模板与 Bangumi 流程
- `llm`: AI 分析与问答
- `home/top/options`: 首页、榜单、枚举选项

---

## 3. 核心规则

- 作品唯一键：`sourceId`
- 一作品一正式记录
- 一作品最多一个收藏状态
- 重复作品导入只更新：标签、平台评分、集数
- 导入定位优先级：`subjectId > sourceId > 标题关键词(+季/Part)`

---

## 4. 导入链路

### 作品导入

1. Bangumi 季度导入（异步任务）
2. sourceId JSON 补录导入（按 sourceId 拉单作品）

### 记录/收藏导入

1. preview 返回结果与候选
2. 前端处理歧义项选择
3. import 正式写入

---

## 5. LLM 链路（简）

```text
Controller -> AnalysisService
          -> ContextBuilder + PromptService
          -> RuntimeSwitchingClient (mock/real)
```

详细见：`docs/llm-guide.md`

