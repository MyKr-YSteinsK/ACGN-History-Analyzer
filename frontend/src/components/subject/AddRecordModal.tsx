import { useMemo, useState } from 'react'
import StatusBadge from '../common/StatusBadge'
import {
    categoryTheme,
    quarterTheme,
    yearChipClassName,
} from '../../theme/acgnTheme'
import { releaseQuarterOptions, type ReleaseQuarter } from '../../types/common'
import { useEscapeToClose } from '../../hooks/useEscapeToClose'
import { buildSeasonPartDisplay } from '../../utils/subjectDisplay'
import type { SubjectCardView } from '../../types/subject'

export type AddRecordPayload = {
    recordYear: number
    recordQuarter: ReleaseQuarter
    scoreValue: number
    note: string
}

type AddRecordModalProps = {
    subject: SubjectCardView | null
    open: boolean
    onClose: () => void
    onSubmit: (subject: SubjectCardView, payload: AddRecordPayload) => void | Promise<void>
    submitting?: boolean
}

const visibleYearCount = 3

function AddRecordModal({
                            subject,
                            open,
                            onClose,
                            onSubmit,
                            submitting = false,
                        }: AddRecordModalProps) {
    if (!open || !subject) {
        return null
    }

    return (
        <AddRecordModalContent
            key={`${subject.id}-${subject.recordId ?? 'new'}`}
            subject={subject}
            onClose={onClose}
            onSubmit={onSubmit}
            submitting={submitting}
        />
    )
}

type AddRecordModalContentProps = {
    subject: SubjectCardView
    onClose: () => void
    onSubmit: (subject: SubjectCardView, payload: AddRecordPayload) => void | Promise<void>
    submitting: boolean
}

function AddRecordModalContent({
                                   subject,
                                   onClose,
                                   onSubmit,
                                   submitting,
                               }: AddRecordModalContentProps) {
    const currentYear = new Date().getFullYear()
    const isEditMode = subject?.libraryStatus === 'WATCHED'

    const years = useMemo(() => {
        const start = 2000
        const end = currentYear + 1
        return Array.from({ length: end - start + 1 }, (_, index) => start + index)
    }, [currentYear])

    const defaultYear = subject?.personalRecordYear ?? currentYear
    const defaultYearIndex = Math.max(0, years.indexOf(defaultYear))
    const defaultStartIndex = Math.min(
        Math.max(0, defaultYearIndex - 1),
        Math.max(0, years.length - visibleYearCount),
    )

    const [yearStartIndex, setYearStartIndex] = useState(defaultStartIndex)
    const [recordYear, setRecordYear] = useState(defaultYear)
    const [recordQuarter, setRecordQuarter] = useState<ReleaseQuarter>(
        subject?.personalRecordQuarter ?? 1,
    )
    const [scoreValue, setScoreValue] = useState(subject?.personalScore ?? 40)
    const [note, setNote] = useState(subject?.personalRecordNote ?? '')

    useEscapeToClose(true, onClose)

    const category = categoryTheme[subject.category]
    const quarter = subject.releaseQuarter ? quarterTheme[subject.releaseQuarter] : null

    const visibleYears = years.slice(yearStartIndex, yearStartIndex + visibleYearCount)
    const canMoveYearLeft = yearStartIndex > 0
    const canMoveYearRight = yearStartIndex + visibleYearCount < years.length

    const safeScoreValue = Math.max(0, Math.min(50, scoreValue))

    function handleSubmit() {
        if (!subject) {
            return
        }

        onSubmit(subject, {
            recordYear,
            recordQuarter,
            scoreValue: safeScoreValue,
            note: note.trim(),
        })
    }

    return (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-6">
            <button
                type="button"
                aria-label="关闭新增记录弹窗"
                onClick={onClose}
                className="absolute inset-0 bg-slate-900/24 backdrop-blur-[3px]"
            />

            <section className="content-enter relative grid max-h-[calc(100vh-48px)] w-full max-w-[900px] overflow-hidden rounded-[34px] border border-white/75 bg-white/84 shadow-[0_30px_110px_rgba(70,80,110,0.30)] backdrop-blur-2xl lg:grid-cols-[330px_1fr]">
                <aside className="flex min-h-0 flex-col border-r border-white/70 bg-white/42 p-5">
                    <p className="text-xs font-semibold tracking-[0.28em] text-sky-500">
                        {isEditMode ? 'EDIT RECORD' : 'ADD RECORD'}
                    </p>

                    <h2 className="mt-2 text-xl font-black text-slate-800">
                        {isEditMode ? '编辑观看记录' : '添加已看记录'}
                    </h2>

                    <p className="mt-2 text-sm leading-6 text-slate-400">
                        作品信息来自作品库，只读展示。这里只记录你的观看年份、月份档期、评分和备注。
                    </p>

                    <div
                        className={[
                            'mt-5 flex aspect-[3/4] items-center justify-center overflow-hidden rounded-[28px] bg-gradient-to-br shadow-sm',
                            category.softGradient,
                        ].join(' ')}
                    >
                        {subject.coverUrl ? (
                            <img
                                src={subject.coverUrl}
                                alt={subject.displayTitle}
                                className="h-full w-full object-cover"
                            />
                        ) : (
                            <div className="text-center">
                <span className={['text-7xl font-black', category.text].join(' ')}>
                  {category.letter}
                </span>
                                <p className="mt-2 text-xs font-semibold text-slate-400">
                                    暂无封面
                                </p>
                            </div>
                        )}
                    </div>

                    <div className="mt-4 flex flex-wrap gap-2">
                        <StatusBadge status={subject.libraryStatus} />

                        <span
                            className={[
                                'rounded-full px-3 py-1 text-xs font-semibold',
                                category.bg,
                                category.text,
                            ].join(' ')}
                        >
              {category.cnName}
            </span>
                    </div>

                    <div className="mt-4 min-h-0 flex-1 overflow-hidden rounded-[24px] border border-white/70 bg-white/48 p-4">
                        <p className="text-xs font-bold text-slate-600">作品简介</p>
                        <p className="mt-2 line-clamp-7 text-xs leading-6 text-slate-500">
                            {subject.summary ??
                                '暂无简介。作品库可补充简介，用于详情展示、导入校验和后续偏好分析。'}
                        </p>
                    </div>
                </aside>

                <main className="min-w-0 p-6">
                    <div className="flex items-start justify-between gap-5">
                        <div className="min-w-0">
                            <h3 className="line-clamp-2 text-2xl font-black leading-snug text-slate-800">
                                {subject.displayTitle}
                            </h3>

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
                        <h4 className="text-sm font-bold text-slate-700">只读作品信息</h4>

                        <div className="mt-4 grid gap-3 text-sm md:grid-cols-2">
                            <ReadonlyField label="主标题" value={subject.titleCn} />
                            <ReadonlyField label="制作方" value={subject.studioName ?? '未知'} />
                            <ReadonlyField
                                label="发行年份"
                                value={subject.releaseYear ? String(subject.releaseYear) : '未知'}
                                valueClassName={yearChipClassName}
                            />
                            <ReadonlyField
                                label="发行月份"
                                value={quarter ? quarter.displayName : '未知'}
                                valueClassName={quarter ? `${quarter.bg} ${quarter.text} ${quarter.border}` : ''}
                            />
                            <ReadonlyField
                                label="季 / Part"
                                value={buildSeasonPartDisplay(subject)}
                            />
                            <ReadonlyField
                                label="总集数"
                                value={subject.episodeCount ? `${subject.episodeCount} 集` : '未知'}
                            />
                        </div>
                    </section>

                    <section className="mt-4 rounded-[28px] border border-white/70 bg-white/55 p-5 shadow-sm">
                        <h4 className="text-sm font-bold text-slate-700">我的记录</h4>

                        <div className="mt-4 grid gap-4 md:grid-cols-2">
                            <div>
                <span className="mb-2 block text-xs font-semibold text-slate-400">
                  观看年份
                </span>

                                <div className="flex items-center gap-2">
                                    <button
                                        type="button"
                                        disabled={!canMoveYearLeft}
                                        onClick={() => setYearStartIndex((current) => Math.max(0, current - 1))}
                                        className={[
                                            'h-11 w-11 rounded-full border text-sm font-bold transition-all duration-200',
                                            canMoveYearLeft
                                                ? 'border-white/80 bg-white/60 text-slate-500 hover:-translate-x-0.5 hover:scale-105 hover:bg-white'
                                                : 'cursor-not-allowed border-white/40 bg-white/30 text-slate-300',
                                        ].join(' ')}
                                    >
                                        ‹
                                    </button>

                                    <div className="flex gap-2 content-enter">
                                        {visibleYears.map((year) => (
                                            <button
                                                key={year}
                                                type="button"
                                                onClick={() => setRecordYear(year)}
                                                className={[
                                                    'h-11 rounded-2xl border px-3 text-sm font-semibold transition-all duration-200',
                                                    recordYear === year
                                                        ? 'scale-[1.03] border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                                        : 'border-white/75 bg-white/60 text-slate-400 hover:-translate-y-0.5 hover:scale-105 hover:bg-white/85 hover:text-slate-600',
                                                ].join(' ')}
                                            >
                                                {year}
                                            </button>
                                        ))}
                                    </div>

                                    <button
                                        type="button"
                                        disabled={!canMoveYearRight}
                                        onClick={() =>
                                            setYearStartIndex((current) =>
                                                Math.min(years.length - visibleYearCount, current + 1),
                                            )
                                        }
                                        className={[
                                            'h-11 w-11 rounded-full border text-sm font-bold transition-all duration-200',
                                            canMoveYearRight
                                                ? 'border-white/80 bg-white/60 text-slate-500 hover:translate-x-0.5 hover:scale-105 hover:bg-white'
                                                : 'cursor-not-allowed border-white/40 bg-white/30 text-slate-300',
                                        ].join(' ')}
                                    >
                                        ›
                                    </button>
                                </div>
                            </div>

                            <div>
                <span className="mb-2 block text-xs font-semibold text-slate-400">
                  记录月份
                </span>
                                <div className="flex gap-2">
                                    {releaseQuarterOptions.map((option) => {
                                        const selected = recordQuarter === option.value
                                        const theme = quarterTheme[option.value]

                                        return (
                                            <button
                                                key={option.value}
                                                type="button"
                                                onClick={() => setRecordQuarter(option.value)}
                                                className={[
                                                    'h-11 flex-1 rounded-2xl border text-sm font-semibold transition-all duration-200',
                                                    selected
                                                        ? `${theme.border} ${theme.bg} ${theme.text} scale-[1.03] shadow-sm`
                                                        : 'border-white/80 bg-white/60 text-slate-400 hover:-translate-y-0.5 hover:scale-105 hover:bg-white',
                                                ].join(' ')}
                                            >
                                                <span className="block leading-tight">{option.label}</span>
                                            </button>
                                        )
                                    })}
                                </div>
                            </div>
                        </div>

                        <div className="mt-4">
                            <div className="mb-2 flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-400">
                  个人评分
                </span>
                                <span className="rounded-full bg-rose-100/85 px-3 py-1 text-sm font-black text-rose-700">
                  {safeScoreValue} / 50
                </span>
                            </div>

                            <input
                                type="range"
                                min={0}
                                max={50}
                                value={safeScoreValue}
                                onChange={(event) => setScoreValue(Number(event.target.value))}
                                className="w-full accent-rose-400"
                            />

                            <div className="mt-2 flex justify-between text-xs text-slate-300">
                                <span>0</span>
                                <span>25</span>
                                <span>50</span>
                            </div>
                        </div>

                        <label className="mt-4 block">
              <span className="mb-2 block text-xs font-semibold text-slate-400">
                评论 / 备注
              </span>
                            <textarea
                                value={note}
                                onChange={(event) => setNote(event.target.value)}
                                rows={3}
                                placeholder="写一点当时的感受、补番背景或个人评价"
                                className="w-full resize-none rounded-2xl border border-white/80 bg-white/70 px-4 py-3 text-sm leading-6 text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white"
                            />
                        </label>
                    </section>

                    <div className="mt-4 flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={submitting}
                            className="rounded-2xl border border-white/80 bg-white/70 px-5 py-3 text-sm font-semibold text-slate-500 transition hover:bg-white"
                        >
                            取消
                        </button>

                        <button
                            type="button"
                            onClick={handleSubmit}
                            disabled={submitting}
                            className={[
                                'rounded-2xl px-6 py-3 text-sm font-semibold text-white transition',
                                submitting
                                    ? 'cursor-not-allowed bg-slate-400'
                                    : 'bg-slate-900 hover:-translate-y-0.5 hover:bg-slate-700',
                            ].join(' ')}
                        >
                            {submitting ? '保存中' : isEditMode ? '保存修改' : '保存记录'}
                        </button>
                    </div>
                </main>
            </section>
        </div>
    )
}

type ReadonlyFieldProps = {
    label: string
    value: string
    valueClassName?: string
}

function ReadonlyField({ label, value, valueClassName = '' }: ReadonlyFieldProps) {
    return (
        <div>
            <p className="text-xs font-semibold text-slate-400">{label}</p>
            <p
                className={[
                    'mt-1 inline-flex rounded-full border border-white/70 bg-white/60 px-3 py-1 text-xs font-semibold text-slate-600',
                    valueClassName,
                ].join(' ')}
            >
                {value}
            </p>
        </div>
    )
}

export default AddRecordModal
