import type { ImportTemplateResponse } from '../../api/importApi'
import type {
    UserSubjectCollectionBatchImportRequest,
    UserSubjectCollectionImportItemResponse,
    UserSubjectCollectionImportPreviewItemResponse,
} from '../../api/collectionApi'
import type {
    UserSubjectRecordBatchImportRequest,
    UserSubjectRecordImportItemResponse,
    UserSubjectRecordImportPreviewItemResponse,
} from '../../api/recordApi'
import type { SubjectImportItemResponse, SubjectImportPreviewItemResponse } from '../../api/subjectApi'
import type { ReleaseQuarter } from '../../types/common'
import type {
    ImportItemFilter,
    ImportMode,
    ImportResultState,
    ImportTarget,
    ParsedPayload,
    PreviewItem,
    PreviewState,
} from './importTypes'

export const importModes: Array<{
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

export const numericFields = new Set([
    'subjectId',
    'franchiseId',
    'seasonIndex',
    'partIndex',
    'releaseYear',
    'releaseQuarter',
    'platformScore',
    'episodeCount',
    'scoreValue',
    'recordYear',
    'recordQuarter',
])

export const arrayFields = new Set(['tagNames'])

export const importFlowSteps = [
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

export const bangumiQuarterOptions: Array<{
    value: ReleaseQuarter
    label: string
    month: number
}> = [
    { value: 1, label: '一月', month: 1 },
    { value: 2, label: '四月', month: 4 },
    { value: 3, label: '七月', month: 7 },
    { value: 4, label: '十月', month: 10 },
]

export function wait(milliseconds: number) {
    return new Promise((resolve) => {
        window.setTimeout(resolve, milliseconds)
    })
}

export function getTargetRootKey(target: ImportTarget) {
    if (target === 'SUBJECT') {
        return 'subjects'
    }
    if (target === 'COLLECTION') {
        return 'collections'
    }
    return 'records'
}

export function getTargetTitle(target: ImportTarget) {
    if (target === 'SUBJECT') {
        return '作品库'
    }
    if (target === 'COLLECTION') {
        return '收藏夹'
    }
    return '个人记录'
}

export function getImportItemFilter(item: PreviewItem): Exclude<ImportItemFilter, 'ALL'> {
    if (item.result === 'CAN_IMPORT' || item.result === 'IMPORTED') {
        return 'SUCCESS'
    }
    if (item.result === 'UPDATED') {
        return 'UPDATED'
    }
    if (item.result === 'SKIPPED') {
        return 'SKIPPED'
    }
    if (item.result === 'AMBIGUOUS') {
        return 'AMBIGUOUS'
    }
    if (item.result === 'ERROR') {
        return 'ERROR'
    }
    return 'SKIPPED'
}

export function getPreviewItemKey(item: PreviewItem, index: number) {
    if (item.sourceId) {
        return `${item.sourcePlatform ?? 'SOURCE'}:${item.sourceId}`
    }

    return `${item.title}:${index}`
}

export function isImportablePreviewItem(item: PreviewItem) {
    return item.result === 'CAN_IMPORT' || item.result === 'UPDATED'
}

export function isAmbiguousPreviewItem(item: PreviewItem) {
    return item.result === 'AMBIGUOUS' && (item.candidates?.length ?? 0) > 1
}

export function buildImportPayloadWithSelections(
    target: ImportTarget,
    payload: ParsedPayload,
    items: PreviewItem[],
    selections: Record<number, number | 'SKIP'>,
    excludedPreviewKeys: string[],
) {
    const isExcluded = (index: number) => {
        const previewItem = items[index]
        if (!previewItem) {
            return false
        }
        return excludedPreviewKeys.includes(getPreviewItemKey(previewItem, index))
    }

    if (target === 'RECORD') {
        const records = [...(payload as UserSubjectRecordBatchImportRequest).records].map((item) => ({
            ...item,
        }))
        records.forEach((record, index) => {
            if (isExcluded(index)) {
                return
            }
            const previewItem = items[index]
            if (!previewItem || !isAmbiguousPreviewItem(previewItem)) {
                return
            }
            const selected = selections[index]
            if (typeof selected === 'number') {
                record.subjectId = selected
            }
        })
        return {
            records: records.filter(
                (_, index) => !isExcluded(index) && selections[index] !== 'SKIP',
            ),
        } as UserSubjectRecordBatchImportRequest
    }

    if (target === 'COLLECTION') {
        const collections = [...(payload as UserSubjectCollectionBatchImportRequest).collections].map(
            (item) => ({ ...item }),
        )
        collections.forEach((collection, index) => {
            if (isExcluded(index)) {
                return
            }
            const previewItem = items[index]
            if (!previewItem || !isAmbiguousPreviewItem(previewItem)) {
                return
            }
            const selected = selections[index]
            if (typeof selected === 'number') {
                collection.subjectId = selected
            }
        })
        return {
            collections: collections.filter(
                (_, index) => !isExcluded(index) && selections[index] !== 'SKIP',
            ),
        } as UserSubjectCollectionBatchImportRequest
    }

    return payload
}

export function stringifyTemplate(template: ImportTemplateResponse | null) {
    if (!template) {
        return ''
    }

    return JSON.stringify(template.templateBody, null, 2)
}

export function parseCsvRows(text: string) {
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

export function normalizeCsvValue(fieldName: string, value: string) {
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

export function parseCsvArrayValue(value: string) {
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

export function escapeCsvCell(value: string) {
    if (/[",\r\n]/.test(value)) {
        return `"${value.replaceAll('"', '""')}"`
    }

    return value
}

export function getCsvExampleValue(fieldName: string, example?: string | null) {
    if (fieldName === 'tagNames') {
        return '战斗|奇幻|漫画改'
    }

    return example ?? ''
}

export function buildCsvTemplate(template: ImportTemplateResponse) {
    const headers = template.fields.map((field) => field.fieldName).join(',')
    const values = template.fields
        .map((field) => escapeCsvCell(getCsvExampleValue(field.fieldName, field.example)))
        .join(',')

    return `${headers}\n${values}`
}

export function parseCsvPayload(text: string, target: ImportTarget) {
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

export function parseDraftPayload(text: string, mode: ImportMode, target: ImportTarget): ParsedPayload {
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

export function getPreviewItems(preview: PreviewState | null): PreviewItem[] {
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
            changeSummary: item.changeSummary,
        }))
    }

    if (preview.target === 'COLLECTION') {
        return preview.data.items.map((item: UserSubjectCollectionImportPreviewItemResponse) => ({
            title: item.subjectDisplayTitle,
            result: item.result,
            message: item.message,
            candidates: item.candidates,
        }))
    }

    return preview.data.items.map((item: UserSubjectRecordImportPreviewItemResponse) => ({
        title: item.subjectDisplayTitle,
        result: item.result,
        message: item.message,
        candidates: item.candidates,
    }))
}

export function getImportResultItems(result: ImportResultState | null): PreviewItem[] {
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

export function pickTemplate(
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

export function isModeAllowedForTarget(mode: ImportMode, target: ImportTarget) {
    if (target === 'SUBJECT') {
        return mode === 'BANGUMI' || mode === 'JSON'
    }

    return mode === 'JSON' || mode === 'CSV'
}
