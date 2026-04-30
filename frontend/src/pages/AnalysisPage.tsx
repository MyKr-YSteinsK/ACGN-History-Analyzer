import { useCallback, useEffect, useMemo, useState } from 'react'
import {
    getCollectionBacklogAnalysis,
    getComprehensiveReportAnalysis,
    getLlmStatus,
    getPreferenceContradictionAnalysis,
    getQuestionAnswerAnalysis,
    getQuarterlyAnalysis,
    getRecommendationAnalysis,
    getScoreStructureAnalysis,
    getTagPreferenceAnalysis,
    getTasteProfileAnalysis,
    getTypePreferenceAnalysis,
    getYearlyAnalysis,
    type LlmAnalysisResponse,
    type LlmRecommendationResponse,
    type LlmStatusResponse,
} from '../api/llmApi'
import { getApiErrorMessage } from '../api/request'
import {
    EmptyState,
    FeedbackBanner,
} from '../components/common/FeedbackState'
import CoverImage from '../components/common/CoverImage'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import { categoryTheme, neutralPanelGradient } from '../theme/acgnTheme'
import { categoryOrder, getReleaseQuarterLabel, type AcgnCategory, type ReleaseQuarter } from '../types/common'

type CategoryFilter = AcgnCategory | 'ALL'
type AnalysisMode =
    | 'YEARLY'
    | 'QUARTERLY'
    | 'TYPE_PREFERENCE'
    | 'SCORE_STRUCTURE'
    | 'TAG_PREFERENCE'
    | 'RECOMMENDATION'
    | 'COLLECTION_BACKLOG'
    | 'TASTE_PROFILE'
    | 'PREFERENCE_CONTRADICTION'
    | 'COMPREHENSIVE_REPORT'
    | 'QUESTION_ANSWER'

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020]
const quarterOptions: ReleaseQuarter[] = [1, 2, 3, 4]

const analysisModes: Array<{
    value: AnalysisMode
    label: string
    description: string
}> = [
    {
        value: 'YEARLY',
        label: '年度总结',
        description: '按年份观察整体记录、评分和标签线索',
    },
    {
        value: 'QUARTERLY',
        label: '月份档期总结',
        description: '聚焦一月 / 四月 / 七月 / 十月档期',
    },
    {
        value: 'TYPE_PREFERENCE',
        label: '类型偏好',
        description: '长期分析动画、漫画、游戏或小说偏好',
    },
    {
        value: 'SCORE_STRUCTURE',
        label: '评分结构',
        description: '观察高分段、中分段和低分段分布',
    },
    {
        value: 'TAG_PREFERENCE',
        label: '标签偏好',
        description: '区分经常看、真正喜欢和风险题材',
    },
    {
        value: 'RECOMMENDATION',
        label: '智能推荐',
        description: '从作品库和收藏夹生成未记录候选',
    },
    {
        value: 'COLLECTION_BACKLOG',
        label: '补坑建议',
        description: '优先整理想看和搁置作品',
    },
    {
        value: 'TASTE_PROFILE',
        label: '口味画像',
        description: '把长期记录整理成个人 ACGN 偏好画像',
    },
    {
        value: 'PREFERENCE_CONTRADICTION',
        label: '偏好矛盾',
        description: '发现看得多但不稳、低频但高命中的方向',
    },
    {
        value: 'COMPREHENSIVE_REPORT',
        label: '一键报告',
        description: '整合年度统计、画像、矛盾和推荐候选',
    },
    {
        value: 'QUESTION_ANSWER',
        label: '自然问答',
        description: '用一句话查询记录、评分、标签或补完建议',
    },
]

function AnalysisPage() {
    const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('YEARLY')
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(1)
    const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL')
    const [question, setQuestion] = useState('2025 年我有哪些高分动画？')
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

        if (analysisMode === 'TYPE_PREFERENCE') {
            return `${categoryName} · 长期偏好`
        }

        if (analysisMode === 'SCORE_STRUCTURE') {
            return `${categoryName} · 评分结构`
        }

        if (analysisMode === 'TAG_PREFERENCE') {
            return `${categoryName} · 标签偏好`
        }

        if (analysisMode === 'RECOMMENDATION') {
            return `${categoryName} · 智能推荐`
        }

        if (analysisMode === 'COLLECTION_BACKLOG') {
            return `${categoryName} · 收藏夹补坑`
        }

        if (analysisMode === 'TASTE_PROFILE') {
            return `${categoryName} · 口味画像`
        }

        if (analysisMode === 'PREFERENCE_CONTRADICTION') {
            return `${categoryName} · 偏好矛盾`
        }

        if (analysisMode === 'COMPREHENSIVE_REPORT') {
            return `${selectedYear} 年 · ${categoryName} · 一键报告`
        }

        if (analysisMode === 'QUESTION_ANSWER') {
            return '自然语言问答 · Mock 规则识别'
        }

        if (analysisMode === 'QUARTERLY') {
            return `${selectedYear} 年 · ${getReleaseQuarterLabel(selectedQuarter)} · ${categoryName}`
        }

        return `${selectedYear} 年 · ${categoryName}`
    }, [analysisMode, selectedCategory, selectedQuarter, selectedYear])

    const activeMode = analysisModes.find((item) => item.value === analysisMode) ?? analysisModes[0]
    const usesYearFilter =
        analysisMode === 'YEARLY'
        || analysisMode === 'QUARTERLY'
        || analysisMode === 'COMPREHENSIVE_REPORT'
    const usesQuarterFilter = analysisMode === 'QUARTERLY'
    const controlGridClassName = usesQuarterFilter
        ? 'mt-6 grid gap-4 xl:grid-cols-[260px_360px_1fr_150px]'
        : usesYearFilter
          ? 'mt-6 grid gap-4 xl:grid-cols-[320px_1fr_150px]'
          : 'mt-6 grid gap-4 xl:grid-cols-[1fr_150px]'

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
        if (analysisMode === 'QUESTION_ANSWER' && !question.trim()) {
            setReport(null)
            setError('请先输入一个想问的问题')
            return
        }

        setLoading(true)
        setError(null)

        try {
            const category =
                selectedCategory === 'ALL' ? undefined : selectedCategory
            const response =
                analysisMode === 'QUESTION_ANSWER'
                    ? await getQuestionAnswerAnalysis({
                          question: question.trim(),
                      })
                    : analysisMode === 'QUARTERLY'
                    ? await getQuarterlyAnalysis({
                          year: selectedYear,
                          quarter: selectedQuarter,
                          category,
                      })
                    : analysisMode === 'TYPE_PREFERENCE'
                      ? await getTypePreferenceAnalysis({
                            category: selectedCategory === 'ALL' ? 'ANIME' : selectedCategory,
                        })
                      : analysisMode === 'SCORE_STRUCTURE'
                        ? await getScoreStructureAnalysis({
                              category,
                          })
                        : analysisMode === 'TAG_PREFERENCE'
                          ? await getTagPreferenceAnalysis({
                                category,
                            })
                          : analysisMode === 'RECOMMENDATION'
                            ? await getRecommendationAnalysis({
                                  category,
                              })
                            : analysisMode === 'COLLECTION_BACKLOG'
                              ? await getCollectionBacklogAnalysis({
                                    category,
                                })
                              : analysisMode === 'TASTE_PROFILE'
                                ? await getTasteProfileAnalysis({
                                      category,
                                  })
                                : analysisMode === 'PREFERENCE_CONTRADICTION'
                                  ? await getPreferenceContradictionAnalysis({
                                        category,
                                    })
                                  : analysisMode === 'COMPREHENSIVE_REPORT'
                                    ? await getComprehensiveReportAnalysis({
                                          year: selectedYear,
                                          category,
                                      })
                                    : await getYearlyAnalysis({
                                          year: selectedYear,
                                          category,
                                      })
            setReport(response)
        } catch (generateError) {
            setReport(null)
            setError(getApiErrorMessage(generateError, '智能分析生成失败'))
        } finally {
            setLoading(false)
        }
    }, [analysisMode, question, selectedCategory, selectedQuarter, selectedYear])

    function handleModeChange(mode: AnalysisMode) {
        setAnalysisMode(mode)

        if (mode === 'QUESTION_ANSWER') {
            setReport(null)
            setError(null)
        }

        if (mode === 'TYPE_PREFERENCE' && selectedCategory === 'ALL') {
            setSelectedCategory('ANIME')
        }
    }

    useEffect(() => {
        if (analysisMode === 'QUESTION_ANSWER') {
            return
        }

        // eslint-disable-next-line react-hooks/set-state-in-effect
        void generateReport()
    }, [analysisMode, generateReport])

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
                                {activeMode.label}
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

                    <div className="mt-6 grid gap-3 md:grid-cols-3 xl:grid-cols-5 2xl:grid-cols-6">
                        {analysisModes.map((mode) => {
                            const selected = analysisMode === mode.value

                            return (
                                <button
                                    key={mode.value}
                                    type="button"
                                    onClick={() => handleModeChange(mode.value)}
                                    className={[
                                        'rounded-[24px] border p-4 text-left transition-all duration-200',
                                        selected
                                            ? 'scale-[1.01] border-sky-200 bg-white/82 text-sky-700 shadow-sm'
                                            : 'border-white/75 bg-white/50 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75',
                                    ].join(' ')}
                                >
                                    <p className="text-sm font-black">{mode.label}</p>
                                    <p className="mt-1 text-xs leading-5 text-slate-400">
                                        {mode.description}
                                    </p>
                                </button>
                            )
                        })}
                    </div>

                    {analysisMode === 'QUESTION_ANSWER' ? (
                        <QuestionAnswerControl
                            question={question}
                            loading={loading}
                            onQuestionChange={setQuestion}
                            onSubmit={() => void generateReport()}
                        />
                    ) : (
                    <div className={controlGridClassName}>
                        {usesYearFilter ? (
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
                        ) : null}

                        {usesQuarterFilter ? (
                            <div>
                                <p className="mb-2 text-xs font-semibold text-slate-400">月份</p>
                                <div className="grid grid-cols-4 gap-2">
                                    {quarterOptions.map((quarter) => {
                                        const selected = selectedQuarter === quarter

                                        return (
                                            <button
                                                key={quarter}
                                                type="button"
                                                onClick={() => setSelectedQuarter(quarter)}
                                                className={[
                                                    'rounded-2xl border px-4 py-2.5 text-sm font-black transition-all duration-200',
                                                    selected
                                                        ? 'border-cyan-200 bg-cyan-100/85 text-cyan-700 shadow-sm'
                                                        : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
                                                ].join(' ')}
                                            >
                                                {getReleaseQuarterLabel(quarter)}
                                            </button>
                                        )
                                    })}
                                </div>
                            </div>
                        ) : null}

                        <div>
                            <p className="mb-2 text-xs font-semibold text-slate-400">类型</p>
                            <div className="grid gap-2 sm:grid-cols-5">
                                {analysisMode !== 'TYPE_PREFERENCE' ? (
                                    <CategoryButton
                                        label="全部"
                                        active={selectedCategory === 'ALL'}
                                        onClick={() => setSelectedCategory('ALL')}
                                    />
                                ) : null}
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
                    )}
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
                    description="选择分析类型和范围后点击生成分析，系统会返回一份结构化 Mock 报告。"
                />
            )}
        </div>
    )
}

type QuestionAnswerControlProps = {
    question: string
    loading: boolean
    onQuestionChange: (value: string) => void
    onSubmit: () => void
}

const questionExamples = [
    '2025 年我有哪些高分动画？',
    '我是不是更喜欢日常标签？',
    '想看里有什么适合优先补？',
]

function QuestionAnswerControl({
    question,
    loading,
    onQuestionChange,
    onSubmit,
}: QuestionAnswerControlProps) {
    return (
        <div className="mt-6 rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
            <div className="grid gap-4 xl:grid-cols-[1fr_150px]">
                <div>
                    <p className="mb-2 text-xs font-semibold text-slate-400">问题</p>
                    <textarea
                        value={question}
                        onChange={(event) => onQuestionChange(event.target.value)}
                        rows={2}
                        placeholder="比如：2025 年我有哪些高分动画？"
                        className="min-h-[88px] w-full resize-none rounded-[24px] border border-white/80 bg-white/70 px-4 py-3 text-sm font-semibold leading-6 text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
                    />
                </div>

                <div className="flex items-end">
                    <button
                        type="button"
                        onClick={onSubmit}
                        disabled={loading || !question.trim()}
                        className={[
                            'h-12 w-full rounded-2xl border px-4 text-sm font-black shadow-sm transition-all duration-200',
                            loading || !question.trim()
                                ? 'cursor-not-allowed border-white/55 bg-white/45 text-slate-300'
                                : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
                        ].join(' ')}
                    >
                        {loading ? '生成中' : '提问'}
                    </button>
                </div>
            </div>

            <div className="mt-3 flex flex-wrap gap-2">
                {questionExamples.map((example) => (
                    <button
                        key={example}
                        type="button"
                        onClick={() => onQuestionChange(example)}
                        className="rounded-full border border-white/75 bg-white/68 px-3 py-1.5 text-xs font-semibold text-slate-500 transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        {example}
                    </button>
                ))}
            </div>
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

                    {report.recommendations?.length ? (
                        <RecommendationGrid recommendations={report.recommendations} />
                    ) : null}
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

type RecommendationGridProps = {
    recommendations: LlmRecommendationResponse[]
}

function RecommendationGrid({ recommendations }: RecommendationGridProps) {
    return (
        <section className="mt-6 rounded-[28px] border border-white/75 bg-white/50 p-5 shadow-sm backdrop-blur">
            <div className="flex flex-wrap items-end justify-between gap-3">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        recommendation candidates
                    </p>
                    <h4 className="mt-2 text-lg font-black text-slate-800">
                        推荐候选
                    </h4>
                </div>
                <span className="rounded-full border border-white/75 bg-white/70 px-3 py-1.5 text-xs font-black text-slate-500">
                    {recommendations.length} 部
                </span>
            </div>

            <div className="mt-4 grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
                {recommendations.map((item) => (
                    <RecommendationCard key={item.subjectId} item={item} />
                ))}
            </div>
        </section>
    )
}

type RecommendationCardProps = {
    item: LlmRecommendationResponse
}

function RecommendationCard({ item }: RecommendationCardProps) {
    const theme = categoryTheme[item.category]

    return (
        <article
            className={[
                'overflow-hidden rounded-[24px] border bg-gradient-to-br shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md',
                theme.border,
                theme.softGradient,
            ].join(' ')}
        >
            <div className="flex gap-4 p-4">
                <div className="h-32 w-[88px] shrink-0 overflow-hidden rounded-[18px] border border-white/80 bg-white/55 shadow-sm">
                    <CoverImage
                        src={item.coverUrl}
                        alt={item.title}
                        className="h-full w-full object-cover"
                        fallback={
                            <div
                                className={[
                                    'flex h-full w-full items-center justify-center bg-gradient-to-br text-3xl font-black',
                                    theme.softGradient,
                                    theme.text,
                                ].join(' ')}
                            >
                                {theme.letter}
                            </div>
                        }
                    />
                </div>

                <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                            <p className="line-clamp-2 text-base font-black leading-snug text-slate-800">
                                {item.title}
                            </p>
                            {item.franchiseName ? (
                                <p className="mt-1 truncate text-xs font-semibold text-slate-400">
                                    {item.franchiseName}
                                </p>
                            ) : null}
                        </div>

                        <span
                            className={[
                                'shrink-0 rounded-full border px-2.5 py-1 text-[11px] font-black',
                                getPriorityClassName(item.priority),
                            ].join(' ')}
                        >
                            {getPriorityLabel(item.priority)}
                        </span>
                    </div>

                    <div className="mt-3 flex flex-wrap items-center gap-2">
                        <span
                            className={[
                                'rounded-full border px-2.5 py-1 text-xs font-black',
                                theme.border,
                                theme.bg,
                                theme.text,
                            ].join(' ')}
                        >
                            {categoryTheme[item.category].cnName}
                        </span>
                        <span className="rounded-full border border-emerald-200 bg-emerald-50/85 px-2.5 py-1 text-xs font-black text-emerald-700">
                            {getSourceLabel(item.source)}
                        </span>
                        <span className="rounded-full border border-rose-200 bg-rose-50/85 px-2.5 py-1 text-xs font-black text-rose-700">
                            匹配 {item.matchScore ?? 0}
                        </span>
                        {item.platformScore !== null && item.platformScore !== undefined ? (
                            <span className="rounded-full border border-amber-200 bg-amber-50/85 px-2.5 py-1 text-xs font-black text-amber-700">
                                {Number(item.platformScore).toFixed(1)}
                            </span>
                        ) : null}
                    </div>
                </div>
            </div>

            <div className="border-t border-white/70 bg-white/48 px-4 py-3">
                <div className="flex flex-wrap gap-1.5">
                    {item.tags.slice(0, 4).map((tag) => (
                        <span
                            key={tag}
                            className="rounded-full bg-white/75 px-2.5 py-1 text-[11px] font-semibold text-slate-500"
                        >
                            #{tag}
                        </span>
                    ))}
                </div>
                <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-500">
                    {item.reason}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-slate-400">
                    {item.evidence}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-amber-700/85">
                    {item.risk}
                </p>
            </div>
        </article>
    )
}

function getSourceLabel(source: LlmRecommendationResponse['source']) {
    if (source === 'WANT') {
        return '想看'
    }

    if (source === 'PAUSED') {
        return '搁置'
    }

    return '作品库'
}

function getPriorityLabel(priority: LlmRecommendationResponse['priority']) {
    if (priority === 'HIGH') {
        return '高'
    }

    if (priority === 'MEDIUM') {
        return '中'
    }

    return '低'
}

function getPriorityClassName(priority: LlmRecommendationResponse['priority']) {
    if (priority === 'HIGH') {
        return 'border-rose-200 bg-rose-50/90 text-rose-700'
    }

    if (priority === 'MEDIUM') {
        return 'border-amber-200 bg-amber-50/90 text-amber-700'
    }

    return 'border-slate-200 bg-slate-50/90 text-slate-500'
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
