import type { LlmAnalysisResponse } from '../../api/llmApi'
import type { UserSubjectRecordResponse } from '../../api/recordApi'
import CoverImage from '../common/CoverImage'
import { getReleaseQuarterLabel } from '../../types/common'
import { formatGeneratedAt, polishAnalysisText } from '../../utils/llmReportFormat'
import SidePanel from './SidePanel'

type InsightSnapshotProps = {
    report: LlmAnalysisResponse
}

function InsightSnapshot({ report }: InsightSnapshotProps) {
    return (
        <SidePanel
            title="分析快照"
            eyebrow="snapshot"
            gradient="from-white/75 to-sky-50/70"
        >
            <div className="grid grid-cols-2 gap-3">
                <SnapshotMetric label="结论段落" value={report.sections.length} />
                <SnapshotMetric label="关键词" value={report.keywords.length} />
                <SnapshotMetric label="推荐候选" value={report.recommendations.length} />
                <SnapshotMetric label="引用依据" value={report.evidence.length} />
            </div>
            <p className="mt-4 rounded-2xl border border-white/70 bg-white/55 px-4 py-3 text-xs leading-5 text-slate-400">
                生成时间：{formatGeneratedAt(report.generatedAt)}
            </p>
        </SidePanel>
    )
}

type ReportReadingHighlightsProps = {
    report: LlmAnalysisResponse
    highScoreRecords: UserSubjectRecordResponse[]
}

function ReportReadingHighlights({ report, highScoreRecords }: ReportReadingHighlightsProps) {
    const posterItems = report.recommendations
        .filter((item) => item.coverUrl)
        .slice(0, 4)
    const visibleHighScoreRecords = highScoreRecords.slice(0, 4)

    return (
        <div className="mt-6 grid gap-4 xl:grid-cols-[1.1fr_1fr]">
            <div className="rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
                <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-300">
                    reading guide
                </p>
                <h4 className="mt-2 text-base font-black text-slate-800">
                    先看这几个结论点
                </h4>
                <div className="mt-4 grid grid-cols-2 gap-3 md:grid-cols-4">
                    <SnapshotMetric label="正文段落" value={report.sections.length} />
                    <SnapshotMetric label="关键词" value={report.keywords.length} />
                    <SnapshotMetric label="推荐作品" value={report.recommendations.length} />
                    <SnapshotMetric label="依据条目" value={report.evidence.length} />
                </div>
            </div>

            {visibleHighScoreRecords.length > 0 ? (
                <div className="rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-300">
                        favorite records
                    </p>
                    <h4 className="mt-2 text-base font-black text-slate-800">
                        高分记录速览
                    </h4>
                    <div className="mt-4 grid grid-cols-2 gap-3 md:grid-cols-4 xl:grid-cols-2 2xl:grid-cols-4">
                        {visibleHighScoreRecords.map((record) => (
                            <div
                                key={record.id}
                                className="overflow-hidden rounded-[20px] border border-white/75 bg-white/62 shadow-sm"
                            >
                                <div className="relative aspect-[3/4] overflow-hidden bg-white/70">
                                    <CoverImage
                                        src={record.subjectCoverUrl}
                                        alt={record.subjectTitle}
                                        className="h-full w-full object-cover"
                                        fallback={
                                            <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-slate-100 to-sky-50 text-2xl font-black text-slate-400">
                                                {record.subjectTitle.slice(0, 1) || 'A'}
                                            </div>
                                        }
                                    />
                                    {record.scoreValue !== null ? (
                                        <span className="absolute right-2 top-2 rounded-full border border-white/80 bg-white/85 px-2.5 py-1 text-xs font-black text-rose-600 shadow-sm">
                                            {record.scoreValue}
                                        </span>
                                    ) : null}
                                </div>
                                <div className="p-3">
                                    <p className="line-clamp-2 text-xs font-black leading-4 text-slate-700">
                                        {record.subjectTitle}
                                    </p>
                                    <p className="mt-1 text-[11px] font-semibold text-slate-400">
                                        {record.recordYear} · {getReleaseQuarterLabel(record.recordQuarter)}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            ) : posterItems.length > 0 ? (
                <div className="rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-300">
                        poster picks
                    </p>
                    <h4 className="mt-2 text-base font-black text-slate-800">
                        报告提到的作品
                    </h4>
                    <div className="mt-4 flex gap-3 overflow-x-auto pb-1">
                        {posterItems.map((item, index) => (
                            <div
                                key={`${item.subjectId ?? item.title}-${index}`}
                                className="w-24 shrink-0"
                            >
                                <div className="h-36 overflow-hidden rounded-[18px] border border-white/80 bg-white/60 shadow-sm">
                                    <CoverImage
                                        src={item.coverUrl}
                                        alt={item.title}
                                        className="h-full w-full object-cover"
                                        fallback={
                                            <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-slate-100 to-sky-50 text-2xl font-black text-slate-400">
                                                {polishAnalysisText(item.title).slice(0, 1) || 'A'}
                                            </div>
                                        }
                                    />
                                </div>
                                <p className="mt-2 line-clamp-2 text-xs font-black leading-4 text-slate-700">
                                    {polishAnalysisText(item.title)}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            ) : null}
        </div>
    )
}

type SnapshotMetricProps = {
    label: string
    value: number
}

function SnapshotMetric({ label, value }: SnapshotMetricProps) {
    return (
        <div className="rounded-[22px] border border-white/75 bg-white/62 p-4 text-center shadow-sm">
            <p className="text-[11px] font-semibold text-slate-400">{label}</p>
            <p className="mt-1 text-2xl font-black text-slate-800">{value}</p>
        </div>
    )
}

export { InsightSnapshot, ReportReadingHighlights }
