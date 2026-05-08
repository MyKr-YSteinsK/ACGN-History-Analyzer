# ACGN History Analyzer Frontend

React 19 + TypeScript + Vite + Tailwind CSS v4 前端。页面以作品库、记录、收藏、导入、分析为核心，所有 API 调用都通过 `src/api/*` 进入后端。

## 目录

```text
src/
  api/          API DTO 与请求封装
  components/   通用 UI、作品卡、分析面板、弹窗和详情抽屉
  hooks/        复用 Hook，包括页面状态持久化
  jobs/         后台任务轮询和全局 Toast 事件
  mappers/      后端响应到前端卡片模型的转换
  pages/        页面级状态和业务编排
  query/        React Query keys 和缓存失效
  theme/        分类、档期、状态、评分视觉 token
  utils/        localStorage cache、LLM 报告格式化等工具
```

## 后台任务

- 通用任务走 `/api/jobs`，`registerBackgroundJobUi` 会立即启动共享轮询。
- Bangumi 季度导入走 `/api/import/bangumi/season/import-jobs`，由 `jobs/bangumiSeasonJobMonitor.ts` 独立轮询。
- 任务完成、失败或超时时都会通过 `jobs/backgroundJobEvents.ts` 发出全局 Toast，路由切换不会中断轮询。

## 页面状态记忆

`hooks/usePersistentState.ts` 使用 localStorage 保存页面筛选和输入。当前覆盖导入中心、作品库、记录库、收藏、系列、TOP、首页和分析页的主要选择项。

## 本地命令

```powershell
npm install
npm run dev
npm run lint
npm run build
```
