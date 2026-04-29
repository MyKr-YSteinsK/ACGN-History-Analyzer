import { useEffect } from 'react'
import CoverImage from '../common/CoverImage'
import StatusBadge from '../common/StatusBadge'
import {
    categoryTheme,
    neutralPanelGradient,
    personalScoreTheme,
    platformScoreTheme,
    quarterTheme,
    yearChipClassName,
} from '../../theme/acgnTheme'
import type { SubjectCardView } from '../../types/subject'

type SubjectDetailDrawerProps = {
    subject: SubjectCardView | null
    open: boolean
    onClose: () => void
    onAddRecord: (subject: SubjectCardView) => void
    onAddToWant: (subject: SubjectCardView) => void
    onAddToPaused: (subject: SubjectCardView) => void
    onSetToNone: (subject: SubjectCardView) => void
    statusAction?: 'WANT' | 'PAUSED' | 'NONE' | null
    detailLoading?: boolean
    showActions?: boolean
    showStatusActions?: boolean
}

function buildSeasonPartText(subject: SubjectCardView) {
    const parts: string[] = []

    if (subject.seasonIndex) {
        parts.push(`第 ${subject.seasonIndex} 季`)
    }

    if (subject.partIndex) {
        parts.push(`Part ${subject.partIndex}`)
    }

    return parts.length > 0 ? parts.join(' · ') : '无季度 / Part 信息'
}

function SubjectDetailDrawer({
                                 subject,
                                 open,
                                 onClose,
                                 onAddRecord,
                                 onAddToWant,
                                 onAddToPaused,
                                 onSetToNone,
                                 statusAction = null,
                                 detailLoading = false,
                                 showActions = true,
                                 showStatusActions = true,
                             }: SubjectDetailDrawerProps) {
    useEffect(() => {
        if (!open) {
            return
        }

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose()
            }
        }

        window.addEventListener('keydown', handleKeyDown)

        return () => {
            window.removeEventListener('keydown', handleKeyDown)
        }
    }, [open, onClose])

    if (!open || !subject) {
        return null
    }

    const category = categoryTheme[subject.category]
    const quarter = subject.releaseQuarter ? quarterTheme[subject.releaseQuarter] : null
    const recordQuarter = subject.personalRecordQuarter
        ? quarterTheme[subject.personalRecordQuarter]
        : null
    const recordPanelGradient = recordQuarter?.panelGradient ?? neutralPanelGradient
    const wantDisabled =
        subject.libraryStatus === 'WANT' || statusAction !== null
    const pausedDisabled =
        subject.libraryStatus === 'PAUSED' || statusAction !== null
    const noneDisabled =
        subject.libraryStatus === 'NONE' || statusAction !== null

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6">
            <button
                type="button"
                aria-label="关闭作品详情"
                onClick={onClose}
                className="absolute inset-0 bg-slate-900/22 backdrop-blur-[3px]"
            />

            <section className="content-enter relative flex max-h-[calc(100vh-48px)] w-full max-w-[920px] overflow-hidden rounded-[34px] border border-white/75 bg-white/82 shadow-[0_30px_110px_rgba(70,80,110,0.28)] backdrop-blur-2xl">
                <div
                    className={[
                        'w-[280px] shrink-0 border-r border-white/65 bg-gradient-to-br p-5',
                        category.softGradient,
                    ].join(' ')}
                >
                    <div
                        className={[
                            'relative aspect-[3/4] overflow-hidden rounded-[28px] bg-gradient-to-br shadow-sm',
                            category.softGradient,
                        ].join(' ')}
                    >
                        <CoverImage
                            src={subject.coverUrl}
                            alt={subject.displayTitle}
                            className="h-full w-full object-cover"
                            fallback={
                            <div className="flex h-full w-full items-center justify-center">
                                <div className="text-center">
                  <span className={['text-8xl font-black', category.text].join(' ')}>
                    {category.letter}
                  </span>
                                    <p className="mt-2 text-xs font-semibold text-slate-400">
                                        No Cover
                                    </p>
                                </div>
                            </div>
                            }
                        />

                        <div className="absolute left-4 top-4">
                            <StatusBadge status={subject.libraryStatus} />
                        </div>

                        <div className="absolute bottom-4 right-4 flex items-center gap-1.5">
                            {subject.personalScore !== null && subject.personalScore !== undefined ? (
                                <div
                                    className={[
                                        'rounded-2xl border px-3 py-1.5 text-sm font-black',
                                        personalScoreTheme.className,
                                    ].join(' ')}
                                    title="个人评分"
                                >
                                    {subject.personalScore}
                                </div>
                            ) : null}

                            {subject.platformScore ? (
                                <div
                                    className={[
                                        'rounded-2xl border px-3 py-1.5 text-sm font-black',
                                        platformScoreTheme.className,
                                    ].join(' ')}
                                    title="平台评分"
                                >
                                    {subject.platformScore.toFixed(1)}
                                </div>
                            ) : null}
                        </div>
                    </div>

                    <div className="mt-4 flex flex-wrap gap-2">
            <span
                className={[
                    'rounded-full px-3 py-1 text-xs font-semibold',
                    category.bg,
                    category.text,
                ].join(' ')}
            >
              {category.cnName}
            </span>

                        {subject.releaseYear ? (
                            <span
                                className={[
                                    'rounded-full border px-3 py-1 text-xs font-semibold',
                                    yearChipClassName,
                                ].join(' ')}
                            >
                {subject.releaseYear}
              </span>
                        ) : null}

                        {quarter ? (
                            <span
                                className={[
                                    'rounded-full border px-3 py-1 text-xs font-semibold',
                                    quarter.bg,
                                    quarter.text,
                                    quarter.border,
                                ].join(' ')}
                        >
                {quarter.shortName}
              </span>
                        ) : null}

                        {subject.episodeCount ? (
                            <span className="rounded-full border border-indigo-100 bg-indigo-50/85 px-3 py-1 text-xs font-semibold text-indigo-600">
                {subject.episodeCount} 集
              </span>
                        ) : null}
                    </div>

                    {showActions ? (
                        <div className="mt-5 grid gap-2">
                            <button
                                type="button"
                                onClick={() => onAddRecord(subject)}
                                className="rounded-2xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:bg-slate-700"
                            >
                                {subject.libraryStatus === 'WATCHED' ? '编辑记录' : '添加已看记录'}
                            </button>

                            {showStatusActions ? (
                                <>
                                    <div className="grid grid-cols-2 gap-2">
                                        <button
                                            type="button"
                                            disabled={wantDisabled}
                                            onClick={() => onAddToWant(subject)}
                                            className={[
                                                'rounded-2xl border px-3 py-2.5 text-sm font-semibold transition',
                                                wantDisabled
                                                    ? 'cursor-not-allowed border-slate-100 bg-slate-100/80 text-slate-400'
                                                    : 'border-emerald-100 bg-emerald-50/85 text-emerald-700 hover:-translate-y-0.5 hover:bg-emerald-100',
                                            ].join(' ')}
                                        >
                                            {statusAction === 'WANT'
                                                ? '保存中'
                                                : subject.libraryStatus === 'WANT'
                                                  ? '已想看'
                                                  : '想看'}
                                        </button>

                                        <button
                                            type="button"
                                            disabled={pausedDisabled}
                                            onClick={() => onAddToPaused(subject)}
                                            className={[
                                                'rounded-2xl border px-3 py-2.5 text-sm font-semibold transition',
                                                pausedDisabled
                                                    ? 'cursor-not-allowed border-slate-100 bg-slate-100/80 text-slate-400'
                                                    : 'border-orange-100 bg-orange-50/85 text-orange-700 hover:-translate-y-0.5 hover:bg-orange-100',
                                            ].join(' ')}
                                        >
                                            {statusAction === 'PAUSED'
                                                ? '保存中'
                                                : subject.libraryStatus === 'PAUSED'
                                                  ? '已搁置'
                                                  : '搁置'}
                                        </button>
                                    </div>

                                    <button
                                        type="button"
                                        disabled={noneDisabled}
                                        onClick={() => onSetToNone(subject)}
                                        className={[
                                            'rounded-2xl border px-3 py-2.5 text-sm font-semibold transition',
                                            noneDisabled
                                                ? 'cursor-not-allowed border-slate-100 bg-slate-100/80 text-slate-400'
                                                : subject.libraryStatus === 'WATCHED'
                                                  ? 'border-rose-100 bg-rose-50/85 text-rose-700 hover:-translate-y-0.5 hover:bg-rose-100'
                                                  : 'border-slate-200 bg-white/75 text-slate-600 hover:-translate-y-0.5 hover:bg-white',
                                        ].join(' ')}
                                    >
                                        {statusAction === 'NONE'
                                            ? '处理中'
                                            : subject.libraryStatus === 'WATCHED'
                                              ? '删除观看记录'
                                              : subject.libraryStatus === 'NONE'
                                                ? '已未记录'
                                                : '设为未记录'}
                                    </button>
                                </>
                            ) : null}
                        </div>
                    ) : null}
                </div>

                <div className="min-w-0 flex-1 p-6">
                    <div className="flex items-start justify-between gap-5">
                        <div className="min-w-0">
                            <p className="text-xs font-semibold tracking-[0.28em] text-sky-500">
                                SUBJECT DETAIL
                            </p>

                            <h2 className="mt-2 line-clamp-2 text-2xl font-black leading-snug text-slate-800">
                                {subject.displayTitle}
                            </h2>

                            {subject.subtitle ? (
                                <p className="mt-1 line-clamp-1 text-sm text-slate-400">
                                    {subject.subtitle}
                                </p>
                            ) : null}
                        </div>

                        <button
                            type="button"
                            onClick={onClose}
                            className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-white/75 bg-white/75 text-xl font-semibold text-slate-500 shadow-sm transition hover:bg-white hover:text-slate-800"
                        >
                            ×
                        </button>
                    </div>

                    <section className="mt-5 rounded-[28px] border border-white/70 bg-white/55 p-5 shadow-sm">
                        <div className="grid gap-5 lg:grid-cols-[1.05fr_0.95fr]">
                            <div>
                                <h3 className="text-sm font-bold text-slate-700">作品信息</h3>

                                <div className="mt-4 grid gap-2.5 text-sm">
                                    <InfoRow label="主标题" value={subject.titleCn} />
                                    <InfoRow label="季度/Part" value={buildSeasonPartText(subject)} />
                                    <InfoRow label="系列" value={subject.franchiseName ?? '未关联系列'} />
                                    <InfoRow label="制作方" value={subject.studioName ?? '未知'} />
                                    <InfoRow label="来源平台" value={subject.sourcePlatform ?? '未设置'} />
                                    <InfoRow label="来源 ID" value={subject.sourceId ?? '未设置'} />
                                </div>
                            </div>

                            <div>
                                <h3 className="text-sm font-bold text-slate-700">标签</h3>

                                {subject.tags.length > 0 ? (
                                    <div className="mt-4 flex flex-wrap gap-2">
                                        {subject.tags.map((tag) => (
                                            <span
                                                key={tag}
                                                className="rounded-full bg-white/75 px-3 py-1 text-xs text-slate-500"
                                            >
                        #{tag}
                      </span>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="mt-3 text-sm text-slate-400">暂无标签</p>
                                )}
                            </div>
                        </div>
                    </section>

                    <section
                        className={[
                            'mt-4 rounded-[28px] border border-white/70 bg-gradient-to-br p-5 shadow-sm transition-all duration-300',
                            recordPanelGradient,
                        ].join(' ')}
                    >
                        <div className="flex flex-wrap items-center justify-between gap-3">
                            <h3 className="text-sm font-bold text-slate-700">我的评价 / 备注</h3>

                            {subject.personalScore !== null && subject.personalScore !== undefined ? (
                                <span
                                    className={[
                                        'rounded-2xl border px-3 py-1.5 text-sm font-black',
                                        personalScoreTheme.className,
                                    ].join(' ')}
                                >
                                    {subject.personalScore} / 50
                                </span>
                            ) : (
                                <span className="rounded-2xl border border-white/70 bg-white/55 px-3 py-1.5 text-xs font-semibold text-slate-400">
                                    暂无评分
                                </span>
                            )}
                        </div>

                        <p className="mt-3 min-h-[54px] whitespace-pre-wrap rounded-[22px] border border-white/70 bg-white/48 px-4 py-3 text-sm leading-7 text-slate-500">
                            {subject.personalRecordNote?.trim() || '暂无正式记录备注。'}
                        </p>
                    </section>

                    <section className="mt-4 rounded-[28px] border border-white/70 bg-white/55 p-5 shadow-sm">
                        <h3 className="text-sm font-bold text-slate-700">简介</h3>

                        <p className="mt-3 line-clamp-5 text-sm leading-7 text-slate-500">
                            {subject.summary ??
                                '暂无简介。后续接入作品详情接口后，这里会展示来自作品库或 Bangumi 的作品简介。'}
                        </p>
                    </section>

                    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 rounded-[28px] border border-white/70 bg-white/45 p-4">
                        <p className="text-xs leading-5 text-slate-400">
                            {detailLoading
                                ? '正在加载后端完整详情...'
                                : '详情、记录和收藏状态已接入后端接口。'}
                        </p>

                        {subject.platformLink ? (
                            <a
                                href={subject.platformLink}
                                target="_blank"
                                rel="noreferrer"
                                className="rounded-2xl border border-white/80 bg-white/75 px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white"
                            >
                                打开外部链接
                            </a>
                        ) : (
                            <button
                                type="button"
                                disabled
                                className="rounded-2xl border border-white/60 bg-white/40 px-4 py-2.5 text-sm font-semibold text-slate-300"
                            >
                                暂无外部链接
                            </button>
                        )}
                    </div>
                </div>
            </section>
        </div>
    )
}

type InfoRowProps = {
    label: string
    value: string
}

function InfoRow({ label, value }: InfoRowProps) {
    return (
        <div className="grid grid-cols-[76px_1fr] gap-3">
            <span className="text-slate-400">{label}</span>
            <span className="line-clamp-1 font-medium text-slate-600">{value}</span>
        </div>
    )
}

export default SubjectDetailDrawer
