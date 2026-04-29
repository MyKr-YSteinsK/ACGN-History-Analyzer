import { apiDelete, apiGet, apiPost, apiPut } from './request'
import type { SubjectResponse } from './subjectApi'

export type FranchiseStatus = 'NORMAL' | 'HIDDEN'

export type FranchiseResponse = {
    id: number
    name: string
    nameCn: string | null
    description: string | null
    status: FranchiseStatus
    createdAt: string
    updatedAt: string
}

export type FranchiseDetailResponse = FranchiseResponse & {
    subjects: SubjectResponse[]
}

export type FranchiseCreateRequest = {
    name: string
    nameCn?: string | null
    description?: string | null
    status: FranchiseStatus
}

export function createFranchise(payload: FranchiseCreateRequest) {
    return apiPost<FranchiseResponse, FranchiseCreateRequest>('/franchises', payload)
}

export function getFranchises() {
    return apiGet<FranchiseResponse[]>('/franchises')
}

export function getFranchiseById(id: number) {
    return apiGet<FranchiseDetailResponse>(`/franchises/${id}`)
}

export function updateFranchise(id: number, payload: FranchiseCreateRequest) {
    return apiPut<FranchiseResponse, FranchiseCreateRequest>(
        `/franchises/${id}`,
        payload,
    )
}

export function deleteFranchise(id: number) {
    return apiDelete<string>(`/franchises/${id}`)
}

export function removeSubjectFromFranchise(franchiseId: number, subjectId: number) {
    return apiDelete<string>(`/franchises/${franchiseId}/subjects/${subjectId}`)
}
