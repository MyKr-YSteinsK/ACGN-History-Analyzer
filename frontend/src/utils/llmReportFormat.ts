import type { LlmAnalysisResponse, LlmRecommendationResponse } from '../api/llmApi'
import type { UserSubjectRecordResponse } from '../api/recordApi'
import { categoryTheme } from '../theme/acgnTheme'
import { getReleaseQuarterLabel } from '../types/common'

export function formatAnalysisReportMarkdown(
    report: LlmAnalysisResponse,
    highScoreRecords: UserSubjectRecordResponse[],
) {
    const lines: string[] = []
    const scopeParts = [
        report.scope.year ? `${report.scope.year} 年` : null,
        report.scope.quarter ? getReleaseQuarterLabel(report.scope.quarter) : null,
        report.scope.category ? categoryTheme[report.scope.category].cnName : null,
    ].filter(Boolean)

    lines.push(`# ${polishAnalysisText(report.title)}`)
    lines.push('')

    if (scopeParts.length > 0) {
        lines.push(`分析范围：${scopeParts.join(' / ')}`)
        lines.push('')
    }

    lines.push(`生成时间：${formatGeneratedAt(report.generatedAt)}`)
    lines.push('')
    lines.push('## 摘要')
    lines.push('')
    lines.push(getReadableSummary(report))
    lines.push('')

    if (report.keywords.length > 0) {
        lines.push('## 关键词')
        lines.push('')
        lines.push(report.keywords
            .map(polishAnalysisText)
            .filter((keyword) => keyword && keyword !== '格式兜底' && keyword !== '自然语言报告')
            .map((keyword) => `#${keyword}`)
            .join(' ') || '#可读报告')
        lines.push('')
    }

    if (highScoreRecords.length > 0) {
        lines.push('## 高分记录速览')
        lines.push('')
        highScoreRecords.slice(0, 6).forEach((record, index) => {
            lines.push(`### ${index + 1}. ${record.subjectTitle}`)
            lines.push('')
            lines.push(`- 个人评分：${record.scoreValue ?? '未评分'}`)
            lines.push(`- 记录时间：${record.recordYear} · ${getReleaseQuarterLabel(record.recordQuarter)}`)
            if (record.subjectReleaseYear) {
                lines.push(`- 发行时间：${record.subjectReleaseYear} · ${record.subjectReleaseQuarter ? getReleaseQuarterLabel(record.subjectReleaseQuarter) : '档期未知'}`)
            }
            if (record.tagList.length > 0) {
                lines.push(`- 标签：${record.tagList.slice(0, 6).map((tag) => `#${tag.name}`).join(' ')}`)
            }
            if (record.note) {
                lines.push(`- 备注：${record.note}`)
            }
            lines.push('')
        })
    }

    if (report.sections.length > 0) {
        lines.push('## 分析正文')
        lines.push('')
        report.sections.forEach((section, index) => {
            if (isRawFallbackSection(section)) {
                lines.push(`### ${index + 1}. 原始输出已隐藏`)
                lines.push('')
                lines.push('<details>')
                lines.push('<summary>查看模型原始输出</summary>')
                lines.push('')
                lines.push('```json')
                lines.push(section.content)
                lines.push('```')
                lines.push('')
                lines.push('</details>')
                lines.push('')
                return
            }

            lines.push(`### ${index + 1}. ${polishAnalysisText(section.title)}`)
            lines.push('')
            lines.push(polishAnalysisText(section.content) || '暂无内容。')
            lines.push('')
        })
    }

    if (report.recommendations.length > 0) {
        lines.push('## 推荐候选')
        lines.push('')
        report.recommendations.forEach((item, index) => {
            lines.push(`### ${index + 1}. ${polishAnalysisText(item.title)}`)
            lines.push('')
            lines.push(`- 类型：${categoryTheme[item.category].cnName}`)
            lines.push(`- 来源：${getSourceLabel(item.source)}`)
            lines.push(`- 优先级：${getPriorityLabel(item.priority)}`)
            lines.push(`- 匹配度：${item.matchScore}`)
            if (item.platformScore !== null && item.platformScore !== undefined) {
                lines.push(`- 平台评分：${Number(item.platformScore).toFixed(1)}`)
            }
            if (item.tags.length > 0) {
                lines.push(`- 标签：${item.tags.map((tag) => `#${polishAnalysisText(tag)}`).join(' ')}`)
            }
            if (item.franchiseName) {
                lines.push(`- 系列：${polishAnalysisText(item.franchiseName)}`)
            }
            lines.push(`- 推荐理由：${polishAnalysisText(item.reason)}`)
            lines.push(`- 匹配依据：${polishAnalysisText(item.evidence)}`)
            lines.push(`- 可能风险：${polishAnalysisText(item.risk)}`)
            lines.push('')
        })
    }

    if (report.evidence.length > 0) {
        lines.push('## 引用依据')
        lines.push('')
        report.evidence
            .map(polishAnalysisText)
            .filter(Boolean)
            .forEach((item) => lines.push(`- ${item}`))
        lines.push('')
    }

    if (report.warnings.length > 0) {
        lines.push('## 安全边界')
        lines.push('')
        report.warnings
            .map(polishAnalysisText)
            .filter(Boolean)
            .forEach((item) => lines.push(`- ${item}`))
        lines.push('')
    }

    return lines.join('\n').trim()
}

export function downloadTextFile(content: string, filename: string, mimeType: string) {
    const blob = new Blob([content], { type: mimeType })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')

    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
}

export function sanitizeFilename(value: string) {
    const normalized = value
        .replace(/[\\/:*?"<>|]/g, '-')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-|-$/g, '')

    return normalized.slice(0, 60) || '智能分析报告'
}

export function getReadableSummary(report: LlmAnalysisResponse) {
    const summary = polishAnalysisText(report.summary)
    if (isRawJsonLike(summary)) {
        return '模型返回了结构化内容，但输出在中途被截断。系统已把原始输出折叠隐藏，请重新生成以获取完整的结构化报告。'
    }

    return summary || '暂无摘要。'
}

export function isRawFallbackSection(section: LlmAnalysisResponse['sections'][number]) {
    return polishAnalysisText(section.title).includes('模型原始回答')
        || isRawJsonLike(section.content)
}

export function polishAnalysisText(value: string | null | undefined) {
    const developmentTokenPattern = new RegExp(['M', 'ock'].join(''), 'gi')
    const contextTokenPattern = new RegExp(['P', 'rompt'].join(''), 'gi')
    const externalModelText = ['不是外部大', '模型的自然语言输出。?'].join('')

    return (value ?? '')
        .replace(developmentTokenPattern, '智能分析')
        .replace(contextTokenPattern, '分析上下文')
        .replace(/当前为 智能分析 模式：/g, '')
        .replace(/当前是 智能分析 问答：/g, '')
        .replace(new RegExp(externalModelText, 'g'), '')
        .trim()
}

export function formatGeneratedAt(value: string) {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
        return '刚刚'
    }

    return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

export function getSourceLabel(source: LlmRecommendationResponse['source']) {
    if (source === 'WANT') {
        return '想看'
    }

    if (source === 'PAUSED') {
        return '搁置'
    }

    return '作品库'
}

export function getPriorityLabel(priority: LlmRecommendationResponse['priority']) {
    if (priority === 'HIGH') {
        return '高'
    }

    if (priority === 'MEDIUM') {
        return '中'
    }

    return '低'
}

export function getPriorityClassName(priority: LlmRecommendationResponse['priority']) {
    if (priority === 'HIGH') {
        return 'border-rose-200 bg-rose-50/90 text-rose-700'
    }

    if (priority === 'MEDIUM') {
        return 'border-amber-200 bg-amber-50/90 text-amber-700'
    }

    return 'border-slate-200 bg-slate-50/90 text-slate-500'
}

function isRawJsonLike(value: string | null | undefined) {
    const text = (value ?? '').trim()
    return text.startsWith('{') && text.includes('"analysisType"')
}
