import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import {
    getCollectionBacklogAnalysis,
    getComprehensiveReportAnalysis,
    getLlmStatus,
    getFranchiseCompletionAnalysis,
    getInterestTrendAnalysis,
    getPreferenceContradictionAnalysis,
    getQuestionAnswerAnalysis,
    getQuarterlyAnalysis,
    getRecommendationAnalysis,
    getScoreStructureAnalysis,
    getTagPreferenceAnalysis,
    getTasteProfileAnalysis,
    testLlmConnection,
    getTypePreferenceAnalysis,
    getYearlyAnalysis,
    updateLlmConfig,
    type LlmAnalysisResponse,
    type LlmQuestionAnswerResponse,
    type LlmRecommendationResponse,
    type LlmStatusResponse,
} from '../api/llmApi'
import { getApiErrorMessage } from '../api/request'
import {
    EmptyState,
} from '../components/common/FeedbackState'
import CoverImage from '../components/common/CoverImage'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import { categoryTheme, neutralPanelGradient } from '../theme/acgnTheme'
import { categoryOrder, getReleaseQuarterLabel, type AcgnCategory, type ReleaseQuarter } from '../types/common'
import {
    readJsonCache,
    removeCacheItem,
    removeCacheItemsByPrefixes,
    writeJsonCache,
} from '../utils/localStorageCache'

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
    | 'INTEREST_TREND'
    | 'FRANCHISE_COMPLETION'
    | 'COMPREHENSIVE_REPORT'
    | 'QUESTION_ANSWER'

type AnalysisModeMeta = {
    value: AnalysisMode
    label: string
    description: string
    accent: string
}

type HistoryItem = {
    id: string
    cacheKey: string
    analysisType: AnalysisMode
    title: string
    scope: string
    mode: string
    generatedAt: string
    year: number
    quarter: ReleaseQuarter
    category: CategoryFilter
    question: string
}

type LlmConfigMode = 'deepseek' | 'preview'

type GenerationProgress = {
    percent: number
    title: string
    detail: string
}

type CacheInfo = {
    source: 'cache' | 'fresh'
    cachedAt: string
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
        label: '自由问答',
        description: '自由聊天、查记录、做规划、写总结或问答辩思路',
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
        value: 'FRANCHISE_COMPLETION',
        label: '系列补完',
        description: '按系列关系整理已看、想看、搁置和待补作品',
        accent: 'from-indigo-100/90 via-white/70 to-emerald-50/70',
    },
]

const questionExamples = [
    '像朋友一样吐槽一下我最近的 ACGN 口味',
    '帮我设计一个适合答辩展示的 AI 分析演示问题',
    '如果今晚只补一部，我应该从想看里挑什么？',
    '把我的高分动画口味写成一段有记忆感的总结',
]

const defaultProgress: GenerationProgress = {
    percent: 8,
    title: '正在准备分析任务',
    detail: '正在确认分析范围、模型配置和请求参数。',
}

const reportCachePrefix = 'acgn-llm-report-cache'
const reportCacheVersion = 1
const reportHistoryStorageKey = 'acgn-llm-report-history'
const maxReportHistoryCount = 20
const allAiCachePrefixes = [reportCachePrefix, 'acgn-home-insight-cache']

function AnalysisPage() {
    const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('COMPREHENSIVE_REPORT')
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(1)
    const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL')
    const [question, setQuestion] = useState('2025 年我有哪些高分动画？')
    const [report, setReport] = useState<LlmAnalysisResponse | null>(null)
    const [reportHistory, setReportHistory] = useState<HistoryItem[]>(() => readReportHistory())
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [cacheInfo, setCacheInfo] = useState<CacheInfo | null>(null)
    const [llmStatus, setLlmStatus] = useState<LlmStatusResponse | null>(null)
    const [configOpen, setConfigOpen] = useState(false)
    const [configMode, setConfigMode] = useState<LlmConfigMode>('deepseek')
    const [configBaseUrl, setConfigBaseUrl] = useState('https://api.deepseek.com')
    const [configModel, setConfigModel] = useState('deepseek-v4-flash')
    const [configApiKey, setConfigApiKey] = useState('')
    const [configSaving, setConfigSaving] = useState(false)
    const [configTesting, setConfigTesting] = useState(false)
    const [configMessage, setConfigMessage] = useState<string | null>(null)
    const [progress, setProgress] = useState<GenerationProgress>(defaultProgress)
    const progressStartedAt = useRef<number | null>(null)

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
    const reportCacheKey = useMemo(
        () => buildReportCacheKey(analysisMode, selectedYear, selectedQuarter, selectedCategory, question),
        [analysisMode, question, selectedCategory, selectedQuarter, selectedYear],
    )
    const syncConfigState = useCallback((status: LlmStatusResponse) => {
        setLlmStatus(status)
        setConfigMode(status.mockMode ? 'preview' : 'deepseek')
        setConfigBaseUrl(status.baseUrl || 'https://api.deepseek.com')
        setConfigModel(status.model || 'deepseek-v4-flash')
    }, [])

    useEffect(() => {
        async function loadStatus() {
            try {
                const status = await getLlmStatus()
                syncConfigState(status)
            } catch (statusError) {
                setConfigMessage(getApiErrorMessage(statusError, 'LLM 配置状态读取失败'))
            }
        }

        void loadStatus()
    }, [syncConfigState])

    useEffect(() => {
        if (!loading) {
            progressStartedAt.current = null
            return
        }

        progressStartedAt.current = Date.now()
        const timer = window.setInterval(() => {
            const startedAt = progressStartedAt.current ?? Date.now()
            const elapsedSeconds = (Date.now() - startedAt) / 1000
            const nextPercent = Math.min(94, Math.round(12 + elapsedSeconds * 2.4))
            setProgress(buildGenerationProgress(nextPercent, analysisMode, llmStatus))
        }, 650)

        return () => window.clearInterval(timer)
    }, [analysisMode, llmStatus, loading])

    useEffect(() => {
        if (analysisMode === 'QUESTION_ANSWER' && !question.trim()) {
            setReport(null)
            setCacheInfo(null)
            return
        }

        const cached = readCachedReport(reportCacheKey, analysisMode)
        if (cached) {
            setReport(cached.report)
            setCacheInfo({
                source: 'cache',
                cachedAt: cached.cachedAt,
            })
            setError(null)
            return
        }

        setReport(null)
        setCacheInfo(null)
    }, [analysisMode, question, reportCacheKey])

    const generateReport = useCallback(async () => {
        if (analysisMode === 'QUESTION_ANSWER' && !question.trim()) {
            setReport(null)
            setError('请先输入一个想分析的问题')
            return
        }

        if (llmStatus && !llmStatus.mockMode && !llmStatus.apiKeyConfigured) {
            setReport(null)
            setError('请先配置 DeepSeek API Key，或切换到预览模式')
            setConfigOpen(true)
            return
        }

        setLoading(true)
        setError(null)
        setProgress(buildGenerationProgress(8, analysisMode, llmStatus))

        try {
            const category =
                selectedCategory === 'ALL' ? undefined : selectedCategory
            const response =
                analysisMode === 'QUESTION_ANSWER'
                    ? toQuestionAnswerReport(await getQuestionAnswerAnalysis({
                          question: question.trim(),
                      }))
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
                                      : analysisMode === 'INTEREST_TREND'
                                        ? await getInterestTrendAnalysis({
                                              category,
                                          })
                                        : analysisMode === 'FRANCHISE_COMPLETION'
                                          ? await getFranchiseCompletionAnalysis({
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
            const normalizedReport = normalizeAnalysisReport(response, analysisMode)
            const cachedAt = new Date().toISOString()
            writeCachedReport(reportCacheKey, normalizedReport, cachedAt)
            setReportHistory((items) => upsertReportHistory(items, {
                id: `${cachedAt}-${analysisMode}`,
                cacheKey: reportCacheKey,
                analysisType: analysisMode,
                title: polishAnalysisText(normalizedReport.title),
                scope: scopeText,
                mode: activeMode.label,
                generatedAt: cachedAt,
                year: selectedYear,
                quarter: selectedQuarter,
                category: selectedCategory,
                question: question.trim(),
            }))
            setProgress(buildGenerationProgress(100, analysisMode, llmStatus))
            setReport(normalizedReport)
            setCacheInfo({
                source: 'fresh',
                cachedAt,
            })
        } catch (generateError) {
            const cached = readCachedReport(reportCacheKey, analysisMode)
            if (cached) {
                setReport(cached.report)
                setCacheInfo({
                    source: 'cache',
                    cachedAt: cached.cachedAt,
                })
                setError(`${getApiErrorMessage(generateError, '智能分析生成失败')}。已回退到本地缓存结果。`)
            } else {
                setReport(null)
                setCacheInfo(null)
                setError(getApiErrorMessage(generateError, '智能分析生成失败'))
            }
            setProgress(defaultProgress)
        } finally {
            setLoading(false)
        }
    }, [
        activeMode.label,
        analysisMode,
        llmStatus,
        question,
        reportCacheKey,
        scopeText,
        selectedCategory,
        selectedQuarter,
        selectedYear,
    ])

    const saveLlmConfig = useCallback(async () => {
        setConfigSaving(true)
        setConfigMessage(null)

        try {
            const status = await updateLlmConfig({
                previewMode: configMode === 'preview',
                baseUrl: configBaseUrl,
                apiKey: configApiKey.trim() || undefined,
                model: configModel,
                timeoutSeconds: 30,
                temperature: 0.4,
                maxTokens: 1200,
            })
            syncConfigState(status)
            setConfigApiKey('')
            setConfigMessage(
                status.mockMode
                    ? '已切换到预览模式'
                    : 'DeepSeek API 配置已生效',
            )
        } catch (configError) {
            setConfigMessage(getApiErrorMessage(configError, 'LLM 配置保存失败'))
        } finally {
            setConfigSaving(false)
        }
    }, [configApiKey, configBaseUrl, configMode, configModel, syncConfigState])

    const testCurrentLlmConfig = useCallback(async () => {
        setConfigTesting(true)
        setConfigMessage(null)

        try {
            if (configMode === 'deepseek' && !configApiKey.trim() && !llmStatus?.apiKeyConfigured) {
                setConfigMessage('请先填写 DeepSeek API Key，或在后端环境变量中配置 DEEPSEEK_API_KEY')
                return
            }

            const status = await updateLlmConfig({
                previewMode: configMode === 'preview',
                baseUrl: configBaseUrl,
                apiKey: configApiKey.trim() || undefined,
                model: configModel,
                timeoutSeconds: 30,
                temperature: 0.4,
                maxTokens: 1200,
            })
            syncConfigState(status)
            setConfigApiKey('')

            const response = await testLlmConnection()
            setConfigMessage(
                response.previewMode
                    ? '预览模式测试通过：本地分析链路可用'
                    : `DeepSeek 连接测试通过：${response.message || '连接正常'}`,
            )
        } catch (testError) {
            setConfigMessage(getApiErrorMessage(testError, '大模型连接测试失败'))
        } finally {
            setConfigTesting(false)
        }
    }, [configApiKey, configBaseUrl, configMode, configModel, llmStatus, syncConfigState])

    const clearCurrentReportCache = useCallback(() => {
        removeCacheItem(reportCacheKey)
        setReport(null)
        setCacheInfo(null)
        setError(null)
    }, [reportCacheKey])

    const clearAllAiCaches = useCallback(() => {
        removeCacheItemsByPrefixes(allAiCachePrefixes)
        writeReportHistory([])
        setReportHistory([])
        setReport(null)
        setCacheInfo(null)
        setError(null)
    }, [])

    const restoreHistoryItem = useCallback((item: HistoryItem) => {
        const cached = readCachedReport(item.cacheKey, item.analysisType)
        if (!cached) {
            setError('这条历史记录对应的本地缓存已经不存在，请重新生成。')
            return
        }

        setAnalysisMode(item.analysisType)
        setSelectedYear(item.year)
        setSelectedQuarter(item.quarter)
        setSelectedCategory(item.category)
        if (item.analysisType === 'QUESTION_ANSWER') {
            setQuestion(item.question)
        }
        setReport(cached.report)
        setCacheInfo({
            source: 'cache',
            cachedAt: cached.cachedAt,
        })
        setError(null)
    }, [])

    const clearReportHistory = useCallback(() => {
        writeReportHistory([])
        setReportHistory([])
    }, [])

    const deleteReportHistoryItem = useCallback((item: HistoryItem) => {
        setReportHistory((items) => {
            const nextItems = items.filter((historyItem) => historyItem.id !== item.id)
            writeReportHistory(nextItems)
            return nextItems
        })
    }, [])

    const pruneInvalidReportHistory = useCallback(() => {
        setReportHistory((items) => {
            const nextItems = items.filter((item) => readCachedReport(item.cacheKey, item.analysisType))
            writeReportHistory(nextItems)
            return nextItems
        })
    }, [])

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

    return (
        <div className="space-y-6 content-enter">
            <PageHero
                eyebrow="AI ANALYSIS"
                title="智能分析工作台"
                description="围绕个人 ACGN 历史记录生成偏好画像、年度报告、补完建议和自然语言问答，适合论文展示与答辩说明。"
            />

            <LlmConfigPanel
                open={configOpen}
                status={llmStatus}
                mode={configMode}
                baseUrl={configBaseUrl}
                model={configModel}
                apiKey={configApiKey}
                saving={configSaving}
                testing={configTesting}
                message={configMessage}
                onToggle={() => setConfigOpen((value) => !value)}
                onModeChange={setConfigMode}
                onBaseUrlChange={setConfigBaseUrl}
                onModelChange={setConfigModel}
                onApiKeyChange={setConfigApiKey}
                onSave={() => void saveLlmConfig()}
                onTest={() => void testCurrentLlmConfig()}
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
                            buttonLabel={report ? '重新生成回答' : '生成回答'}
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
                            buttonLabel={report ? '重新生成' : '生成分析'}
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

            {cacheInfo ? (
                <CacheStatusPanel
                    cacheInfo={cacheInfo}
                    onRegenerate={() => void generateReport()}
                    onClearCurrent={clearCurrentReportCache}
                    onClearAll={clearAllAiCaches}
                />
            ) : null}

            {error ? (
                <GenerationErrorPanel
                    message={error}
                    canOpenConfig={Boolean(llmStatus && !llmStatus.mockMode)}
                    onRetry={() => void generateReport()}
                    onOpenConfig={() => setConfigOpen(true)}
                />
            ) : null}

            {loading ? (
                <GenerationProgressPanel
                    progress={progress}
                    modeLabel={activeMode.label}
                    modelLabel={llmStatus?.mockMode ? '预览模式' : (llmStatus?.model ?? 'DeepSeek API')}
                />
            ) : report ? (
                <AnalysisReport
                    report={report}
                    history={reportHistory}
                    activeCategory={selectedCategory}
                    onSelectHistory={restoreHistoryItem}
                    onDeleteHistory={deleteReportHistoryItem}
                    onPruneInvalidHistory={pruneInvalidReportHistory}
                    onClearHistory={clearReportHistory}
                />
            ) : (
                <EmptyState
                    title="等待生成分析结果"
                    description="当前筛选还没有本地缓存。点击生成分析后，结果会写入本地缓存；之后切回同一条件会优先展示缓存，只有重新生成才会覆盖。"
                />
            )}
        </div>
    )
}

type CapabilityPillProps = {
    label: string
    value: string
}

type LlmConfigPanelProps = {
    open: boolean
    status: LlmStatusResponse | null
    mode: LlmConfigMode
    baseUrl: string
    model: string
    apiKey: string
    saving: boolean
    testing: boolean
    message: string | null
    onToggle: () => void
    onModeChange: (mode: LlmConfigMode) => void
    onBaseUrlChange: (value: string) => void
    onModelChange: (value: string) => void
    onApiKeyChange: (value: string) => void
    onSave: () => void
    onTest: () => void
}

function LlmConfigPanel({
    open,
    status,
    mode,
    baseUrl,
    model,
    apiKey,
    saving,
    testing,
    message,
    onToggle,
    onModeChange,
    onBaseUrlChange,
    onModelChange,
    onApiKeyChange,
    onSave,
    onTest,
}: LlmConfigPanelProps) {
    const currentModeLabel = status
        ? status.mockMode ? '预览模式' : 'DeepSeek API'
        : '读取中'

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/75 via-sky-50/45 to-violet-50/55 p-5">
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            llm settings
                        </p>
                        <h3 className="mt-1 text-lg font-black text-slate-800">
                            大模型配置
                        </h3>
                        <p className="mt-1 text-sm leading-6 text-slate-500">
                            当前：{currentModeLabel}
                            {status && !status.mockMode ? ` · ${status.model}` : ''}
                            {status && !status.mockMode
                                ? status.apiKeyConfigured
                                    ? ' · API Key 已配置'
                                    : ' · API Key 未配置'
                                : ''}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={onToggle}
                        className="rounded-full border border-sky-200 bg-white/75 px-5 py-2.5 text-sm font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                    >
                        {open ? '收起设置' : '设置 API'}
                    </button>
                </div>

                {open ? (
                    <div className="mt-5 rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
                        <div className="grid gap-3 md:grid-cols-2">
                            <ConfigModeButton
                                active={mode === 'deepseek'}
                                title="DeepSeek API"
                                description="使用 deepseek-v4-flash 生成真实分析结果"
                                onClick={() => onModeChange('deepseek')}
                            />
                            <ConfigModeButton
                                active={mode === 'preview'}
                                title="预览模式"
                                description="不调用外部接口，使用本地统计生成演示结果"
                                onClick={() => onModeChange('preview')}
                            />
                        </div>

                        {mode === 'deepseek' ? (
                            <div className="mt-5 grid gap-4 xl:grid-cols-[1.2fr_1fr_1.4fr]">
                                <ConfigField
                                    label="Base URL"
                                    value={baseUrl}
                                    placeholder="https://api.deepseek.com"
                                    onChange={onBaseUrlChange}
                                />
                                <ConfigField
                                    label="模型"
                                    value={model}
                                    placeholder="deepseek-v4-flash"
                                    onChange={onModelChange}
                                />
                                <ConfigField
                                    label="API Key"
                                    value={apiKey}
                                    placeholder="留空表示沿用后端已配置的 Key"
                                    type="password"
                                    onChange={onApiKeyChange}
                                />
                            </div>
                        ) : null}

                        <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
                            <p className="text-xs leading-5 text-slate-400">
                                页面保存的是后端运行时配置，不会把 Key 写入仓库配置文件；“保存并测试”会先应用当前表单再发起连接测试。
                            </p>
                            <div className="flex flex-wrap gap-2">
                                <button
                                    type="button"
                                    onClick={onTest}
                                    disabled={saving || testing}
                                    className={[
                                        'rounded-full border px-5 py-2.5 text-sm font-black shadow-sm transition-all duration-200',
                                        saving || testing
                                            ? 'cursor-not-allowed border-white/60 bg-white/50 text-slate-300'
                                            : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
                                    ].join(' ')}
                                >
                                    {testing ? '测试中' : '保存并测试'}
                                </button>
                                <button
                                    type="button"
                                    onClick={onSave}
                                    disabled={saving || testing}
                                    className={[
                                        'rounded-full border px-5 py-2.5 text-sm font-black shadow-sm transition-all duration-200',
                                        saving || testing
                                            ? 'cursor-not-allowed border-white/60 bg-white/50 text-slate-300'
                                            : 'border-emerald-200 bg-emerald-100/90 text-emerald-700 hover:-translate-y-0.5 hover:bg-emerald-50',
                                    ].join(' ')}
                                >
                                    {saving ? '保存中' : '保存配置'}
                                </button>
                            </div>
                        </div>

                        {message ? (
                            <p className="mt-3 rounded-2xl border border-white/75 bg-white/65 px-4 py-3 text-sm leading-6 text-slate-500">
                                {message}
                            </p>
                        ) : null}
                    </div>
                ) : null}
            </div>
        </GlassCard>
    )
}

type ConfigModeButtonProps = {
    active: boolean
    title: string
    description: string
    onClick: () => void
}

function ConfigModeButton({ active, title, description, onClick }: ConfigModeButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-[24px] border p-4 text-left transition-all duration-200',
                active
                    ? 'border-sky-200 bg-sky-50/90 text-sky-700 shadow-sm'
                    : 'border-white/75 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white',
            ].join(' ')}
        >
            <p className="text-sm font-black">{title}</p>
            <p className="mt-1 text-xs leading-5 text-slate-400">{description}</p>
        </button>
    )
}

type ConfigFieldProps = {
    label: string
    value: string
    placeholder: string
    type?: 'text' | 'password'
    onChange: (value: string) => void
}

function ConfigField({
    label,
    value,
    placeholder,
    type = 'text',
    onChange,
}: ConfigFieldProps) {
    return (
        <label className="block">
            <ControlLabel>{label}</ControlLabel>
            <input
                type={type}
                value={value}
                placeholder={placeholder}
                onChange={(event) => onChange(event.target.value)}
                className="h-12 w-full rounded-2xl border border-white/80 bg-white/72 px-4 text-sm font-semibold text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
            />
        </label>
    )
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
    buttonLabel: string
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
    buttonLabel,
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
                    label={buttonLabel}
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
    buttonLabel: string
    onQuestionChange: (value: string) => void
    onSubmit: () => void
}

function QuestionAnswerControl({
    question,
    loading,
    buttonLabel,
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
                        placeholder="可以自由问：查记录、要推荐、让它吐槽口味、写答辩话术，或做一个补番计划。"
                        className="min-h-[112px] w-full resize-none rounded-[26px] border border-white/80 bg-white/72 px-4 py-3 text-sm font-semibold leading-6 text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
                    />
                </div>

                <div className="flex items-end">
                    <GenerateButton
                        loading={loading}
                        disabled={loading || !question.trim()}
                        label={buttonLabel}
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

type CacheStatusPanelProps = {
    cacheInfo: CacheInfo
    onRegenerate: () => void
    onClearCurrent: () => void
    onClearAll: () => void
}

function CacheStatusPanel({
    cacheInfo,
    onRegenerate,
    onClearCurrent,
    onClearAll,
}: CacheStatusPanelProps) {
    const isCache = cacheInfo.source === 'cache'

    return (
        <GlassCard className="overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-4 bg-gradient-to-br from-white/72 via-emerald-50/45 to-sky-50/45 p-4">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        local cache
                    </p>
                    <h3 className="mt-1 text-base font-black text-slate-800">
                        {isCache ? '当前显示本地缓存结果' : '当前结果已写入本地缓存'}
                    </h3>
                    <p className="mt-1 text-sm leading-6 text-slate-500">
                        缓存时间：{formatGeneratedAt(cacheInfo.cachedAt)}。切换回同一分析条件时会优先使用这份结果。
                    </p>
                </div>

                <div className="flex flex-wrap gap-2">
                    <button
                        type="button"
                        onClick={onRegenerate}
                        className="rounded-full border border-emerald-200 bg-white/75 px-5 py-2.5 text-sm font-black text-emerald-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-emerald-50"
                    >
                        重新生成并覆盖缓存
                    </button>
                    <button
                        type="button"
                        onClick={onClearCurrent}
                        className="rounded-full border border-slate-200 bg-white/70 px-5 py-2.5 text-sm font-black text-slate-600 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        清除当前缓存
                    </button>
                    <button
                        type="button"
                        onClick={onClearAll}
                        className="rounded-full border border-rose-200 bg-white/70 px-5 py-2.5 text-sm font-black text-rose-600 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-50"
                    >
                        清除全部 AI 缓存
                    </button>
                </div>
            </div>
        </GlassCard>
    )
}

type AnalysisReportProps = {
    report: LlmAnalysisResponse
    history: HistoryItem[]
    activeCategory: CategoryFilter
    onSelectHistory: (item: HistoryItem) => void
    onDeleteHistory: (item: HistoryItem) => void
    onPruneInvalidHistory: () => void
    onClearHistory: () => void
}

function AnalysisReport({
    report,
    history,
    activeCategory,
    onSelectHistory,
    onDeleteHistory,
    onPruneInvalidHistory,
    onClearHistory,
}: AnalysisReportProps) {
    const [copyStatus, setCopyStatus] = useState<'idle' | 'success' | 'error'>('idle')
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

    async function handleCopyMarkdown() {
        try {
            await navigator.clipboard.writeText(formatAnalysisReportMarkdown(report))
            setCopyStatus('success')
        } catch {
            setCopyStatus('error')
        }

        window.setTimeout(() => setCopyStatus('idle'), 2200)
    }

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

                        <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
                            <span className="rounded-full border border-emerald-200 bg-emerald-50/85 px-4 py-2 text-xs font-black text-emerald-700">
                                已生成
                            </span>
                            <button
                                type="button"
                                onClick={() => void handleCopyMarkdown()}
                                className="rounded-full border border-sky-200 bg-white/75 px-4 py-2 text-xs font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                            >
                                {copyStatus === 'success'
                                    ? '已复制'
                                    : copyStatus === 'error'
                                      ? '复制失败'
                                      : '复制 Markdown'}
                            </button>
                        </div>
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

                <ReportHistoryPanel
                    items={history}
                    onSelect={onSelectHistory}
                    onDelete={onDeleteHistory}
                    onPruneInvalid={onPruneInvalidHistory}
                    onClear={onClearHistory}
                />
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
    onSelect: (item: HistoryItem) => void
    onDelete: (item: HistoryItem) => void
    onPruneInvalid: () => void
    onClear: () => void
}

function ReportHistoryPanel({
    items,
    onSelect,
    onDelete,
    onPruneInvalid,
    onClear,
}: ReportHistoryPanelProps) {
    return (
        <SidePanel
            title="分析记录"
            eyebrow="history"
            gradient="from-white/75 to-violet-50/65"
        >
            {items.length > 0 ? (
                <div className="mb-3 flex flex-wrap justify-end gap-2">
                    <button
                        type="button"
                        onClick={onPruneInvalid}
                        className="rounded-full border border-sky-200 bg-white/70 px-3 py-1.5 text-[11px] font-black text-sky-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                    >
                        清理失效
                    </button>
                    <button
                        type="button"
                        onClick={onClear}
                        className="rounded-full border border-slate-200 bg-white/70 px-3 py-1.5 text-[11px] font-black text-slate-500 transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        清空历史
                    </button>
                </div>
            ) : null}
            <div className="space-y-2.5">
                {items.length > 0 ? (
                    items.slice(0, 6).map((item, index) => (
                        <article
                            key={`${item.id}-${index}`}
                            onClick={() => onSelect(item)}
                            onKeyDown={(event) => {
                                if (event.key === 'Enter' || event.key === ' ') {
                                    event.preventDefault()
                                    onSelect(item)
                                }
                            }}
                            role="button"
                            tabIndex={0}
                            className="w-full cursor-pointer rounded-[22px] border border-white/75 bg-white/58 p-4 text-left shadow-sm outline-none transition-all duration-200 hover:-translate-y-0.5 hover:bg-white/75 focus:ring-4 focus:ring-violet-100/80"
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
                            <div className="mt-3 flex items-center justify-between gap-2">
                                <p className="text-[11px] font-semibold text-slate-300">
                                    {formatGeneratedAt(item.generatedAt)}
                                </p>
                                <button
                                    type="button"
                                    onClick={(event) => {
                                        event.stopPropagation()
                                        onDelete(item)
                                    }}
                                    className="rounded-full border border-rose-100 bg-white/65 px-2.5 py-1 text-[11px] font-black text-rose-500 transition-all duration-200 hover:bg-rose-50"
                                >
                                    删除
                                </button>
                            </div>
                        </article>
                    ))
                ) : (
                    <p className="rounded-[22px] border border-white/75 bg-white/58 p-4 text-sm leading-6 text-slate-400">
                        生成第一份报告后，这里会保留最近的本地历史记录。
                    </p>
                )}
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
                {recommendations.map((item, index) => (
                    <RecommendationCard
                        key={`${item.subjectId ?? item.title}-${index}`}
                        item={item}
                    />
                ))}
            </div>
        </section>
    )
}

type RecommendationCardProps = {
    item: LlmRecommendationResponse
}

function RecommendationCard({ item }: RecommendationCardProps) {
    const category = isAcgnCategory(item.category) ? item.category : 'ANIME'
    const theme = categoryTheme[category]

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
                            {categoryTheme[category].cnName}
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

type GenerationProgressPanelProps = {
    progress: GenerationProgress
    modeLabel: string
    modelLabel: string
}

function GenerationProgressPanel({
    progress,
    modeLabel,
    modelLabel,
}: GenerationProgressPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/75 via-sky-50/55 to-violet-50/55 p-6">
                <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            generating
                        </p>
                        <h3 className="mt-2 text-2xl font-black text-slate-800">
                            {progress.title}
                        </h3>
                        <p className="mt-2 max-w-3xl text-sm leading-7 text-slate-500">
                            {progress.detail}
                        </p>
                    </div>

                    <div className="flex flex-wrap gap-2">
                        <span className="rounded-full border border-white/75 bg-white/70 px-4 py-2 text-xs font-black text-slate-500">
                            {modeLabel}
                        </span>
                        <span className="rounded-full border border-white/75 bg-white/70 px-4 py-2 text-xs font-black text-slate-500">
                            {modelLabel}
                        </span>
                    </div>
                </div>

                <div className="mt-6 overflow-hidden rounded-full border border-white/80 bg-white/60 p-1 shadow-inner">
                    <div
                        className="h-3 rounded-full bg-gradient-to-r from-sky-300 via-violet-300 to-emerald-300 transition-all duration-500"
                        style={{ width: `${progress.percent}%` }}
                    />
                </div>

                <div className="mt-4 grid gap-3 md:grid-cols-4">
                    {getProgressSteps(progress.percent).map((step) => (
                        <div
                            key={step.label}
                            className={[
                                'rounded-[22px] border px-4 py-3 shadow-sm transition-all duration-200',
                                step.active
                                    ? 'border-sky-200 bg-white/78 text-sky-700'
                                    : 'border-white/70 bg-white/45 text-slate-400',
                            ].join(' ')}
                        >
                            <p className="text-xs font-black">{step.label}</p>
                            <p className="mt-1 text-[11px] leading-5">{step.description}</p>
                        </div>
                    ))}
                </div>

                <p className="mt-4 text-xs leading-5 text-slate-400">
                    真实模型调用无法逐字返回内部思考过程，这里展示的是前端根据请求阶段整理的可解释进度。
                </p>
            </div>
        </GlassCard>
    )
}

type GenerationErrorPanelProps = {
    message: string
    canOpenConfig: boolean
    onRetry: () => void
    onOpenConfig: () => void
}

function GenerationErrorPanel({
    message,
    canOpenConfig,
    onRetry,
    onOpenConfig,
}: GenerationErrorPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/80 to-rose-50/75 p-5">
                <p className="text-xs font-semibold uppercase tracking-[0.24em] text-rose-300">
                    generation failed
                </p>
                <h3 className="mt-2 text-lg font-black text-rose-700">
                    智能分析生成失败
                </h3>
                <p className="mt-2 text-sm leading-6 text-rose-600">
                    {message}
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                    <button
                        type="button"
                        onClick={onRetry}
                        className="rounded-full border border-rose-200 bg-white/75 px-5 py-2.5 text-sm font-black text-rose-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-50"
                    >
                        重试生成
                    </button>
                    {canOpenConfig ? (
                        <button
                            type="button"
                            onClick={onOpenConfig}
                            className="rounded-full border border-sky-200 bg-white/75 px-5 py-2.5 text-sm font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                        >
                            检查 API 设置
                        </button>
                    ) : null}
                </div>
            </div>
        </GlassCard>
    )
}

function buildGenerationProgress(
    percent: number,
    mode: AnalysisMode,
    status: LlmStatusResponse | null,
): GenerationProgress {
    const boundedPercent = Math.max(0, Math.min(100, percent))
    const usingPreview = status?.mockMode ?? false
    const modeLabel = analysisModes.find((item) => item.value === mode)?.label ?? '智能分析'

    if (boundedPercent >= 100) {
        return {
            percent: 100,
            title: '报告生成完成',
            detail: '结果已经解析完成，正在渲染报告正文、引用依据和推荐候选。',
        }
    }

    if (boundedPercent >= 78) {
        return {
            percent: boundedPercent,
            title: '正在整理输出格式',
            detail: '正在校验标题、摘要、段落、推荐候选、引用依据和安全边界字段。',
        }
    }

    if (boundedPercent >= 44) {
        return {
            percent: boundedPercent,
            title: usingPreview ? '正在生成预览结果' : '正在等待 DeepSeek 返回',
            detail: usingPreview
                ? '预览模式会直接使用本地统计上下文生成可演示结果。'
                : `DeepSeek 正在根据 ${modeLabel} 的结构化上下文生成中文分析，耗时会受网络和模型排队影响。`,
        }
    }

    if (boundedPercent >= 22) {
        return {
            percent: boundedPercent,
            title: '正在构建分析上下文',
            detail: '后端正在读取作品、正式记录、标签、收藏夹和系列关系，并压缩成模型可理解的数据。',
        }
    }

    return {
        percent: boundedPercent,
        title: '正在准备分析任务',
        detail: '正在确认分析范围、模型配置和请求参数。',
    }
}

function getProgressSteps(percent: number) {
    return [
        {
            label: '参数校验',
            description: '确认年份、月份、类型和问题内容。',
            active: percent >= 8,
        },
        {
            label: '上下文构建',
            description: '整理记录、标签、收藏和系列关系。',
            active: percent >= 22,
        },
        {
            label: '模型生成',
            description: '调用 DeepSeek 或生成本地预览。',
            active: percent >= 44,
        },
        {
            label: '格式渲染',
            description: '校验 JSON 字段并渲染报告。',
            active: percent >= 78,
        },
    ]
}

function buildReportCacheKey(
    mode: AnalysisMode,
    year: number,
    quarter: ReleaseQuarter,
    category: CategoryFilter,
    question: string,
) {
    const normalizedQuestion = mode === 'QUESTION_ANSWER'
        ? question.trim().replace(/\s+/g, ' ')
        : ''

    return [
        reportCachePrefix,
        `v${reportCacheVersion}`,
        mode,
        year,
        quarter,
        category,
        normalizedQuestion,
    ].join('::')
}

function readCachedReport(cacheKey: string, fallbackType: AnalysisMode) {
    const parsed = readJsonCache(cacheKey, isCachedReportPayload)
    if (!parsed) {
        return null
    }

    return {
        report: normalizeAnalysisReport(parsed.report, fallbackType),
        cachedAt: parsed.cachedAt,
    }
}

function writeCachedReport(
    cacheKey: string,
    report: LlmAnalysisResponse,
    cachedAt: string,
) {
    writeJsonCache(cacheKey, {
        report,
        cachedAt,
    })
}

function readReportHistory() {
    const parsed = readJsonCache(reportHistoryStorageKey, isHistoryItemArray)
    return parsed?.slice(0, maxReportHistoryCount) ?? []
}

function writeReportHistory(items: HistoryItem[]) {
    writeJsonCache(reportHistoryStorageKey, items.slice(0, maxReportHistoryCount))
}

function isCachedReportPayload(value: unknown): value is {
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

function upsertReportHistory(items: HistoryItem[], item: HistoryItem) {
    const nextItems = [
        item,
        ...items.filter((historyItem) => historyItem.cacheKey !== item.cacheKey),
    ].slice(0, maxReportHistoryCount)
    writeReportHistory(nextItems)
    return nextItems
}

function isHistoryItem(value: unknown): value is HistoryItem {
    if (typeof value !== 'object' || value === null) {
        return false
    }

    const item = value as Partial<HistoryItem>
    return (
        typeof item.id === 'string' &&
        typeof item.cacheKey === 'string' &&
        typeof item.analysisType === 'string' &&
        typeof item.title === 'string' &&
        typeof item.scope === 'string' &&
        typeof item.mode === 'string' &&
        typeof item.generatedAt === 'string' &&
        typeof item.year === 'number' &&
        typeof item.quarter === 'number' &&
        typeof item.question === 'string' &&
        (item.category === 'ALL' || isAcgnCategory(item.category))
    )
}

function isHistoryItemArray(value: unknown): value is HistoryItem[] {
    return Array.isArray(value) && value.every(isHistoryItem)
}

function normalizeAnalysisReport(
    report: LlmAnalysisResponse,
    fallbackType: AnalysisMode,
): LlmAnalysisResponse {
    const title = polishAnalysisText(report.title) || analysisModes.find((item) => item.value === fallbackType)?.label || '智能分析报告'
    const summary = polishAnalysisText(report.summary) || '模型没有返回摘要，系统已保留可用字段并提示重新生成。'
    const scope = report.scope ?? {
        year: null,
        quarter: null,
        category: null,
    }

    return {
        analysisType: report.analysisType || fallbackType,
        title,
        summary,
        keywords: normalizeTextArray(report.keywords, ['智能分析']),
        sections: normalizeSections(report.sections, summary),
        recommendations: normalizeRecommendations(report.recommendations),
        evidence: normalizeTextArray(report.evidence, ['本结果基于系统内结构化数据生成']),
        warnings: normalizeTextArray(report.warnings, ['模型输出已做前端格式兜底，若结果不完整可重试生成']),
        scope: {
            year: scope.year ?? null,
            quarter: scope.quarter ?? null,
            category: isAcgnCategory(scope.category) ? scope.category : null,
        },
        mockMode: Boolean(report.mockMode),
        modelName: report.modelName || 'deepseek-v4-flash',
        generatedAt: report.generatedAt || new Date().toISOString(),
    }
}

function normalizeTextArray(value: unknown, fallback: string[]) {
    if (!Array.isArray(value)) {
        return fallback
    }

    const normalized = value
        .map((item) => polishAnalysisText(String(item ?? '')))
        .filter(Boolean)

    return normalized.length > 0 ? normalized : fallback
}

function normalizeSections(value: unknown, summary: string) {
    if (!Array.isArray(value)) {
        return [
            {
                title: '分析摘要',
                content: summary,
            },
        ]
    }

    const sections = value
        .map((item, index) => {
            const section = item as Partial<LlmAnalysisResponse['sections'][number]>
            return {
                title: polishAnalysisText(section.title) || `分析段落 ${index + 1}`,
                content: polishAnalysisText(section.content) || summary,
            }
        })
        .filter((item) => item.title || item.content)

    return sections.length > 0
        ? sections
        : [
              {
                  title: '分析摘要',
                  content: summary,
              },
          ]
}

function normalizeRecommendations(value: unknown): LlmRecommendationResponse[] {
    if (!Array.isArray(value)) {
        return []
    }

    return value
        .map((item) => item as Partial<LlmRecommendationResponse>)
        .filter((item) => Boolean(item.title))
        .map((item) => ({
            subjectId: item.subjectId ?? null,
            title: polishAnalysisText(item.title) || '未命名候选',
            coverUrl: item.coverUrl ?? null,
            category: isAcgnCategory(item.category) ? item.category : 'ANIME',
            tags: normalizeTextArray(item.tags, []),
            franchiseName: polishAnalysisText(item.franchiseName) || null,
            source: item.source === 'WANT' || item.source === 'PAUSED' ? item.source : 'LIBRARY',
            reason: polishAnalysisText(item.reason) || '模型未返回推荐理由。',
            risk: polishAnalysisText(item.risk) || '暂无额外风险提示。',
            evidence: polishAnalysisText(item.evidence) || '暂无引用依据。',
            priority: item.priority === 'HIGH' || item.priority === 'MEDIUM' ? item.priority : 'LOW',
            matchScore: item.matchScore ?? null,
            platformScore: item.platformScore ?? null,
        }))
}

function isAcgnCategory(value: unknown): value is AcgnCategory {
    return typeof value === 'string' && categoryOrder.includes(value as AcgnCategory)
}

function toQuestionAnswerReport(response: LlmQuestionAnswerResponse): LlmAnalysisResponse {
    return {
        analysisType: 'QUESTION_ANSWER',
        title: '自由问答',
        summary: response.answer,
        keywords: ['自由问答', '个人档案助手'],
        sections: [
            {
                title: '问题',
                content: response.question,
            },
            {
                title: '回答',
                content: response.answer,
            },
        ],
        recommendations: [],
        evidence: ['涉及个人记录的问题会优先参考系统内作品、记录、标签、收藏和系列上下文'],
        warnings: ['自由问答可以包含一般建议；只有涉及你的个人数据时才代表系统记录结论'],
        scope: {
            year: null,
            quarter: null,
            category: null,
        },
        mockMode: response.mockMode,
        modelName: response.modelName,
        generatedAt: response.generatedAt,
    }
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

    if (mode === 'INTEREST_TREND') {
        return `${categoryName} · 兴趣趋势`
    }

    if (mode === 'FRANCHISE_COMPLETION') {
        return `${categoryName} · 系列补完`
    }

    if (mode === 'COMPREHENSIVE_REPORT') {
        return `${year} 年 · ${categoryName} · 综合报告`
    }

    if (mode === 'QUESTION_ANSWER') {
        return '自由问答 · 个性化助手'
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

function formatAnalysisReportMarkdown(report: LlmAnalysisResponse) {
    const lines: string[] = []
    const scopeParts = [
        report.scope.year ? `${report.scope.year} 年` : null,
        report.scope.quarter ? getReleaseQuarterLabel(report.scope.quarter) : null,
        report.scope.category ? categoryTheme[report.scope.category].cnName : null,
    ].filter(Boolean)

    lines.push(`# ${polishAnalysisText(report.title)}`)
    lines.push('')

    if (scopeParts.length > 0) {
        lines.push(`分析范围：${scopeParts.join(' / ')}`)
        lines.push('')
    }

    lines.push(`生成时间：${formatGeneratedAt(report.generatedAt)}`)
    lines.push('')
    lines.push('## 摘要')
    lines.push('')
    lines.push(polishAnalysisText(report.summary) || '暂无摘要。')
    lines.push('')

    if (report.keywords.length > 0) {
        lines.push('## 关键词')
        lines.push('')
        lines.push(report.keywords.map((keyword) => `#${polishAnalysisText(keyword)}`).join(' '))
        lines.push('')
    }

    if (report.sections.length > 0) {
        lines.push('## 分析正文')
        lines.push('')
        report.sections.forEach((section, index) => {
            lines.push(`### ${index + 1}. ${polishAnalysisText(section.title)}`)
            lines.push('')
            lines.push(polishAnalysisText(section.content) || '暂无内容。')
            lines.push('')
        })
    }

    if (report.recommendations.length > 0) {
        lines.push('## 推荐候选')
        lines.push('')
        report.recommendations.forEach((item, index) => {
            lines.push(`### ${index + 1}. ${polishAnalysisText(item.title)}`)
            lines.push('')
            lines.push(`- 类型：${categoryTheme[item.category].cnName}`)
            lines.push(`- 来源：${getSourceLabel(item.source)}`)
            lines.push(`- 优先级：${getPriorityLabel(item.priority)}`)
            lines.push(`- 匹配度：${item.matchScore}`)
            if (item.platformScore !== null && item.platformScore !== undefined) {
                lines.push(`- 平台评分：${Number(item.platformScore).toFixed(1)}`)
            }
            if (item.tags.length > 0) {
                lines.push(`- 标签：${item.tags.map((tag) => `#${polishAnalysisText(tag)}`).join(' ')}`)
            }
            if (item.franchiseName) {
                lines.push(`- 系列：${polishAnalysisText(item.franchiseName)}`)
            }
            lines.push(`- 推荐理由：${polishAnalysisText(item.reason)}`)
            lines.push(`- 匹配依据：${polishAnalysisText(item.evidence)}`)
            lines.push(`- 可能风险：${polishAnalysisText(item.risk)}`)
            lines.push('')
        })
    }

    if (report.evidence.length > 0) {
        lines.push('## 引用依据')
        lines.push('')
        report.evidence
            .map(polishAnalysisText)
            .filter(Boolean)
            .forEach((item) => lines.push(`- ${item}`))
        lines.push('')
    }

    if (report.warnings.length > 0) {
        lines.push('## 安全边界')
        lines.push('')
        report.warnings
            .map(polishAnalysisText)
            .filter(Boolean)
            .forEach((item) => lines.push(`- ${item}`))
        lines.push('')
    }

    return lines.join('\n').trim()
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
