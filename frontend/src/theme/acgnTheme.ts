export type AcgnCategory = 'ANIME' | 'COMIC' | 'GAME' | 'NOVEL'

export type AcgnSeason = 'SPRING' | 'SUMMER' | 'AUTUMN' | 'WINTER'

export const categoryTheme = {
    ANIME: {
        key: 'ANIME',
        letter: 'A',
        enName: 'Anime',
        cnName: '动画',
        text: 'text-sky-600',
        bg: 'bg-sky-100/80',
        border: 'border-sky-200/80',
        softGradient: 'from-sky-100/80 to-blue-50/70',
    },
    COMIC: {
        key: 'COMIC',
        letter: 'C',
        enName: 'Comic',
        cnName: '漫画',
        text: 'text-emerald-600',
        bg: 'bg-emerald-100/80',
        border: 'border-emerald-200/80',
        softGradient: 'from-emerald-100/80 to-teal-50/70',
    },
    GAME: {
        key: 'GAME',
        letter: 'G',
        enName: 'Game',
        cnName: '游戏',
        text: 'text-pink-600',
        bg: 'bg-pink-100/80',
        border: 'border-pink-200/80',
        softGradient: 'from-pink-100/80 to-rose-50/70',
    },
    NOVEL: {
        key: 'NOVEL',
        letter: 'N',
        enName: 'Novel',
        cnName: '小说',
        text: 'text-violet-600',
        bg: 'bg-violet-100/80',
        border: 'border-violet-200/80',
        softGradient: 'from-violet-100/80 to-purple-50/70',
    },
} as const

export const seasonTheme = {
    SPRING: {
        key: 'SPRING',
        cnName: '春',
        text: 'text-green-600',
        bg: 'bg-green-100/80',
        border: 'border-green-200/80',
    },
    SUMMER: {
        key: 'SUMMER',
        cnName: '夏',
        text: 'text-cyan-600',
        bg: 'bg-cyan-100/80',
        border: 'border-cyan-200/80',
    },
    AUTUMN: {
        key: 'AUTUMN',
        cnName: '秋',
        text: 'text-orange-600',
        bg: 'bg-orange-100/80',
        border: 'border-orange-200/80',
    },
    WINTER: {
        key: 'WINTER',
        cnName: '冬',
        text: 'text-slate-600',
        bg: 'bg-slate-100/80',
        border: 'border-slate-200/80',
    },
} as const

export const appTheme = {
    appName: 'ACGN History Analyzer',
    cnName: 'ACGN 个人历史数据分析系统',
    description:
        '用于整理个人动画、漫画、游戏、小说历史记录，并在后续阶段接入 LLM 智能分析。',
}