import { apiDelete, apiGet, apiPost, apiPostWithConfig, apiPut } from './request'
import type { AcgnCategory, CollectionType, ReleaseQuarter } from '../types/common'
import type { PageQuery, PageResponse } from '../types/api'
import type { SubjectMatchCandidateResponse } from './recordApi'

export type UserSubjectCollectionResponse = {
    id: number
    subjectId: number
    subjectTitle: string
    subjectCategory: AcgnCategory | null
    subjectReleaseYear: number | null
    subjectReleaseQuarter: ReleaseQuarter | null
    subjectCoverUrl: string | null
    collectionType: CollectionType
    collectionTypeLabel: string
    note: string | null
    createdAt: string
    updatedAt: string
}

export type UserSubjectCollectionCreateRequest = {
    subjectId: number
    collectionType: CollectionType
    note?: string | null
}

export type UserSubjectCollectionUpdateRequest = {
    collectionType: CollectionType
    note?: string | null
}

export type CollectionPageParams = PageQuery & {
    type?: CollectionType
    category?: AcgnCategory
    keyword?: string
}

export type UserSubjectCollectionImportItemRequest = {
    subjectId?: number | null
    subjectTitle?: string | null
    subjectSeasonIndex?: number | null
    subjectPartIndex?: number | null
    sourceId?: string | null
    collectionType: CollectionType
    note?: string | null
}

export type UserSubjectCollectionBatchImportRequest = {
    collections: UserSubjectCollectionImportItemRequest[]
}

export type UserSubjectCollectionImportPreviewItemResponse = {
    subjectDisplayTitle: string
    result: string
    message: string
    candidates?: SubjectMatchCandidateResponse[]
}

export type UserSubjectCollectionImportPreviewResponse = {
    totalCount: number
    canImportCount: number
    skippedCount: number
    items: UserSubjectCollectionImportPreviewItemResponse[]
}

export type UserSubjectCollectionImportItemResponse = {
    collectionId: number | null
    subjectDisplayTitle: string
    result: string
    message: string
}

export type UserSubjectCollectionImportResponse = {
    totalCount: number
    importedCount: number
    skippedCount: number
    items: UserSubjectCollectionImportItemResponse[]
}

const IMPORT_REQUEST_TIMEOUT_MS = 180000

export function createOrMoveCollection(payload: UserSubjectCollectionCreateRequest) {
    return apiPost<UserSubjectCollectionResponse, UserSubjectCollectionCreateRequest>(
        '/collections',
        payload,
    )
}

export function addToWant(subjectId: number) {
    return apiPost<UserSubjectCollectionResponse>(`/collections/want/${subjectId}`)
}

export function addToPaused(subjectId: number) {
    return apiPost<UserSubjectCollectionResponse>(`/collections/paused/${subjectId}`)
}

export function getCollections(params?: CollectionPageParams) {
    return apiGet<PageResponse<UserSubjectCollectionResponse>>('/collections', params)
}

export function getCollectionById(id: number) {
    return apiGet<UserSubjectCollectionResponse>(`/collections/${id}`)
}

export function updateCollection(
    id: number,
    payload: UserSubjectCollectionUpdateRequest,
) {
    return apiPut<UserSubjectCollectionResponse, UserSubjectCollectionUpdateRequest>(
        `/collections/${id}`,
        payload,
    )
}

export function deleteCollection(id: number) {
    return apiDelete<string>(`/collections/${id}`)
}

export function previewImportCollections(payload: UserSubjectCollectionBatchImportRequest) {
    return apiPostWithConfig<
        UserSubjectCollectionImportPreviewResponse,
        UserSubjectCollectionBatchImportRequest
    >('/collections/import/preview', payload, { timeout: IMPORT_REQUEST_TIMEOUT_MS })
}

export function importCollections(payload: UserSubjectCollectionBatchImportRequest) {
    return apiPostWithConfig<
        UserSubjectCollectionImportResponse,
        UserSubjectCollectionBatchImportRequest
    >('/collections/import', payload, { timeout: IMPORT_REQUEST_TIMEOUT_MS })
}
