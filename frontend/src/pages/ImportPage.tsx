import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
    getCollectionImportTemplate,
    getBangumiSeasonImportJobStatus,
    getImportOverview,
    getRecordImportTemplate,
    getSubjectImportTemplate,
    previewBangumiSeasonImport,
    startBangumiSeasonImportJob,
    type BangumiSeasonImportRequest,
    type BangumiImportJobStatusResponse,
    type ImportOverviewResponse,
    type ImportTemplateResponse,
} from '../api/importApi'
import {
    importCollections,
    previewImportCollections,
    type UserSubjectCollectionBatchImportRequest,
    type UserSubjectCollectionImportItemResponse,
    type UserSubjectCollectionImportPreviewItemResponse,
    type UserSubjectCollectionImportPreviewResponse,
    type UserSubjectCollectionImportResponse,
} from '../api/collectionApi'
import {
    importRecords,
    previewImportRecords,
    type UserSubjectRecordBatchImportRequest,
    type UserSubjectRecordImportItemResponse,
    type UserSubjectRecordImportPreviewItemResponse,
    type UserSubjectRecordImportPreviewResponse,
    type UserSubjectRecordImportResponse,
} from '../api/recordApi'
import {
    type SubjectImportItemResponse,
    type SubjectImportPreviewItemResponse,
    type SubjectImportPreviewResponse,
    type SubjectImportResponse,
} from '../api/subjectApi'
import { FeedbackBanner } from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import { quarterTheme } from '../theme/acgnTheme'
import type { ReleaseQuarter } from '../types/common'

type ImportTarget = 'SUBJECT' | 'COLLECTION' | 'RECORD'

type ImportMode = 'JSON' | 'CSV' | 'EXCEL' | 'BANGUMI' | 'BACKUP' | 'MANUAL'

type ParsedPayload =
    | UserSubjectCollectionBatchImportRequest
    | UserSubjectRecordBatchImportRequest

type PreviewState =
    | {
          target: 'SUBJECT'
          data: SubjectImportPreviewResponse
      }
    | {
          target: 'RECORD'
          data: UserSubjectRecordImportPreviewResponse
      }
    | {
          target: 'COLLECTION'
          data: UserSubjectCollectionImportPreviewResponse
      }

type ImportResultState =
    | {
          target: 'SUBJECT'
          data: SubjectImportResponse
      }
    | {
          target: 'RECORD'
          data: UserSubjectRecordImportResponse
      }
    | {
          target: 'COLLECTION'
          data: UserSubjectCollectionImportResponse
      }

type PreviewItem = {
    title: string
    result: string
    message: string
    sourcePlatform?: string | null
    sourceId?: string | null
}

type BangumiPreviewProgress = {
    active: boolean
    percent: number
    message: string
}

type ImportItemFilter = 'ALL' | 'SUCCESS' | 'WARNING' | 'ERROR'

const importModes: Array<{
    value: ImportMode
    title: string
    description: string
    enabled: boolean
}> = [
    {
        value: 'JSON',
        title: 'JSON 粘贴',
        description: '使用模板 JSON 预览后写入',
        enabled: true,
    },
    {
        value: 'CSV',
        title: 'CSV 批量',
        description: '首行使用模板字段名',
        enabled: true,
    },
    {
        value: 'EXCEL',
        title: 'Excel',
        description: '表格批量导入入口',
        enabled: false,
    },
    {
        value: 'BANGUMI',
        title: 'Bangumi 新番',
        description: '按年份档期自动拉取动画',
        enabled: true,
    },
    {
        value: 'BACKUP',
        title: 'JSON 备份',
        description: '全量备份恢复入口',
        enabled: false,
    },
    {
        value: 'MANUAL',
        title: '快速录入',
        description: '轻量手动录入入口',
        enabled: false,
    },
]

const numericFields = new Set([
    'subjectId',
    'franchiseId',
    'seasonIndex',
    'partIndex',
    'releaseYear',
    'releaseQuarter',
    'platformScore',
    'episodeCount',
    'subjectSeasonIndex',
    'subjectPartIndex',
    'scoreValue',
    'recordYear',
    'recordQuarter',
])

const arrayFields = new Set(['tagNames'])

const importTips = [
    {
        title: '作品唯一键',
        description: 'titleCn + subtitle + seasonIndex + partIndex + category 全部相同视为重复作品。',
    },
    {
        title: '标签写 tagNames',
        description: '推荐 3-7 个中文标签。JSON 用数组，CSV 可用 战斗|奇幻|漫画改。',
    },
    {
        title: '封面可填远程 URL',
        description: '正式导入时后端会尝试下载到 uploads/covers，并返回 /api/assets/covers 路径。',
    },
    {
        title: '收藏 / 记录匹配作品',
        description: '收藏可用 subjectId 或作品唯一键；记录用作品唯一键定位已有作品。',
    },
]

const importFlowSteps = [
    {
        step: 1,
        title: '选择对象',
        description: '作品库、收藏夹或个人记录',
    },
    {
        step: 2,
        title: '准备内容',
        description: '粘贴 JSON / CSV 或读取文件',
    },
    {
        step: 3,
        title: '预览校验',
        description: '检查可导入、警告和错误',
    },
    {
        step: 4,
        title: '确认写入',
        description: '合法数据写入数据库',
    },
]

const bangumiQuarterOptions: Array<{
    value: ReleaseQuarter
    label: string
    month: number
}> = [
    { value: 1, label: '一月', month: 1 },
    { value: 2, label: '四月', month: 4 },
    { value: 3, label: '七月', month: 7 },
    { value: 4, label: '十月', month: 10 },
]

function getErrorMessage(error: unknown) {
    if (error instanceof Error) {
        return error.message
    }

    return '请求失败，请稍后重试'
}

function wait(milliseconds: number) {
    return new Promise((resolve) => {
        window.setTimeout(resolve, milliseconds)
    })
}

function getTargetRootKey(target: ImportTarget) {
    if (target === 'SUBJECT') {
        return 'subjects'
    }
    if (target === 'COLLECTION') {
        return 'collections'
    }
    return 'records'
}

function getTargetTitle(target: ImportTarget) {
    if (target === 'SUBJECT') {
        return '作品库'
    }
    if (target === 'COLLECTION') {
        return '收藏夹'
    }
    return '个人记录'
}

function getImportItemFilter(item: PreviewItem): Exclude<ImportItemFilter, 'ALL'> {
    if (item.result === 'CAN_IMPORT' || item.result === 'IMPORTED') {
        return 'SUCCESS'
    }

    if (item.result === 'UPDATED') {
        return 'WARNING'
    }

    if (item.result === 'SKIPPED') {
        return 'WARNING'
    }

    if (item.result === 'ERROR') {
        return 'ERROR'
    }

    return 'WARNING'
}

function getPreviewItemKey(item: PreviewItem, index: number) {
    if (item.sourceId) {
        return `${item.sourcePlatform ?? 'SOURCE'}:${item.sourceId}`
    }

    return `${item.title}:${index}`
}

function isImportablePreviewItem(item: PreviewItem) {
    return item.result === 'CAN_IMPORT'
}

function stringifyTemplate(template: ImportTemplateResponse | null) {
    if (!template) {
        return ''
    }

    return JSON.stringify(template.templateBody, null, 2)
}

function parseCsvRows(text: string) {
    const rows: string[][] = []
    let current = ''
    let row: string[] = []
    let inQuotes = false

    for (let index = 0; index < text.length; index += 1) {
        const char = text[index]
        const next = text[index + 1]

        if (char === '"' && inQuotes && next === '"') {
            current += '"'
            index += 1
            continue
        }

        if (char === '"') {
            inQuotes = !inQuotes
            continue
        }

        if (char === ',' && !inQuotes) {
            row.push(current.trim())
            current = ''
            continue
        }

        if ((char === '\n' || char === '\r') && !inQuotes) {
            if (char === '\r' && next === '\n') {
                index += 1
            }

            row.push(current.trim())
            current = ''

            if (row.some((cell) => cell.length > 0)) {
                rows.push(row)
            }

            row = []
            continue
        }

        current += char
    }

    row.push(current.trim())

    if (row.some((cell) => cell.length > 0)) {
        rows.push(row)
    }

    return rows
}

function normalizeCsvValue(fieldName: string, value: string) {
    if (value === '') {
        return null
    }

    if (arrayFields.has(fieldName)) {
        return parseCsvArrayValue(value)
    }

    if (numericFields.has(fieldName)) {
        const numberValue = Number(value)
        return Number.isNaN(numberValue) ? value : numberValue
    }

    return value
}

function parseCsvArrayValue(value: string) {
    const trimmedValue = value.trim()

    if (!trimmedValue) {
        return []
    }

    if (trimmedValue.startsWith('[')) {
        const parsed = JSON.parse(trimmedValue) as unknown
        if (!Array.isArray(parsed)) {
            throw new Error('tagNames JSON 必须是数组')
        }

        return parsed
            .map((item) => String(item).trim())
            .filter(Boolean)
    }

    return trimmedValue
        .split(/[|;；、，,]/)
        .map((item) => item.trim())
        .filter(Boolean)
}

function escapeCsvCell(value: string) {
    if (/[",\r\n]/.test(value)) {
        return `"${value.replaceAll('"', '""')}"`
    }

    return value
}

function getCsvExampleValue(fieldName: string, example?: string | null) {
    if (fieldName === 'tagNames') {
        return '战斗|奇幻|漫画改'
    }

    return example ?? ''
}

function buildCsvTemplate(template: ImportTemplateResponse) {
    const headers = template.fields.map((field) => field.fieldName).join(',')
    const values = template.fields
        .map((field) => escapeCsvCell(getCsvExampleValue(field.fieldName, field.example)))
        .join(',')

    return `${headers}\n${values}`
}

function parseCsvPayload(text: string, target: ImportTarget) {
    const rows = parseCsvRows(text)

    if (rows.length < 2) {
        throw new Error('CSV 至少需要一行字段名和一行数据')
    }

    const [headers, ...dataRows] = rows
    const items = dataRows.map((row) => {
        const item: Record<string, unknown> = {}

        headers.forEach((header, index) => {
            if (!header) {
                return
            }

            item[header] = normalizeCsvValue(header, row[index] ?? '')
        })

        return item
    })

    return {
        [getTargetRootKey(target)]: items,
    }
}

function parseDraftPayload(text: string, mode: ImportMode, target: ImportTarget): ParsedPayload {
    const trimmedText = text.trim()

    if (!trimmedText) {
        throw new Error('导入内容不能为空')
    }

    if (mode === 'CSV') {
        return parseCsvPayload(trimmedText, target) as ParsedPayload
    }

    if (mode !== 'JSON') {
        throw new Error('当前导入方式需要先使用 JSON 或 CSV 模板完成校验')
    }

    const parsed = JSON.parse(trimmedText) as unknown

    if (!parsed || typeof parsed !== 'object') {
        throw new Error('JSON 顶层必须是对象')
    }

    const rootKey = getTargetRootKey(target)
    const rootValue = (parsed as Record<string, unknown>)[rootKey]

    if (!Array.isArray(rootValue)) {
        throw new Error(`JSON 中必须包含 ${rootKey} 数组`)
    }

    return parsed as ParsedPayload
}

function getPreviewItems(preview: PreviewState | null): PreviewItem[] {
    if (!preview) {
        return []
    }

    if (preview.target === 'SUBJECT') {
        return preview.data.items.map((item: SubjectImportPreviewItemResponse) => ({
            title: item.displayTitle,
            result: item.result,
            message: item.message,
            sourcePlatform: item.sourcePlatform,
            sourceId: item.sourceId,
        }))
    }

    if (preview.target === 'COLLECTION') {
        return preview.data.items.map((item: UserSubjectCollectionImportPreviewItemResponse) => ({
            title: item.subjectDisplayTitle,
            result: item.result,
            message: item.message,
        }))
    }

    return preview.data.items.map((item: UserSubjectRecordImportPreviewItemResponse) => ({
        title: item.subjectDisplayTitle,
        result: item.result,
        message: item.message,
    }))
}

function getImportResultItems(result: ImportResultState | null): PreviewItem[] {
    if (!result) {
        return []
    }

    if (result.target === 'SUBJECT') {
        return result.data.items.map((item: SubjectImportItemResponse) => ({
            title: item.displayTitle,
            result: item.result,
            message: item.message,
        }))
    }

    if (result.target === 'COLLECTION') {
        return result.data.items.map((item: UserSubjectCollectionImportItemResponse) => ({
            title: item.subjectDisplayTitle,
            result: item.result,
            message: item.message,
        }))
    }

    return result.data.items.map((item: UserSubjectRecordImportItemResponse) => ({
        title: item.subjectDisplayTitle,
        result: item.result,
        message: item.message,
    }))
}

function pickTemplate(
    target: ImportTarget,
    subjectTemplate: ImportTemplateResponse | null,
    collectionTemplate: ImportTemplateResponse | null,
    recordTemplate: ImportTemplateResponse | null,
) {
    if (target === 'SUBJECT') {
        return subjectTemplate
    }
    if (target === 'COLLECTION') {
        return collectionTemplate
    }
    return recordTemplate
}

function isModeAllowedForTarget(mode: ImportMode, target: ImportTarget) {
    if (target === 'SUBJECT') {
        return mode === 'BANGUMI'
    }

    return mode === 'JSON' || mode === 'CSV'
}

function ImportPage() {
    const [overview, setOverview] = useState<ImportOverviewResponse | null>(null)
    const [subjectTemplate, setSubjectTemplate] = useState<ImportTemplateResponse | null>(null)
    const [collectionTemplate, setCollectionTemplate] =
        useState<ImportTemplateResponse | null>(null)
    const [recordTemplate, setRecordTemplate] = useState<ImportTemplateResponse | null>(null)
    const [selectedTarget, setSelectedTarget] = useState<ImportTarget>('SUBJECT')
    const [selectedMode, setSelectedMode] = useState<ImportMode>('BANGUMI')
    const [draftText, setDraftText] = useState('')
    const [preview, setPreview] = useState<PreviewState | null>(null)
    const [excludedPreviewKeys, setExcludedPreviewKeys] = useState<string[]>([])
    const [importResult, setImportResult] = useState<ImportResultState | null>(null)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [loadingDocs, setLoadingDocs] = useState(false)
    const [submitting, setSubmitting] = useState(false)
    const [bangumiYear, setBangumiYear] = useState(() => new Date().getFullYear())
    const [bangumiQuarter, setBangumiQuarter] = useState<ReleaseQuarter>(1)
    const [bangumiIncludeWeb, setBangumiIncludeWeb] = useState(false)
    const [bangumiJapaneseOnly, setBangumiJapaneseOnly] = useState(true)
    const [bangumiJobStatus, setBangumiJobStatus] =
        useState<BangumiImportJobStatusResponse | null>(null)
    const [bangumiPreviewProgress, setBangumiPreviewProgress] =
        useState<BangumiPreviewProgress | null>(null)
    const fileInputRef = useRef<HTMLInputElement | null>(null)

    const selectedTemplate = pickTemplate(
        selectedTarget,
        subjectTemplate,
        collectionTemplate,
        recordTemplate,
    )
    const selectedRootKey = getTargetRootKey(selectedTarget)
    const previewItems = useMemo(() => getPreviewItems(preview), [preview])
    const activePreviewItems = useMemo(
        () =>
            previewItems.filter(
                (item, index) =>
                    !excludedPreviewKeys.includes(getPreviewItemKey(item, index)),
            ),
        [excludedPreviewKeys, previewItems],
    )
    const resultItems = useMemo(() => getImportResultItems(importResult), [importResult])
    const previewItemMetrics = useMemo(() => {
        return previewItems.reduce(
            (accumulator, item) => {
                if (item.result === 'ERROR') {
                    accumulator.error += 1
                } else if (item.result === 'UPDATED') {
                    accumulator.updated += 1
                } else if (item.result === 'SKIPPED') {
                    accumulator.skipped += 1
                } else {
                    accumulator.canImport += 1
                }
                return accumulator
            },
            { canImport: 0, updated: 0, skipped: 0, error: 0 },
        )
    }, [previewItems])

    const activePreview =
        preview && preview.target === selectedTarget ? preview.data : null
    const activeResult =
        importResult && importResult.target === selectedTarget ? importResult.data : null
    const isBangumiMode = selectedMode === 'BANGUMI'
    const bangumiSelectedQuarter = bangumiQuarterOptions.find(
        (option) => option.value === bangumiQuarter,
    )
    const bangumiPayload = useMemo<BangumiSeasonImportRequest>(
        () => ({
            year: bangumiYear,
            quarter: bangumiQuarter,
            includeWeb: bangumiIncludeWeb,
            japaneseOnly: bangumiJapaneseOnly,
            excludedSourceIds: previewItems
                .filter((item, index) => excludedPreviewKeys.includes(getPreviewItemKey(item, index)))
                .filter((item) => item.sourcePlatform === 'BANGUMI' && item.sourceId)
                .map((item) => item.sourceId as string),
        }),
        [
            bangumiIncludeWeb,
            bangumiJapaneseOnly,
            bangumiQuarter,
            bangumiYear,
            excludedPreviewKeys,
            previewItems,
        ],
    )
    const activeCanImportCount = activePreviewItems.filter(isImportablePreviewItem).length

    const draftLineCount = useMemo(() => {
        return draftText.trim() ? draftText.trim().split(/\r?\n/).length : 0
    }, [draftText])
    const currentFlowStep = activeResult
        ? 4
        : activePreview
          ? 3
          : isBangumiMode || draftText.trim()
            ? 2
            : 1

    const loadDocs = useCallback(async () => {
        setLoadingDocs(true)
        setFeedbackMessage(null)

        try {
            const [
                overviewResponse,
                subjectTemplateResponse,
                collectionTemplateResponse,
                recordTemplateResponse,
            ] =
                await Promise.all([
                    getImportOverview(),
                    getSubjectImportTemplate(),
                    getCollectionImportTemplate(),
                    getRecordImportTemplate(),
                ])

            setOverview(overviewResponse)
            setSubjectTemplate(subjectTemplateResponse)
            setCollectionTemplate(collectionTemplateResponse)
            setRecordTemplate(recordTemplateResponse)
            setDraftText('')
        } catch (error) {
            setFeedbackMessage(getErrorMessage(error))
        } finally {
            setLoadingDocs(false)
        }
    }, [])

    useEffect(() => {
        // 导入页打开后先加载后端提供的导入说明和模板字段。
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadDocs()
    }, [loadDocs])

    function resetResultState() {
        setPreview(null)
        setExcludedPreviewKeys([])
        setImportResult(null)
        setFeedbackMessage(null)
        setBangumiJobStatus(null)
        setBangumiPreviewProgress(null)
    }

    async function waitForBangumiImportJob(jobId: string) {
        for (let attempt = 0; attempt < 300; attempt += 1) {
            await wait(1000)
            const status = await getBangumiSeasonImportJobStatus(jobId)
            setBangumiJobStatus(status)

            if (status.status === 'SUCCESS') {
                if (status.result) {
                    setImportResult({ target: 'SUBJECT', data: status.result })
                    setFeedbackMessage(`已从 Bangumi 导入 ${status.result.importedCount} 条作品`)
                } else {
                    setFeedbackMessage('Bangumi 导入完成')
                }
                return
            }

            if (status.status === 'FAILED') {
                throw new Error(status.errorMessage || 'Bangumi 导入失败')
            }
        }

        throw new Error('Bangumi 导入任务等待超时，请稍后刷新作品库确认结果')
    }

    function handleTargetChange(target: ImportTarget) {
        setSelectedTarget(target)
        resetResultState()

        if (target === 'SUBJECT') {
            setSelectedMode('BANGUMI')
            setDraftText('')
            return
        }

        if (selectedMode === 'BANGUMI') {
            setSelectedMode('JSON')
        }

        const nextTemplate = pickTemplate(
            target,
            subjectTemplate,
            collectionTemplate,
            recordTemplate,
        )

        if (nextTemplate) {
            setDraftText(stringifyTemplate(nextTemplate))
        }
    }

    function handleModeChange(mode: ImportMode) {
        if (!isModeAllowedForTarget(mode, selectedTarget)) {
            return
        }

        setSelectedMode(mode)
        resetResultState()

        if (mode === 'BANGUMI') {
            setSelectedTarget('SUBJECT')
            setDraftText('')
            return
        }

        if (mode === 'JSON' && selectedTemplate) {
            setDraftText(stringifyTemplate(selectedTemplate))
            return
        }

        if (mode === 'CSV' && selectedTemplate) {
            setDraftText(buildCsvTemplate(selectedTemplate))
        }
    }

    function handleLoadTemplate() {
        if (isBangumiMode || !selectedTemplate) {
            return
        }

        resetResultState()
        setDraftText(
            selectedMode === 'CSV'
                ? buildCsvTemplate(selectedTemplate)
                : stringifyTemplate(selectedTemplate),
        )
    }

    async function handleCopyTemplate() {
        if (isBangumiMode || !selectedTemplate) {
            return
        }

        const templateText =
            selectedMode === 'CSV'
                ? buildCsvTemplate(selectedTemplate)
                : stringifyTemplate(selectedTemplate)

        try {
            await navigator.clipboard.writeText(templateText)
            setFeedbackMessage('已复制当前模板')
        } catch {
            setFeedbackMessage('浏览器阻止了复制，请使用“载入模板”后手动复制文本框内容')
        }
    }

    async function handlePreview() {
        setSubmitting(true)
        setPreview(null)
        setExcludedPreviewKeys([])
        setImportResult(null)
        setFeedbackMessage(null)
        let previewTimer: number | undefined

        try {
            if (isBangumiMode) {
                setBangumiPreviewProgress({
                    active: true,
                    percent: 8,
                    message: '正在连接 Bangumi，准备获取候选作品',
                })
                previewTimer = window.setInterval(() => {
                    setBangumiPreviewProgress((current) => {
                        if (!current || !current.active) {
                            return current
                        }

                        const nextPercent = Math.min(88, current.percent + 7)
                        const nextMessage =
                            nextPercent < 35
                                ? '正在获取 Bangumi 作品列表'
                                : nextPercent < 65
                                  ? '正在整理标题、评分、集数和标签'
                                  : '正在进行重复检查与导入预校验'

                        return {
                            active: true,
                            percent: nextPercent,
                            message: nextMessage,
                        }
                    })
                }, 650)
                const response = await previewBangumiSeasonImport(bangumiPayload)
                if (previewTimer !== undefined) {
                    window.clearInterval(previewTimer)
                }
                setBangumiPreviewProgress({
                    active: false,
                    percent: 100,
                    message: `预览完成，可导入 ${response.canImportCount} 条作品`,
                })
                setSelectedTarget('SUBJECT')
                setPreview({ target: 'SUBJECT', data: response })
                setFeedbackMessage(
                    `已获取 ${bangumiYear} ${bangumiSelectedQuarter?.label ?? ''} Bangumi 新番预览`,
                )
                return
            }

            const payload = parseDraftPayload(draftText, selectedMode, selectedTarget)

            if (selectedTarget === 'COLLECTION') {
                const response = await previewImportCollections(
                    payload as UserSubjectCollectionBatchImportRequest,
                )
                setPreview({ target: 'COLLECTION', data: response })
            } else {
                const response = await previewImportRecords(
                    payload as UserSubjectRecordBatchImportRequest,
                )
                setPreview({ target: 'RECORD', data: response })
            }
        } catch (error) {
            if (previewTimer !== undefined) {
                window.clearInterval(previewTimer)
            }
            setBangumiPreviewProgress((current) =>
                current
                    ? {
                          active: false,
                          percent: current.percent,
                          message: '预览失败，请查看错误信息',
                      }
                    : current,
            )
            setFeedbackMessage(getErrorMessage(error))
        } finally {
            setSubmitting(false)
        }
    }

    async function handleImport() {
        setSubmitting(true)
        setFeedbackMessage(null)

        try {
            if (isBangumiMode) {
                const startedJob = await startBangumiSeasonImportJob(bangumiPayload)
                setSelectedTarget('SUBJECT')
                setBangumiJobStatus({
                    jobId: startedJob.jobId,
                    status: 'RUNNING',
                    stage: 'STARTING',
                    message: '导入任务已启动，正在准备请求 Bangumi',
                    percent: 1,
                    result: null,
                    errorMessage: null,
                    createdAt: new Date().toISOString(),
                    updatedAt: new Date().toISOString(),
                })
                await waitForBangumiImportJob(startedJob.jobId)
                return
            }

            const payload = parseDraftPayload(draftText, selectedMode, selectedTarget)

            if (selectedTarget === 'COLLECTION') {
                const response = await importCollections(
                    payload as UserSubjectCollectionBatchImportRequest,
                )
                setImportResult({ target: 'COLLECTION', data: response })
                setFeedbackMessage(`已导入 ${response.importedCount} 条收藏`)
            } else {
                const response = await importRecords(
                    payload as UserSubjectRecordBatchImportRequest,
                )
                setImportResult({ target: 'RECORD', data: response })
                setFeedbackMessage(`已导入 ${response.importedCount} 条记录`)
            }
        } catch (error) {
            setFeedbackMessage(getErrorMessage(error))
        } finally {
            setSubmitting(false)
        }
    }

    function handleFileChange(file: File | null) {
        if (!file) {
            return
        }

        const reader = new FileReader()

        reader.onload = () => {
            resetResultState()
            setDraftText(String(reader.result ?? ''))
        }

        reader.onerror = () => {
            setFeedbackMessage('文件读取失败，请重新选择')
        }

        reader.readAsText(file, 'utf-8')
    }

    return (
        <div className="space-y-6 pb-10 lg:pb-16 content-enter">
            <PageHero
                eyebrow="IMPORT"
                title="导入中心"
                description="作品库只走 Bangumi 自动导入；收藏夹和个人记录使用 JSON / CSV 手动导入。所有数据先预览校验，再确认写入。"
            />

            <ImportFlowStepper currentStep={currentFlowStep} />

            <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
                {importTips.map((tip) => (
                    <div
                        key={tip.title}
                        className="rounded-[24px] border border-white/70 bg-white/50 p-4 shadow-sm backdrop-blur-xl"
                    >
                        <p className="text-sm font-black text-slate-700">{tip.title}</p>
                        <p className="mt-1 text-xs leading-5 text-slate-400">
                            {tip.description}
                        </p>
                    </div>
                ))}
            </section>

            <GlassCard className="overflow-hidden">
                <div className="bg-gradient-to-br from-white/62 via-white/45 to-sky-50/45 p-6">
                    <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
                        <div>
                            <div className="mb-3 flex flex-wrap items-end justify-between gap-3">
                                <div>
                                    <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                                        import target
                                    </p>
                                    <h3 className="mt-1 text-xl font-black text-slate-800">
                                        选择导入对象
                                    </h3>
                                </div>
                            </div>

                            <div className="grid gap-3 md:grid-cols-3">
                                <TargetCard
                                    title="作品库"
                                    description="仅允许 Bangumi 自动导入，避免污染作品库"
                                    active={selectedTarget === 'SUBJECT'}
                                    onClick={() => handleTargetChange('SUBJECT')}
                                />
                                <TargetCard
                                    title="收藏夹"
                                    description="允许 JSON / CSV 导入想看与搁置"
                                    active={selectedTarget === 'COLLECTION'}
                                    onClick={() => handleTargetChange('COLLECTION')}
                                />
                                <TargetCard
                                    title="个人记录"
                                    description="允许 JSON / CSV 导入正式记录"
                                    active={selectedTarget === 'RECORD'}
                                    onClick={() => handleTargetChange('RECORD')}
                                />
                            </div>
                        </div>

                        <div className="rounded-[26px] border border-white/70 bg-white/55 p-4 shadow-sm backdrop-blur">
                            <p className="text-xs font-semibold text-slate-400">推荐顺序</p>
                            <p className="mt-2 text-sm font-semibold leading-6 text-slate-600">
                                {overview?.recommendedOrder ?? '先导入作品库，再导入个人记录。'}
                            </p>
                            <div className="mt-3 flex flex-wrap gap-2">
                                {(overview?.steps ?? []).slice(0, 3).map((step, index) => (
                                    <span
                                        key={step}
                                        className="rounded-full border border-white/70 bg-white/65 px-3 py-1 text-[11px] font-semibold text-slate-400"
                                    >
                                        {index + 1}. {step}
                                    </span>
                                ))}
                            </div>
                            <span className="mt-4 inline-flex rounded-full border border-white/80 bg-white/65 px-3 py-1.5 text-xs font-semibold text-slate-500">
                                文档位置：docs/import-guide.md
                            </span>
                        </div>
                    </div>
                </div>
            </GlassCard>

            <div className="grid gap-6 xl:grid-cols-[280px_1fr]">
                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/62 via-white/45 to-violet-50/45 p-5">
                        <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                            import method
                        </p>
                        <h3 className="mt-1 text-xl font-black text-slate-800">
                            导入方式
                        </h3>

                        <div className="mt-4 grid gap-2">
                            {importModes
                                .filter((mode) => isModeAllowedForTarget(mode.value, selectedTarget))
                                .map((mode) => {
                                const active = selectedMode === mode.value
                                const enabled = mode.enabled

                                return (
                                    <button
                                        key={mode.value}
                                        type="button"
                                        disabled={!enabled}
                                        onClick={() => handleModeChange(mode.value)}
                                        className={[
                                            'rounded-[20px] border p-3 text-left transition-all duration-200',
                                            active
                                                ? 'border-sky-200 bg-sky-100/80 text-sky-700 shadow-sm'
                                                : enabled
                                                  ? 'border-white/70 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75'
                                                  : 'cursor-not-allowed border-white/50 bg-white/35 text-slate-300',
                                        ].join(' ')}
                                    >
                                        <p className="text-sm font-black">{mode.title}</p>
                                        <p className="mt-1 text-xs leading-5 opacity-75">
                                            {mode.description}
                                        </p>
                                    </button>
                                )
                            })}
                        </div>
                    </div>
                </GlassCard>

                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/62 via-white/45 to-slate-50/60 p-5">
                        <div className="flex flex-wrap items-start justify-between gap-4">
                            <div>
                                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                                    {isBangumiMode ? 'bangumi season import' : 'payload editor'}
                                </p>
                                <h3 className="mt-1 text-xl font-black text-slate-800">
                                    {isBangumiMode
                                        ? 'Bangumi 新番导入'
                                        : `${getTargetTitle(selectedTarget)}导入内容`}
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    {isBangumiMode
                                        ? `当前选择：${bangumiYear} ${bangumiSelectedQuarter?.label ?? ''} · 写入作品库`
                                        : `当前根字段：${selectedRootKey} · ${draftLineCount} 行文本`}
                                </p>
                            </div>

                            <div className={['flex flex-wrap gap-2', isBangumiMode ? 'hidden' : ''].join(' ')}>
                                <button
                                    type="button"
                                    onClick={handleLoadTemplate}
                                    disabled={isBangumiMode || !selectedTemplate}
                                    className="rounded-2xl border border-white/80 bg-white/70 px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
                                >
                                    载入模板
                                </button>
                                <button
                                    type="button"
                                    onClick={() => void handleCopyTemplate()}
                                    disabled={isBangumiMode || !selectedTemplate}
                                    className="rounded-2xl border border-white/80 bg-white/70 px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
                                >
                                    复制模板
                                </button>
                                <button
                                    type="button"
                                    onClick={() => fileInputRef.current?.click()}
                                    disabled={isBangumiMode}
                                    className="rounded-2xl border border-white/80 bg-white/70 px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white"
                                >
                                    读取文件
                                </button>
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept=".json,.csv,.txt"
                                    className="hidden"
                                    onChange={(event) => {
                                        handleFileChange(event.target.files?.[0] ?? null)
                                        event.target.value = ''
                                    }}
                                />
                            </div>
                        </div>

                        {isBangumiMode ? (
                            <BangumiSeasonPanel
                                year={bangumiYear}
                                quarter={bangumiQuarter}
                                includeWeb={bangumiIncludeWeb}
                                japaneseOnly={bangumiJapaneseOnly}
                                jobStatus={bangumiJobStatus}
                                previewProgress={bangumiPreviewProgress}
                                onYearChange={(year) => {
                                    setBangumiYear(year)
                                    resetResultState()
                                }}
                                onQuarterChange={(quarter) => {
                                    setBangumiQuarter(quarter)
                                    resetResultState()
                                }}
                                onIncludeWebChange={(includeWeb) => {
                                    setBangumiIncludeWeb(includeWeb)
                                    resetResultState()
                                }}
                                onJapaneseOnlyChange={(japaneseOnly) => {
                                    setBangumiJapaneseOnly(japaneseOnly)
                                    resetResultState()
                                }}
                            />
                        ) : (
                            <textarea
                                value={draftText}
                                onChange={(event) => {
                                    setDraftText(event.target.value)
                                    resetResultState()
                                }}
                                spellCheck={false}
                                className="mt-4 min-h-[320px] w-full resize-y rounded-[26px] border border-white/75 bg-white/68 px-4 py-4 font-mono text-xs leading-6 text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white"
                                placeholder={
                                    selectedMode === 'CSV'
                                        ? '第一行写字段名，每行对应一条数据'
                                        : '粘贴后端模板 JSON'
                                }
                            />
                        )}

                        <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
                            <div className="flex flex-wrap gap-2 text-xs text-slate-400">
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    预览不写库
                                </span>
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    确认才写入
                                </span>
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    {isBangumiMode ? '自动缓存封面' : '错误行不阻塞合法行'}
                                </span>
                            </div>

                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    onClick={() => void handlePreview()}
                                    disabled={
                                        submitting ||
                                        loadingDocs ||
                                        (!isBangumiMode && !draftText.trim())
                                    }
                                    className="rounded-2xl border border-sky-200 bg-sky-100/85 px-5 py-3 text-sm font-semibold text-sky-700 transition hover:-translate-y-0.5 hover:bg-sky-100 disabled:cursor-not-allowed disabled:border-slate-100 disabled:bg-slate-100 disabled:text-slate-300"
                                >
                                    {submitting ? '处理中' : '预览校验'}
                                </button>

                                <button
                                    type="button"
                                    onClick={() => void handleImport()}
                                    disabled={
                                        submitting ||
                                        !activePreview ||
                                        activeCanImportCount === 0
                                    }
                                    className="rounded-2xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                                >
                                    确认写入
                                </button>
                            </div>
                        </div>
                    </div>
                </GlassCard>
            </div>

            {feedbackMessage ? (
                <FeedbackBanner>{feedbackMessage}</FeedbackBanner>
            ) : null}

            <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
                <PreviewPanel
                    title="预览校验"
                    totalCount={activePreviewItems.length}
                    canImportCount={previewItemMetrics.canImport}
                    updatedCount={previewItemMetrics.updated}
                    skippedCount={previewItemMetrics.skipped}
                    errorCount={previewItemMetrics.error}
                    items={activePreviewItems}
                    onExcludeItem={
                        isBangumiMode
                            ? (item, index) => {
                                  setExcludedPreviewKeys((currentKeys) => [
                                      ...currentKeys,
                                      getPreviewItemKey(item, index),
                                  ])
                              }
                            : undefined
                    }
                    emptyText="先点击预览校验，查看可导入、更新、跳过和错误项。"
                />

                <ResultPanel
                    result={activeResult}
                    items={resultItems}
                />
            </div>

            <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] xl:items-stretch">
                <TemplateFieldPanel template={selectedTemplate} bangumiMode={isBangumiMode} />
                <EndpointPanel overview={overview} />
            </div>
        </div>
    )
}

type TargetCardProps = {
    title: string
    description: string
    active: boolean
    disabled?: boolean
    onClick: () => void
}

type ImportFlowStepperProps = {
    currentStep: number
}

function ImportFlowStepper({ currentStep }: ImportFlowStepperProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/68 via-sky-50/50 to-violet-50/45 p-5">
                <div className="flex flex-wrap items-end justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            import workflow
                        </p>
                        <h3 className="mt-1 text-xl font-black text-slate-800">
                            批量导入流程
                        </h3>
                    </div>

                    <span className="rounded-full border border-white/75 bg-white/65 px-4 py-2 text-xs font-black text-slate-500">
                        当前第 {currentStep} 步
                    </span>
                </div>

                <div className="mt-5 grid gap-3 md:grid-cols-4">
                    {importFlowSteps.map((item) => {
                        const active = currentStep === item.step
                        const completed = currentStep > item.step

                        return (
                            <div
                                key={item.step}
                                className={[
                                    'relative overflow-hidden rounded-[24px] border p-4 shadow-sm transition-all duration-300',
                                    active
                                        ? 'scale-[1.015] border-sky-200 bg-sky-100/78 text-sky-700'
                                        : completed
                                          ? 'border-emerald-100 bg-emerald-50/70 text-emerald-700'
                                          : 'border-white/70 bg-white/55 text-slate-400',
                                ].join(' ')}
                            >
                                <div
                                    className={[
                                        'mb-3 h-1.5 rounded-full',
                                        active
                                            ? 'bg-sky-300'
                                            : completed
                                              ? 'bg-emerald-300'
                                              : 'bg-slate-200/80',
                                    ].join(' ')}
                                />
                                <div className="flex items-start gap-3">
                                    <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border border-white/75 bg-white/70 text-sm font-black">
                                        {completed ? '✓' : item.step}
                                    </span>
                                    <div>
                                        <p className="text-sm font-black">{item.title}</p>
                                        <p className="mt-1 text-xs leading-5 opacity-75">
                                            {item.description}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )
                    })}
                </div>
            </div>
        </GlassCard>
    )
}

function TargetCard({ title, description, active, disabled = false, onClick }: TargetCardProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            className={[
                'rounded-[24px] border p-4 text-left transition-all duration-200',
                disabled
                    ? 'cursor-not-allowed border-white/50 bg-white/35 text-slate-300'
                    : active
                    ? 'border-sky-200 bg-sky-100/80 text-sky-700 shadow-sm'
                    : 'border-white/70 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <p className="text-lg font-black">{title}</p>
            <p className="mt-1 text-sm leading-6 opacity-75">{description}</p>
        </button>
    )
}

type BangumiSeasonPanelProps = {
    year: number
    quarter: ReleaseQuarter
    includeWeb: boolean
    japaneseOnly: boolean
    jobStatus: BangumiImportJobStatusResponse | null
    previewProgress: BangumiPreviewProgress | null
    onYearChange: (year: number) => void
    onQuarterChange: (quarter: ReleaseQuarter) => void
    onIncludeWebChange: (includeWeb: boolean) => void
    onJapaneseOnlyChange: (japaneseOnly: boolean) => void
}

function BangumiSeasonPanel({
    year,
    quarter,
    includeWeb,
    japaneseOnly,
    jobStatus,
    previewProgress,
    onYearChange,
    onQuarterChange,
    onIncludeWebChange,
    onJapaneseOnlyChange,
}: BangumiSeasonPanelProps) {
    const selectedQuarter = bangumiQuarterOptions.find((option) => option.value === quarter)
    const visibleYears = [year - 1, year, year + 1]
    const progressPercent = jobStatus?.percent ?? 0
    const progressMessage = jobStatus?.errorMessage || jobStatus?.message || '等待导入任务开始'
    const visibleProgress = jobStatus
        ? {
              title: 'Bangumi 导入进度',
              stage: jobStatus.stage,
              percent: progressPercent,
              message: progressMessage,
              status: jobStatus.status,
          }
        : previewProgress
          ? {
                title: 'Bangumi 预览进度',
                stage: previewProgress.active ? 'PREVIEWING' : 'PREVIEWED',
                percent: previewProgress.percent,
                message: previewProgress.message,
                status: previewProgress.active ? 'RUNNING' : 'SUCCESS',
            }
          : null
    const progressTone =
        visibleProgress?.status === 'FAILED'
            ? 'from-rose-400 to-orange-300'
            : visibleProgress?.status === 'SUCCESS'
              ? 'from-emerald-400 to-sky-300'
              : 'from-sky-400 to-violet-300'

    return (
        <div className="mt-4 overflow-hidden rounded-[28px] border border-sky-100/80 bg-gradient-to-br from-sky-50/72 via-white/62 to-violet-50/60 p-5 shadow-sm">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-sky-400">
                        official bangumi api
                    </p>
                    <h4 className="mt-1 text-2xl font-black text-slate-800">
                        {year} {selectedQuarter?.label ?? ''}动画档期
                    </h4>
                    <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
                        调用 Bangumi 官方作品浏览接口，拉取对应年份与月份档期的 TV 动画；预览只检查重复和字段，确认写入后会进入作品库并缓存封面。
                    </p>
                </div>

                <div className="flex items-center gap-2 rounded-[24px] border border-white/80 bg-white/70 p-1.5 shadow-sm">
                    <button
                        type="button"
                        onClick={() => onYearChange(year - 1)}
                        className="flex h-10 w-10 items-center justify-center rounded-2xl border border-sky-100 bg-sky-50/80 text-lg font-black text-sky-600 transition hover:-translate-x-0.5 hover:bg-sky-100"
                        aria-label="上一年"
                    >
                        &lt;
                    </button>
                    <div className="grid grid-cols-3 gap-1.5">
                        {visibleYears.map((visibleYear) => {
                            const active = visibleYear === year

                            return (
                                <button
                                    key={visibleYear}
                                    type="button"
                                    onClick={() => onYearChange(visibleYear)}
                                    className={[
                                        'h-10 rounded-2xl border px-4 text-sm font-black transition-all duration-200',
                                        active
                                            ? 'scale-[1.03] border-sky-200 bg-sky-100/90 text-sky-700 shadow-sm'
                                            : 'border-white/75 bg-white/60 text-slate-400 hover:bg-white hover:text-slate-600',
                                    ].join(' ')}
                                >
                                    {visibleYear}
                                </button>
                            )
                        })}
                    </div>
                    <button
                        type="button"
                        onClick={() => onYearChange(year + 1)}
                        className="flex h-10 w-10 items-center justify-center rounded-2xl border border-sky-100 bg-sky-50/80 text-lg font-black text-sky-600 transition hover:translate-x-0.5 hover:bg-sky-100"
                        aria-label="下一年"
                    >
                        &gt;
                    </button>
                </div>
            </div>

            <div className="mt-5 grid gap-3 sm:grid-cols-4">
                {bangumiQuarterOptions.map((option) => {
                    const active = quarter === option.value
                    const theme = quarterTheme[option.value]

                    return (
                        <button
                            key={option.value}
                            type="button"
                            onClick={() => onQuarterChange(option.value)}
                            className={[
                                'rounded-[24px] border p-4 text-left transition-all duration-200',
                                active
                                    ? `scale-[1.02] ${theme.border} ${theme.bg} ${theme.text} shadow-md shadow-sky-100/70`
                                    : 'border-white/70 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75',
                            ].join(' ')}
                        >
                            <span className="text-2xl font-black">{option.label}</span>
                            <span className="mt-1 block text-xs font-semibold text-slate-400">
                                对应 {option.month} 月档
                            </span>
                        </button>
                    )
                })}
            </div>

            <div className="mt-5 grid gap-3 md:grid-cols-2">
                <label className="flex cursor-pointer items-start gap-3 rounded-[22px] border border-white/75 bg-white/58 p-4 transition hover:bg-white/75">
                    <input
                        type="checkbox"
                        checked={japaneseOnly}
                        onChange={(event) => onJapaneseOnlyChange(event.target.checked)}
                        className="mt-1 h-4 w-4 accent-sky-500"
                    />
                    <span>
                        <span className="block text-sm font-black text-slate-700">优先日本动画</span>
                        <span className="mt-1 block text-xs leading-5 text-slate-400">
                            使用 Bangumi 的“日本”标签做轻量过滤；如果你发现漏作品，可以关闭后重新预览。
                        </span>
                    </span>
                </label>

                <label className="flex cursor-pointer items-start gap-3 rounded-[22px] border border-white/75 bg-white/58 p-4 transition hover:bg-white/75">
                    <input
                        type="checkbox"
                        checked={includeWeb}
                        onChange={(event) => onIncludeWebChange(event.target.checked)}
                        className="mt-1 h-4 w-4 accent-violet-500"
                    />
                    <span>
                        <span className="block text-sm font-black text-slate-700">包含 WEB 动画</span>
                        <span className="mt-1 block text-xs leading-5 text-slate-400">
                            默认只导入 TV 动画；打开后会额外拉取 WEB 类目，适合补充网播短片或平台动画。
                        </span>
                    </span>
                </label>
            </div>

            {visibleProgress ? (
                <div className="mt-5 rounded-[24px] border border-white/75 bg-white/68 p-4 shadow-sm">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                        <div>
                            <p className="text-sm font-black text-slate-700">
                                {visibleProgress.title}
                            </p>
                            <p className="mt-1 text-xs leading-5 text-slate-400">
                                {visibleProgress.message}
                            </p>
                        </div>
                        <span className="rounded-full border border-sky-100 bg-sky-50/85 px-3 py-1 text-xs font-black text-sky-600">
                            {visibleProgress.stage} · {visibleProgress.percent}%
                        </span>
                    </div>

                    <div className="mt-4 h-3 overflow-hidden rounded-full bg-slate-100/80">
                        <div
                            className={[
                                'h-full rounded-full bg-gradient-to-r transition-all duration-500',
                                progressTone,
                            ].join(' ')}
                            style={{ width: `${Math.max(3, visibleProgress.percent)}%` }}
                        />
                    </div>
                </div>
            ) : null}
        </div>
    )
}

type PreviewPanelProps = {
    title: string
    totalCount: number
    canImportCount: number
    updatedCount: number
    skippedCount: number
    errorCount: number
    items: PreviewItem[]
    onExcludeItem?: (item: PreviewItem, index: number) => void
    emptyText: string
}

function PreviewPanel({
                          title,
                          totalCount,
                          canImportCount,
                          updatedCount,
                          skippedCount,
                          errorCount,
                          items,
                          onExcludeItem,
                          emptyText,
                      }: PreviewPanelProps) {
    const warningCount = Math.max(0, skippedCount)

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-emerald-50/45 p-5">
                <h3 className="text-xl font-black text-slate-800">{title}</h3>

                <div className="mt-4 grid grid-cols-2 gap-3 lg:grid-cols-5">
                    <StatCard label="总数" value={totalCount} />
                    <StatCard label="可导入" value={canImportCount} tone="good" />
                    <StatCard label="更新" value={updatedCount} tone="warn" />
                    <StatCard label="跳过" value={warningCount} tone="normal" />
                    <StatCard label="错误" value={errorCount} tone="danger" />
                </div>

                <ImportItemList
                    items={items}
                    onExcludeItem={onExcludeItem}
                    emptyText={emptyText}
                />
            </div>
        </GlassCard>
    )
}

type ResultPanelProps = {
    result:
        | SubjectImportResponse
        | UserSubjectCollectionImportResponse
        | UserSubjectRecordImportResponse
        | null
    items: PreviewItem[]
}

function ResultPanel({ result, items }: ResultPanelProps) {
    const errorCount = items.filter((item) => item.result === 'ERROR').length
    const updatedCount = items.filter((item) => item.result === 'UPDATED').length
    const skippedCount = items.filter((item) => item.result === 'SKIPPED').length

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-amber-50/45 p-5">
                <h3 className="text-xl font-black text-slate-800">写入结果</h3>

                <div className="mt-4 grid grid-cols-2 gap-3 lg:grid-cols-5">
                    <StatCard label="总数" value={result?.totalCount ?? 0} />
                    <StatCard label="已导入" value={result?.importedCount ?? 0} tone="good" />
                    <StatCard label="更新" value={updatedCount} tone="warn" />
                    <StatCard label="跳过" value={skippedCount} tone="normal" />
                    <StatCard label="错误" value={errorCount} tone="danger" />
                </div>

                <ImportItemList
                    items={items}
                    emptyText="确认写入后，这里会显示每条数据的写入结果。"
                />
            </div>
        </GlassCard>
    )
}

type StatCardProps = {
    label: string
    value: number
    tone?: 'normal' | 'good' | 'warn' | 'danger'
}

function StatCard({ label, value, tone = 'normal' }: StatCardProps) {
    const textClassName =
        tone === 'good'
            ? 'text-emerald-600'
            : tone === 'warn'
              ? 'text-orange-600'
              : tone === 'danger'
                ? 'text-rose-600'
              : 'text-slate-800'

    return (
        <div className="rounded-[22px] border border-white/70 bg-white/60 p-4 shadow-sm">
            <p className="text-xs font-semibold text-slate-400">{label}</p>
            <p className={['mt-1 text-3xl font-black', textClassName].join(' ')}>
                {value}
            </p>
        </div>
    )
}

type ImportItemListProps = {
    items: PreviewItem[]
    onExcludeItem?: (item: PreviewItem, index: number) => void
    emptyText: string
}

function ImportItemList({ items, onExcludeItem, emptyText }: ImportItemListProps) {
    const [filter, setFilter] = useState<ImportItemFilter>('ALL')
    const successCount = items.filter((item) => getImportItemFilter(item) === 'SUCCESS').length
    const warningCount = items.filter((item) => getImportItemFilter(item) === 'WARNING').length
    const errorCount = items.filter((item) => getImportItemFilter(item) === 'ERROR').length

    const filterOptions: Array<{
        value: ImportItemFilter
        label: string
        count: number
    }> = [
        { value: 'ALL', label: '全部', count: items.length },
        { value: 'SUCCESS', label: '可导入', count: successCount },
        { value: 'WARNING', label: '更新/跳过', count: warningCount },
        { value: 'ERROR', label: '错误', count: errorCount },
    ]

    const visibleItems = filter === 'ALL' ? items : items.filter((item) => getImportItemFilter(item) === filter)

    if (items.length === 0) {
        return (
            <div className="mt-4 rounded-[24px] border border-dashed border-slate-200 bg-white/45 p-8 text-center text-sm font-semibold text-slate-400">
                {emptyText}
            </div>
        )
    }

    return (
        <div className="mt-4">
            <div className="mb-3 flex flex-wrap gap-2">
                {filterOptions.map((option) => {
                    const selected = filter === option.value

                    return (
                        <button
                            key={option.value}
                            type="button"
                            onClick={() => setFilter(option.value)}
                            className={[
                                'rounded-full border px-3 py-1.5 text-xs font-semibold transition',
                                selected
                                    ? 'border-slate-200 bg-slate-900 text-white shadow-sm'
                                    : 'border-white/75 bg-white/62 text-slate-400 hover:bg-white hover:text-slate-600',
                            ].join(' ')}
                        >
                            {option.label} {option.count}
                        </button>
                    )
                })}
            </div>

            {visibleItems.length === 0 ? (
                <div className="rounded-[24px] border border-dashed border-slate-200 bg-white/45 p-8 text-center text-sm font-semibold text-slate-400">
                    当前筛选下没有结果
                </div>
            ) : null}

            <div className="max-h-[320px] space-y-2 overflow-auto pr-1">
                {visibleItems.map((item, index) => {
                    const canImport = item.result === 'CAN_IMPORT' || item.result === 'IMPORTED'
                    const hasError = item.result === 'ERROR'
                    const className = canImport
                        ? 'border-emerald-100 bg-emerald-50/70 text-emerald-700'
                        : hasError
                          ? 'border-rose-100 bg-rose-50/75 text-rose-700'
                          : 'border-orange-100 bg-orange-50/70 text-orange-700'
                    const statusText =
                        item.result === 'CAN_IMPORT'
                            ? '可导入'
                            : item.result === 'UPDATED'
                              ? '更新'
                              : item.result === 'SKIPPED'
                                ? '跳过'
                                : item.result

                    return (
                        <div
                            key={`${item.title}-${index}`}
                            className="rounded-[20px] border border-white/70 bg-white/58 p-3 shadow-sm"
                        >
                            <div className="flex flex-wrap items-center justify-between gap-2">
                                <p className="line-clamp-1 text-sm font-bold text-slate-700">
                                    {item.title}
                                </p>
                                <span
                                    className={[
                                        'rounded-full border px-2.5 py-0.5 text-[11px] font-black',
                                        className,
                                    ].join(' ')}
                                >
                                    {statusText}
                                </span>
                            </div>
                            <div className="mt-1 flex flex-wrap items-center justify-between gap-2">
                                <p className="text-xs leading-5 text-slate-400">{item.message}</p>
                                {onExcludeItem && item.result === 'CAN_IMPORT' ? (
                                    <button
                                        type="button"
                                        onClick={() => onExcludeItem(item, index)}
                                        className="rounded-full border border-rose-100 bg-rose-50/80 px-2.5 py-1 text-[11px] font-semibold text-rose-600 transition hover:bg-rose-100"
                                    >
                                        排除
                                    </button>
                                ) : null}
                            </div>
                        </div>
                    )
                })}
            </div>
        </div>
    )
}

type TemplateFieldPanelProps = {
    template: ImportTemplateResponse | null
    bangumiMode?: boolean
}

function TemplateFieldPanel({ template, bangumiMode = false }: TemplateFieldPanelProps) {
    const bangumiFields: Array<{
        fieldName: string
        required: boolean
        description: string
        example: string
        notes?: string
    }> = [
        {
            fieldName: 'year',
            required: true,
            description: '选择要导入的新番年份。',
            example: '2026',
            notes: '用于决定拉取哪个年份的 Bangumi 番剧。',
        },
        {
            fieldName: 'quarter',
            required: true,
            description: '选择一月、四月、七月、十月档期。',
            example: '一月',
            notes: '对应季度档期，系统会按档期抓取作品。',
        },
        {
            fieldName: 'includeWeb',
            required: false,
            description: '是否额外纳入 WEB 动画。',
            example: 'false',
            notes: '开启后会把部分 WEB 播放作品一起纳入预览。',
        },
        {
            fieldName: 'japaneseOnly',
            required: false,
            description: '是否只保留日本动画标签命中的作品。',
            example: 'true',
            notes: '用于过滤非日本动画或标签不匹配的条目。',
        },
    ]
    const fields = bangumiMode ? bangumiFields : (template?.fields ?? [])

    return (
        <GlassCard className="h-full overflow-hidden">
            <div className="flex h-full flex-col bg-gradient-to-br from-white/62 via-white/45 to-slate-50/60 p-5">
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <h3 className="text-xl font-black text-slate-800">模板字段说明</h3>
                        <p className="mt-1 text-sm text-slate-400">
                            {bangumiMode
                                ? '作品库只通过 Bangumi 新番导入；手动模板仅用于收藏夹和个人记录。'
                                : template?.recommendedOrder ?? '模板字段正在加载'}
                        </p>
                    </div>
                    <span className="shrink-0 rounded-full border border-white/75 bg-white/65 px-3 py-1 text-[11px] font-semibold text-slate-400">
                        {bangumiMode ? 'Bangumi only' : '手动模板'}
                    </span>
                </div>

                <div className="mt-4 grid flex-1 content-start gap-2 md:grid-cols-2">
                    {fields.map((field) => (
                        <div
                            key={field.fieldName}
                            className="rounded-[20px] border border-white/70 bg-white/58 p-3"
                        >
                            <div className="flex flex-wrap items-center gap-2">
                                <span className="text-sm font-black text-slate-700">
                                    {field.fieldName}
                                </span>
                                {field.required ? (
                                    <span className="rounded-full border border-rose-100 bg-rose-50/80 px-2 py-0.5 text-[11px] font-semibold text-rose-600">
                                        必填
                                    </span>
                                ) : (
                                    <span className="rounded-full border border-slate-100 bg-slate-50/80 px-2 py-0.5 text-[11px] font-semibold text-slate-400">
                                        可选
                                    </span>
                                )}
                            </div>
                            <p className="mt-1 text-xs leading-5 text-slate-500">
                                {field.description}
                            </p>
                            <p className="mt-1 text-[11px] text-slate-400">
                                示例：{field.example ?? '空'}
                            </p>
                            {field.notes ? (
                                <p className="mt-1 text-[11px] leading-5 text-slate-400">
                                    {field.notes}
                                </p>
                            ) : null}
                        </div>
                    ))}
                </div>
            </div>
        </GlassCard>
    )
}

type EndpointPanelProps = {
    overview: ImportOverviewResponse | null
}

function EndpointPanel({ overview }: EndpointPanelProps) {
    return (
        <GlassCard className="h-full overflow-hidden">
            <div className="flex h-full flex-col bg-gradient-to-br from-white/62 via-white/45 to-violet-50/45 p-5">
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <h3 className="text-xl font-black text-slate-800">导入接口链路</h3>
                        <p className="mt-1 text-sm text-slate-400">
                            {overview?.moduleName ?? '导入模块'}
                        </p>
                    </div>
                    <span className="shrink-0 rounded-full border border-white/75 bg-white/65 px-3 py-1 text-[11px] font-semibold text-slate-400">
                        {overview?.endpoints?.length ?? 0} 项
                    </span>
                </div>

                <div className="mt-4 grid flex-1 content-start gap-2 overflow-auto pr-1">
                    {(overview?.endpoints ?? []).map((endpoint) => (
                        <div
                            key={`${endpoint.method}-${endpoint.path}`}
                            className="rounded-[20px] border border-white/70 bg-white/58 p-3"
                        >
                            <div className="flex flex-wrap items-center gap-2">
                                <span className="rounded-full border border-sky-100 bg-sky-50/80 px-2.5 py-0.5 text-[11px] font-black text-sky-600">
                                    {endpoint.method}
                                </span>
                                <span className="text-sm font-bold text-slate-700">
                                    {endpoint.name}
                                </span>
                            </div>
                            <p className="mt-1 font-mono text-xs text-slate-400">
                                {endpoint.path}
                            </p>
                            <p className="mt-1 text-xs leading-5 text-slate-500">
                                {endpoint.description}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </GlassCard>
    )
}

export default ImportPage
