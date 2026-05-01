import { apiGet, apiPost, apiRequest } from './request'
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
    excludedSourceIds?: string[]
}

export type BangumiSubjectImportItemRequest = {
    sourceId: string
    releaseYear: number
    releaseQuarter: ReleaseQuarter
}

export type BangumiSubjectBatchImportRequest = {
    subjects: BangumiSubjectImportItemRequest[]
}

export type BangumiImportJobStartResponse = {
    jobId: string
}

export type BangumiImportJobStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'

export type BangumiImportJobStatusResponse = {
    jobId: string
    status: BangumiImportJobStatus
    stage: string
    message: string
    percent: number
    result: SubjectImportResponse | null
    errorMessage: string | null
    createdAt: string
    updatedAt: string
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
    return apiRequest<SubjectImportPreviewResponse>({
        method: 'POST',
        url: '/import/bangumi/season/preview',
        data: payload,
        timeout: 45000,
    })
}

export function startBangumiSeasonImportJob(payload: BangumiSeasonImportRequest) {
    return apiPost<BangumiImportJobStartResponse, BangumiSeasonImportRequest>(
        '/import/bangumi/season/import-jobs',
        payload,
    )
}

export function getBangumiSeasonImportJobStatus(jobId: string) {
    return apiGet<BangumiImportJobStatusResponse>(
        `/import/bangumi/season/import-jobs/${jobId}`,
    )
}

export function previewBangumiSubjectImport(payload: BangumiSubjectBatchImportRequest) {
    return apiPost<SubjectImportPreviewResponse, BangumiSubjectBatchImportRequest>(
        '/import/bangumi/subject/preview',
        payload,
    )
}

export function importBangumiSubject(payload: BangumiSubjectBatchImportRequest) {
    return apiPost<SubjectImportResponse, BangumiSubjectBatchImportRequest>(
        '/import/bangumi/subject/import',
        payload,
    )
}
