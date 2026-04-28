import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
    getImportOverview,
    getRecordImportTemplate,
    getSubjectImportTemplate,
    type ImportOverviewResponse,
    type ImportTemplateResponse,
} from '../api/importApi'
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
    importSubjects,
    previewImportSubjects,
    type SubjectBatchImportRequest,
    type SubjectImportItemResponse,
    type SubjectImportPreviewItemResponse,
    type SubjectImportPreviewResponse,
    type SubjectImportResponse,
} from '../api/subjectApi'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'

type ImportTarget = 'SUBJECT' | 'RECORD'

type ImportMode = 'JSON' | 'CSV' | 'EXCEL' | 'BANGUMI' | 'BACKUP' | 'MANUAL'

type ParsedPayload = SubjectBatchImportRequest | UserSubjectRecordBatchImportRequest

type PreviewState =
    | {
          target: 'SUBJECT'
          data: SubjectImportPreviewResponse
      }
    | {
          target: 'RECORD'
          data: UserSubjectRecordImportPreviewResponse
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

type PreviewItem = {
    title: string
    result: string
    message: string
}

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
        description: '后续扩展',
        enabled: false,
    },
    {
        value: 'BANGUMI',
        title: 'Bangumi 链接',
        description: '后续接平台解析',
        enabled: false,
    },
    {
        value: 'BACKUP',
        title: 'JSON 备份',
        description: '后续恢复全量数据',
        enabled: false,
    },
    {
        value: 'MANUAL',
        title: '快速录入',
        description: '后续做轻量表单',
        enabled: false,
    },
]

const numericFields = new Set([
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

function getErrorMessage(error: unknown) {
    if (error instanceof Error) {
        return error.message
    }

    return '请求失败，请稍后重试'
}

function getTargetRootKey(target: ImportTarget) {
    return target === 'SUBJECT' ? 'subjects' : 'records'
}

function getTargetTitle(target: ImportTarget) {
    return target === 'SUBJECT' ? '作品库' : '个人记录'
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

    if (numericFields.has(fieldName)) {
        const numberValue = Number(value)
        return Number.isNaN(numberValue) ? value : numberValue
    }

    return value
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
        throw new Error('当前方式暂未接入预览接口')
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

    return result.data.items.map((item: UserSubjectRecordImportItemResponse) => ({
        title: item.subjectDisplayTitle,
        result: item.result,
        message: item.message,
    }))
}

function ImportPage() {
    const [overview, setOverview] = useState<ImportOverviewResponse | null>(null)
    const [subjectTemplate, setSubjectTemplate] = useState<ImportTemplateResponse | null>(null)
    const [recordTemplate, setRecordTemplate] = useState<ImportTemplateResponse | null>(null)
    const [selectedTarget, setSelectedTarget] = useState<ImportTarget>('SUBJECT')
    const [selectedMode, setSelectedMode] = useState<ImportMode>('JSON')
    const [draftText, setDraftText] = useState('')
    const [preview, setPreview] = useState<PreviewState | null>(null)
    const [importResult, setImportResult] = useState<ImportResultState | null>(null)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [loadingDocs, setLoadingDocs] = useState(false)
    const [submitting, setSubmitting] = useState(false)
    const fileInputRef = useRef<HTMLInputElement | null>(null)

    const selectedTemplate = selectedTarget === 'SUBJECT' ? subjectTemplate : recordTemplate
    const selectedRootKey = getTargetRootKey(selectedTarget)
    const previewItems = getPreviewItems(preview)
    const resultItems = getImportResultItems(importResult)

    const activePreview =
        preview && preview.target === selectedTarget ? preview.data : null
    const activeResult =
        importResult && importResult.target === selectedTarget ? importResult.data : null

    const draftLineCount = useMemo(() => {
        return draftText.trim() ? draftText.trim().split(/\r?\n/).length : 0
    }, [draftText])

    const loadDocs = useCallback(async () => {
        setLoadingDocs(true)
        setFeedbackMessage(null)

        try {
            const [overviewResponse, subjectTemplateResponse, recordTemplateResponse] =
                await Promise.all([
                    getImportOverview(),
                    getSubjectImportTemplate(),
                    getRecordImportTemplate(),
                ])

            setOverview(overviewResponse)
            setSubjectTemplate(subjectTemplateResponse)
            setRecordTemplate(recordTemplateResponse)
            setDraftText(JSON.stringify(subjectTemplateResponse.templateBody, null, 2))
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
        setImportResult(null)
        setFeedbackMessage(null)
    }

    function handleTargetChange(target: ImportTarget) {
        setSelectedTarget(target)
        resetResultState()

        const nextTemplate = target === 'SUBJECT' ? subjectTemplate : recordTemplate

        if (nextTemplate) {
            setDraftText(stringifyTemplate(nextTemplate))
        }
    }

    function handleModeChange(mode: ImportMode) {
        setSelectedMode(mode)
        resetResultState()

        if (mode === 'JSON' && selectedTemplate) {
            setDraftText(stringifyTemplate(selectedTemplate))
            return
        }

        if (mode === 'CSV' && selectedTemplate) {
            const headers = selectedTemplate.fields.map((field) => field.fieldName).join(',')
            const values = selectedTemplate.fields.map((field) => field.example ?? '').join(',')
            setDraftText(`${headers}\n${values}`)
        }
    }

    function handleLoadTemplate() {
        if (!selectedTemplate) {
            return
        }

        resetResultState()
        setDraftText(
            selectedMode === 'CSV'
                ? `${selectedTemplate.fields.map((field) => field.fieldName).join(',')}\n${selectedTemplate.fields.map((field) => field.example ?? '').join(',')}`
                : stringifyTemplate(selectedTemplate),
        )
    }

    async function handlePreview() {
        setSubmitting(true)
        setPreview(null)
        setImportResult(null)
        setFeedbackMessage(null)

        try {
            const payload = parseDraftPayload(draftText, selectedMode, selectedTarget)

            if (selectedTarget === 'SUBJECT') {
                const response = await previewImportSubjects(payload as SubjectBatchImportRequest)
                setPreview({ target: 'SUBJECT', data: response })
            } else {
                const response = await previewImportRecords(
                    payload as UserSubjectRecordBatchImportRequest,
                )
                setPreview({ target: 'RECORD', data: response })
            }
        } catch (error) {
            setFeedbackMessage(getErrorMessage(error))
        } finally {
            setSubmitting(false)
        }
    }

    async function handleImport() {
        setSubmitting(true)
        setFeedbackMessage(null)

        try {
            const payload = parseDraftPayload(draftText, selectedMode, selectedTarget)

            if (selectedTarget === 'SUBJECT') {
                const response = await importSubjects(payload as SubjectBatchImportRequest)
                setImportResult({ target: 'SUBJECT', data: response })
                setFeedbackMessage(`已导入 ${response.importedCount} 条作品`)
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
        <div className="space-y-6 content-enter">
            <PageHero
                eyebrow="IMPORT"
                title="导入中心"
                description="先预览校验，再确认写入。当前优先支持作品库和个人记录的 JSON / CSV 导入。"
            />

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

                            <div className="grid gap-3 md:grid-cols-2">
                                <TargetCard
                                    title="作品库"
                                    description="导入作品本体，建议先于记录导入"
                                    active={selectedTarget === 'SUBJECT'}
                                    onClick={() => handleTargetChange('SUBJECT')}
                                />
                                <TargetCard
                                    title="个人记录"
                                    description="导入正式看过 / 读过 / 玩过的记录"
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
                            {importModes.map((mode) => {
                                const active = selectedMode === mode.value

                                return (
                                    <button
                                        key={mode.value}
                                        type="button"
                                        disabled={!mode.enabled}
                                        onClick={() => handleModeChange(mode.value)}
                                        className={[
                                            'rounded-[20px] border p-3 text-left transition-all duration-200',
                                            active
                                                ? 'border-sky-200 bg-sky-100/80 text-sky-700 shadow-sm'
                                                : mode.enabled
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
                                    payload editor
                                </p>
                                <h3 className="mt-1 text-xl font-black text-slate-800">
                                    {getTargetTitle(selectedTarget)}导入内容
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    当前根字段：{selectedRootKey} · {draftLineCount} 行文本
                                </p>
                            </div>

                            <div className="flex flex-wrap gap-2">
                                <button
                                    type="button"
                                    onClick={handleLoadTemplate}
                                    disabled={!selectedTemplate}
                                    className="rounded-2xl border border-white/80 bg-white/70 px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
                                >
                                    载入模板
                                </button>
                                <button
                                    type="button"
                                    onClick={() => fileInputRef.current?.click()}
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
                                    ? '第一行写字段名，后续每行一条数据'
                                    : '粘贴后端模板 JSON'
                            }
                        />

                        <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
                            <div className="flex flex-wrap gap-2 text-xs text-slate-400">
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    Preview 不写库
                                </span>
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    Import 才写入
                                </span>
                            </div>

                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    onClick={() => void handlePreview()}
                                    disabled={submitting || loadingDocs || !draftText.trim()}
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
                                        activePreview.canImportCount === 0
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
                <div className="rounded-[24px] border border-sky-100 bg-white/62 px-5 py-4 text-sm font-semibold text-sky-700 shadow-sm backdrop-blur-xl">
                    {feedbackMessage}
                </div>
            ) : null}

            <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
                <PreviewPanel
                    title="预览校验"
                    totalCount={activePreview?.totalCount ?? 0}
                    canImportCount={activePreview?.canImportCount ?? 0}
                    skippedCount={activePreview?.skippedCount ?? 0}
                    items={previewItems}
                    emptyText="先点击预览校验，查看可导入、警告和跳过项。"
                />

                <ResultPanel
                    result={activeResult}
                    items={resultItems}
                />
            </div>

            <div className="grid gap-6 xl:grid-cols-[1fr_1fr]">
                <TemplateFieldPanel template={selectedTemplate} />
                <EndpointPanel overview={overview} />
            </div>
        </div>
    )
}

type TargetCardProps = {
    title: string
    description: string
    active: boolean
    onClick: () => void
}

function TargetCard({ title, description, active, onClick }: TargetCardProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-[24px] border p-4 text-left transition-all duration-200',
                active
                    ? 'border-sky-200 bg-sky-100/80 text-sky-700 shadow-sm'
                    : 'border-white/70 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75',
            ].join(' ')}
        >
            <p className="text-lg font-black">{title}</p>
            <p className="mt-1 text-sm leading-6 opacity-75">{description}</p>
        </button>
    )
}

type PreviewPanelProps = {
    title: string
    totalCount: number
    canImportCount: number
    skippedCount: number
    items: PreviewItem[]
    emptyText: string
}

function PreviewPanel({
                          title,
                          totalCount,
                          canImportCount,
                          skippedCount,
                          items,
                          emptyText,
                      }: PreviewPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-emerald-50/45 p-5">
                <h3 className="text-xl font-black text-slate-800">{title}</h3>

                <div className="mt-4 grid grid-cols-3 gap-3">
                    <StatCard label="总数" value={totalCount} />
                    <StatCard label="可导入" value={canImportCount} tone="good" />
                    <StatCard label="警告/跳过" value={skippedCount} tone="warn" />
                </div>

                <ImportItemList
                    items={items}
                    emptyText={emptyText}
                />
            </div>
        </GlassCard>
    )
}

type ResultPanelProps = {
    result:
        | SubjectImportResponse
        | UserSubjectRecordImportResponse
        | null
    items: PreviewItem[]
}

function ResultPanel({ result, items }: ResultPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-amber-50/45 p-5">
                <h3 className="text-xl font-black text-slate-800">写入结果</h3>

                <div className="mt-4 grid grid-cols-3 gap-3">
                    <StatCard label="总数" value={result?.totalCount ?? 0} />
                    <StatCard label="已导入" value={result?.importedCount ?? 0} tone="good" />
                    <StatCard label="跳过" value={result?.skippedCount ?? 0} tone="warn" />
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
    tone?: 'normal' | 'good' | 'warn'
}

function StatCard({ label, value, tone = 'normal' }: StatCardProps) {
    const textClassName =
        tone === 'good'
            ? 'text-emerald-600'
            : tone === 'warn'
              ? 'text-orange-600'
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
    emptyText: string
}

function ImportItemList({ items, emptyText }: ImportItemListProps) {
    if (items.length === 0) {
        return (
            <div className="mt-4 rounded-[24px] border border-dashed border-slate-200 bg-white/45 p-8 text-center text-sm font-semibold text-slate-400">
                {emptyText}
            </div>
        )
    }

    return (
        <div className="mt-4 max-h-[320px] space-y-2 overflow-auto pr-1">
            {items.map((item, index) => {
                const canImport = item.result === 'CAN_IMPORT' || item.result === 'IMPORTED'
                const className = canImport
                    ? 'border-emerald-100 bg-emerald-50/70 text-emerald-700'
                    : 'border-orange-100 bg-orange-50/70 text-orange-700'

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
                                {item.result}
                            </span>
                        </div>
                        <p className="mt-1 text-xs leading-5 text-slate-400">{item.message}</p>
                    </div>
                )
            })}
        </div>
    )
}

type TemplateFieldPanelProps = {
    template: ImportTemplateResponse | null
}

function TemplateFieldPanel({ template }: TemplateFieldPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-slate-50/60 p-5">
                <h3 className="text-xl font-black text-slate-800">模板字段说明</h3>
                <p className="mt-1 text-sm text-slate-400">
                    {template?.recommendedOrder ?? '等待模板加载'}
                </p>

                <div className="mt-4 max-h-[420px] space-y-2 overflow-auto pr-1">
                    {(template?.fields ?? []).map((field) => (
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
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-white/45 to-violet-50/45 p-5">
                <h3 className="text-xl font-black text-slate-800">导入接口链路</h3>
                <p className="mt-1 text-sm text-slate-400">
                    {overview?.moduleName ?? 'IMPORT_OVERVIEW'}
                </p>

                <div className="mt-4 grid gap-2">
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
