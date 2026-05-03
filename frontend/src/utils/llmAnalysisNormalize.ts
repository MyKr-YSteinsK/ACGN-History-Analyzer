import type {
    LlmAnalysisResponse,
    LlmAnalysisType,
    LlmRecommendationResponse,
} from '../api/llmApi'
import { categoryOrder, type AcgnCategory } from '../types/common'
import { polishAnalysisText } from './llmReportFormat'

export function isAcgnCategory(value: unknown): value is AcgnCategory {
    return typeof value === 'string' && categoryOrder.includes(value as AcgnCategory)
}

export function normalizeLlmAnalysisReport(
    report: LlmAnalysisResponse | Partial<LlmAnalysisResponse> | null | undefined,
    fallbackType: LlmAnalysisType,
    fallbackTitle?: string,
): LlmAnalysisResponse {
    const title =
        polishAnalysisText(report?.title)
        || fallbackTitle
        || '智能分析报告'
    const summary =
        polishAnalysisText(report?.summary)
        || '模型没有返回摘要，系统已保留可用字段并提示重新生成。'
    const scope = report?.scope ?? {
        year: null,
        quarter: null,
        category: null,
    }

    return {
        analysisType: (report?.analysisType as LlmAnalysisType | undefined) || fallbackType,
        title,
        summary,
        keywords: normalizeTextArray(report?.keywords, ['智能分析']),
        sections: normalizeSections(report?.sections, summary),
        recommendations: normalizeRecommendations(report?.recommendations),
        evidence: normalizeTextArray(report?.evidence, ['本结果基于系统内结构化数据生成']),
        warnings: normalizeTextArray(report?.warnings, ['模型输出已做前端格式兜底，若结果不完整可重试生成']),
        scope: {
            year: scope.year ?? null,
            quarter: scope.quarter ?? null,
            category: isAcgnCategory(scope.category) ? scope.category : null,
        },
        mockMode: Boolean(report?.mockMode),
        modelName: report?.modelName || 'deepseek-v4-flash',
        generatedAt: report?.generatedAt || new Date().toISOString(),
    }
}

function normalizeTextArray(value: unknown, fallback: string[]) {
    if (!Array.isArray(value)) {
        return fallback
    }

    const normalized = value
        .map((item) => polishAnalysisText(String(item ?? '')))
        .filter(Boolean)

    return normalized.length > 0 ? normalized : fallback
}

function normalizeSections(value: unknown, summary: string) {
    if (!Array.isArray(value)) {
        return [
            {
                title: '分析摘要',
                content: summary,
            },
        ]
    }

    const sections = value
        .map((item, index) => {
            const section = item as Partial<LlmAnalysisResponse['sections'][number]>
            return {
                title: polishAnalysisText(section.title) || `分析段落 ${index + 1}`,
                content: polishAnalysisText(section.content) || summary,
            }
        })
        .filter((item) => item.title || item.content)

    return sections.length > 0
        ? sections
        : [
              {
                  title: '分析摘要',
                  content: summary,
              },
          ]
}

function normalizeRecommendations(value: unknown): LlmRecommendationResponse[] {
    if (!Array.isArray(value)) {
        return []
    }

    return value
        .map((item) => item as Partial<LlmRecommendationResponse>)
        .filter((item) => Boolean(item.title))
        .map((item) => ({
            subjectId: item.subjectId ?? null,
            title: polishAnalysisText(item.title) || '未命名候选',
            coverUrl: item.coverUrl ?? null,
            category: isAcgnCategory(item.category) ? item.category : 'ANIME',
            tags: normalizeTextArray(item.tags, []),
            franchiseName: polishAnalysisText(item.franchiseName) || null,
            source: item.source === 'WANT' || item.source === 'PAUSED' ? item.source : 'LIBRARY',
            reason: polishAnalysisText(item.reason) || '模型未返回推荐理由。',
            risk: polishAnalysisText(item.risk) || '暂无额外风险提示。',
            evidence: polishAnalysisText(item.evidence) || '暂无引用依据。',
            priority: item.priority === 'HIGH' || item.priority === 'MEDIUM' ? item.priority : 'LOW',
            matchScore: item.matchScore ?? null,
            platformScore: item.platformScore ?? null,
        }))
}
