import type { AcgnCategory, ReleaseQuarter } from './common'

export type CategoryFilter = AcgnCategory | 'ALL'

export type AnalysisMode =
    | 'YEARLY'
    | 'QUARTERLY'
    | 'TYPE_PREFERENCE'
    | 'SCORE_STRUCTURE'
    | 'TAG_PREFERENCE'
    | 'RECOMMENDATION'
    | 'COLLECTION_BACKLOG'
    | 'TASTE_PROFILE'
    | 'PREFERENCE_CONTRADICTION'
    | 'INTEREST_TREND'
    | 'FRANCHISE_COMPLETION'
    | 'COMPREHENSIVE_REPORT'
    | 'QUESTION_ANSWER'

export type AnalysisModeMeta = {
    value: AnalysisMode
    label: string
    description: string
    accent: string
}

export type ReportHistoryItem = {
    id: string
    cacheKey: string
    analysisType: AnalysisMode
    title: string
    scope: string
    mode: string
    generatedAt: string
    year: number
    quarter: ReleaseQuarter
    category: CategoryFilter
    question: string
}

export type GenerationJobStatus = 'queued' | 'running' | 'completed' | 'failed'
