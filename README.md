# ACGN History Analyzer

面向 ACGN 的大语言模型个人历史数据智能分析系统开发。

它现在还不是一个“AI 会替你评价作品”的系统。这个阶段我先把地基打稳：作品库、正式记录、收藏夹、系列、导入、统计、榜单。因为对个人历史数据来说，先把数据放对位置，比急着接一个模型更重要。

ACGN 在这里指：

```text
A = Anime   动画
C = Comic   漫画
G = Game    游戏
N = Novel   小说
```

这个项目想解决的不是“我看过哪些动画”这么简单的问题，而是：几年之后回头看，自己到底喜欢过什么、在哪些时间段集中补过什么、哪些系列一路追了下来、哪些作品只是暂时放进了想看。

## 现在能做什么

- 作品库：管理动画、漫画、游戏、小说这些作品本体。
- 我的记录：只保存正式看过、读过、玩过的记录。
- 收藏夹：单独保存想看和搁置，不和正式记录混在一起。
- 系列页：把多季、Part、外传、跨媒介作品串起来。
- 首页统计：按年份、月份档期、类型做可视化统计。
- TOP 榜单：按个人评分展示高分作品和分数段统计。
- 导入中心：支持作品、收藏、记录的 JSON / CSV 预览和导入。
- 标签筛选：作品可以用 `#奇幻`、`#战斗`、`#校园` 这类标签细分。
- 本地封面缓存：导入远程封面时会尽量缓存到本地，页面不再完全依赖外链。

## 当前边界

当前阶段不接入 LLM。

项目里会保留一点为后续智能分析准备的数据结构，比如标签、来源平台、来源 ID、系列关系。但真正的 LLM 分析报告、个人偏好总结、推荐理由生成，会放到下一阶段。

这样做是有意的。一个个人历史分析系统如果底层数据不干净，后面接再聪明的模型，也只会把混乱包装得更漂亮。

## 技术栈

后端：

```text
Java 17
Spring Boot
Spring MVC
Spring Data JPA
MySQL
RESTful API
```

前端：

```text
React
TypeScript
Vite
Tailwind CSS
React Router
Axios
```

开发工具建议：

```text
IntelliJ IDEA
Navicat 17
Node.js
Maven
```

## 项目结构

```text
ACGN-History-Analyzer
├─ backend                 Spring Boot 后端
│  ├─ src/main/java
│  │  └─ com/mykr/acgnhistoryanalyzer
│  │     ├─ controller     接口入口
│  │     ├─ service        业务逻辑
│  │     ├─ entity         JPA 实体
│  │     ├─ repository     数据访问
│  │     ├─ request        请求 DTO
│  │     ├─ response       响应 DTO
│  │     └─ specification  动态查询条件
│  └─ src/main/resources
│     └─ application.properties
├─ frontend                React 前端
│  └─ src
│     ├─ api              接口封装
│     ├─ components       通用组件和业务组件
│     ├─ pages            页面
│     ├─ routes           路由
│     ├─ theme            主题色和视觉规则
│     ├─ types            TypeScript 类型
│     └─ mappers          后端数据到前端视图模型的转换
├─ docs
│  └─ import-guide.md     导入数据规范
├─ sql
└─ uploads                本地封面缓存，不提交 git
```

## 运行前准备

### 1. 创建数据库

MySQL 中创建数据库：

```sql
CREATE DATABASE acgn_history_analyzer
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

当前开发配置在：

```text
backend/src/main/resources/application.properties
```

默认配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/acgn_history_analyzer
spring.datasource.username=root
spring.datasource.password=abc123
spring.jpa.hibernate.ddl-auto=update
```

如果你的 MySQL 用户名或密码不同，改这里即可。

### 2. 启动后端

推荐用 IntelliJ IDEA：

1. 打开 `backend` 项目。
2. 等 Maven 依赖加载完成。
3. 运行 Spring Boot 启动类。
4. 后端默认监听：

```text
http://localhost:8080
```

也可以用本机 Maven：

```powershell
cd backend
mvn spring-boot:run
```

仓库里有 `mvnw.cmd`，但当前没有完整提交 `.mvn/wrapper` 配置。最稳的是直接用本机 Maven 或 IntelliJ。

### 3. 启动前端

```powershell
cd frontend
npm install
npm.cmd run dev
```

如果 PowerShell 提示 `npm.ps1 cannot be loaded`，用 `npm.cmd`，不要用裸的 `npm`。

前端默认地址通常是：

```text
http://localhost:5173
```

前端通过 Vite 代理访问后端 `/api`。如果前端页面报：

```text
Request failed with status code 502
```

先检查后端是否已经启动。这个项目的前端不是纯静态玩具页，后端没起来，作品库、记录、收藏夹这些页面都会拿不到数据。

## 页面说明

### 首页

首页是统计门面，不是普通欢迎页。

它会展示：

- 年份类型总览
- 类型月份分布
- 评分前三
- 月份类型构成
- 相对往年变化

当前项目统一使用 1 / 4 / 7 / 10 月档期，不再使用春夏秋冬。

```text
1 = 一月
2 = 四月
3 = 七月
4 = 十月
```

### 作品库

作品库保存作品本体。

它不代表“我看过”。例如《进击的巨人 Final Season Part 1》是作品库中的一个作品；我是否看过、什么时候看、打几分，是另一张正式记录表的事。

作品库支持：

- 类型切换
- 搜索
- 年份筛选
- 月份筛选
- 状态筛选
- 标签筛选
- 分页
- 详情弹窗
- 添加 / 编辑正式记录
- 加入想看
- 加入搁置
- 删除作品

### 我的记录

这里只展示正式完成记录。

不会出现想看，也不会出现搁置。

记录里保存：

- 记录年份
- 记录月份档期
- 个人评分，0 到 50 分
- 评价 / 备注

### 收藏夹

收藏夹只管理两种状态：

```text
WANT   想看
PAUSED 搁置
```

如果一个作品被添加正式记录，它就不应该继续留在想看或搁置里。这个规则在后端和前端都已经处理。

收藏夹支持：

- 想看 / 搁置切换
- 类型筛选
- 搜索
- 分页
- 移至另一清单
- 添加已看
- 删除收藏

### 系列

系列页是项目特色页。

它用来连接这些东西：

- 多季
- Part
- 外传
- 重制版
- 跨动画 / 漫画 / 游戏 / 小说的同一系列

例如一个系列下可以同时有动画、漫画和游戏。系列页会横向展示时间线，也会统计已看、想看、搁置和作品数量。

### TOP 榜单

TOP 页不做“全站 all”。它按 ACGN 类型切换：

```text
动画 / 漫画 / 游戏 / 小说
```

榜单按个人评分排序，但不显式标排名。因为同分作品可能很多，用排名反而容易制造一种假的精确感。

### 导入中心

导入中心支持：

- 作品库导入
- 收藏夹导入
- 正式记录导入
- JSON 粘贴
- CSV 粘贴 / 文件读取
- 预览校验
- 确认写入
- 错误行不阻塞合法行

推荐导入顺序：

```text
subjects -> collections -> records
```

完整字段规范在：

```text
docs/import-guide.md
```

## 数据模型的几个关键约定

### Subject：作品本体

作品库只表示作品本体。

作品唯一性规则：

```text
titleCn + subtitle + seasonIndex + partIndex + category
```

这些字段全部相同，才视为同一作品。

### UserSubjectRecord：正式记录

正式记录只表示已经完成的观看、阅读或游玩记录。

想看和搁置不要写进 record。

### UserSubjectCollection：收藏夹

收藏夹独立于正式记录。

只管理：

```text
WANT
PAUSED
```

### Franchise：系列

系列用于把相关作品串起来。

正式记录导入或添加成功后，后端会做轻量系列检测：如果发现同名多季、Part 或跨媒介作品，会自动创建或复用系列并关联。

### Tag：结构化标签

作品可以带多个标签，例如：

```text
#奇幻 #战斗 #校园 #恋爱喜剧 #漫画改
```

这些标签不是装饰。后续做 LLM 偏好分析时，它们会成为很重要的结构化线索。

## 封面图片

作品导入时，`coverUrl` 可以写远程图片地址，例如 Bangumi 封面。

正式导入时后端会尝试把封面下载到：

```text
uploads/covers/
```

数据库中保存的地址会变成：

```text
/api/assets/covers/xxx.jpg
```

`uploads/` 是本地缓存目录，不应该提交到 git。

如果手动放图，也放到：

```text
uploads/covers/
```

然后 JSON 中写：

```json
"coverUrl": "/api/assets/covers/your-file-name.jpg"
```

## 常见问题

### 前端 502

通常是后端没启动。

前端 Vite 代理会把 `/api` 转给后端。如果后端没在 `8080` 端口运行，页面就会请求失败。

### npm 在 PowerShell 里不能运行

用：

```powershell
npm.cmd run dev
npm.cmd run build
npm.cmd run lint
```

### 导入时某几行失败

这是正常设计。

导入接口按行处理。字段错误会返回 `ERROR`，重复或无法匹配会返回 `SKIPPED`，其他合法行仍然可以继续导入。

### 系列没有自动出现

系列自动检测主要在正式记录创建后触发。

如果只是把一堆未记录作品导入作品库，系统不会急着把它们全部塞进系列表。这样可以避免系列页被纯库存数据撑乱。

## 答辩演示路线

推荐按这个顺序演示：

1. 首页：展示最新年份、月份档期、类型统计和评分前三。
2. 作品库：按类型、年份、月份、标签筛选作品。
3. 作品详情：展示作品本体、平台评分、个人评分、标签、简介。
4. 添加正式记录：从未记录变为已看，填写年份、月份、评分、备注。
5. 收藏夹：展示想看 / 搁置，演示移至另一清单和添加已看。
6. 系列页：展示同一系列下多季、Part、跨媒介作品的时间线。
7. TOP 榜单：展示高分作品和分数段统计。
8. 导入中心：展示 preview，再展示错误行不阻塞合法行。

这条路线能说明项目不是单纯 CRUD，而是在围绕“个人 ACGN 历史数据”组织信息。

## 开发备注

前端页面不是一次性堆在一个文件里的。

大致拆法是：

- `api/`：只负责请求后端。
- `types/`：定义 TypeScript 类型，让数据结构更清楚。
- `mappers/`：把后端返回值转换成前端卡片需要的视图模型。
- `components/`：可复用的界面块，比如卡片、弹窗、反馈状态。
- `pages/`：页面级逻辑，负责把接口、状态和组件串起来。

这样拆不是为了显得复杂，而是为了以后继续接 AI 分析时，不至于把所有东西揉成一团。

## 下一阶段

当前阶段目标是功能完整、可运行、可展示、可答辩。

后续可以继续做：

- 年度观看风格总结
- 标签偏好分析
- 系列补全建议
- 推荐理由生成
- 个人兴趣变化报告
- 基于 LLM 的自然语言查询

但这些都应该建立在当前这套干净的数据结构上。

## 最后

这个项目不是要做一个冷冰冰的后台。

它更像是给自己的 ACGN 记忆修一间档案室：有封面，有标签，有时间线，有想补完的坑，也有当年打下的分数和备注。

等数据越来越多，它才会慢慢变得有意思。
