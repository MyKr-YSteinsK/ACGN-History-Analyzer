import { useCallback, useEffect, useMemo, useState } from 'react'
import {
    getLlmStatus,
    getYearlyAnalysis,
    type LlmAnalysisResponse,
    type LlmStatusResponse,
} from '../api/llmApi'
import { getApiErrorMessage } from '../api/request'
import {
    EmptyState,
    FeedbackBanner,
} from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import { categoryTheme, neutralPanelGradient } from '../theme/acgnTheme'
import { categoryOrder, type AcgnCategory } from '../types/common'

type CategoryFilter = AcgnCategory | 'ALL'

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020]

function AnalysisPage() {
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL')
    const [status, setStatus] = useState<LlmStatusResponse | null>(null)
    const [report, setReport] = useState<LlmAnalysisResponse | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const activeTheme =
        selectedCategory === 'ALL'
            ? null
            : categoryTheme[selectedCategory]
    const panelGradient = activeTheme?.softGradient ?? neutralPanelGradient

    const scopeText = useMemo(() => {
        const categoryName =
            selectedCategory === 'ALL'
                ? '全部类型'
                : categoryTheme[selectedCategory].cnName

        return `${selectedYear} 年 · ${categoryName}`
    }, [selectedCategory, selectedYear])

    useEffect(() => {
        async function loadStatus() {
            try {
                setStatus(await getLlmStatus())
            } catch (statusError) {
                setError(getApiErrorMessage(statusError, 'AI 配置状态加载失败'))
            }
        }

        void loadStatus()
    }, [])

    const generateReport = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const response = await getYearlyAnalysis({
                year: selectedYear,
                category: selectedCategory === 'ALL' ? undefined : selectedCategory,
            })
            setReport(response)
        } catch (generateError) {
            setReport(null)
            setError(getApiErrorMessage(generateError, '智能分析生成失败'))
        } finally {
            setLoading(false)
        }
    }, [selectedCategory, selectedYear])

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void generateReport()
    }, [generateReport])

    return (
        <div className="space-y-6 content-enter">
            <PageHero
                eyebrow="AI ANALYSIS"
                title="智能分析"
                description="第三阶段先跑通 AI 功能形态：后端统一接口、Mock 降级、结构化报告、引用依据。现在不接真实模型，演示更稳。"
            />

            <GlassCard className="overflow-hidden">
                <div
                    className={[
                        'bg-gradient-to-br p-6 transition-all duration-300',
                        panelGradient,
                    ].join(' ')}
                >
                    <div className="flex flex-wrap items-start justify-between gap-5">
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                                analysis control
                            </p>
                            <h2 className="mt-2 text-2xl font-black text-slate-800">
                                年度智能总结
                            </h2>
                            <p className="mt-2 text-sm text-slate-500">
                                当前范围：{scopeText}
                            </p>
                        </div>

                        <div className="flex flex-wrap items-center gap-2">
                            <StatusPill label="模式" value={status?.mockMode ? 'Mock' : 'Online'} />
                            <StatusPill label="模型" value={status?.model ?? '未加载'} />
                        </div>
                    </div>

                    <div className="mt-6 grid gap-4 xl:grid-cols-[360px_1fr_150px]">
                        <div>
                            <p className="mb-2 text-xs font-semibold text-slate-400">年份</p>
                            <div className="flex flex-wrap gap-2">
                                {yearOptions.map((year) => {
                                    const selected = selectedYear === year

                                    return (
                                        <button
                                            key={year}
                                            type="button"
                                            onClick={() => setSelectedYear(year)}
                                            className={[
                                                'rounded-2xl border px-4 py-2.5 text-sm font-black transition-all duration-200',
                                                selected
                                                    ? 'border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                                    : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
                                            ].join(' ')}
                                        >
                                            {year}
                                        </button>
                                    )
                                })}
                            </div>
                        </div>

                        <div>
                            <p className="mb-2 text-xs font-semibold text-slate-400">类型</p>
                            <div className="grid gap-2 sm:grid-cols-5">
                                <CategoryButton
                                    label="全部"
                                    active={selectedCategory === 'ALL'}
                                    onClick={() => setSelectedCategory('ALL')}
                                />
                                {categoryOrder.map((category) => (
                                    <CategoryButton
                                        key={category}
                                        category={category}
                                        label={categoryTheme[category].cnName}
                                        active={selectedCategory === category}
                                        onClick={() => setSelectedCategory(category)}
                                    />
                                ))}
                            </div>
                        </div>

                        <div className="flex items-end">
                            <button
                                type="button"
                                onClick={() => void generateReport()}
                                disabled={loading}
                                className={[
                                    'h-12 w-full rounded-2xl border px-4 text-sm font-black shadow-sm transition-all duration-200',
                                    loading
                                        ? 'cursor-wait border-white/55 bg-white/45 text-slate-300'
                                        : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
                                ].join(' ')}
                            >
                                {loading ? '生成中' : '生成分析'}
                            </button>
                        </div>
                    </div>
                </div>
            </GlassCard>

            {error ? <FeedbackBanner tone="error">{error}</FeedbackBanner> : null}

            {loading ? (
                <ReportSkeleton />
            ) : report ? (
                <AnalysisReport report={report} />
            ) : (
                <EmptyState
                    title="还没有分析报告"
                    description="选择年份和类型后点击生成分析，系统会返回一份结构化 Mock 报告。"
                />
            )}
        </div>
    )
}

type StatusPillProps = {
    label: string
    value: string
}

function StatusPill({ label, value }: StatusPillProps) {
    return (
        <div className="rounded-2xl border border-white/70 bg-white/58 px-4 py-2 shadow-sm backdrop-blur">
            <p className="text-[11px] font-semibold text-slate-400">{label}</p>
            <p className="mt-0.5 text-sm font-black text-slate-700">{value}</p>
        </div>
    )
}

type CategoryButtonProps = {
    label: string
    active: boolean
    category?: AcgnCategory
    onClick: () => void
}

function CategoryButton({ label, active, category, onClick }: CategoryButtonProps) {
    const theme = category ? categoryTheme[category] : null

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-2xl border px-4 py-3 text-sm font-black transition-all duration-200',
                active && theme
                    ? `${theme.border} ${theme.bg} ${theme.text} scale-[1.02] shadow-sm`
                    : active
                      ? 'scale-[1.02] border-slate-200 bg-white/90 text-slate-700 shadow-sm'
                      : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
            ].join(' ')}
        >
            {label}
        </button>
    )
}

type AnalysisReportProps = {
    report: LlmAnalysisResponse
}

function AnalysisReport({ report }: AnalysisReportProps) {
    return (
        <div className="grid gap-6 xl:grid-cols-[1fr_360px]">
            <GlassCard className="overflow-hidden">
                <div className="bg-gradient-to-br from-white/70 via-sky-50/60 to-violet-50/55 p-6">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                        <div>
                            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                                structured report
                            </p>
                            <h3 className="mt-2 text-2xl font-black text-slate-800">
                                {report.title}
                            </h3>
                            <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-500">
                                {report.summary}
                            </p>
                        </div>

                        <span className="rounded-full border border-rose-200 bg-rose-50/85 px-4 py-2 text-xs font-black text-rose-600">
                            {report.mockMode ? 'MOCK' : 'ONLINE'}
                        </span>
                    </div>

                    <div className="mt-5 flex flex-wrap gap-2">
                        {report.keywords.map((keyword) => (
                            <span
                                key={keyword}
                                className="rounded-full border border-white/75 bg-white/65 px-3 py-1.5 text-xs font-semibold text-slate-500"
                            >
                                #{keyword}
                            </span>
                        ))}
                    </div>

                    <div className="mt-6 grid gap-4">
                        {report.sections.map((section) => (
                            <article
                                key={section.title}
                                className="rounded-[24px] border border-white/75 bg-white/62 p-5 shadow-sm backdrop-blur"
                            >
                                <h4 className="text-base font-black text-slate-800">
                                    {section.title}
                                </h4>
                                <p className="mt-2 text-sm leading-7 text-slate-500">
                                    {section.content}
                                </p>
                            </article>
                        ))}
                    </div>
                </div>
            </GlassCard>

            <div className="space-y-6">
                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/70 to-emerald-50/60 p-5">
                        <h3 className="text-lg font-black text-slate-800">引用依据</h3>
                        <div className="mt-4 space-y-2">
                            {report.evidence.map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-white/75 bg-white/62 px-4 py-3 text-sm leading-6 text-slate-500"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </div>
                </GlassCard>

                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/70 to-amber-50/70 p-5">
                        <h3 className="text-lg font-black text-slate-800">边界说明</h3>
                        <div className="mt-4 space-y-2">
                            {report.warnings.map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-amber-100 bg-white/62 px-4 py-3 text-sm leading-6 text-amber-700"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </div>
                </GlassCard>
            </div>
        </div>
    )
}

function ReportSkeleton() {
    return (
        <GlassCard className="overflow-hidden">
            <div className="space-y-4 bg-white/55 p-6">
                <div className="h-7 w-72 animate-pulse rounded-full bg-slate-200/70" />
                <div className="h-20 animate-pulse rounded-[24px] bg-slate-200/60" />
                <div className="grid gap-4 md:grid-cols-3">
                    {Array.from({ length: 3 }, (_, index) => (
                        <div
                            key={index}
                            className="h-36 animate-pulse rounded-[24px] bg-slate-200/55"
                        />
                    ))}
                </div>
            </div>
        </GlassCard>
    )
}

export default AnalysisPage
