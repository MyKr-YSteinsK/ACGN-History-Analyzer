import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
    getHighScoreRecords,
    type UserSubjectRecordResponse,
} from '../api/recordApi'
import {
    getLlmStatus,
    testLlmConnection,
    updateLlmConfig,
    type LlmAnalysisResponse,
    type LlmAnalysisType,
    type LlmQuestionAnswerResponse,
    type LlmStatusResponse,
} from '../api/llmApi'
import {
    enqueueBackgroundJob,
    registerBackgroundJobUi,
    waitForBackgroundJob,
} from '../api/jobApi'
import { ApiRequestError, getApiErrorMessage } from '../api/request'
import {
    EmptyState,
} from '../components/common/FeedbackState'
import LlmConfigPanel, { type LlmConfigMode } from '../components/analysis/LlmConfigPanel'
import AnalysisReport from '../components/analysis/AnalysisReport'
import {
    CacheStatusPanel,
    GenerationErrorPanel,
    GenerationProgressPanel,
} from '../components/analysis/AnalysisRuntimePanels'
import { AnalysisWorkspacePanel, ScopeSummary } from '../components/analysis/AnalysisControls'
import GenerationQueuePanel from '../components/analysis/GenerationQueuePanel'
import PageHero from '../components/layout/PageHero'
import { HOME_INSIGHT_CACHE_PREFIX } from '../constants/cacheKeys'
import { categoryTheme, neutralPanelGradient } from '../theme/acgnTheme'
import { getReleaseQuarterLabel, type ReleaseQuarter } from '../types/common'
import type {
    AnalysisMode,
    AnalysisModeMeta,
    CategoryFilter,
    GenerationJobStatus,
    ReportHistoryItem,
} from '../types/analysis'
import {
    listCacheKeysByPrefixes,
    readJsonCache,
    removeCacheItem,
    removeCacheItemsByPrefixes,
    writeJsonCache,
} from '../utils/localStorageCache'
import { polishAnalysisText } from '../utils/llmReportFormat'
import { isAcgnCategory, normalizeLlmAnalysisReport } from '../utils/llmAnalysisNormalize'

type GenerationProgress = {
    percent: number
    title: string
    detail: string
}

type CacheInfo = {
    source: 'cache' | 'fresh'
    cachedAt: string
    expiresAt: string
}

type GenerationJob = {
    id: string
    cacheKey: string
    analysisType: AnalysisMode
    modeLabel: string
    scope: string
    year: number
    quarter: ReleaseQuarter
    category: CategoryFilter
    question: string
    status: GenerationJobStatus
    createdAt: string
    completedAt?: string
    message?: string
}

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
        value: 'TYPE_PREFERENCE',
        label: '类型偏好',
        description: '按动画、漫画、游戏、小说拆分长期兴趣重心',
        accent: 'from-fuchsia-100/90 via-white/70 to-sky-50/70',
    },
    {
        value: 'INTEREST_TREND',
        label: '兴趣趋势',
        description: '观察近期口味是否在题材、评分和节奏上转向',
        accent: 'from-lime-100/90 via-white/70 to-cyan-50/70',
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

const defaultProgress: GenerationProgress = {
    percent: 8,
    title: '正在准备分析任务',
    detail: '正在确认分析范围、模型配置和请求参数。',
}

const reportCachePrefix = 'acgn-llm-report-cache'
const reportCacheVersion = 1
const reportHistoryStorageKey = 'acgn-llm-report-history'
const maxReportHistoryCount = 20
const reportCacheTtlMs = 7 * 24 * 60 * 60 * 1000
const allAiCachePrefixes = [reportCachePrefix, HOME_INSIGHT_CACHE_PREFIX]

function AnalysisPage() {
    const [analysisMode, setAnalysisMode] = useState<AnalysisMode>('COMPREHENSIVE_REPORT')
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(1)
    const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL')
    const [question, setQuestion] = useState('2025 年我有哪些高分动画？')
    const [report, setReport] = useState<LlmAnalysisResponse | null>(null)
    const [reportHistory, setReportHistory] = useState<ReportHistoryItem[]>(() => readReportHistory())
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [cacheInfo, setCacheInfo] = useState<CacheInfo | null>(null)
    const [highScoreRecords, setHighScoreRecords] = useState<UserSubjectRecordResponse[]>([])
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
    const [generationJobs, setGenerationJobs] = useState<GenerationJob[]>([])
    const [activeGenerationJob, setActiveGenerationJob] = useState<GenerationJob | null>(null)
    const progressStartedAt = useRef<number | null>(null)
    const processingQueueRef = useRef(false)
    const currentSelectionRef = useRef({
        cacheKey: '',
        analysisType: 'COMPREHENSIVE_REPORT' as AnalysisMode,
    })

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
    currentSelectionRef.current = {
        cacheKey: reportCacheKey,
        analysisType: analysisMode,
    }
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
        const removedCount = pruneExpiredReportCaches()
        if (removedCount <= 0) {
            return
        }

        setReportHistory((items) => {
            const nextItems = items.filter((item) => readCachedReport(item.cacheKey, item.analysisType))
            writeReportHistory(nextItems)
            return nextItems
        })
    }, [])

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
            setProgress(buildGenerationProgress(
                nextPercent,
                activeGenerationJob?.analysisType ?? analysisMode,
                llmStatus,
            ))
        }, 650)

        return () => window.clearInterval(timer)
    }, [activeGenerationJob, analysisMode, llmStatus, loading])

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
                expiresAt: cached.expiresAt,
            })
        setError(null)
            return
        }

        setReport(null)
        setCacheInfo(null)
    }, [analysisMode, question, reportCacheKey])

    useEffect(() => {
        async function loadHighScoreRecords() {
            try {
                const records = await getHighScoreRecords({
                    year: usesYearFilter ? selectedYear : undefined,
                    quarter: usesQuarterFilter ? selectedQuarter : undefined,
                    minScore: 45,
                })
                const filteredRecords = records
                    .filter((record) => (
                        selectedCategory === 'ALL' || record.subjectCategory === selectedCategory
                    ))
                    .sort((first, second) => (second.scoreValue ?? -1) - (first.scoreValue ?? -1))
                    .slice(0, 6)
                setHighScoreRecords(filteredRecords)
            } catch {
                setHighScoreRecords([])
            }
        }

        if (analysisMode === 'QUESTION_ANSWER') {
            setHighScoreRecords([])
            return
        }

        void loadHighScoreRecords()
    }, [
        analysisMode,
        selectedCategory,
        selectedQuarter,
        selectedYear,
        usesQuarterFilter,
        usesYearFilter,
    ])

    const runGenerationJob = useCallback(async (job: GenerationJob) => {
        processingQueueRef.current = true
        setLoading(true)
        setActiveGenerationJob(job)
        setProgress(buildGenerationProgress(8, job.analysisType, llmStatus))
        setGenerationJobs((items) => items.map((item) => (
            item.id === job.id
                ? { ...item, status: 'running', message: '正在生成' }
                : item
        )))

        try {
            const response = await requestAnalysisResponse(job)
            const normalizedReport = normalizeLlmAnalysisReport(
                response,
                job.analysisType as LlmAnalysisType,
                analysisModes.find((item) => item.value === job.analysisType)?.label,
            )
            const cachedAt = new Date().toISOString()
            writeCachedReport(job.cacheKey, normalizedReport, cachedAt)
            setReportHistory((items) => upsertReportHistory(items, {
                id: `${cachedAt}-${job.analysisType}`,
                cacheKey: job.cacheKey,
                analysisType: job.analysisType,
                title: polishAnalysisText(normalizedReport.title),
                scope: job.scope,
                mode: job.modeLabel,
                generatedAt: cachedAt,
                year: job.year,
                quarter: job.quarter,
                category: job.category,
                question: job.question,
            }))
            setGenerationJobs((items) => trimFinishedGenerationJobs(items.map((item) => (
                item.id === job.id
                    ? {
                          ...item,
                          status: 'completed',
                          completedAt: cachedAt,
                          message: polishAnalysisText(normalizedReport.title) || '已生成',
                      }
                    : item
            ))))
            setProgress(buildGenerationProgress(100, job.analysisType, llmStatus))

            if (currentSelectionRef.current.cacheKey === job.cacheKey) {
                setReport(normalizedReport)
                setCacheInfo({
                    source: 'fresh',
                    cachedAt,
                    expiresAt: getReportCacheExpiresAt(cachedAt),
                })
                setError(null)
            }
        } catch (generateError) {
            const cached = readCachedReport(job.cacheKey, job.analysisType)
            const errorMessage = getApiErrorMessage(generateError, '智能分析生成失败')
            setGenerationJobs((items) => trimFinishedGenerationJobs(items.map((item) => (
                item.id === job.id
                    ? {
                          ...item,
                          status: 'failed',
                          completedAt: new Date().toISOString(),
                          message: cached ? `${errorMessage}，已保留缓存` : errorMessage,
                      }
                    : item
            ))))

            if (currentSelectionRef.current.cacheKey === job.cacheKey) {
                if (cached) {
                    setReport(cached.report)
                    setCacheInfo({
                        source: 'cache',
                        cachedAt: cached.cachedAt,
                        expiresAt: cached.expiresAt,
                    })
                    setError(`${errorMessage}。已回退到本地缓存结果。`)
                } else {
                    setReport(null)
                    setCacheInfo(null)
                    setError(errorMessage)
                }
            }
            setProgress(defaultProgress)
        } finally {
            processingQueueRef.current = false
            setLoading(false)
            setActiveGenerationJob(null)
            setGenerationJobs((items) => [...items])
        }
    }, [llmStatus])

    useEffect(() => {
        if (processingQueueRef.current) {
            return
        }

        const nextJob = generationJobs.find((job) => job.status === 'queued')
        if (!nextJob) {
            return
        }

        void runGenerationJob(nextJob)
    }, [generationJobs, runGenerationJob])

    const generateReport = useCallback(() => {
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

        setError(null)

        const createdAt = new Date().toISOString()
        const job: GenerationJob = {
            id: `${createdAt}-${analysisMode}-${Math.random().toString(36).slice(2, 8)}`,
            cacheKey: reportCacheKey,
            analysisType: analysisMode,
            modeLabel: activeMode.label,
            scope: scopeText,
                            year: selectedYear,
                            quarter: selectedQuarter,
            category: selectedCategory,
            question: question.trim(),
            status: 'queued',
            createdAt,
            message: '等待前面的报告生成完成',
        }
        setGenerationJobs((items) => [
            ...items,
            job,
        ].slice(-12))
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
                maxTokens: 3000,
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
                maxTokens: 3000,
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

    const restoreHistoryItem = useCallback((item: ReportHistoryItem) => {
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
            expiresAt: cached.expiresAt,
        })
        setError(null)
    }, [])

    const clearReportHistory = useCallback(() => {
        writeReportHistory([])
        setReportHistory([])
    }, [])

    const deleteReportHistoryItem = useCallback((item: ReportHistoryItem) => {
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

    const pruneExpiredReportHistory = useCallback(() => {
        pruneExpiredReportCaches()
        setReportHistory((items) => {
            const nextItems = items.filter((item) => readCachedReport(item.cacheKey, item.analysisType))
            writeReportHistory(nextItems)
            return nextItems
        })

        const cached = readCachedReport(reportCacheKey, analysisMode)
        if (!cached && cacheInfo?.source === 'cache') {
            setReport(null)
            setCacheInfo(null)
        }
    }, [analysisMode, cacheInfo, reportCacheKey])

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

            <AnalysisWorkspacePanel
                panelGradient={panelGradient}
                activeMode={activeMode}
                analysisModes={analysisModes}
                analysisMode={analysisMode}
                            loading={loading}
                reportReady={Boolean(report)}
                            selectedYear={selectedYear}
                            selectedQuarter={selectedQuarter}
                            selectedCategory={selectedCategory}
                            usesYearFilter={usesYearFilter}
                            usesQuarterFilter={usesQuarterFilter}
                usesQuestionInput={usesQuestionInput}
                question={question}
                onModeChange={handleModeChange}
                            onYearChange={setSelectedYear}
                            onQuarterChange={setSelectedQuarter}
                            onCategoryChange={setSelectedCategory}
                onQuestionChange={setQuestion}
                            onSubmit={() => void generateReport()}
                        />

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

            {loading && activeGenerationJob ? (
                <GenerationProgressPanel
                    progress={progress}
                    modeLabel={activeGenerationJob.modeLabel}
                    modelLabel={llmStatus?.mockMode ? '预览模式' : (llmStatus?.model ?? 'DeepSeek API')}
                />
            ) : null}

            {generationJobs.length > 0 ? (
                <GenerationQueuePanel
                    jobs={generationJobs}
                    onClearFinished={() => setGenerationJobs((items) => (
                        items.filter((item) => item.status === 'queued' || item.status === 'running')
                    ))}
                />
            ) : null}

            {report ? (
                <AnalysisReport
                    report={report}
                    history={reportHistory}
                    activeCategory={selectedCategory}
                    highScoreRecords={highScoreRecords}
                    onSelectHistory={restoreHistoryItem}
                    onDeleteHistory={deleteReportHistoryItem}
                    onPruneInvalidHistory={pruneInvalidReportHistory}
                    onPruneExpiredHistory={pruneExpiredReportHistory}
                    onClearHistory={clearReportHistory}
                />
            ) : (
                <EmptyState
                    title="等待生成分析结果"
                    description="当前筛选还没有本地缓存。点击生成会把任务加入队列，后台依次生成；你可以继续切换条件并继续添加其它报告。"
                />
            )}
        </div>
    )
}

function buildLlmJobPayload(job: GenerationJob): Record<string, unknown> {
    const category = job.category === 'ALL' ? undefined : job.category

    switch (job.analysisType) {
        case 'QUESTION_ANSWER':
            return { llmKind: 'QUESTION_ANSWER', question: job.question }
        case 'QUARTERLY':
            return {
                llmKind: 'QUARTERLY',
                year: job.year,
                quarter: job.quarter,
                category,
            }
        case 'TYPE_PREFERENCE':
            return {
                llmKind: 'TYPE_PREFERENCE',
                category: job.category === 'ALL' ? 'ANIME' : job.category,
            }
        case 'SCORE_STRUCTURE':
            return { llmKind: 'SCORE_STRUCTURE', category }
        case 'TAG_PREFERENCE':
            return { llmKind: 'TAG_PREFERENCE', category }
        case 'RECOMMENDATION':
            return { llmKind: 'RECOMMENDATION', category }
        case 'COLLECTION_BACKLOG':
            return { llmKind: 'COLLECTION_BACKLOG', category }
        case 'TASTE_PROFILE':
            return { llmKind: 'TASTE_PROFILE', category }
        case 'PREFERENCE_CONTRADICTION':
            return { llmKind: 'PREFERENCE_CONTRADICTION', category }
        case 'INTEREST_TREND':
            return { llmKind: 'INTEREST_TREND', category }
        case 'FRANCHISE_COMPLETION':
            return { llmKind: 'FRANCHISE_COMPLETION', category }
        case 'COMPREHENSIVE_REPORT':
            return { llmKind: 'COMPREHENSIVE_REPORT', year: job.year, category }
        case 'YEARLY':
            return { llmKind: 'YEARLY', year: job.year, category }
        default:
            return { llmKind: 'YEARLY', year: job.year, category }
    }
}

function parseLlmBackgroundJobResult(
    resultJson: string | null,
    analysisType: AnalysisMode,
): LlmAnalysisResponse {
    const root = JSON.parse(resultJson ?? '{}') as Record<string, unknown>

    if (analysisType === 'QUESTION_ANSWER') {
        return toQuestionAnswerReport(root.llmQuestionAnswer as LlmQuestionAnswerResponse)
    }

    return normalizeLlmAnalysisReport(
        root.llmAnalysis as LlmAnalysisResponse,
        analysisType as LlmAnalysisType,
        analysisModes.find((item) => item.value === analysisType)?.label,
    )
}

async function requestAnalysisResponse(job: GenerationJob) {
    const payload = buildLlmJobPayload(job)
    const { jobId } = await enqueueBackgroundJob({
        jobType: 'LLM_ANALYSIS',
        payload,
    })
    registerBackgroundJobUi(jobId, {
        silent: false,
        title: `${job.modeLabel} · ${job.scope}`,
        href: '/analysis',
    })
    const res = await waitForBackgroundJob(jobId)

    if (res.status === 'FAILED' || res.status === 'CANCELLED') {
        throw new ApiRequestError(
            res.status === 'CANCELLED' ? '任务已取消' : (res.errorMessage || '生成失败'),
        )
    }

    return parseLlmBackgroundJobResult(res.resultJson, job.analysisType)
}

function trimFinishedGenerationJobs(jobs: GenerationJob[]) {
    const activeJobs = jobs.filter((job) => job.status === 'queued' || job.status === 'running')
    const finishedJobs = jobs.filter((job) => job.status !== 'queued' && job.status !== 'running')
    return [
        ...activeJobs,
        ...finishedJobs.slice(-6),
    ].slice(-12)
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

    const expiresAt = parsed.expiresAt ?? getReportCacheExpiresAt(parsed.cachedAt)
    if (isExpiredAt(expiresAt)) {
        removeCacheItem(cacheKey)
        return null
    }

    return {
        report: normalizeLlmAnalysisReport(
            parsed.report,
            fallbackType as LlmAnalysisType,
            analysisModes.find((item) => item.value === fallbackType)?.label,
        ),
        cachedAt: parsed.cachedAt,
        expiresAt,
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
        expiresAt: getReportCacheExpiresAt(cachedAt),
    })
}

function getReportCacheExpiresAt(cachedAt: string) {
    const cachedTime = new Date(cachedAt).getTime()
    const baseTime = Number.isNaN(cachedTime) ? Date.now() : cachedTime
    return new Date(baseTime + reportCacheTtlMs).toISOString()
}

function isExpiredAt(expiresAt: string) {
    const expiresTime = new Date(expiresAt).getTime()
    return Number.isNaN(expiresTime) || expiresTime <= Date.now()
}

function pruneExpiredReportCaches() {
    let removedCount = 0

    listCacheKeysByPrefixes([reportCachePrefix]).forEach((key) => {
        const parsed = readJsonCache(key, isCachedReportPayload)
        if (!parsed) {
            removeCacheItem(key)
            removedCount += 1
            return
        }

        const expiresAt = parsed.expiresAt ?? getReportCacheExpiresAt(parsed.cachedAt)
        if (isExpiredAt(expiresAt)) {
            removeCacheItem(key)
            removedCount += 1
        }
    })

    return removedCount
}

function readReportHistory() {
    const parsed = readJsonCache(reportHistoryStorageKey, isHistoryItemArray)
    return parsed?.slice(0, maxReportHistoryCount) ?? []
}

function writeReportHistory(items: ReportHistoryItem[]) {
    writeJsonCache(reportHistoryStorageKey, items.slice(0, maxReportHistoryCount))
}

function isCachedReportPayload(value: unknown): value is {
    report: LlmAnalysisResponse
    cachedAt: string
    expiresAt?: string
} {
    if (typeof value !== 'object' || value === null) {
        return false
    }

    const payload = value as {
        report?: unknown
        cachedAt?: unknown
        expiresAt?: unknown
    }

    return typeof payload.report === 'object'
        && payload.report !== null
        && typeof payload.cachedAt === 'string'
        && (payload.expiresAt === undefined || typeof payload.expiresAt === 'string')
}

function upsertReportHistory(items: ReportHistoryItem[], item: ReportHistoryItem) {
    const nextItems = [
        item,
        ...items.filter((historyItem) => historyItem.cacheKey !== item.cacheKey),
    ].slice(0, maxReportHistoryCount)
    writeReportHistory(nextItems)
    return nextItems
}

function isHistoryItem(value: unknown): value is ReportHistoryItem {
    if (typeof value !== 'object' || value === null) {
        return false
    }

    const item = value as Partial<ReportHistoryItem>
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

function isHistoryItemArray(value: unknown): value is ReportHistoryItem[] {
    return Array.isArray(value) && value.every(isHistoryItem)
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

export default AnalysisPage
