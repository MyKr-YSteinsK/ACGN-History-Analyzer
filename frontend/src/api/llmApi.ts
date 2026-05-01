import { apiGet, apiPost, apiPostWithConfig } from './request'
import type { AcgnCategory, ReleaseQuarter } from '../types/common'

const LLM_GENERATION_TIMEOUT_MS = 60000

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
    | 'INTEREST_TREND'
    | 'FRANCHISE_COMPLETION'
    | 'COMPREHENSIVE_REPORT'
    | 'HOME_INSIGHT'
    | 'QUESTION_ANSWER'

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
    subjectId: number | null
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
    matchScore: number | null
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
    baseUrl: string
    model: string
    mockMode: boolean
    apiKeyConfigured: boolean
}

export type LlmConfigUpdateRequest = {
    previewMode: boolean
    baseUrl?: string
    apiKey?: string
    model?: string
    timeoutSeconds?: number
    temperature?: number
    maxTokens?: number
}

export type LlmConnectionTestResponse = {
    available: boolean
    previewMode: boolean
    provider: string
    model: string
    message: string
    checkedAt: string
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

export type InterestTrendAnalysisParams = {
    category?: AcgnCategory
}

export type FranchiseCompletionAnalysisParams = {
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

export type QuestionAnswerAnalysisParams = {
    question: string
}

export type LlmQuestionAnswerResponse = {
    question: string
    answer: string
    mockMode: boolean
    modelName: string
    generatedAt: string
}

export function getLlmStatus() {
    return apiGet<LlmStatusResponse>('/llm/status')
}

export function updateLlmConfig(data: LlmConfigUpdateRequest) {
    return apiPost<LlmStatusResponse, LlmConfigUpdateRequest>('/llm/config', data)
}

export function testLlmConnection() {
    return llmPost<LlmConnectionTestResponse>('/llm/test-connection')
}

export function getYearlyAnalysis(params: YearlyAnalysisParams) {
    return llmPost<LlmAnalysisResponse, YearlyAnalysisParams>('/llm/analysis/yearly', params)
}

export function getQuarterlyAnalysis(params: QuarterlyAnalysisParams) {
    return llmPost<LlmAnalysisResponse, QuarterlyAnalysisParams>('/llm/analysis/quarterly', params)
}

export function getTypePreferenceAnalysis(params: TypePreferenceAnalysisParams) {
    return llmPost<LlmAnalysisResponse, TypePreferenceAnalysisParams>('/llm/analysis/type-preference', params)
}

export function getScoreStructureAnalysis(params: ScoreStructureAnalysisParams) {
    return llmPost<LlmAnalysisResponse, ScoreStructureAnalysisParams>('/llm/analysis/score-structure', params)
}

export function getTagPreferenceAnalysis(params: TagPreferenceAnalysisParams) {
    return llmPost<LlmAnalysisResponse, TagPreferenceAnalysisParams>('/llm/analysis/tag-preference', params)
}

export function getRecommendationAnalysis(params: RecommendationAnalysisParams) {
    return llmPost<LlmAnalysisResponse, RecommendationAnalysisParams>('/llm/analysis/recommendations', params)
}

export function getCollectionBacklogAnalysis(params: CollectionBacklogAnalysisParams) {
    return llmPost<LlmAnalysisResponse, CollectionBacklogAnalysisParams>('/llm/analysis/collection-backlog', params)
}

export function getTasteProfileAnalysis(params: TasteProfileAnalysisParams) {
    return llmPost<LlmAnalysisResponse, TasteProfileAnalysisParams>('/llm/analysis/taste-profile', params)
}

export function getPreferenceContradictionAnalysis(params: PreferenceContradictionAnalysisParams) {
    return llmPost<LlmAnalysisResponse, PreferenceContradictionAnalysisParams>('/llm/analysis/preference-contradictions', params)
}

export function getInterestTrendAnalysis(params: InterestTrendAnalysisParams) {
    return llmPost<LlmAnalysisResponse, InterestTrendAnalysisParams>('/llm/analysis/interest-trend', params)
}

export function getFranchiseCompletionAnalysis(params: FranchiseCompletionAnalysisParams) {
    return llmPost<LlmAnalysisResponse, FranchiseCompletionAnalysisParams>('/llm/analysis/franchise-completion', params)
}

export function getComprehensiveReportAnalysis(params: ComprehensiveReportAnalysisParams) {
    return llmPost<LlmAnalysisResponse, ComprehensiveReportAnalysisParams>('/llm/analysis/comprehensive-report', params)
}

export function getHomeInsightAnalysis(params: HomeInsightAnalysisParams) {
    return llmPost<LlmAnalysisResponse, HomeInsightAnalysisParams>('/llm/analysis/home-insight', params)
}

export function getQuestionAnswerAnalysis(params: QuestionAnswerAnalysisParams) {
    return llmPost<LlmQuestionAnswerResponse, QuestionAnswerAnalysisParams>('/llm/qa', params)
}

function llmPost<T, B = unknown>(url: string, data?: B) {
    return apiPostWithConfig<T, B>(url, data, {
        timeout: LLM_GENERATION_TIMEOUT_MS,
    })
}
