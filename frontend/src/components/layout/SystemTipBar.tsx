import { useMemo } from 'react'
import { useLocation } from 'react-router-dom'

const routeTips: Array<{
    match: (pathname: string) => boolean
    title: string
    description: string
}> = [
    {
        match: (pathname) => pathname === '/',
        title: '今天从总览开始也可以',
        description: '首页会把近期记录、收藏和 AI 摘要放在一起，适合先找出最值得继续整理的方向。',
    },
    {
        match: (pathname) => pathname.startsWith('/records'),
        title: '记录越完整，后面的分析越像你',
        description: '评分、完成时间和简短备注都会成为 AI 判断口味变化的线索。',
    },
    {
        match: (pathname) => pathname.startsWith('/library'),
        title: '作品库是整个系统的底座',
        description: '先把作品、标签、系列关系维护好，推荐和统计就会少很多偏差。',
    },
    {
        match: (pathname) => pathname.startsWith('/collections'),
        title: '收藏状态管理',
        description: '这里只维护想看与搁置清单，可按类型和关键词快速整理。',
    },
    {
        match: (pathname) => pathname.startsWith('/import'),
        title: '导入前先预览一眼',
        description: 'Bangumi 数据可能有别名、分季和封面差异，预览能避免把不想要的条目写进本地库。',
    },
    {
        match: (pathname) => pathname.startsWith('/analysis'),
        title: 'AI 结果可以重生成，也可以先看缓存',
        description: '真实模型偶尔会慢，系统会优先保留可用结果；需要新观点时再覆盖缓存。',
    },
]

function SystemTipBar() {
    const location = useLocation()
    const tip = useMemo(
        () => routeTips.find((item) => item.match(location.pathname)) ?? routeTips[0],
        [location.pathname],
    )

    if (
        location.pathname.startsWith('/franchises') ||
        location.pathname.startsWith('/top') ||
        location.pathname.startsWith('/import')
    ) {
        return null
    }

    return (
        <div className="mb-5 rounded-[26px] border border-white/75 bg-white/58 px-5 py-4 shadow-sm backdrop-blur">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-300">
                system note
            </p>
            <div className="mt-2">
                <div>
                    <h2 className="text-base font-black text-slate-700">{tip.title}</h2>
                    <p className="mt-1 text-sm leading-6 text-slate-500">{tip.description}</p>
                </div>
            </div>
        </div>
    )
}

export default SystemTipBar
