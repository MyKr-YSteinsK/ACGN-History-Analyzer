import type { AcgnCategory, LibraryStatus, ReleaseQuarter } from '../types/common'

export const categoryTheme: Record<
    AcgnCategory,
    {
        key: AcgnCategory
        letter: string
        enName: string
        cnName: string
        text: string
        bg: string
        border: string
        ring: string
        softGradient: string
    }
> = {
    ANIME: {
        key: 'ANIME',
        letter: 'A',
        enName: 'Anime',
        cnName: '动画',
        text: 'text-sky-600',
        bg: 'bg-sky-100/80',
        border: 'border-sky-200/80',
        ring: 'ring-sky-200/70',
        softGradient: 'from-sky-100/90 via-blue-50/75 to-white/70',
    },
    COMIC: {
        key: 'COMIC',
        letter: 'C',
        enName: 'Comic',
        cnName: '漫画',
        text: 'text-emerald-600',
        bg: 'bg-emerald-100/80',
        border: 'border-emerald-200/80',
        ring: 'ring-emerald-200/70',
        softGradient: 'from-emerald-100/90 via-teal-50/75 to-white/70',
    },
    GAME: {
        key: 'GAME',
        letter: 'G',
        enName: 'Game',
        cnName: '游戏',
        text: 'text-pink-600',
        bg: 'bg-pink-100/80',
        border: 'border-pink-200/80',
        ring: 'ring-pink-200/70',
        softGradient: 'from-pink-100/90 via-rose-50/75 to-white/70',
    },
    NOVEL: {
        key: 'NOVEL',
        letter: 'N',
        enName: 'Novel',
        cnName: '小说',
        text: 'text-violet-600',
        bg: 'bg-violet-100/80',
        border: 'border-violet-200/80',
        ring: 'ring-violet-200/70',
        softGradient: 'from-violet-100/90 via-purple-50/75 to-white/70',
    },
}

export const quarterTheme: Record<
    ReleaseQuarter,
    {
        key: ReleaseQuarter
        cnName: string
        monthLabel: string
        shortName: string
        displayName: string
        text: string
        bg: string
        border: string
        panelGradient: string
    }
> = {
    1: {
        key: 1,
        cnName: '一月',
        monthLabel: '一月',
        shortName: '一月',
        displayName: '一月',
        text: 'text-blue-700',
        bg: 'bg-blue-100/70',
        border: 'border-blue-200/75',
        panelGradient: 'from-blue-100/65 via-white/55 to-slate-100/65',
    },
    2: {
        key: 2,
        cnName: '四月',
        monthLabel: '四月',
        shortName: '四月',
        displayName: '四月',
        text: 'text-green-600',
        bg: 'bg-green-100/80',
        border: 'border-green-200/80',
        panelGradient: 'from-green-100/70 via-white/55 to-emerald-50/60',
    },
    3: {
        key: 3,
        cnName: '七月',
        monthLabel: '七月',
        shortName: '七月',
        displayName: '七月',
        text: 'text-cyan-600',
        bg: 'bg-cyan-100/80',
        border: 'border-cyan-200/80',
        panelGradient: 'from-cyan-100/70 via-white/55 to-sky-50/60',
    },
    4: {
        key: 4,
        cnName: '十月',
        monthLabel: '十月',
        shortName: '十月',
        displayName: '十月',
        text: 'text-orange-600',
        bg: 'bg-orange-100/80',
        border: 'border-orange-200/80',
        panelGradient: 'from-orange-100/75 via-white/55 to-amber-50/65',
    },
}

export const neutralPanelGradient =
    'from-white/60 via-white/45 to-slate-50/60'

export const yearChipClassName =
    'border-amber-200/75 bg-amber-50/90 text-amber-700'

export const libraryStatusTheme: Record<
    LibraryStatus,
    {
        label: string
        className: string
    }
> = {
    WATCHED: {
        label: '看过',
        className: 'border-sky-200 bg-sky-100/80 text-sky-700',
    },
    WANT: {
        label: '想看',
        className: 'border-emerald-200 bg-emerald-100/80 text-emerald-700',
    },
    PAUSED: {
        label: '搁置',
        className: 'border-orange-200 bg-orange-100/80 text-orange-700',
    },
    NONE: {
        label: '未记录',
        className: 'border-slate-200 bg-slate-100/80 text-slate-500',
    },
}

export const appTheme = {
    appName: 'ACGN History Analyzer',
    cnName: 'ACGN 个人历史数据分析系统',
    description:
        '用于整理个人动画、漫画、游戏、小说历史记录，并在后续阶段接入 LLM 智能分析。',
}

export const platformScoreTheme = {
    label: '平台',
    className:
        'border-amber-200/80 bg-amber-50/90 text-amber-700 shadow-sm backdrop-blur',
}

export const personalScoreTheme = {
    label: '个人',
    className:
        'border-rose-200/80 bg-rose-50/90 text-rose-700 shadow-sm backdrop-blur',
}
