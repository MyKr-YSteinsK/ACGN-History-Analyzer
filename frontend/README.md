# Frontend

React 19 + TypeScript + Vite + Tailwind CSS v4 前端。页面以作品库、记录、收藏、导入、系列、榜单和分析为核心。

## 目录

```text
src/
  routes/       路由和懒加载
  pages/        页面级状态与业务编排
  components/   通用 UI、作品卡、详情抽屉、分析面板
  api/          API DTO 与请求封装
  jobs/         后台任务轮询和全局 Toast
  hooks/        复用 Hook，包括 usePersistentState
  mappers/      后端响应到前端视图模型的转换
  query/        React Query keys 和缓存失效
  theme/        分类、档期、状态、评分视觉 token
  utils/        localStorage cache、报告格式化等工具
```

## UI 约定

- 桌面端使用左侧导航，移动端使用顶部网格导航。
- 页面主风格是浅色玻璃拟态，具体 token 在 `src/theme/acgnTheme.ts`。
- 全局背景、进入动画和卡片网格在 `src/index.css`。
- 后台任务完成通知由 `jobs/BackgroundJobsProvider.tsx` 统一渲染。

## 后台任务

- 通用任务走 `/api/jobs`，`registerBackgroundJobUi` 会注册 UI 信息并启动共享轮询。
- Bangumi 季度导入走 `/api/import/bangumi/season/import-jobs`，由 `jobs/bangumiSeasonJobMonitor.ts` 独立轮询。
- 路由切换不会中断任务等待；完成、失败或超时都会显示全局 Toast。

## 页面状态

`hooks/usePersistentState.ts` 使用 localStorage 保存筛选和输入。当前覆盖导入中心、作品库、记录库、收藏、系列、TOP、首页和分析页。

## 命令

```powershell
npm install
npm run dev
npm run lint
npm run build
```
