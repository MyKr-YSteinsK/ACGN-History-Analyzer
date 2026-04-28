import { apiGet } from './request'
import type { EnumOptionResponse } from '../types/api'

export function getCategoryOptions() {
    return apiGet<EnumOptionResponse[]>('/options/categories')
}

export function getQuarterOptions() {
    return apiGet<EnumOptionResponse[]>('/options/quarters')
}

export function getCollectionTypeOptions() {
    return apiGet<EnumOptionResponse[]>('/options/collection-types')
}

export function getLibraryStatusOptions() {
    return apiGet<EnumOptionResponse[]>('/options/library-statuses')
}
