import type { SubjectPageSort } from '../../api/subjectApi'
import type { LibraryStatus, ReleaseQuarter } from '../../types/common'

/** Inclusive lower bound for discrete year chips (older works use “before” filter). */
export const LIBRARY_FILTER_YEAR_MIN = 2000

/** Default year when opening 作品库. */
export const LIBRARY_DEFAULT_YEAR = 2026

/** API: subjects with release_year strictly less than this value. */
export const LIBRARY_RELEASE_YEAR_BEFORE_EXCLUSIVE = 2000

export const LIBRARY_BEFORE_YEAR_LABEL = '2000 以前'

function buildLibraryYearOptions(): number[] {
    const max = new Date().getFullYear() + 2
    const span = Math.max(0, max - LIBRARY_FILTER_YEAR_MIN)
    return Array.from({ length: span + 1 }, (_, index) => LIBRARY_FILTER_YEAR_MIN + index)
}

export const yearOptions = buildLibraryYearOptions()

export type LibrarySortOption = {
    value: SubjectPageSort
    label: string
}

export const sortOptions: LibrarySortOption[] = [
    { value: 'TITLE_ASC', label: '默认' },
    { value: 'PLATFORM_SCORE_DESC', label: '评分降序' },
    { value: 'PLATFORM_SCORE_ASC', label: '评分升序' },
]

export const quarterOptions: Array<{
    value: ReleaseQuarter | 'ALL'
    label: string
    className?: string
}> = [
    { value: 'ALL', label: '全部月份' },
    {
        value: 1,
        label: '一月',
        className: 'border-blue-200 bg-blue-100/80 text-blue-700 shadow-sm',
    },
    {
        value: 2,
        label: '四月',
        className: 'border-green-200 bg-green-100/85 text-green-700 shadow-sm',
    },
    {
        value: 3,
        label: '七月',
        className: 'border-cyan-200 bg-cyan-100/85 text-cyan-700 shadow-sm',
    },
    {
        value: 4,
        label: '十月',
        className: 'border-orange-200 bg-orange-100/85 text-orange-700 shadow-sm',
    },
]

export const statusOptions: Array<{
    value: LibraryStatus | 'ALL'
    label: string
    className?: string
}> = [
    { value: 'ALL', label: '全部状态' },
    {
        value: 'WATCHED',
        label: '看过',
        className: 'border-sky-200 bg-sky-100/85 text-sky-700 shadow-sm',
    },
    {
        value: 'WANT',
        label: '想看',
        className: 'border-emerald-200 bg-emerald-100/85 text-emerald-700 shadow-sm',
    },
    {
        value: 'PAUSED',
        label: '搁置',
        className: 'border-orange-200 bg-orange-100/85 text-orange-700 shadow-sm',
    },
    {
        value: 'NONE',
        label: '未记录',
        className: 'border-slate-200 bg-slate-100/85 text-slate-600 shadow-sm',
    },
]

export const tagChipClassNames: Record<string, string> = {
    sky: 'border-sky-200 bg-sky-100/80 text-sky-700',
    blue: 'border-blue-200 bg-blue-100/80 text-blue-700',
    cyan: 'border-cyan-200 bg-cyan-100/80 text-cyan-700',
    emerald: 'border-emerald-200 bg-emerald-100/80 text-emerald-700',
    amber: 'border-amber-200 bg-amber-100/80 text-amber-700',
    rose: 'border-rose-200 bg-rose-100/80 text-rose-700',
    pink: 'border-pink-200 bg-pink-100/80 text-pink-700',
    violet: 'border-violet-200 bg-violet-100/80 text-violet-700',
    slate: 'border-slate-200 bg-slate-100/80 text-slate-600',
}
