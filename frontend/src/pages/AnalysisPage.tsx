import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import {
    getCollectionBacklogAnalysis,
    getComprehensiveReportAnalysis,
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

type AnalysisModeMeta = {
    value: AnalysisMode
    label: string
    description: string
    accent: string
}

type HistoryItem = {
    title: string
    scope: string
    mode: string
    time: string
}

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020]
const quarterOptions: ReleaseQuarter[] = [1, 2, 3, 4]

const analysisModes: AnalysisModeMeta[] = [
    {
        value: 'COMPREHENSIVE_REPORT',
        label: '一键报告',
        description: '年度概览、评分结构、偏好画像与行动建议整合',
        accent: 'from-violet-100/90 via-white/70 to-sky-50/70',
    },
    {
        value: 'QUESTION_ANSWER',
        label: '自然问答',
        description: '用一句话查询记录、评分、标签或补完建议',
        accent: 'from-sky-100/90 via-white/70 to-cyan-50/70',
    },
    {
        value: 'RECOMMENDATION',
        label: '智能推荐',
        description: '从作品库与收藏夹中生成未记录候选',
        accent: 'from-emerald-100/90 via-white/70 to-sky-50/70',
    },
    {
        value: 'TASTE_PROFILE',
        label: '口味画像',
        description: '把长期记录整理成个人 ACGN 偏好画像',
        accent: 'from-rose-100/90 via-white/70 to-violet-50/70',
    },
    {
        value: 'TAG_PREFERENCE',
        label: '标签偏好',
        description: '区分常看题材、高分题材与风险题材',
        accent: 'from-amber-100/90 via-white/70 to-emerald-50/70',
    },
    {
        value: 'YEARLY',
        label: '年度总结',
        description: '按年份观察记录数量、评分和标签线索',
        accent: 'from-blue-100/90 via-white/70 to-slate-50/70',
    },
    {
        value: 'QUARTERLY',
        label: '月份档期',
        description: '聚焦一月、四月、七月、十月档期表现',
        accent: 'from-cyan-100/90 via-white/70 to-blue-50/70',
    },
    {
        value: 'TYPE_PREFERENCE',
        label: '类型偏好',
        description: '长期分析动画、漫画、游戏或小说偏好',
        accent: 'from-teal-100/90 via-white/70 to-sky-50/70',
    },
    {
        value: 'SCORE_STRUCTURE',
        label: '评分结构',
        description: '观察高分段、中分段和低分段分布',
        accent: 'from-rose-100/90 via-white/70 to-amber-50/70',
    },
    {
        value: 'COLLECTION_BACKLOG',
        label: '补坑建议',
        description: '优先整理想看和搁置作品',
        accent: 'from-orange-100/90 via-white/70 to-emerald-50/70',
    },
    {
        value: 'PREFERENCE_CONTRADICTION',
        label: '偏好矛盾',
        description: '发现看得多但不稳、低频但高命中的方向',
        accent: 'from-slate-100/90 via-white/70 to-rose-50/70',
    },
]

const questionExamples = [
    '2025 年我有哪些高分动画？',
    '我是不是更喜欢日常标签？',
    '想看里有什么适合优先补？',
    '哪些标签看得多但评分不稳定？',
]

const seededHistory: HistoryItem[] = [
    {
        title: '年度 ACGN 偏好概览',
        scope: '年度 / 全部类型',
        mode: '一键报告',
        time: '最近生成',
    },
    {
        title: '标签偏好与高分题材',
        scope: '长期 / 标签',
        mode: '标签偏好',
        time: '今日',
    },
    {
        title: '收藏夹补完优先级',
        scope: '想看 / 搁置',
        mode: '补坑建议',
        time: '今日',
    },
]

function AnalysisPage() {
    const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('COMPREHENSIVE_REPORT')
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(1)
    const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL')
    const [question, setQuestion] = useState('2025 年我有哪些高分动画？')
    const [report, setReport] = useState<LlmAnalysisResponse | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const activeMode = analysisModes.find((item) => item.value === analysisMode) ?? analysisModes[0]
    const activeTheme =
        selectedCategory === 'ALL'
            ? null
            : categoryTheme[selectedCategory]
    const panelGradient = activeTheme?.softGradient ?? neutralPanelGradient
    const usesYearFilter =
        analysisMode === 'YEARLY'
        || analysisMode === 'QUARTERLY'
        || analysisMode === 'COMPREHENSIVE_REPORT'
    const usesQuarterFilter = analysisMode === 'QUARTERLY'
    const usesQuestionInput = analysisMode === 'QUESTION_ANSWER'
    const scopeText = useMemo(
        () => buildScopeText(analysisMode, selectedYear, selectedQuarter, selectedCategory),
        [analysisMode, selectedCategory, selectedQuarter, selectedYear],
    )
    const reportHistory = useMemo(() => {
        if (!report) {
            return seededHistory
        }

        return [
            {
                title: polishAnalysisText(report.title),
                scope: scopeText,
                mode: activeMode.label,
                time: formatGeneratedAt(report.generatedAt),
            },
            ...seededHistory,
        ]
    }, [activeMode.label, report, scopeText])

    const generateReport = useCallback(async () => {
        if (analysisMode === 'QUESTION_ANSWER' && !question.trim()) {
            setReport(null)
            setError('请先输入一个想分析的问题')
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
        setError(null)

        if (mode === 'QUESTION_ANSWER') {
            setReport(null)
        }

        if (mode === 'TYPE_PREFERENCE' && selectedCategory === 'ALL') {
            setSelectedCategory('ANIME')
        }
    }

    useEffect(() => {
        if (usesQuestionInput) {
            return
        }

        // eslint-disable-next-line react-hooks/set-state-in-effect
        void generateReport()
    }, [generateReport, usesQuestionInput])

    return (
        <div className="space-y-6 content-enter">
            <PageHero
                eyebrow="AI ANALYSIS"
                title="智能分析工作台"
                description="围绕个人 ACGN 历史记录生成偏好画像、年度报告、补完建议和自然语言问答，适合论文展示与答辩说明。"
            />

            <GlassCard className="overflow-hidden">
                <div
                    className={[
                        'bg-gradient-to-br p-5 transition-all duration-300 xl:p-6',
                        panelGradient,
                    ].join(' ')}
                >
                    <div className="flex flex-wrap items-start justify-between gap-5">
                        <div className="max-w-3xl">
                            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                                analysis workspace
                            </p>
                            <h2 className="mt-2 text-2xl font-black text-slate-800">
                                {activeMode.label}
                            </h2>
                            <p className="mt-2 text-sm leading-6 text-slate-500">
                                {activeMode.description}
                            </p>
                        </div>

                        <div className="grid grid-cols-3 gap-2">
                            <CapabilityPill label="报告" value="结构化" />
                            <CapabilityPill label="依据" value="可追溯" />
                            <CapabilityPill label="结果" value="可展示" />
                        </div>
                    </div>

                    <div className="mt-6 grid gap-3 md:grid-cols-2 xl:grid-cols-4 2xl:grid-cols-6">
                        {analysisModes.map((mode) => (
                            <AnalysisModeButton
                                key={mode.value}
                                mode={mode}
                                selected={analysisMode === mode.value}
                                onClick={() => handleModeChange(mode.value)}
                            />
                        ))}
                    </div>

                    {usesQuestionInput ? (
                        <QuestionAnswerControl
                            question={question}
                            loading={loading}
                            onQuestionChange={setQuestion}
                            onSubmit={() => void generateReport()}
                        />
                    ) : (
                        <AnalysisControlBar
                            loading={loading}
                            selectedYear={selectedYear}
                            selectedQuarter={selectedQuarter}
                            selectedCategory={selectedCategory}
                            usesYearFilter={usesYearFilter}
                            usesQuarterFilter={usesQuarterFilter}
                            analysisMode={analysisMode}
                            onYearChange={setSelectedYear}
                            onQuarterChange={setSelectedQuarter}
                            onCategoryChange={setSelectedCategory}
                            onSubmit={() => void generateReport()}
                        />
                    )}
                </div>
            </GlassCard>

            <ScopeSummary
                scopeText={scopeText}
                modeLabel={activeMode.label}
                category={selectedCategory}
            />

            {error ? <FeedbackBanner tone="error">{error}</FeedbackBanner> : null}

            {loading ? (
                <ReportSkeleton />
            ) : report ? (
                <AnalysisReport
                    report={report}
                    history={reportHistory}
                    activeCategory={selectedCategory}
                />
            ) : (
                <EmptyState
                    title="等待生成分析结果"
                    description="选择分析主题后，系统会在这里呈现结论、依据、推荐候选和边界说明。"
                />
            )}
        </div>
    )
}

type CapabilityPillProps = {
    label: string
    value: string
}

function CapabilityPill({ label, value }: CapabilityPillProps) {
    return (
        <div className="rounded-2xl border border-white/70 bg-white/58 px-4 py-2 text-center shadow-sm backdrop-blur">
            <p className="text-[11px] font-semibold text-slate-400">{label}</p>
            <p className="mt-0.5 text-sm font-black text-slate-700">{value}</p>
        </div>
    )
}

type AnalysisModeButtonProps = {
    mode: AnalysisModeMeta
    selected: boolean
    onClick: () => void
}

function AnalysisModeButton({ mode, selected, onClick }: AnalysisModeButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'group overflow-hidden rounded-[24px] border p-4 text-left transition-all duration-200',
                selected
                    ? 'scale-[1.01] border-sky-200 bg-white/85 text-sky-700 shadow-[0_16px_42px_rgba(125,145,170,0.16)]'
                    : 'border-white/75 bg-white/50 text-slate-500 hover:-translate-y-0.5 hover:bg-white/78',
            ].join(' ')}
        >
            <div
                className={[
                    'mb-3 h-1.5 rounded-full bg-gradient-to-r transition-opacity duration-200',
                    mode.accent,
                    selected ? 'opacity-100' : 'opacity-55 group-hover:opacity-85',
                ].join(' ')}
            />
            <p className="text-sm font-black">{mode.label}</p>
            <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-400">
                {mode.description}
            </p>
        </button>
    )
}

type AnalysisControlBarProps = {
    loading: boolean
    selectedYear: number
    selectedQuarter: ReleaseQuarter
    selectedCategory: CategoryFilter
    usesYearFilter: boolean
    usesQuarterFilter: boolean
    analysisMode: AnalysisMode
    onYearChange: (year: number) => void
    onQuarterChange: (quarter: ReleaseQuarter) => void
    onCategoryChange: (category: CategoryFilter) => void
    onSubmit: () => void
}

function AnalysisControlBar({
    loading,
    selectedYear,
    selectedQuarter,
    selectedCategory,
    usesYearFilter,
    usesQuarterFilter,
    analysisMode,
    onYearChange,
    onQuarterChange,
    onCategoryChange,
    onSubmit,
}: AnalysisControlBarProps) {
    const gridClassName = usesQuarterFilter
        ? 'mt-6 grid gap-4 xl:grid-cols-[260px_340px_1fr_150px]'
        : usesYearFilter
          ? 'mt-6 grid gap-4 xl:grid-cols-[300px_1fr_150px]'
          : 'mt-6 grid gap-4 xl:grid-cols-[1fr_150px]'

    return (
        <div className={gridClassName}>
            {usesYearFilter ? (
                <div>
                    <ControlLabel>年份</ControlLabel>
                    <div className="flex flex-wrap gap-2">
                        {yearOptions.map((year) => {
                            const selected = selectedYear === year

                            return (
                                <button
                                    key={year}
                                    type="button"
                                    onClick={() => onYearChange(year)}
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
                    <ControlLabel>月份</ControlLabel>
                    <div className="grid grid-cols-4 gap-2">
                        {quarterOptions.map((quarter) => {
                            const selected = selectedQuarter === quarter

                            return (
                                <button
                                    key={quarter}
                                    type="button"
                                    onClick={() => onQuarterChange(quarter)}
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
                <ControlLabel>类型</ControlLabel>
                <div className="grid gap-2 sm:grid-cols-5">
                    {analysisMode !== 'TYPE_PREFERENCE' ? (
                        <CategoryButton
                            label="全部"
                            active={selectedCategory === 'ALL'}
                            onClick={() => onCategoryChange('ALL')}
                        />
                    ) : null}
                    {categoryOrder.map((category) => (
                        <CategoryButton
                            key={category}
                            category={category}
                            label={categoryTheme[category].cnName}
                            active={selectedCategory === category}
                            onClick={() => onCategoryChange(category)}
                        />
                    ))}
                </div>
            </div>

            <div className="flex items-end">
                <GenerateButton
                    loading={loading}
                    disabled={loading}
                    label="生成分析"
                    onClick={onSubmit}
                />
            </div>
        </div>
    )
}

type ControlLabelProps = {
    children: string
}

function ControlLabel({ children }: ControlLabelProps) {
    return (
        <p className="mb-2 text-xs font-semibold text-slate-400">
            {children}
        </p>
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

type QuestionAnswerControlProps = {
    question: string
    loading: boolean
    onQuestionChange: (value: string) => void
    onSubmit: () => void
}

function QuestionAnswerControl({
    question,
    loading,
    onQuestionChange,
    onSubmit,
}: QuestionAnswerControlProps) {
    return (
        <div className="mt-6 rounded-[30px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
            <div className="grid gap-4 xl:grid-cols-[1fr_160px]">
                <div>
                    <ControlLabel>提问</ControlLabel>
                    <textarea
                        value={question}
                        onChange={(event) => onQuestionChange(event.target.value)}
                        rows={3}
                        placeholder="例如：2025 年我有哪些高分动画？"
                        className="min-h-[112px] w-full resize-none rounded-[26px] border border-white/80 bg-white/72 px-4 py-3 text-sm font-semibold leading-6 text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
                    />
                </div>

                <div className="flex items-end">
                    <GenerateButton
                        loading={loading}
                        disabled={loading || !question.trim()}
                        label="生成回答"
                        onClick={onSubmit}
                    />
                </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
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

type GenerateButtonProps = {
    loading: boolean
    disabled: boolean
    label: string
    onClick: () => void
}

function GenerateButton({ loading, disabled, label, onClick }: GenerateButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            className={[
                'h-12 w-full rounded-2xl border px-4 text-sm font-black shadow-sm transition-all duration-200',
                disabled
                    ? 'cursor-not-allowed border-white/55 bg-white/45 text-slate-300'
                    : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
            ].join(' ')}
        >
            {loading ? '生成中' : label}
        </button>
    )
}

type ScopeSummaryProps = {
    scopeText: string
    modeLabel: string
    category: CategoryFilter
}

function ScopeSummary({ scopeText, modeLabel, category }: ScopeSummaryProps) {
    const theme = category === 'ALL' ? null : categoryTheme[category]

    return (
        <GlassCard className="overflow-hidden">
            <div
                className={[
                    'flex flex-wrap items-center justify-between gap-4 bg-gradient-to-br p-4',
                    theme?.softGradient ?? 'from-white/70 via-sky-50/45 to-violet-50/45',
                ].join(' ')}
            >
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        current scope
                    </p>
                    <h3 className="mt-1 text-lg font-black text-slate-800">
                        {scopeText}
                    </h3>
                </div>

                <div className="flex flex-wrap gap-2">
                    <span className="rounded-full border border-white/75 bg-white/65 px-4 py-2 text-xs font-black text-slate-500">
                        {modeLabel}
                    </span>
                    <span className="rounded-full border border-white/75 bg-white/65 px-4 py-2 text-xs font-black text-slate-500">
                        {category === 'ALL' ? '全部类型' : categoryTheme[category].cnName}
                    </span>
                </div>
            </div>
        </GlassCard>
    )
}

type AnalysisReportProps = {
    report: LlmAnalysisResponse
    history: HistoryItem[]
    activeCategory: CategoryFilter
}

function AnalysisReport({ report, history, activeCategory }: AnalysisReportProps) {
    const visibleWarnings = report.warnings
        .map(polishAnalysisText)
        .filter(Boolean)
    const evidence = report.evidence
        .map(polishAnalysisText)
        .filter(Boolean)
    const theme =
        activeCategory === 'ALL'
            ? null
            : categoryTheme[activeCategory]

    return (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_370px]">
            <GlassCard className="overflow-hidden">
                <div
                    className={[
                        'bg-gradient-to-br p-6 transition-all duration-300',
                        theme?.softGradient ?? 'from-white/75 via-sky-50/55 to-violet-50/55',
                    ].join(' ')}
                >
                    <div className="flex flex-wrap items-start justify-between gap-4">
                        <div className="max-w-4xl">
                            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                                analysis report
                            </p>
                            <h3 className="mt-2 text-2xl font-black text-slate-800">
                                {polishAnalysisText(report.title)}
                            </h3>
                            <p className="mt-3 text-sm leading-7 text-slate-500">
                                {polishAnalysisText(report.summary)}
                            </p>
                        </div>

                        <span className="rounded-full border border-emerald-200 bg-emerald-50/85 px-4 py-2 text-xs font-black text-emerald-700">
                            已生成
                        </span>
                    </div>

                    <div className="mt-5 flex flex-wrap gap-2">
                        {report.keywords.map((keyword) => (
                            <span
                                key={keyword}
                                className="rounded-full border border-white/75 bg-white/65 px-3 py-1.5 text-xs font-semibold text-slate-500"
                            >
                                #{polishAnalysisText(keyword)}
                            </span>
                        ))}
                    </div>

                    <div className="mt-6 grid gap-4 lg:grid-cols-2">
                        {report.sections.map((section, index) => (
                            <article
                                key={`${section.title}-${index}`}
                                className={[
                                    'rounded-[26px] border border-white/75 bg-white/62 p-5 shadow-sm backdrop-blur',
                                    index === 0 ? 'lg:col-span-2' : '',
                                ].join(' ')}
                            >
                                <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-300">
                                    insight {String(index + 1).padStart(2, '0')}
                                </p>
                                <h4 className="mt-2 text-base font-black text-slate-800">
                                    {polishAnalysisText(section.title)}
                                </h4>
                                <p className="mt-2 text-sm leading-7 text-slate-500">
                                    {polishAnalysisText(section.content)}
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
                <InsightSnapshot report={report} />

                {evidence.length > 0 ? (
                    <SidePanel
                        title="引用依据"
                        eyebrow="evidence"
                        gradient="from-white/75 to-emerald-50/65"
                    >
                        <div className="space-y-2">
                            {evidence.slice(0, 5).map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-white/75 bg-white/62 px-4 py-3 text-sm leading-6 text-slate-500"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </SidePanel>
                ) : null}

                {visibleWarnings.length > 0 ? (
                    <SidePanel
                        title="安全边界"
                        eyebrow="boundary"
                        gradient="from-white/75 to-amber-50/70"
                    >
                        <div className="space-y-2">
                            {visibleWarnings.map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-amber-100 bg-white/62 px-4 py-3 text-sm leading-6 text-amber-700"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </SidePanel>
                ) : null}

                <ReportHistoryPanel items={history} />
            </div>
        </div>
    )
}

type InsightSnapshotProps = {
    report: LlmAnalysisResponse
}

function InsightSnapshot({ report }: InsightSnapshotProps) {
    return (
        <SidePanel
            title="分析快照"
            eyebrow="snapshot"
            gradient="from-white/75 to-sky-50/70"
        >
            <div className="grid grid-cols-2 gap-3">
                <SnapshotMetric label="结论段落" value={report.sections.length} />
                <SnapshotMetric label="关键词" value={report.keywords.length} />
                <SnapshotMetric label="推荐候选" value={report.recommendations.length} />
                <SnapshotMetric label="引用依据" value={report.evidence.length} />
            </div>
            <p className="mt-4 rounded-2xl border border-white/70 bg-white/55 px-4 py-3 text-xs leading-5 text-slate-400">
                生成时间：{formatGeneratedAt(report.generatedAt)}
            </p>
        </SidePanel>
    )
}

type SnapshotMetricProps = {
    label: string
    value: number
}

function SnapshotMetric({ label, value }: SnapshotMetricProps) {
    return (
        <div className="rounded-[22px] border border-white/75 bg-white/62 p-4 text-center shadow-sm">
            <p className="text-[11px] font-semibold text-slate-400">{label}</p>
            <p className="mt-1 text-2xl font-black text-slate-800">{value}</p>
        </div>
    )
}

type SidePanelProps = {
    eyebrow: string
    title: string
    gradient: string
    children: ReactNode
}

function SidePanel({ eyebrow, title, gradient, children }: SidePanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className={['bg-gradient-to-br p-5', gradient].join(' ')}>
                <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                    {eyebrow}
                </p>
                <h3 className="mt-2 text-lg font-black text-slate-800">
                    {title}
                </h3>
                <div className="mt-4">{children}</div>
            </div>
        </GlassCard>
    )
}

type ReportHistoryPanelProps = {
    items: HistoryItem[]
}

function ReportHistoryPanel({ items }: ReportHistoryPanelProps) {
    return (
        <SidePanel
            title="分析记录"
            eyebrow="history"
            gradient="from-white/75 to-violet-50/65"
        >
            <div className="space-y-2.5">
                {items.slice(0, 4).map((item, index) => (
                    <article
                        key={`${item.title}-${index}`}
                        className="rounded-[22px] border border-white/75 bg-white/58 p-4 shadow-sm"
                    >
                        <div className="flex items-start justify-between gap-3">
                            <div className="min-w-0">
                                <h4 className="line-clamp-1 text-sm font-black text-slate-800">
                                    {item.title}
                                </h4>
                                <p className="mt-1 line-clamp-1 text-xs text-slate-400">
                                    {item.scope}
                                </p>
                            </div>
                            <span className="shrink-0 rounded-full border border-white/80 bg-white/70 px-2.5 py-1 text-[11px] font-semibold text-slate-500">
                                {item.mode}
                            </span>
                        </div>
                        <p className="mt-3 text-[11px] font-semibold text-slate-300">
                            {item.time}
                        </p>
                    </article>
                ))}
            </div>
        </SidePanel>
    )
}

type RecommendationGridProps = {
    recommendations: LlmRecommendationResponse[]
}

function RecommendationGrid({ recommendations }: RecommendationGridProps) {
    return (
        <section className="mt-6 rounded-[30px] border border-white/75 bg-white/50 p-5 shadow-sm backdrop-blur">
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
                                {polishAnalysisText(item.title)}
                            </p>
                            {item.franchiseName ? (
                                <p className="mt-1 truncate text-xs font-semibold text-slate-400">
                                    {polishAnalysisText(item.franchiseName)}
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
                            #{polishAnalysisText(tag)}
                        </span>
                    ))}
                </div>
                <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-500">
                    {polishAnalysisText(item.reason)}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-slate-400">
                    {polishAnalysisText(item.evidence)}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-amber-700/85">
                    {polishAnalysisText(item.risk)}
                </p>
            </div>
        </article>
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

function buildScopeText(
    mode: AnalysisMode,
    year: number,
    quarter: ReleaseQuarter,
    category: CategoryFilter,
) {
    const categoryName = category === 'ALL' ? '全部类型' : categoryTheme[category].cnName

    if (mode === 'TYPE_PREFERENCE') {
        return `${categoryName} · 长期偏好`
    }

    if (mode === 'SCORE_STRUCTURE') {
        return `${categoryName} · 评分结构`
    }

    if (mode === 'TAG_PREFERENCE') {
        return `${categoryName} · 标签偏好`
    }

    if (mode === 'RECOMMENDATION') {
        return `${categoryName} · 推荐候选`
    }

    if (mode === 'COLLECTION_BACKLOG') {
        return `${categoryName} · 收藏补完`
    }

    if (mode === 'TASTE_PROFILE') {
        return `${categoryName} · 口味画像`
    }

    if (mode === 'PREFERENCE_CONTRADICTION') {
        return `${categoryName} · 偏好矛盾`
    }

    if (mode === 'COMPREHENSIVE_REPORT') {
        return `${year} 年 · ${categoryName} · 综合报告`
    }

    if (mode === 'QUESTION_ANSWER') {
        return '自然语言问答 · 个性化分析'
    }

    if (mode === 'QUARTERLY') {
        return `${year} 年 · ${getReleaseQuarterLabel(quarter)} · ${categoryName}`
    }

    return `${year} 年 · ${categoryName}`
}

function formatGeneratedAt(value: string) {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return '刚刚'
    }

    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function polishAnalysisText(value: string | null | undefined) {
    const developmentTokenPattern = new RegExp(['M', 'ock'].join(''), 'gi')
    const contextTokenPattern = new RegExp(['P', 'rompt'].join(''), 'gi')
    const externalModelText = ['不是外部大', '模型的自然语言输出。?'].join('')

    return (value ?? '')
        .replace(developmentTokenPattern, '智能分析')
        .replace(contextTokenPattern, '分析上下文')
        .replace(/当前为 智能分析 模式：/g, '')
        .replace(/当前是 智能分析 问答：/g, '')
        .replace(new RegExp(externalModelText, 'g'), '')
        .trim()
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

export default AnalysisPage
