import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type { AcgnCategory, ReleaseQuarter } from '../types/common'
import type { PageQuery, PageResponse } from '../types/api'
import type { TagResponse } from './tagApi'

export type RecordPageSort =
    | 'RECORD_TIME_DESC'
    | 'RECORD_TIME_ASC'
    | 'SCORE_DESC'
    | 'SCORE_ASC'
    | 'TITLE_ASC'
    | 'TITLE_DESC'

export type UserSubjectRecordResponse = {
    id: number
    subjectId: number
    subjectTitle: string
    subjectCategory: AcgnCategory | null
    subjectReleaseYear: number | null
    subjectReleaseQuarter: ReleaseQuarter | null
    subjectCoverUrl: string | null
    tagList: TagResponse[]
    scoreValue: number | null
    recordYear: number
    recordQuarter: ReleaseQuarter
    note: string | null
    createdAt: string
    updatedAt: string
}

export type UserSubjectRecordCreateRequest = {
    subjectId: number
    scoreValue?: number | null
    recordYear: number
    recordQuarter: ReleaseQuarter
    note?: string | null
}

export type RecordPageParams = PageQuery & {
    year?: number
    quarter?: ReleaseQuarter
    minScore?: number
    maxScore?: number
    category?: AcgnCategory
    keyword?: string
    sort?: RecordPageSort
}

export type RecordListParams = {
    year?: number
    quarter?: ReleaseQuarter
    minScore?: number
    maxScore?: number
}

export type RecordScoreBandStatsResponse = {
    totalCount: number
    excellentCount: number
    normalCount: number
    badCount: number
    unratedCount: number
}

export type RecordQuarterOverviewResponse = {
    year: number
    quarter: ReleaseQuarter
    totalCount: number
    highScoreCount: number
    watchedCount: number
    onHoldCount: number
    wantToWatchCount: number
    excellentCount: number
    normalCount: number
    badCount: number
    unratedCount: number
}

export type RecordYearOverviewResponse = {
    year: number
    totalCount: number
    highScoreCount: number
    watchedCount: number
    onHoldCount: number
    wantToWatchCount: number
    excellentCount: number
    normalCount: number
    badCount: number
    unratedCount: number
    q1Count: number
    q2Count: number
    q3Count: number
    q4Count: number
}

export type UserSubjectRecordImportItemRequest = {
    subjectId?: number | null
    subjectTitle?: string | null
    subjectSeasonIndex?: number | null
    subjectPartIndex?: number | null
    sourceId?: string | null
    scoreValue?: number | null
    recordYear: number
    recordQuarter: ReleaseQuarter
    note?: string | null
}

export type SubjectMatchCandidateResponse = {
    subjectId: number
    displayTitle: string
    seasonIndex: number | null
    partIndex: number | null
    releaseYear: number | null
    releaseQuarter: ReleaseQuarter | null
    category: AcgnCategory | null
}

export type UserSubjectRecordBatchImportRequest = {
    records: UserSubjectRecordImportItemRequest[]
}

export type UserSubjectRecordImportPreviewItemResponse = {
    subjectDisplayTitle: string
    result: string
    message: string
    candidates?: SubjectMatchCandidateResponse[]
}

export type UserSubjectRecordImportPreviewResponse = {
    totalCount: number
    canImportCount: number
    skippedCount: number
    items: UserSubjectRecordImportPreviewItemResponse[]
}

export type UserSubjectRecordImportItemResponse = {
    recordId: number | null
    subjectDisplayTitle: string
    result: string
    message: string
}

export type UserSubjectRecordImportResponse = {
    totalCount: number
    importedCount: number
    skippedCount: number
    items: UserSubjectRecordImportItemResponse[]
}

export function createRecord(payload: UserSubjectRecordCreateRequest) {
    return apiPost<UserSubjectRecordResponse, UserSubjectRecordCreateRequest>('/records', payload)
}

export function getRecords(params?: RecordListParams) {
    return apiGet<UserSubjectRecordResponse[]>('/records', params)
}

export function getRecordPage(params?: RecordPageParams) {
    return apiGet<PageResponse<UserSubjectRecordResponse>>('/records/page', params)
}

export function getRecordById(id: number) {
    return apiGet<UserSubjectRecordResponse>(`/records/${id}`)
}

export function updateRecord(id: number, payload: UserSubjectRecordCreateRequest) {
    return apiPut<UserSubjectRecordResponse, UserSubjectRecordCreateRequest>(`/records/${id}`, payload)
}

export function deleteRecord(id: number) {
    return apiDelete<string>(`/records/${id}`)
}

export function previewImportRecords(payload: UserSubjectRecordBatchImportRequest) {
    return apiPost<
        UserSubjectRecordImportPreviewResponse,
        UserSubjectRecordBatchImportRequest
    >('/records/import/preview', payload)
}

export function importRecords(payload: UserSubjectRecordBatchImportRequest) {
    return apiPost<UserSubjectRecordImportResponse, UserSubjectRecordBatchImportRequest>(
        '/records/import',
        payload,
    )
}

export function getScoreBandStats(params?: {
    year?: number
    quarter?: ReleaseQuarter
}) {
    return apiGet<RecordScoreBandStatsResponse>('/records/stats/score-bands', params)
}

export function getQuarterOverview(params: {
    year: number
    quarter: ReleaseQuarter
}) {
    return apiGet<RecordQuarterOverviewResponse>('/records/stats/quarter-overview', params)
}

export function getYearOverview(year: number) {
    return apiGet<RecordYearOverviewResponse>('/records/stats/year-overview', { year })
}

export function getHighScoreRecords(params?: {
    year?: number
    quarter?: ReleaseQuarter
    minScore?: number
}) {
    return apiGet<UserSubjectRecordResponse[]>('/records/high-score', params)
}
