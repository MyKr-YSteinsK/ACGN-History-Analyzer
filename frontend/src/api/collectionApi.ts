import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type { AcgnCategory, CollectionType, ReleaseQuarter } from '../types/common'
import type { PageQuery, PageResponse } from '../types/api'

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
    subjectTitleCn?: string | null
    subjectSubtitle?: string | null
    subjectSeasonIndex?: number | null
    subjectPartIndex?: number | null
    subjectCategory?: AcgnCategory | null
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
    return apiPost<
        UserSubjectCollectionImportPreviewResponse,
        UserSubjectCollectionBatchImportRequest
    >('/collections/import/preview', payload)
}

export function importCollections(payload: UserSubjectCollectionBatchImportRequest) {
    return apiPost<
        UserSubjectCollectionImportResponse,
        UserSubjectCollectionBatchImportRequest
    >('/collections/import', payload)
}
