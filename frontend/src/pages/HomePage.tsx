import { useQuery } from '@tanstack/react-query'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
    enqueueBackgroundJob,
    registerBackgroundJobUi,
    waitForBackgroundJob,
} from '../api/jobApi'
import type { LlmAnalysisResponse } from '../api/llmApi'
import { ApiRequestError, getApiErrorMessage } from '../api/request'
import {
    getRecords,
    type UserSubjectRecordResponse,
} from '../api/recordApi'
import { getSubjectById } from '../api/subjectApi'
import CoverImage from '../components/common/CoverImage'
import { FeedbackBanner } from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { HOME_INSIGHT_CACHE_PREFIX } from '../constants/cacheKeys'
import { mapSubjectDetailToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    neutralPanelGradient,
    personalScoreTheme,
    quarterTheme,
} from '../theme/acgnTheme'
import {
    categoryOrder,
    getCurrentReleaseQuarter,
    getReleaseQuarterLabel,
    releaseQuarters,
    type AcgnCategory,
    type ReleaseQuarter,
} from '../types/common'
import type { SubjectCardView } from '../types/subject'
import {
    readJsonCache,
    removeCacheItem,
    writeJsonCache,
} from '../utils/localStorageCache'
import { formatRecordRelease, sortRecordsByScoreDesc } from '../utils/recordDisplay'
import { queryKeys } from '../query/queryKeys'
import { normalizeLlmAnalysisReport } from '../utils/llmAnalysisNormalize'

type CategoryCount = {
    category: AcgnCategory
    count: number
    previousCount?: number
    diff?: number
}

type QuarterCount = {
    quarter: ReleaseQuarter
    count: number
}

type InsightCacheInfo = {
    source: 'cache' | 'fresh'
    cachedAt: string
}

const homeInsightCacheVersion = 1

function buildHomeInsightCacheKey(
    year: number,
    quarter: ReleaseQuarter,
    category: AcgnCategory,
) {
    return [
        HOME_INSIGHT_CACHE_PREFIX,
        `v${homeInsightCacheVersion}`,
        year,
        quarter,
        category,
    ].join('::')
}

function readHomeInsightCache(cacheKey: string) {
    return readJsonCache(cacheKey, isHomeInsightCachePayload)
}

function writeHomeInsightCache(
    cacheKey: string,
    report: LlmAnalysisResponse,
    cachedAt: string,
) {
    writeJsonCache(cacheKey, {
        report,
        cachedAt,
    })
}

function removeHomeInsightCache(cacheKey: string) {
    removeCacheItem(cacheKey)
}

function isHomeInsightCachePayload(value: unknown): value is {
    report: LlmAnalysisResponse
    cachedAt: string
} {
    if (typeof value !== 'object' || value === null) {
        return false
    }

    const payload = value as {
        report?: unknown
        cachedAt?: unknown
    }

    return typeof payload.report === 'object'
        && payload.report !== null
        && typeof payload.cachedAt === 'string'
}

function formatHomeInsightTime(value: string) {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return '刚刚'
    }

    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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

    return [...releaseQuarters]
        .reverse()
        .find((quarter) => countRecords(records, year, category, quarter) > 0) ?? currentQuarter
}

function formatChange(value: number) {
    if (value > 0) {
        return `+${value}`
    }

    return String(value)
}

function getPreviousQuarterPosition(year: number, quarter: ReleaseQuarter) {
    if (quarter === 1) {
        return {
            year: year - 1,
            quarter: 4 as ReleaseQuarter,
        }
    }

    return {
        year,
        quarter: (quarter - 1) as ReleaseQuarter,
    }
}

function HomePage() {
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(getCurrentReleaseQuarter())
    const [selectedCategory, setSelectedCategory] = useState<AcgnCategory>('ANIME')
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [detailLoading, setDetailLoading] = useState(false)
    const [insightLoading, setInsightLoading] = useState(false)
    const [homeInsight, setHomeInsight] = useState<LlmAnalysisResponse | null>(null)
    const [insightCacheInfo, setInsightCacheInfo] = useState<InsightCacheInfo | null>(null)
    const [insightError, setInsightError] = useState<string | null>(null)
    const [error, setError] = useState<string | null>(null)
    const latestSelectionApplied = useRef(false)

    const selectedCategoryTheme = categoryTheme[selectedCategory]
    const selectedQuarterTheme = quarterTheme[selectedQuarter]
    const selectedQuarterGradient =
        selectedQuarterTheme?.panelGradient ?? neutralPanelGradient
    const homeInsightCacheKey = useMemo(
        () => buildHomeInsightCacheKey(selectedYear, selectedQuarter, selectedCategory),
        [selectedCategory, selectedQuarter, selectedYear],
    )

    const recordsQuery = useQuery({
        queryKey: queryKeys.recordsList(),
        queryFn: () => getRecords(),
    })

    const records = recordsQuery.data ?? []

    useEffect(() => {
        if (!records.length || latestSelectionApplied.current) {
            return
        }

        const latestPosition = getLatestRecordPosition(records)

        if (latestPosition) {
            setSelectedYear(latestPosition.year)
            setSelectedQuarter(latestPosition.quarter)
        }

        latestSelectionApplied.current = true
    }, [records])

    useEffect(() => {
        if (recordsQuery.isError) {
            setError(getApiErrorMessage(recordsQuery.error))
        } else {
            setError(null)
        }
    }, [recordsQuery.error, recordsQuery.isError])

    const listBlocking = recordsQuery.isPending && !recordsQuery.data

    const loadHomeInsight = useCallback(async (forceRefresh = false) => {
        const cached = forceRefresh ? null : readHomeInsightCache(homeInsightCacheKey)
        if (cached) {
            setHomeInsight(cached.report)
            setInsightCacheInfo({
                source: 'cache',
                cachedAt: cached.cachedAt,
            })
            setInsightError(null)
            return
        }

        setInsightLoading(true)
        setInsightError(null)

        try {
            const { jobId } = await enqueueBackgroundJob({
                jobType: 'LLM_ANALYSIS',
                payload: {
                    llmKind: 'HOME_INSIGHT',
                    year: selectedYear,
                    quarter: selectedQuarter,
                    category: selectedCategory,
                },
            })
            registerBackgroundJobUi(jobId, { silent: true, title: '首页智能洞察' })
            const res = await waitForBackgroundJob(jobId)
            if (res.status === 'FAILED' || res.status === 'CANCELLED') {
                throw new ApiRequestError(
                    res.status === 'CANCELLED' ? '任务已取消' : (res.errorMessage || '生成失败'),
                )
            }
            const root = JSON.parse(res.resultJson ?? '{}') as { llmAnalysis?: unknown }
            const normalized = normalizeLlmAnalysisReport(
                root.llmAnalysis as LlmAnalysisResponse,
                'HOME_INSIGHT',
                '首页智能洞察',
            )
            const cachedAt = new Date().toISOString()
            writeHomeInsightCache(homeInsightCacheKey, normalized, cachedAt)
            setHomeInsight(normalized)
            setInsightCacheInfo({
                source: 'fresh',
                cachedAt,
            })
        } catch (insightLoadError) {
            const fallback = readHomeInsightCache(homeInsightCacheKey)
            if (fallback) {
                setHomeInsight(fallback.report)
                setInsightCacheInfo({
                    source: 'cache',
                    cachedAt: fallback.cachedAt,
                })
                setInsightError(`${getApiErrorMessage(insightLoadError)}。已显示本地缓存解读。`)
            } else {
                setHomeInsight(null)
                setInsightCacheInfo(null)
                setInsightError(getApiErrorMessage(insightLoadError))
            }
        } finally {
            setInsightLoading(false)
        }
    }, [homeInsightCacheKey, selectedCategory, selectedQuarter, selectedYear])

    const clearHomeInsightCache = useCallback(() => {
        removeHomeInsightCache(homeInsightCacheKey)
        setHomeInsight(null)
        setInsightCacheInfo(null)
        setInsightError(null)
    }, [homeInsightCacheKey])

    useEffect(() => {
        void loadHomeInsight(false)
    }, [loadHomeInsight])

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
        return categoryOrder.map<CategoryCount>((category) => {
            const currentCount = countRecords(records, selectedYear, category)
            const previousCount = countRecords(records, selectedYear - 1, category)

            return {
                category,
                count: currentCount,
                previousCount,
                diff: currentCount - previousCount,
            }
        })
    }, [records, selectedYear])

    const quarterCategoryCounts = useMemo(() => {
        return categoryOrder.map<CategoryCount>((category) => ({
            category,
            count: countRecords(records, selectedYear, category, selectedQuarter),
        }))
    }, [records, selectedQuarter, selectedYear])

    const selectedCategoryQuarterCounts = useMemo(() => {
        return releaseQuarters.map<QuarterCount>((quarter) => ({
            quarter,
            count: countRecords(records, selectedYear, selectedCategory, quarter),
        }))
    }, [records, selectedCategory, selectedYear])

    const quarterChangeRows = useMemo(() => {
        const previousPosition = getPreviousQuarterPosition(selectedYear, selectedQuarter)

        return categoryOrder.map((category) => {
            const currentCount = countRecords(records, selectedYear, category, selectedQuarter)
            const previousCount = countRecords(
                records,
                previousPosition.year,
                category,
                previousPosition.quarter,
            )

            return {
                category,
                currentCount,
                previousCount,
                diff: currentCount - previousCount,
            }
        })
    }, [records, selectedQuarter, selectedYear])

    const scoreBandRows = useMemo(() => {
        const bands = [
            { label: '47-50', min: 47, max: 50 },
            { label: '40-46', min: 40, max: 46 },
            { label: '30-39', min: 30, max: 39 },
            { label: '0-29', min: 0, max: 29 },
        ]

        return bands.map((band) => ({
            ...band,
            count: selectedQuarterRecords.filter((record) => {
                const score = record.scoreValue ?? -1
                return score >= band.min && score <= band.max
            }).length,
        }))
    }, [selectedQuarterRecords])

    const topRecords = useMemo(() => {
        return sortRecordsByScoreDesc(
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
    const maxChangeAbs = Math.max(1, ...quarterChangeRows.map((item) => Math.abs(item.diff)))
    const maxScoreBandCount = Math.max(1, ...scoreBandRows.map((item) => item.count))
    const previousQuarterPosition = getPreviousQuarterPosition(selectedYear, selectedQuarter)

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
            setError(getApiErrorMessage(detailError))
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
                                    最新记录定位：{selectedYear} · {selectedQuarterTheme.displayName} · {selectedCategoryTheme.cnName}
                                </p>
                            </div>

                            <div className="grid min-w-[360px] gap-3 sm:grid-cols-3">
                                <MetricPill label="年度记录" value={totalYearRecords} />
                                <MetricPill label="当前类型" value={selectedCategoryTotal} />
                                <MetricPill label="本月本类" value={selectedQuarterTotal} />
                            </div>
                        </div>
                    </div>
                </GlassCard>

                {error ? (
                    <FeedbackBanner tone="error">{error}</FeedbackBanner>
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

                        <div className="mt-4 grid gap-3 lg:grid-cols-[1fr_1fr_0.9fr]">
                            <StatSummaryCard
                                label="同比增减"
                                value={formatChange(
                                    yearCategoryCounts.find((item) => item.category === selectedCategory)?.diff ?? 0,
                                )}
                                hint={`${selectedYear} 相比 ${selectedYear - 1}`}
                            />
                            <StatSummaryCard
                                label="当前类型占比"
                                value={`${Math.round(
                                    (selectedCategoryTotal / Math.max(1, totalYearRecords)) * 100,
                                )}%`}
                                hint="当前年份总记录占比"
                            />
                            <StatSummaryCard
                                label="当前季度记录"
                                value={String(selectedQuarterTotal)}
                                hint={`${selectedYear}${selectedQuarterTheme.displayName}`}
                            />
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
                                eyebrow="month distribution"
                                title={`${selectedCategoryTheme.cnName}月份分布`}
                                description="按一月、四月、七月、十月口径统计，点击有数据的月份切换。"
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
                                title={`${selectedQuarterTheme.displayName}评分前三`}
                                description={`${selectedYear} · ${selectedCategoryTheme.cnName} · 按个人评分排序`}
                            />

                            <TopScorePreview
                                loading={listBlocking}
                                records={topRecords}
                                category={selectedCategory}
                                onOpenDetail={handleOpenDetail}
                            />
                        </div>
                    </GlassCard>
                </div>

                <div className="grid gap-6 xl:grid-cols-[1.08fr_1fr_0.88fr]">
                    <GlassCard className="overflow-hidden">
                        <div
                            className={[
                                'h-full min-h-[380px] bg-gradient-to-br p-5 transition-all duration-300',
                                selectedQuarterGradient,
                            ].join(' ')}
                        >
                            <SectionHeader
                                eyebrow="quarter change"
                                title="相对上期变化"
                                description={`${selectedYear}${selectedQuarterTheme.displayName} 对比 ${previousQuarterPosition.year}${quarterTheme[previousQuarterPosition.quarter].displayName}`}
                            />

                            <div className="mt-4 grid gap-2.5">
                                {quarterChangeRows.map((item) => (
                                    <QuarterChangeRow
                                        key={item.category}
                                        row={item}
                                        active={item.category === selectedCategory}
                                        total={maxQuarterCategoryCount}
                                        maxAbs={maxChangeAbs}
                                        onClick={() => handleCategoryChange(item.category)}
                                    />
                                ))}
                            </div>
                        </div>
                    </GlassCard>

                    <ScoreBandPanel
                        rows={scoreBandRows}
                        maxCount={maxScoreBandCount}
                        category={selectedCategory}
                        quarter={selectedQuarter}
                    />

                    <HomeInsightPanel
                        report={homeInsight}
                        loading={insightLoading}
                        error={insightError}
                        cacheInfo={insightCacheInfo}
                        category={selectedCategory}
                        year={selectedYear}
                        quarter={selectedQuarter}
                        onRefresh={() => void loadHomeInsight(true)}
                        onClearCache={clearHomeInsightCache}
                    />
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

type StatSummaryCardProps = {
    label: string
    value: string
    hint: string
}

function StatSummaryCard({ label, value, hint }: StatSummaryCardProps) {
    return (
        <div className="rounded-[22px] border border-white/70 bg-white/58 px-4 py-3 shadow-sm backdrop-blur">
            <p className="text-xs font-semibold text-slate-400">{label}</p>
            <p className="mt-1 text-2xl font-black text-slate-800">{value}</p>
            <p className="mt-1 text-[11px] font-semibold text-slate-400">{hint}</p>
        </div>
    )
}

type HomeInsightPanelProps = {
    report: LlmAnalysisResponse | null
    loading: boolean
    error: string | null
    cacheInfo: InsightCacheInfo | null
    category: AcgnCategory
    year: number
    quarter: ReleaseQuarter
    onRefresh: () => void
    onClearCache: () => void
}

function HomeInsightPanel({
                              report,
                              loading,
                              error,
                              cacheInfo,
                              category,
                              year,
                              quarter,
                              onRefresh,
                              onClearCache,
                          }: HomeInsightPanelProps) {
    const theme = categoryTheme[category]
    const visibleSections = (report?.sections ?? [])
        .filter((section) => {
            const title = section.title.toLowerCase()
            return !['统计', '数量', '占比', '同比', '变化', '记录'].some((keyword) =>
                title.includes(keyword),
            )
        })
        .slice(0, 2)
    const visibleRecommendation = (report?.recommendations ?? []).find(
        (item) => item.reason && item.reason.trim().length > 0,
    )

    return (
        <GlassCard className="overflow-hidden">
            <div className="flex h-full min-h-[380px] flex-col bg-gradient-to-br p-5 transition-all duration-300">
                <SectionHeader
                    eyebrow="ai insight"
                    title="智能洞察"
                    description={`${year} · ${getReleaseQuarterLabel(quarter)} · ${theme.cnName}`}
                />

                <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
                    <span className="rounded-full border border-white/75 bg-white/62 px-3 py-1.5 text-[11px] font-semibold text-slate-400">
                        {cacheInfo
                            ? cacheInfo.source === 'cache'
                                ? `缓存 ${formatHomeInsightTime(cacheInfo.cachedAt)}`
                                : `已更新 ${formatHomeInsightTime(cacheInfo.cachedAt)}`
                            : '未缓存'}
                    </span>
                    <div className="flex flex-wrap gap-2">
                        <button
                            type="button"
                            onClick={onRefresh}
                            disabled={loading}
                            className={[
                                'rounded-full border px-3 py-1.5 text-[11px] font-black transition-all duration-200',
                                loading
                                    ? 'cursor-not-allowed border-white/60 bg-white/45 text-slate-300'
                                    : 'border-sky-200 bg-white/70 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
                            ].join(' ')}
                        >
                            {loading ? '生成中' : '重新生成'}
                        </button>
                        <button
                            type="button"
                            onClick={onClearCache}
                            disabled={loading || !cacheInfo}
                            className={[
                                'rounded-full border px-3 py-1.5 text-[11px] font-black transition-all duration-200',
                                loading || !cacheInfo
                                    ? 'cursor-not-allowed border-white/60 bg-white/45 text-slate-300'
                                    : 'border-slate-200 bg-white/70 text-slate-600 hover:-translate-y-0.5 hover:bg-white',
                            ].join(' ')}
                        >
                            清除缓存
                        </button>
                    </div>
                </div>

                {loading ? (
                    <div className="mt-4 space-y-3">
                        <div className="h-20 animate-pulse rounded-[22px] bg-white/55" />
                        <div className="h-16 animate-pulse rounded-[22px] bg-white/45" />
                        <div className="h-16 animate-pulse rounded-[22px] bg-white/45" />
                    </div>
                ) : error ? (
                    <div className="mt-4 rounded-[24px] border border-rose-100 bg-white/62 p-4 text-sm leading-6 text-rose-600">
                        {error}
                    </div>
                ) : report ? (
                    <div className="mt-4 flex flex-1 flex-col gap-3">
                        <div className="rounded-[24px] border border-white/75 bg-white/62 p-4 shadow-sm backdrop-blur">
                            <p className="text-sm leading-6 text-slate-500">
                                {report.summary}
                            </p>
                        </div>

                        <div className="flex-1 space-y-2.5 overflow-y-auto pr-1">
                            {visibleSections.map((section) => (
                                <article
                                    key={section.title}
                                    className="rounded-[22px] border border-white/70 bg-white/52 p-3.5"
                                >
                                    <h4 className="text-sm font-black text-slate-800">
                                        {section.title}
                                    </h4>
                                    <p className="mt-1 text-xs leading-5 text-slate-500">
                                        {section.content}
                                    </p>
                                </article>
                            ))}

                            {visibleRecommendation ? (
                                <div className="rounded-[22px] border border-white/70 bg-white/52 p-3.5">
                                    <h4 className="text-sm font-black text-slate-800">行动建议</h4>
                                    <p className="mt-1 text-xs leading-5 text-slate-500">
                                        {visibleRecommendation.reason}
                                    </p>
                                </div>
                            ) : null}
                        </div>
                    </div>
                ) : (
                    <div className="mt-4 rounded-[24px] border border-dashed border-slate-200 bg-white/45 p-4 text-sm leading-6 text-slate-400">
                        当前暂无首页解读。
                    </div>
                )}
            </div>
        </GlassCard>
    )
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
                <span className="text-right">
                    <span className={['block text-3xl font-black', theme.text].join(' ')}>
                        {item.count}
                    </span>
                    <span
                        className={[
                            'mt-0.5 block text-xs font-black',
                            (item.diff ?? 0) >= 0 ? 'text-emerald-600' : 'text-rose-600',
                        ].join(' ')}
                    >
                        {formatChange(item.diff ?? 0)}
                    </span>
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
        <div className="mt-5 flex h-[190px] items-end gap-4 overflow-hidden rounded-[28px] border border-white/60 bg-white/28 px-4 pb-3 pt-4">
            {values.map((item) => {
                const theme = quarterTheme[item.quarter]
                const selected = selectedQuarter === item.quarter
                const disabled = item.count === 0
                const height = item.count === 0 ? 14 : Math.min(118, 24 + (item.count / maxCount) * 94)

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
                                'rounded-full px-3 py-1 text-xs font-black',
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
                {formatRecordRelease(record)}
            </p>
        </button>
    )
}

type QuarterChangeRowProps = {
    row: {
        category: AcgnCategory
        currentCount: number
        previousCount: number
        diff: number
    }
    active: boolean
    total: number
    maxAbs: number
    onClick: () => void
}

function QuarterChangeRow({ row, active, total, maxAbs, onClick }: QuarterChangeRowProps) {
    const theme = categoryTheme[row.category]
    const percent = Math.min(100, Math.round((Math.abs(row.diff) / Math.max(1, maxAbs)) * 100))
    const countPercent = Math.min(100, Math.round((row.currentCount / Math.max(1, total)) * 100))
    const diffClassName = row.diff >= 0 ? 'text-emerald-600' : 'text-rose-600'

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'grid w-full grid-cols-[76px_1fr_78px] items-center gap-3 rounded-[20px] border p-3 text-left transition-all duration-200',
                active
                    ? `${theme.border} bg-white/72 shadow-sm`
                    : 'border-white/70 bg-white/50 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <span className={['font-black', theme.text].join(' ')}>{theme.cnName}</span>

            <div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-900/[0.06]">
                    <div
                        className={['h-full rounded-full transition-all duration-300', theme.bg].join(' ')}
                        style={{ width: `${Math.max(8, countPercent)}%` }}
                    />
                </div>
                <div className="mt-1 h-1.5 overflow-hidden rounded-full bg-slate-900/[0.04]">
                    <div
                        className={[
                            'h-full rounded-full transition-all duration-300',
                            row.diff >= 0 ? 'bg-emerald-200' : 'bg-rose-200',
                        ].join(' ')}
                        style={{ width: `${Math.max(8, percent)}%` }}
                    />
                </div>
                <p className="mt-1 text-[11px] text-slate-400">
                    本期 {row.currentCount} / 上期 {row.previousCount}
                </p>
            </div>

            <div className="text-right">
                <span className="block text-lg font-black text-slate-800">{row.currentCount}</span>
                <span className={['block text-xs font-black', diffClassName].join(' ')}>
                    {formatChange(row.diff)}
                </span>
            </div>
        </button>
    )
}

type ScoreBandPanelProps = {
    rows: Array<{
        label: string
        count: number
    }>
    maxCount: number
    category: AcgnCategory
    quarter: ReleaseQuarter
}

function ScoreBandPanel({ rows, maxCount, category, quarter }: ScoreBandPanelProps) {
    const theme = categoryTheme[category]
    const quarterDisplayName = quarterTheme[quarter].displayName

    return (
        <GlassCard className="overflow-hidden">
            <div
                className={[
                    'h-full min-h-[380px] bg-gradient-to-br p-5 transition-all duration-300',
                    theme.softGradient,
                ].join(' ')}
            >
                <SectionHeader
                    eyebrow="score bands"
                    title="分数区间分布"
                    description={`${quarterDisplayName} · ${theme.cnName} · 个人评分`}
                />

                <div className="mt-4 grid gap-3">
                    {rows.map((row) => {
                        const percent = Math.min(100, Math.round((row.count / maxCount) * 100))

                        return (
                            <div
                                key={row.label}
                                className="rounded-[22px] border border-white/70 bg-white/58 p-3 shadow-sm"
                            >
                                <div className="flex items-center justify-between gap-3">
                                    <span className={['text-sm font-black', theme.text].join(' ')}>
                                        {row.label}
                                    </span>
                                    <span className="text-lg font-black text-slate-800">
                                        {row.count}
                                    </span>
                                </div>
                                <div className="mt-2 h-2 overflow-hidden rounded-full bg-white/80">
                                    <div
                                        className={['h-full rounded-full transition-all duration-300', theme.bg].join(' ')}
                                        style={{ width: `${Math.max(8, percent)}%` }}
                                    />
                                </div>
                            </div>
                        )
                    })}
                </div>
            </div>
        </GlassCard>
    )
}

export default HomePage
