import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type { UserSubjectCollectionResponse } from './collectionApi'
import type { UserSubjectRecordResponse } from './recordApi'
import type { TagResponse } from './tagApi'
import type { AcgnCategory, LibraryStatus, ReleaseQuarter } from '../types/common'
import type { PageQuery, PageResponse } from '../types/api'

export type SubjectStatus = 'NORMAL' | 'HIDDEN'

export type SubjectPageSort =
    | 'TITLE_ASC'
    | 'TITLE_DESC'
    | 'PLATFORM_SCORE_DESC'
    | 'PLATFORM_SCORE_ASC'
    | 'RELEASE_NEWEST'
    | 'RELEASE_OLDEST'

export type FranchiseSimpleResponse = {
    id: number
    name: string
    nameCn: string | null
}

export type SubjectResponse = {
    id: number
    titleCn: string
    subtitle: string | null
    displayTitle: string
    seasonIndex: number | null
    partIndex: number | null
    franchiseId: number | null
    coverUrl: string | null
    summary: string | null
    releaseYear: number
    releaseQuarter: ReleaseQuarter
    category: AcgnCategory
    studioName: string | null
    platformLink: string | null
    platformScore: number | null
    personalScore: number | null
    episodeCount: number | null
    tagList: TagResponse[]
    sourcePlatform: string | null
    sourceId: string | null
    status: SubjectStatus
    libraryStatus: LibraryStatus
    libraryStatusLabel: string
    createdAt: string
    updatedAt: string
}

export type SubjectDetailResponse = SubjectResponse & {
    recorded: boolean
    recordCount: number
    records: UserSubjectRecordResponse[]
    record: UserSubjectRecordResponse | null
    collection: UserSubjectCollectionResponse | null
    franchise: FranchiseSimpleResponse | null
}

export type SubjectCreateRequest = {
    titleCn: string
    subtitle?: string | null
    displayTitle: string
    seasonIndex?: number | null
    partIndex?: number | null
    franchiseId?: number | null
    franchiseName?: string | null
    coverUrl?: string | null
    summary?: string | null
    releaseYear: number
    releaseQuarter: ReleaseQuarter
    category: AcgnCategory
    studioName?: string | null
    platformLink?: string | null
    platformScore?: number | null
    episodeCount?: number | null
    tagNames?: string[] | null
    sourcePlatform?: string | null
    sourceId?: string | null
    status: SubjectStatus
}

export type SubjectListParams = {
    year?: number
    quarter?: ReleaseQuarter
    category?: AcgnCategory
    keyword?: string
    status?: SubjectStatus | LibraryStatus
    tagName?: string
}

export type SubjectPageParams = SubjectListParams &
    PageQuery & {
        sort?: SubjectPageSort
    }

export type SubjectBatchImportRequest = {
    subjects: SubjectCreateRequest[]
}

export type SubjectImportPreviewItemResponse = {
    displayTitle: string
    result: string
    message: string
    sourcePlatform?: string | null
    sourceId?: string | null
    changeSummary?: string | null
}

export type SubjectImportPreviewResponse = {
    totalCount: number
    canImportCount: number
    skippedCount: number
    updatedCount: number
    items: SubjectImportPreviewItemResponse[]
}

export type SubjectImportItemResponse = {
    subjectId: number | null
    displayTitle: string
    result: string
    message: string
}

export type SubjectImportResponse = {
    totalCount: number
    importedCount: number
    skippedCount: number
    items: SubjectImportItemResponse[]
}

export function createSubject(payload: SubjectCreateRequest) {
    return apiPost<SubjectResponse, SubjectCreateRequest>('/subjects', payload)
}

export function getSubjects(params?: SubjectListParams) {
    return apiGet<SubjectResponse[]>('/subjects', params)
}

export function getSubjectPage(params?: SubjectPageParams) {
    return apiGet<PageResponse<SubjectResponse>>('/subjects/page', params)
}

export function getSubjectById(id: number) {
    return apiGet<SubjectDetailResponse>(`/subjects/${id}`)
}

export function updateSubject(id: number, payload: SubjectCreateRequest) {
    return apiPut<SubjectResponse, SubjectCreateRequest>(`/subjects/${id}`, payload)
}

export function deleteSubject(id: number) {
    return apiDelete<string>(`/subjects/${id}`)
}

export function previewImportSubjects(payload: SubjectBatchImportRequest) {
    return apiPost<SubjectImportPreviewResponse, SubjectBatchImportRequest>(
        '/subjects/import/preview',
        payload,
    )
}

export function importSubjects(payload: SubjectBatchImportRequest) {
    return apiPost<SubjectImportResponse, SubjectBatchImportRequest>(
        '/subjects/import',
        payload,
    )
}
