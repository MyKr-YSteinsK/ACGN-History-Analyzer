import type { AcgnCategory, CollectionType, ReleaseQuarter } from '../types/common'
import type { SubjectPageSort } from '../api/subjectApi'
import type { UserSubjectRecordResponse } from '../api/recordApi'

export const queryKeys = {
    subjectPage: (params: {
        category?: AcgnCategory
        keyword?: string
        year?: number
        quarter?: ReleaseQuarter
        status?: string
        tagName?: string
        page: number
        size: number
        sort: SubjectPageSort
    }) => ['subjectPage', params] as const,

    tags: (keyword?: string) => ['tags', keyword ?? ''] as const,

    collections: (params: {
        type: CollectionType
        category: AcgnCategory
        keyword?: string
        page: number
        size: number
    }) => ['collections', params] as const,

    recordsList: () => ['records', 'list'] as const,

    franchisesList: () => ['franchises', 'list'] as const,

    franchisesOverview: () => ['franchises', 'overview'] as const,

    topPage: (category: AcgnCategory) => ['top', category] as const,

    backgroundJob: (jobId: string) => ['backgroundJob', jobId] as const,
}

export type TopPageBundle = {
    rankingRecords: UserSubjectRecordResponse[]
    scoreBands: Array<{
        key: string
        label: string
        minScore: number
        maxScore: number
        count: number
        className: string
    }>
}
