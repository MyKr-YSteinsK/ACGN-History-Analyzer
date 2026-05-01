import { useState } from 'react'
import type { LlmAnalysisResponse } from '../../api/llmApi'
import type { UserSubjectRecordResponse } from '../../api/recordApi'
import { categoryTheme } from '../../theme/acgnTheme'
import type { CategoryFilter, ReportHistoryItem } from '../../types/analysis'
import {
    downloadTextFile,
    formatAnalysisReportMarkdown,
    getReadableSummary,
    isRawFallbackSection,
    polishAnalysisText,
    sanitizeFilename,
} from '../../utils/llmReportFormat'
import GlassCard from '../common/GlassCard'
import ReportHistoryPanel from './ReportHistoryPanel'
import { InsightSnapshot, ReportReadingHighlights } from './ReportHighlights'
import RecommendationGrid from './ReportRecommendations'
import SidePanel from './SidePanel'

type AnalysisReportProps = {
    report: LlmAnalysisResponse
    history: ReportHistoryItem[]
    activeCategory: CategoryFilter
    highScoreRecords: UserSubjectRecordResponse[]
    onSelectHistory: (item: ReportHistoryItem) => void
    onDeleteHistory: (item: ReportHistoryItem) => void
    onPruneInvalidHistory: () => void
    onPruneExpiredHistory: () => void
    onClearHistory: () => void
}

function AnalysisReport({
    report,
    history,
    activeCategory,
    highScoreRecords,
    onSelectHistory,
    onDeleteHistory,
    onPruneInvalidHistory,
    onPruneExpiredHistory,
    onClearHistory,
}: AnalysisReportProps) {
    const [copyStatus, setCopyStatus] = useState<'idle' | 'success' | 'error'>('idle')
    const [downloadStatus, setDownloadStatus] = useState<'idle' | 'success' | 'error'>('idle')
    const visibleWarnings = report.warnings
        .map(polishAnalysisText)
        .filter(Boolean)
    const evidence = report.evidence
        .map(polishAnalysisText)
        .filter(Boolean)
    const summaryText = getReadableSummary(report)
    const visibleKeywords = report.keywords
        .map(polishAnalysisText)
        .filter((keyword) => keyword && keyword !== '格式兜底' && keyword !== '自然语言报告')
    const keywords = visibleKeywords.length > 0 ? visibleKeywords : ['可读报告']
    const theme =
        activeCategory === 'ALL'
            ? null
            : categoryTheme[activeCategory]

    async function handleCopyMarkdown() {
        try {
            await navigator.clipboard.writeText(formatAnalysisReportMarkdown(report, highScoreRecords))
            setCopyStatus('success')
        } catch {
            setCopyStatus('error')
        }

        window.setTimeout(() => setCopyStatus('idle'), 2200)
    }

    function handleDownloadMarkdown() {
        try {
            downloadTextFile(
                formatAnalysisReportMarkdown(report, highScoreRecords),
                `${sanitizeFilename(polishAnalysisText(report.title) || '智能分析报告')}.md`,
                'text/markdown;charset=utf-8',
            )
            setDownloadStatus('success')
        } catch {
            setDownloadStatus('error')
        }

        window.setTimeout(() => setDownloadStatus('idle'), 2200)
    }

    return (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_370px]">
            <GlassCard className="overflow-hidden">
                <div
                    className={[
                        'bg-gradient-to-br p-6 transition-all duration-300',
                        theme?.softGradient ?? 'from-white/75 via-sky-50/55 to-violet-50/55',
                    ].join(' ')}
                >
                    <div className="flex flex-wrap items-start justify-between gap-4">
                        <div className="max-w-4xl">
                            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                                analysis report
                            </p>
                            <h3 className="mt-2 text-2xl font-black text-slate-800">
                                {polishAnalysisText(report.title)}
                            </h3>
                            <p className="mt-3 text-sm leading-7 text-slate-500">
                                {summaryText}
                            </p>
                        </div>

                        <div className="flex shrink-0 flex-wrap items-center justify-end gap-2">
                            <span className="rounded-full border border-emerald-200 bg-emerald-50/85 px-4 py-2 text-xs font-black text-emerald-700">
                                已生成
                            </span>
                            <button
                                type="button"
                                onClick={() => void handleCopyMarkdown()}
                                className="rounded-full border border-sky-200 bg-white/75 px-4 py-2 text-xs font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                            >
                                {copyStatus === 'success'
                                    ? '已复制'
                                    : copyStatus === 'error'
                                      ? '复制失败'
                                      : '复制 Markdown'}
                            </button>
                            <button
                                type="button"
                                onClick={handleDownloadMarkdown}
                                className="rounded-full border border-emerald-200 bg-white/75 px-4 py-2 text-xs font-black text-emerald-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-emerald-50"
                            >
                                {downloadStatus === 'success'
                                    ? '已下载'
                                    : downloadStatus === 'error'
                                      ? '下载失败'
                                      : '下载 Markdown'}
                            </button>
                        </div>
                    </div>

                    <div className="mt-5 flex flex-wrap gap-2">
                        {keywords.map((keyword) => (
                            <span
                                key={keyword}
                                className="rounded-full border border-white/75 bg-white/65 px-3 py-1.5 text-xs font-semibold text-slate-500"
                            >
                                #{keyword}
                            </span>
                        ))}
                    </div>

                    <ReportReadingHighlights
                        report={report}
                        highScoreRecords={highScoreRecords}
                    />

                    <div className="mt-6 grid gap-4 lg:grid-cols-2">
                        {report.sections.map((section, index) => {
                            const rawFallbackSection = isRawFallbackSection(section)

                            return (
                                <article
                                    key={`${section.title}-${index}`}
                                    className={[
                                        'rounded-[26px] border border-white/75 bg-white/62 p-5 shadow-sm backdrop-blur',
                                        index === 0 ? 'lg:col-span-2' : '',
                                    ].join(' ')}
                                >
                                    <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-300">
                                        insight {String(index + 1).padStart(2, '0')}
                                    </p>
                                    <h4 className="mt-2 text-base font-black text-slate-800">
                                        {rawFallbackSection ? '原始输出已隐藏' : polishAnalysisText(section.title)}
                                    </h4>
                                    {rawFallbackSection ? (
                                        <details className="mt-3 rounded-2xl border border-slate-100 bg-white/58 px-4 py-3">
                                            <summary className="cursor-pointer text-sm font-black text-slate-500">
                                                查看模型原始输出
                                            </summary>
                                            <pre className="mt-3 max-h-96 overflow-auto whitespace-pre-wrap rounded-2xl bg-slate-950/90 p-4 text-xs leading-5 text-slate-100">
                                                {section.content}
                                            </pre>
                                        </details>
                                    ) : (
                                        <p className="mt-2 text-sm leading-7 text-slate-500">
                                            {polishAnalysisText(section.content)}
                                        </p>
                                    )}
                                </article>
                            )
                        })}
                    </div>

                    {report.recommendations?.length ? (
                        <RecommendationGrid recommendations={report.recommendations} />
                    ) : null}
                </div>
            </GlassCard>

            <div className="space-y-6">
                <InsightSnapshot report={report} />

                {evidence.length > 0 ? (
                    <SidePanel
                        title="引用依据"
                        eyebrow="evidence"
                        gradient="from-white/75 to-emerald-50/65"
                    >
                        <div className="space-y-2">
                            {evidence.slice(0, 5).map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-white/75 bg-white/62 px-4 py-3 text-sm leading-6 text-slate-500"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </SidePanel>
                ) : null}

                {visibleWarnings.length > 0 ? (
                    <SidePanel
                        title="安全边界"
                        eyebrow="boundary"
                        gradient="from-white/75 to-amber-50/70"
                    >
                        <div className="space-y-2">
                            {visibleWarnings.map((item) => (
                                <p
                                    key={item}
                                    className="rounded-2xl border border-amber-100 bg-white/62 px-4 py-3 text-sm leading-6 text-amber-700"
                                >
                                    {item}
                                </p>
                            ))}
                        </div>
                    </SidePanel>
                ) : null}

                <ReportHistoryPanel
                    items={history}
                    onSelect={onSelectHistory}
                    onDelete={onDeleteHistory}
                    onPruneInvalid={onPruneInvalidHistory}
                    onPruneExpired={onPruneExpiredHistory}
                    onClear={onClearHistory}
                />
            </div>
        </div>
    )
}

export default AnalysisReport
