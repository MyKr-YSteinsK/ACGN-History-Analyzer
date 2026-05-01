import StatusBadge from '../common/StatusBadge'
import CoverImage from '../common/CoverImage'
import {
    categoryTheme,
    personalScoreTheme,
    platformScoreTheme,
    quarterTheme,
} from '../../theme/acgnTheme'
import type { SubjectCardView } from '../../types/subject'

type SubjectCardProps = {
    subject: SubjectCardView
    onOpenDetail?: (subject: SubjectCardView) => void
}

function buildSubjectMetaTitle(subject: SubjectCardView) {
    const parts: string[] = []

    if (subject.seasonIndex) {
        parts.push(`S${subject.seasonIndex}`)
    }

    if (subject.partIndex) {
        parts.push(`Part ${subject.partIndex}`)
    }

    if (parts.length > 0) {
        return parts.join(' · ')
    }

    return subject.subtitle ?? ''
}

function SubjectCard({ subject, onOpenDetail }: SubjectCardProps) {
    const category = categoryTheme[subject.category]
    const quarter = subject.releaseQuarter ? quarterTheme[subject.releaseQuarter] : null
    const metaTitle = buildSubjectMetaTitle(subject)
    const releaseText = subject.releaseYear
        ? `${subject.releaseYear}${quarter?.monthLabel ?? ''}`
        : null

    return (
        <article
            className={[
                'group overflow-hidden rounded-[24px] border border-white/70 bg-gradient-to-br shadow-sm backdrop-blur transition-all duration-200',
                'hover:-translate-y-1 hover:shadow-[0_18px_48px_rgba(120,135,160,0.16)]',
                category.softGradient,
            ].join(' ')}
        >
            <div className="relative aspect-[3/4] overflow-hidden bg-slate-100">
                <CoverImage
                    src={subject.coverUrl}
                    alt={subject.displayTitle}
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.04]"
                    fallback={
                    <div
                        className={[
                            'flex h-full w-full items-center justify-center bg-gradient-to-br',
                            category.softGradient,
                        ].join(' ')}
                    >
                        <div className="text-center">
              <span className={['text-6xl font-black', category.text].join(' ')}>
                {category.letter}
              </span>
                            <p className="mt-1.5 text-xs font-semibold text-slate-400">
                                暂无封面
                            </p>
                        </div>
                    </div>
                    }
                />

                <div className="absolute left-3 top-3">
                    <StatusBadge status={subject.libraryStatus} />
                </div>

                <div className="absolute right-3 top-3 flex items-center gap-1.5">
                    {subject.personalScore !== null && subject.personalScore !== undefined ? (
                        <div
                            className={[
                                'rounded-2xl border px-2.5 py-1 text-[11px] font-black',
                                personalScoreTheme.className,
                            ].join(' ')}
                            title="个人评分"
                        >
                            {subject.personalScore}
                        </div>
                    ) : null}

                    {subject.platformScore !== null && subject.platformScore !== undefined ? (
                        <div
                            className={[
                                'rounded-2xl border px-2.5 py-1 text-[11px] font-black',
                                platformScoreTheme.className,
                            ].join(' ')}
                            title="平台评分"
                        >
                            {subject.platformScore.toFixed(1)}
                        </div>
                    ) : null}
                </div>
            </div>

            <div className="m-2.5 rounded-[20px] border border-white/65 bg-white/58 p-3.5 shadow-sm backdrop-blur">
                <div className="min-w-0">
                    <h3 className="line-clamp-1 text-[14px] font-bold text-slate-800">
                        {subject.displayTitle}
                    </h3>

                    {metaTitle ? (
                        <p className="mt-0.5 line-clamp-1 text-[11px] font-medium text-slate-400">
                            {metaTitle}
                        </p>
                    ) : (
                        <p className="mt-0.5 text-[11px] text-slate-300">暂无副标题</p>
                    )}
                </div>

                <div className="mt-3 flex flex-wrap gap-1.5">
          <span
              className={[
                  'rounded-full px-2.5 py-0.5 text-[11px] font-semibold',
                  category.bg,
                  category.text,
              ].join(' ')}
          >
            {category.cnName}
          </span>

                    {releaseText ? (
                        <span
                            className={[
                                'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold',
                                quarter
                                    ? `${quarter.bg} ${quarter.text} ${quarter.border}`
                                    : 'border-amber-200/75 bg-amber-50/90 text-amber-700',
                            ].join(' ')}
                        >
              {releaseText}
            </span>
                    ) : null}

                    {subject.episodeCount ? (
                        <span className="rounded-full border border-indigo-100 bg-indigo-50/85 px-2.5 py-0.5 text-[11px] font-semibold text-indigo-600">
              {subject.episodeCount} 集
            </span>
                    ) : null}
                </div>

                <div className="mt-3 flex min-h-[24px] flex-wrap gap-1.5">
                    {subject.tags.slice(0, 3).map((tag) => (
                        <span
                            key={tag}
                            className="rounded-full bg-white/75 px-2 py-0.5 text-[11px] text-slate-400"
                        >
              #{tag}
            </span>
                    ))}
                </div>

                <div className="mt-3 flex min-w-0 items-center justify-between gap-3 border-t border-slate-100 pt-3">
                    <p className="min-w-0 flex-1 truncate text-[11px] text-slate-400">
                        {subject.studioName ?? '制作方未知'}
                    </p>

                    <button
                        type="button"
                        onClick={() => onOpenDetail?.(subject)}
                        className="shrink-0 whitespace-nowrap rounded-full border border-slate-200/70 bg-white/75 px-3 py-1.5 text-[11px] font-semibold text-slate-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-slate-900 hover:text-white"
                    >
                        详情
                    </button>
                </div>
            </div>
        </article>
    )
}

export default SubjectCard
