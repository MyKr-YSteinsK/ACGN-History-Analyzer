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
        description: '偏好报告与智能问答',
    },
]

function AppNavigation() {
    return (
        <aside className="w-full shrink-0 rounded-[28px] border border-white/70 bg-white/58 p-3 shadow-[0_18px_60px_rgba(120,135,160,0.14)] backdrop-blur-xl lg:sticky lg:top-6 lg:h-fit lg:w-72 lg:rounded-[32px] lg:p-4 lg:shadow-[0_24px_70px_rgba(120,135,160,0.16)]">
            <div className="flex items-center justify-between gap-4 px-3 py-3 lg:block lg:px-4">
                <div>
                    <p className="text-xs font-semibold tracking-[0.28em] text-sky-500">
                        ACGN
                    </p>
                    <h1 className="mt-1 text-lg font-black text-slate-800 lg:mt-2 lg:text-xl">
                        个人历史库
                    </h1>
                </div>
                <p className="hidden max-w-[220px] text-right text-xs leading-5 text-slate-500 sm:block lg:mt-2 lg:block lg:max-w-none lg:text-left">
                    作品、记录、收藏、系列与统计的统一入口
                </p>
            </div>

            <nav className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-4 lg:mt-4 lg:block lg:space-y-2">
                {navItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        end={item.path === '/'}
                        className={({ isActive }) =>
                            [
                                'block rounded-2xl border px-3 py-2.5 transition-all duration-200 lg:rounded-3xl lg:px-4 lg:py-3',
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

                                <p className="mt-1 hidden text-xs leading-5 text-slate-400 lg:block">
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
