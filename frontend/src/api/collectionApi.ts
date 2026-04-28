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
