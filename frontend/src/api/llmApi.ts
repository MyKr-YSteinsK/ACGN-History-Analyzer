import { apiGet } from './request'
import type { AcgnCategory, ReleaseQuarter } from '../types/common'

export type LlmAnalysisType =
    | 'YEARLY'
    | 'QUARTERLY'
    | 'TYPE_PREFERENCE'
    | 'SCORE_STRUCTURE'
    | 'TAG_PREFERENCE'
    | 'RECOMMENDATION'

export type LlmAnalysisScope = {
    year: number | null
    quarter: ReleaseQuarter | null
    category: AcgnCategory | null
}

export type LlmAnalysisSection = {
    title: string
    content: string
}

export type LlmAnalysisResponse = {
    analysisType: LlmAnalysisType
    title: string
    summary: string
    keywords: string[]
    sections: LlmAnalysisSection[]
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
