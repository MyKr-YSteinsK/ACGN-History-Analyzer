import { apiGet } from './request'

export type TagResponse = {
    id: number
    name: string
    tagType: 'GENRE' | 'ELEMENT' | 'MOOD' | 'STYLE' | 'CUSTOM'
    colorKey: string
    builtin: boolean
    status: 'NORMAL' | 'HIDDEN'
    createdAt: string
    updatedAt: string
}

export function getTags(params?: { keyword?: string }) {
    return apiGet<TagResponse[]>('/tags', params)
}
