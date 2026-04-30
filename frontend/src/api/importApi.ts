import { apiGet, apiPost } from './request'
import type { ReleaseQuarter } from '../types/common'
import type {
    SubjectImportPreviewResponse,
    SubjectImportResponse,
} from './subjectApi'

export type ImportEndpointDocResponse = {
    name: string
    method: string
    path: string
    description: string
}

export type ImportFieldDocResponse = {
    fieldName: string
    required: boolean
    description: string
    example: string
    notes: string
}

export type ImportOverviewResponse = {
    moduleName: string
    recommendedOrder: string
    steps: string[]
    notes: string[]
    endpoints: ImportEndpointDocResponse[]
}

export type ImportTemplateResponse = {
    importType: string
    endpoint: string
    recommendedOrder: string
    rules: string[]
    templateBody: Record<string, unknown>
    fields: ImportFieldDocResponse[]
}

export type BangumiSeasonImportRequest = {
    year: number
    quarter: ReleaseQuarter
    includeWeb: boolean
    japaneseOnly: boolean
}

export function getImportOverview() {
    return apiGet<ImportOverviewResponse>('/import/overview')
}

export function getSubjectImportTemplate() {
    return apiGet<ImportTemplateResponse>('/import/templates/subject')
}

export function getRecordImportTemplate() {
    return apiGet<ImportTemplateResponse>('/import/templates/record')
}

export function getCollectionImportTemplate() {
    return apiGet<ImportTemplateResponse>('/import/templates/collection')
}

export function previewBangumiSeasonImport(payload: BangumiSeasonImportRequest) {
    return apiPost<SubjectImportPreviewResponse, BangumiSeasonImportRequest>(
        '/import/bangumi/season/preview',
        payload,
    )
}

export function importBangumiSeason(payload: BangumiSeasonImportRequest) {
    return apiPost<SubjectImportResponse, BangumiSeasonImportRequest>(
        '/import/bangumi/season/import',
        payload,
    )
}
