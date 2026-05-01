import type { LlmRecommendationResponse } from '../../api/llmApi'
import { categoryTheme } from '../../theme/acgnTheme'
import { categoryOrder, type AcgnCategory } from '../../types/common'
import {
    getPriorityClassName,
    getPriorityLabel,
    getSourceLabel,
    polishAnalysisText,
} from '../../utils/llmReportFormat'
import CoverImage from '../common/CoverImage'

type RecommendationGridProps = {
    recommendations: LlmRecommendationResponse[]
}

function RecommendationGrid({ recommendations }: RecommendationGridProps) {
    return (
        <section className="mt-6 rounded-[30px] border border-white/75 bg-white/50 p-5 shadow-sm backdrop-blur">
            <div className="flex flex-wrap items-end justify-between gap-3">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        recommendation candidates
                    </p>
                    <h4 className="mt-2 text-lg font-black text-slate-800">
                        推荐候选
                    </h4>
                </div>
                <span className="rounded-full border border-white/75 bg-white/70 px-3 py-1.5 text-xs font-black text-slate-500">
                    {recommendations.length} 部
                </span>
            </div>

            <div className="mt-4 grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
                {recommendations.map((item, index) => (
                    <RecommendationCard
                        key={`${item.subjectId ?? item.title}-${index}`}
                        item={item}
                    />
                ))}
            </div>
        </section>
    )
}

type RecommendationCardProps = {
    item: LlmRecommendationResponse
}

function RecommendationCard({ item }: RecommendationCardProps) {
    const category = isAcgnCategory(item.category) ? item.category : 'ANIME'
    const theme = categoryTheme[category]

    return (
        <article
            className={[
                'overflow-hidden rounded-[24px] border bg-gradient-to-br shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md',
                theme.border,
                theme.softGradient,
            ].join(' ')}
        >
            <div className="flex gap-4 p-4">
                <div className="h-32 w-[88px] shrink-0 overflow-hidden rounded-[18px] border border-white/80 bg-white/55 shadow-sm">
                    <CoverImage
                        src={item.coverUrl}
                        alt={item.title}
                        className="h-full w-full object-cover"
                        fallback={
                            <div
                                className={[
                                    'flex h-full w-full items-center justify-center bg-gradient-to-br text-3xl font-black',
                                    theme.softGradient,
                                    theme.text,
                                ].join(' ')}
                            >
                                {theme.letter}
                            </div>
                        }
                    />
                </div>

                <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-2">
                        <div className="min-w-0">
                            <p className="line-clamp-2 text-base font-black leading-snug text-slate-800">
                                {polishAnalysisText(item.title)}
                            </p>
                            {item.franchiseName ? (
                                <p className="mt-1 truncate text-xs font-semibold text-slate-400">
                                    {polishAnalysisText(item.franchiseName)}
                                </p>
                            ) : null}
                        </div>

                        <span
                            className={[
                                'shrink-0 rounded-full border px-2.5 py-1 text-[11px] font-black',
                                getPriorityClassName(item.priority),
                            ].join(' ')}
                        >
                            {getPriorityLabel(item.priority)}
                        </span>
                    </div>

                    <div className="mt-3 flex flex-wrap items-center gap-2">
                        <span
                            className={[
                                'rounded-full border px-2.5 py-1 text-xs font-black',
                                theme.border,
                                theme.bg,
                                theme.text,
                            ].join(' ')}
                        >
                            {categoryTheme[category].cnName}
                        </span>
                        <span className="rounded-full border border-emerald-200 bg-emerald-50/85 px-2.5 py-1 text-xs font-black text-emerald-700">
                            {getSourceLabel(item.source)}
                        </span>
                        <span className="rounded-full border border-rose-200 bg-rose-50/85 px-2.5 py-1 text-xs font-black text-rose-700">
                            匹配 {item.matchScore ?? 0}
                        </span>
                        {item.platformScore !== null && item.platformScore !== undefined ? (
                            <span className="rounded-full border border-amber-200 bg-amber-50/85 px-2.5 py-1 text-xs font-black text-amber-700">
                                {Number(item.platformScore).toFixed(1)}
                            </span>
                        ) : null}
                    </div>
                </div>
            </div>

            <div className="border-t border-white/70 bg-white/48 px-4 py-3">
                <div className="flex flex-wrap gap-1.5">
                    {item.tags.slice(0, 4).map((tag) => (
                        <span
                            key={tag}
                            className="rounded-full bg-white/75 px-2.5 py-1 text-[11px] font-semibold text-slate-500"
                        >
                            #{polishAnalysisText(tag)}
                        </span>
                    ))}
                </div>
                <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-500">
                    {polishAnalysisText(item.reason)}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-slate-400">
                    {polishAnalysisText(item.evidence)}
                </p>
                <p className="mt-2 line-clamp-2 text-xs leading-5 text-amber-700/85">
                    {polishAnalysisText(item.risk)}
                </p>
            </div>
        </article>
    )
}

function isAcgnCategory(value: unknown): value is AcgnCategory {
    return typeof value === 'string' && categoryOrder.includes(value as AcgnCategory)
}

export default RecommendationGrid
