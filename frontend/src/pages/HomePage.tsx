import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
    getRecords,
    type UserSubjectRecordResponse,
} from '../api/recordApi'
import { getSubjectById } from '../api/subjectApi'
import CoverImage from '../components/common/CoverImage'
import GlassCard from '../components/common/GlassCard'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { mapSubjectDetailToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    neutralPanelGradient,
    personalScoreTheme,
    quarterTheme,
} from '../theme/acgnTheme'
import {
    categoryOrder,
    getReleaseQuarterLabel,
    type AcgnCategory,
    type ReleaseQuarter,
} from '../types/common'
import type { SubjectCardView } from '../types/subject'

type CategoryCount = {
    category: AcgnCategory
    count: number
}

type QuarterCount = {
    quarter: ReleaseQuarter
    count: number
}

const quarters: ReleaseQuarter[] = [1, 2, 3, 4]

function getCurrentQuarter(): ReleaseQuarter {
    const month = new Date().getMonth() + 1

    if (month <= 3) {
        return 1
    }

    if (month <= 6) {
        return 2
    }

    if (month <= 9) {
        return 3
    }

    return 4
}

function getErrorMessage(error: unknown) {
    if (error instanceof Error) {
        return error.message
    }

    return '请求失败，请稍后重试'
}

function sortRecordsByScore(records: UserSubjectRecordResponse[]) {
    return [...records].sort((first, second) => {
        const scoreCompare = (second.scoreValue ?? -1) - (first.scoreValue ?? -1)

        if (scoreCompare !== 0) {
            return scoreCompare
        }

        return second.id - first.id
    })
}

function getReleaseText(record: UserSubjectRecordResponse) {
    if (!record.subjectReleaseYear) {
        return '发行未知'
    }

    const quarterLabel = record.subjectReleaseQuarter
        ? getReleaseQuarterLabel(record.subjectReleaseQuarter)
        : ''

    return `${record.subjectReleaseYear}${quarterLabel}`
}

function getLatestRecordPosition(records: UserSubjectRecordResponse[]) {
    const latestRecord = [...records].sort((first, second) => {
        const yearCompare = second.recordYear - first.recordYear

        if (yearCompare !== 0) {
            return yearCompare
        }

        return second.recordQuarter - first.recordQuarter
    })[0]

    if (!latestRecord) {
        return null
    }

    return {
        year: latestRecord.recordYear,
        quarter: latestRecord.recordQuarter,
    }
}

function countRecords(
    records: UserSubjectRecordResponse[],
    year: number,
    category: AcgnCategory,
    quarter?: ReleaseQuarter,
) {
    return records.filter((record) => {
        const matchYear = record.recordYear === year
        const matchCategory = record.subjectCategory === category
        const matchQuarter = quarter === undefined || record.recordQuarter === quarter

        return matchYear && matchCategory && matchQuarter
    }).length
}

function getPreferredQuarter(
    records: UserSubjectRecordResponse[],
    year: number,
    category: AcgnCategory,
    currentQuarter: ReleaseQuarter,
) {
    if (countRecords(records, year, category, currentQuarter) > 0) {
        return currentQuarter
    }

    return [...quarters]
        .reverse()
        .find((quarter) => countRecords(records, year, category, quarter) > 0) ?? currentQuarter
}

function formatChange(value: number) {
    if (value > 0) {
        return `+${value}`
    }

    return String(value)
}

function HomePage() {
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(getCurrentQuarter())
    const [selectedCategory, setSelectedCategory] = useState<AcgnCategory>('ANIME')
    const [records, setRecords] = useState<UserSubjectRecordResponse[]>([])
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [loading, setLoading] = useState(false)
    const [detailLoading, setDetailLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const latestSelectionApplied = useRef(false)

    const selectedCategoryTheme = categoryTheme[selectedCategory]
    const selectedQuarterTheme = quarterTheme[selectedQuarter]
    const selectedQuarterGradient =
        selectedQuarterTheme?.panelGradient ?? neutralPanelGradient

    const loadRecords = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const response = await getRecords()
            setRecords(response)

            if (!latestSelectionApplied.current) {
                const latestPosition = getLatestRecordPosition(response)

                if (latestPosition) {
                    setSelectedYear(latestPosition.year)
                    setSelectedQuarter(latestPosition.quarter)
                }

                latestSelectionApplied.current = true
            }
        } catch (loadError) {
            setRecords([])
            setError(getErrorMessage(loadError))
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        // 首页默认定位到已有正式记录中的最新年份和最新季度。
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadRecords()
    }, [loadRecords])

    const yearRecords = useMemo(() => {
        return records.filter((record) => record.recordYear === selectedYear)
    }, [records, selectedYear])

    const selectedCategoryYearRecords = useMemo(() => {
        return yearRecords.filter((record) => record.subjectCategory === selectedCategory)
    }, [selectedCategory, yearRecords])

    const selectedQuarterRecords = useMemo(() => {
        return selectedCategoryYearRecords.filter(
            (record) => record.recordQuarter === selectedQuarter,
        )
    }, [selectedCategoryYearRecords, selectedQuarter])

    const yearCategoryCounts = useMemo(() => {
        return categoryOrder.map<CategoryCount>((category) => ({
            category,
            count: countRecords(records, selectedYear, category),
        }))
    }, [records, selectedYear])

    const quarterCategoryCounts = useMemo(() => {
        return categoryOrder.map<CategoryCount>((category) => ({
            category,
            count: countRecords(records, selectedYear, category, selectedQuarter),
        }))
    }, [records, selectedQuarter, selectedYear])

    const selectedCategoryQuarterCounts = useMemo(() => {
        return quarters.map<QuarterCount>((quarter) => ({
            quarter,
            count: countRecords(records, selectedYear, selectedCategory, quarter),
        }))
    }, [records, selectedCategory, selectedYear])

    const yearChangeRows = useMemo(() => {
        return categoryOrder.map((category) => {
            const currentCount = countRecords(records, selectedYear, category, selectedQuarter)
            const previousCount = countRecords(records, selectedYear - 1, category, selectedQuarter)

            return {
                category,
                currentCount,
                previousCount,
                diff: currentCount - previousCount,
            }
        })
    }, [records, selectedQuarter, selectedYear])

    const topRecords = useMemo(() => {
        return sortRecordsByScore(
            selectedQuarterRecords.filter((record) => record.scoreValue !== null),
        ).slice(0, 3)
    }, [selectedQuarterRecords])

    const totalYearRecords = yearRecords.length
    const selectedCategoryTotal = selectedCategoryYearRecords.length
    const selectedQuarterTotal = selectedQuarterRecords.length
    const maxSeasonCount = Math.max(
        1,
        ...selectedCategoryQuarterCounts.map((item) => item.count),
    )
    const maxYearCategoryCount = Math.max(1, ...yearCategoryCounts.map((item) => item.count))
    const maxQuarterCategoryCount = Math.max(1, ...quarterCategoryCounts.map((item) => item.count))
    const maxChangeAbs = Math.max(1, ...yearChangeRows.map((item) => Math.abs(item.diff)))
    const selectedChangeRow =
        yearChangeRows.find((item) => item.category === selectedCategory) ?? yearChangeRows[0]

    function handleYearChange(year: number) {
        const preferredQuarter = getPreferredQuarter(
            records,
            year,
            selectedCategory,
            selectedQuarter,
        )

        setSelectedYear(year)
        setSelectedQuarter(preferredQuarter)
    }

    function handleCategoryChange(category: AcgnCategory) {
        const preferredQuarter = getPreferredQuarter(
            records,
            selectedYear,
            category,
            selectedQuarter,
        )

        setSelectedCategory(category)
        setSelectedQuarter(preferredQuarter)
    }

    async function handleOpenDetail(record: UserSubjectRecordResponse) {
        setDetailLoading(true)
        setError(null)

        try {
            const detail = await getSubjectById(record.subjectId)
            setSelectedSubject(mapSubjectDetailToCardView(detail))
        } catch (detailError) {
            setError(getErrorMessage(detailError))
        } finally {
            setDetailLoading(false)
        }
    }

    return (
        <>
            <div className="space-y-5 content-enter">
                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-5 transition-all duration-300',
                            selectedCategoryTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="flex flex-wrap items-center justify-between gap-4">
                            <div>
                                <p className="text-xs font-semibold tracking-[0.28em] text-slate-400">
                                    DASHBOARD
                                </p>
                                <h2 className="mt-2 text-2xl font-black text-slate-800">
                                    ACGN 个人历史统计
                                </h2>
                                <p className="mt-2 text-sm text-slate-500">
                                    最新记录定位：{selectedYear} · {selectedQuarterTheme.cnName} · {selectedCategoryTheme.cnName}
                                </p>
                            </div>

                            <div className="grid min-w-[360px] gap-3 sm:grid-cols-3">
                                <MetricPill label="年度记录" value={totalYearRecords} />
                                <MetricPill label="当前类型" value={selectedCategoryTotal} />
                                <MetricPill label="本季本类" value={selectedQuarterTotal} />
                            </div>
                        </div>
                    </div>
                </GlassCard>

                {error ? (
                    <div className="rounded-[24px] border border-rose-100 bg-white/62 px-5 py-4 text-sm font-semibold text-rose-600 shadow-sm backdrop-blur-xl">
                        {error}
                    </div>
                ) : null}

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-5 transition-all duration-300',
                            selectedCategoryTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="mb-4 flex flex-wrap items-center justify-between gap-4">
                            <div>
                                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                                    year overview
                                </p>
                                <h3 className="mt-1 text-2xl font-black text-slate-800">
                                    {selectedYear} 年类型总览
                                </h3>
                            </div>

                            <YearSelector
                                selectedYear={selectedYear}
                                onChange={handleYearChange}
                            />
                        </div>

                        <div className="grid gap-3 md:grid-cols-4">
                            {yearCategoryCounts.map((item) => (
                                <CategoryOverviewCard
                                    key={item.category}
                                    item={item}
                                    active={item.category === selectedCategory}
                                    total={maxYearCategoryCount}
                                    onClick={() => handleCategoryChange(item.category)}
                                />
                            ))}
                        </div>
                    </div>
                </GlassCard>

                <div className="grid gap-5 xl:grid-cols-[0.92fr_1.08fr]">
                    <GlassCard className="overflow-hidden">
                        <div
                            className={[
                                'bg-gradient-to-br p-5 transition-all duration-300',
                                selectedCategoryTheme.softGradient,
                            ].join(' ')}
                        >
                            <SectionHeader
                                eyebrow="season distribution"
                                title={`${selectedCategoryTheme.cnName}四季分布`}
                                description="点击有数据的季节切换当前季度。"
                            />

                            <SeasonBarChart
                                values={selectedCategoryQuarterCounts}
                                maxCount={maxSeasonCount}
                                selectedQuarter={selectedQuarter}
                                onSelect={setSelectedQuarter}
                            />
                        </div>
                    </GlassCard>

                    <GlassCard className="overflow-hidden">
                        <div
                            className={[
                                'bg-gradient-to-br p-5 transition-all duration-300',
                                selectedQuarterGradient,
                            ].join(' ')}
                        >
                            <SectionHeader
                                eyebrow="top score preview"
                                title={`${selectedQuarterTheme.cnName}季评分前三`}
                                description={`${selectedYear} · ${selectedCategoryTheme.cnName} · 按个人评分排序`}
                            />

                            <TopScorePreview
                                loading={loading}
                                records={topRecords}
                                category={selectedCategory}
                                onOpenDetail={handleOpenDetail}
                            />
                        </div>
                    </GlassCard>
                </div>

                <div className="grid gap-5 xl:grid-cols-[1fr_1fr]">
                    <GlassCard className="overflow-hidden">
                        <div
                            className={[
                                'bg-gradient-to-br p-5 transition-all duration-300',
                                selectedQuarterGradient,
                            ].join(' ')}
                        >
                            <SectionHeader
                                eyebrow="type composition"
                                title={`${selectedQuarterTheme.cnName}季类型构成`}
                                description="点击类型会同步切换全局类型。"
                            />

                            <div className="mt-4 grid grid-cols-2 gap-3">
                                {quarterCategoryCounts.map((item) => (
                                    <QuarterCompositionCard
                                        key={item.category}
                                        item={item}
                                        active={item.category === selectedCategory}
                                        total={maxQuarterCategoryCount}
                                        onClick={() => handleCategoryChange(item.category)}
                                    />
                                ))}
                            </div>
                        </div>
                    </GlassCard>

                    <GlassCard className="overflow-hidden">
                        <div
                            className={[
                                'bg-gradient-to-br p-5 transition-all duration-300',
                                selectedCategoryTheme.softGradient,
                            ].join(' ')}
                        >
                            <SectionHeader
                                eyebrow="year change"
                                title="相对往年同期变化"
                                description={`${selectedQuarterTheme.cnName}季 · 当前类型 ${formatChange(selectedChangeRow?.diff ?? 0)}`}
                            />

                            <div className="mt-4 grid gap-2.5">
                                {yearChangeRows.map((item) => (
                                    <YearChangeRow
                                        key={item.category}
                                        row={item}
                                        active={item.category === selectedCategory}
                                        maxAbs={maxChangeAbs}
                                        onClick={() => handleCategoryChange(item.category)}
                                    />
                                ))}
                            </div>
                        </div>
                    </GlassCard>
                </div>
            </div>

            <SubjectDetailDrawer
                open={selectedSubject !== null}
                subject={selectedSubject}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={() => undefined}
                onAddToWant={() => undefined}
                onAddToPaused={() => undefined}
                onSetToNone={() => undefined}
                detailLoading={detailLoading}
                showActions={false}
            />
        </>
    )
}

type MetricPillProps = {
    label: string
    value: number | string
}

function MetricPill({ label, value }: MetricPillProps) {
    return (
        <div className="rounded-[22px] border border-white/70 bg-white/58 px-4 py-3 shadow-sm backdrop-blur">
            <p className="text-xs font-semibold text-slate-400">{label}</p>
            <p className="mt-1 text-2xl font-black text-slate-800">{value}</p>
        </div>
    )
}

type YearSelectorProps = {
    selectedYear: number
    onChange: (year: number) => void
}

function YearSelector({ selectedYear, onChange }: YearSelectorProps) {
    const visibleYears = [selectedYear - 1, selectedYear, selectedYear + 1]

    return (
        <div className="flex items-center gap-2">
            <button
                type="button"
                onClick={() => onChange(selectedYear - 1)}
                className="h-11 w-11 rounded-2xl border border-amber-200/75 bg-white/78 text-lg font-black text-amber-600 shadow-sm transition-all duration-200 hover:-translate-x-0.5 hover:bg-amber-50"
                aria-label="选择上一年"
            >
                &lt;
            </button>

            <div className="grid grid-cols-3 gap-2">
                {visibleYears.map((year) => {
                    const selected = year === selectedYear

                    return (
                        <button
                            key={year}
                            type="button"
                            onClick={() => onChange(year)}
                            className={[
                                'h-11 rounded-2xl border px-4 text-sm font-black transition-all duration-200',
                                selected
                                    ? 'scale-[1.03] border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                    : 'border-white/75 bg-white/62 text-slate-400 hover:-translate-y-0.5 hover:bg-white hover:text-slate-600',
                            ].join(' ')}
                        >
                            {year}
                        </button>
                    )
                })}
            </div>

            <button
                type="button"
                onClick={() => onChange(selectedYear + 1)}
                className="h-11 w-11 rounded-2xl border border-amber-200/75 bg-white/78 text-lg font-black text-amber-600 shadow-sm transition-all duration-200 hover:translate-x-0.5 hover:bg-amber-50"
                aria-label="选择下一年"
            >
                &gt;
            </button>
        </div>
    )
}

type SectionHeaderProps = {
    eyebrow: string
    title: string
    description: string
}

function SectionHeader({ eyebrow, title, description }: SectionHeaderProps) {
    return (
        <div className="flex items-start justify-between gap-4">
            <div>
                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                    {eyebrow}
                </p>
                <h3 className="mt-1 text-xl font-black text-slate-800">{title}</h3>
                <p className="mt-1 text-sm text-slate-400">{description}</p>
            </div>
        </div>
    )
}

type CategoryOverviewCardProps = {
    item: CategoryCount
    active: boolean
    total: number
    onClick: () => void
}

function CategoryOverviewCard({ item, active, total, onClick }: CategoryOverviewCardProps) {
    const theme = categoryTheme[item.category]
    const percent = Math.min(100, Math.round((item.count / Math.max(1, total)) * 100))

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'group overflow-hidden rounded-[24px] border p-4 text-left transition-all duration-200',
                active
                    ? `${theme.border} bg-gradient-to-br ${theme.softGradient} scale-[1.015] shadow-[0_18px_42px_rgba(120,135,160,0.16)]`
                    : 'border-white/70 bg-white/55 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <div className="flex items-end gap-1 leading-none">
                <span className={['text-4xl font-black leading-none', theme.text].join(' ')}>
                    {theme.letter}
                </span>
                <span className="pb-[3px] text-lg font-black text-slate-800">
                    {theme.enName.slice(1).toUpperCase()}
                </span>
            </div>

            <div className="mt-3 flex items-end justify-between gap-3">
                <span className="text-sm font-semibold text-slate-500">{theme.cnName}</span>
                <span className={['text-3xl font-black', theme.text].join(' ')}>
                    {item.count}
                </span>
            </div>

            <div className="mt-3 h-2 overflow-hidden rounded-full bg-white/75">
                <div
                    className={[
                        'h-full rounded-full transition-all duration-300',
                        active ? theme.bg : 'bg-slate-200/70',
                    ].join(' ')}
                    style={{ width: `${Math.max(8, percent)}%` }}
                />
            </div>
        </button>
    )
}

type SeasonBarChartProps = {
    values: QuarterCount[]
    maxCount: number
    selectedQuarter: ReleaseQuarter
    onSelect: (quarter: ReleaseQuarter) => void
}

function SeasonBarChart({
                            values,
                            maxCount,
                            selectedQuarter,
                            onSelect,
                        }: SeasonBarChartProps) {
    return (
        <div className="mt-5 flex h-[210px] items-end gap-4">
            {values.map((item) => {
                const theme = quarterTheme[item.quarter]
                const selected = selectedQuarter === item.quarter
                const disabled = item.count === 0
                const height = item.count === 0 ? 18 : 32 + (item.count / maxCount) * 138

                return (
                    <button
                        key={item.quarter}
                        type="button"
                        disabled={disabled}
                        onClick={() => onSelect(item.quarter)}
                        className={[
                            'flex flex-1 flex-col items-center gap-2 transition-all duration-200',
                            selected ? 'scale-[1.04]' : 'hover:scale-[1.015]',
                            disabled ? 'cursor-not-allowed opacity-40' : '',
                        ].join(' ')}
                    >
                        <div className="flex w-full flex-1 items-end">
                            <div
                                className={[
                                    'w-full rounded-t-[22px] border transition-all duration-300',
                                    selected
                                        ? `${theme.border} ${theme.bg} shadow-[0_16px_36px_rgba(120,135,160,0.20)]`
                                        : 'border-white/70 bg-white/62',
                                ].join(' ')}
                                style={{ height }}
                            />
                        </div>

                        <span
                            className={[
                                'rounded-full px-3 py-1 text-sm font-black',
                                selected ? `${theme.bg} ${theme.text}` : 'text-slate-400',
                            ].join(' ')}
                        >
                            {theme.cnName}
                        </span>

                        <span className="text-xs font-semibold text-slate-400">
                            {item.count}
                        </span>
                    </button>
                )
            })}
        </div>
    )
}

type TopScorePreviewProps = {
    loading: boolean
    records: UserSubjectRecordResponse[]
    category: AcgnCategory
    onOpenDetail: (record: UserSubjectRecordResponse) => void
}

function TopScorePreview({
                             loading,
                             records,
                             category,
                             onOpenDetail,
                         }: TopScorePreviewProps) {
    if (loading) {
        return (
            <div className="mt-5 flex min-h-[260px] items-center justify-center gap-4">
                {Array.from({ length: 3 }, (_, index) => (
                    <div
                        key={index}
                        className="h-[230px] w-[160px] animate-pulse rounded-[24px] border border-white/70 bg-white/50"
                    />
                ))}
            </div>
        )
    }

    if (records.length === 0) {
        return (
            <div className="mt-5 flex min-h-[260px] items-center justify-center rounded-[28px] border border-dashed border-slate-200 bg-white/45 text-sm font-semibold text-slate-400">
                当前选择下暂无评分记录
            </div>
        )
    }

    return (
        <div className="mt-5 flex min-h-[260px] items-center justify-center gap-4">
            {records.map((record) => (
                <TopScoreCard
                    key={record.id}
                    record={record}
                    category={category}
                    onOpenDetail={onOpenDetail}
                />
            ))}
        </div>
    )
}

type TopScoreCardProps = {
    record: UserSubjectRecordResponse
    category: AcgnCategory
    onOpenDetail: (record: UserSubjectRecordResponse) => void
}

function TopScoreCard({ record, category, onOpenDetail }: TopScoreCardProps) {
    const theme = categoryTheme[record.subjectCategory ?? category]

    return (
        <button
            type="button"
            onClick={() => onOpenDetail(record)}
            className={[
                'group w-[168px] overflow-hidden rounded-[24px] border border-white/70 bg-gradient-to-br p-2.5 text-left shadow-sm transition-all duration-200',
                'hover:-translate-y-1 hover:shadow-[0_18px_42px_rgba(120,135,160,0.16)]',
                theme.softGradient,
            ].join(' ')}
        >
            <div className="relative aspect-[3/4] overflow-hidden rounded-[20px] bg-white/50">
                <CoverImage
                    src={record.subjectCoverUrl}
                    alt={record.subjectTitle}
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.04]"
                    fallback={
                    <div
                        className={[
                            'flex h-full w-full items-center justify-center bg-gradient-to-br',
                            theme.softGradient,
                        ].join(' ')}
                    >
                        <span className={['text-5xl font-black', theme.text].join(' ')}>
                            {theme.letter}
                        </span>
                    </div>
                    }
                />

                <span
                    className={[
                        'absolute right-2 top-2 rounded-2xl border px-2.5 py-1 text-xs font-black',
                        personalScoreTheme.className,
                    ].join(' ')}
                >
                    {record.scoreValue}
                </span>
            </div>

            <h4 className="mt-3 line-clamp-1 text-sm font-black text-slate-800">
                {record.subjectTitle}
            </h4>

            <p className="mt-1 text-[11px] font-semibold text-slate-400">
                {getReleaseText(record)}
            </p>
        </button>
    )
}

type QuarterCompositionCardProps = {
    item: CategoryCount
    active: boolean
    total: number
    onClick: () => void
}

function QuarterCompositionCard({
                                    item,
                                    active,
                                    total,
                                    onClick,
                                }: QuarterCompositionCardProps) {
    const theme = categoryTheme[item.category]
    const percent = Math.min(100, Math.round((item.count / Math.max(1, total)) * 100))

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-[24px] border p-4 text-left transition-all duration-200',
                active
                    ? `${theme.border} bg-gradient-to-br ${theme.softGradient} scale-[1.015] shadow-sm`
                    : 'border-white/70 bg-white/55 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <div className="flex items-center justify-between gap-3">
                <span className={['text-sm font-black', theme.text].join(' ')}>
                    {theme.cnName}
                </span>
                <span className="text-3xl font-black text-slate-800">{item.count}</span>
            </div>

            <div className="mt-3 h-2 overflow-hidden rounded-full bg-white/80">
                <div
                    className={[
                        'h-full rounded-full transition-all duration-300',
                        active ? theme.bg : 'bg-slate-200/70',
                    ].join(' ')}
                    style={{ width: `${Math.max(8, percent)}%` }}
                />
            </div>
        </button>
    )
}

type YearChangeRowProps = {
    row: {
        category: AcgnCategory
        currentCount: number
        previousCount: number
        diff: number
    }
    active: boolean
    maxAbs: number
    onClick: () => void
}

function YearChangeRow({ row, active, maxAbs, onClick }: YearChangeRowProps) {
    const theme = categoryTheme[row.category]
    const percent = Math.min(100, Math.round((Math.abs(row.diff) / Math.max(1, maxAbs)) * 100))
    const diffClassName = row.diff >= 0 ? 'text-emerald-600' : 'text-rose-600'

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'grid w-full grid-cols-[76px_1fr_70px] items-center gap-3 rounded-[20px] border p-3 text-left transition-all duration-200',
                active
                    ? `${theme.border} bg-white/72 shadow-sm`
                    : 'border-white/70 bg-white/50 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <span className={['font-black', theme.text].join(' ')}>{theme.cnName}</span>

            <div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-900/[0.06]">
                    <div
                        className={[
                            'h-full rounded-full transition-all duration-300',
                            row.diff >= 0 ? 'bg-emerald-200' : 'bg-rose-200',
                        ].join(' ')}
                        style={{ width: `${Math.max(8, percent)}%` }}
                    />
                </div>
                <p className="mt-1 text-[11px] text-slate-400">
                    今年 {row.currentCount} / 去年 {row.previousCount}
                </p>
            </div>

            <span className={['text-right text-lg font-black', diffClassName].join(' ')}>
                {formatChange(row.diff)}
            </span>
        </button>
    )
}

export default HomePage
