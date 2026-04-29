import { NavLink } from 'react-router-dom'

type NavItem = {
    path: string
    label: string
    description: string
}

const navItems: NavItem[] = [
    {
        path: '/',
        label: '首页统计',
        description: '年度与月份总览',
    },
    {
        path: '/records',
        label: '我的记录',
        description: '正式看过 / 读过 / 玩过',
    },
    {
        path: '/library',
        label: '作品库',
        description: '管理作品基础信息',
    },
    {
        path: '/top',
        label: 'TOP 榜单',
        description: '个人高分作品',
    },
    {
        path: '/collections',
        label: '收藏夹',
        description: '想看与搁置',
    },
    {
        path: '/import',
        label: '导入',
        description: '批量录入与校验',
    },
    {
        path: '/franchises',
        label: '系列',
        description: '跨季 / 跨媒介整理',
    },
    {
        path: '/analysis',
        label: '智能分析',
        description: 'Mock 报告与 AI 功能入口',
    },
]

function AppNavigation() {
    return (
        <aside className="sticky top-6 h-fit w-72 shrink-0 rounded-[32px] border border-white/70 bg-white/55 p-4 shadow-[0_24px_70px_rgba(120,135,160,0.16)] backdrop-blur-xl">
            <div className="px-4 py-3">
                <p className="text-xs font-semibold tracking-[0.28em] text-sky-500">
                    ACGN
                </p>
                <h1 className="mt-2 text-xl font-bold text-slate-800">个人历史库</h1>
                <p className="mt-2 text-xs leading-5 text-slate-500">
                    作品、记录、收藏、系列与统计的统一入口
                </p>
            </div>

            <nav className="mt-4 space-y-2">
                {navItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        end={item.path === '/'}
                        className={({ isActive }) =>
                            [
                                'block rounded-3xl border px-4 py-3 transition-all duration-200',
                                isActive
                                    ? 'border-sky-200 bg-sky-100/75 shadow-sm'
                                    : 'border-transparent bg-white/30 hover:border-white/80 hover:bg-white/70',
                            ].join(' ')
                        }
                    >
                        {({ isActive }) => (
                            <>
                                <div className="flex items-center justify-between gap-3">
                  <span
                      className={[
                          'text-sm font-semibold',
                          isActive ? 'text-sky-700' : 'text-slate-700',
                      ].join(' ')}
                  >
                    {item.label}
                  </span>

                                    <span
                                        className={[
                                            'h-2 w-2 rounded-full',
                                            isActive ? 'bg-sky-400' : 'bg-slate-200',
                                        ].join(' ')}
                                    />
                                </div>

                                <p className="mt-1 text-xs leading-5 text-slate-400">
                                    {item.description}
                                </p>
                            </>
                        )}
                    </NavLink>
                ))}
            </nav>
        </aside>
    )
}

export default AppNavigation
