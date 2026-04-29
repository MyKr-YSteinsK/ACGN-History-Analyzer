import { apiGet } from './request'
import type { AcgnCategory, ReleaseQuarter } from '../types/common'

export type LlmAnalysisType =
    | 'YEARLY'
    | 'QUARTERLY'
    | 'TYPE_PREFERENCE'
    | 'SCORE_STRUCTURE'
    | 'TAG_PREFERENCE'
    | 'RECOMMENDATION'
    | 'COLLECTION_BACKLOG'
    | 'TASTE_PROFILE'
    | 'PREFERENCE_CONTRADICTION'
    | 'COMPREHENSIVE_REPORT'
    | 'HOME_INSIGHT'

export type LlmAnalysisScope = {
    year: number | null
    quarter: ReleaseQuarter | null
    category: AcgnCategory | null
}

export type LlmAnalysisSection = {
    title: string
    content: string
}

export type LlmRecommendationSource = 'WANT' | 'PAUSED' | 'LIBRARY'

export type LlmRecommendationPriority = 'HIGH' | 'MEDIUM' | 'LOW'

export type LlmRecommendationResponse = {
    subjectId: number
    title: string
    coverUrl: string | null
    category: AcgnCategory
    tags: string[]
    franchiseName: string | null
    source: LlmRecommendationSource
    reason: string
    risk: string
    evidence: string
    priority: LlmRecommendationPriority
    matchScore: number
    platformScore: number | null
}

export type LlmAnalysisResponse = {
    analysisType: LlmAnalysisType
    title: string
    summary: string
    keywords: string[]
    sections: LlmAnalysisSection[]
    recommendations: LlmRecommendationResponse[]
    evidence: string[]
    warnings: string[]
    scope: LlmAnalysisScope
    mockMode: boolean
    modelName: string
    generatedAt: string
}

export type LlmStatusResponse = {
    enabled: boolean
    provider: string
    model: string
    mockMode: boolean
}

export type YearlyAnalysisParams = {
    year?: number
    category?: AcgnCategory
}

export type QuarterlyAnalysisParams = YearlyAnalysisParams & {
    quarter?: ReleaseQuarter
}

export type TypePreferenceAnalysisParams = {
    category?: AcgnCategory
}

export type ScoreStructureAnalysisParams = {
    category?: AcgnCategory
}

export type TagPreferenceAnalysisParams = {
    category?: AcgnCategory
}

export type RecommendationAnalysisParams = {
    category?: AcgnCategory
}

export type CollectionBacklogAnalysisParams = {
    category?: AcgnCategory
}

export type TasteProfileAnalysisParams = {
    category?: AcgnCategory
}

export type PreferenceContradictionAnalysisParams = {
    category?: AcgnCategory
}

export type ComprehensiveReportAnalysisParams = {
    year?: number
    category?: AcgnCategory
}

export type HomeInsightAnalysisParams = {
    year: number
    quarter: ReleaseQuarter
    category: AcgnCategory
}

export function getLlmStatus() {
    return apiGet<LlmStatusResponse>('/llm/status')
}

export function getYearlyAnalysis(params: YearlyAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/yearly', params)
}

export function getQuarterlyAnalysis(params: QuarterlyAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/quarterly', params)
}

export function getTypePreferenceAnalysis(params: TypePreferenceAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/type-preference', params)
}

export function getScoreStructureAnalysis(params: ScoreStructureAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/score-structure', params)
}

export function getTagPreferenceAnalysis(params: TagPreferenceAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/tag-preference', params)
}

export function getRecommendationAnalysis(params: RecommendationAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/recommendations', params)
}

export function getCollectionBacklogAnalysis(params: CollectionBacklogAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/collection-backlog', params)
}

export function getTasteProfileAnalysis(params: TasteProfileAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/taste-profile', params)
}

export function getPreferenceContradictionAnalysis(params: PreferenceContradictionAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/preference-contradictions', params)
}

export function getComprehensiveReportAnalysis(params: ComprehensiveReportAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/comprehensive-report', params)
}

export function getHomeInsightAnalysis(params: HomeInsightAnalysisParams) {
    return apiGet<LlmAnalysisResponse>('/llm/analysis/home-insight', params)
}
