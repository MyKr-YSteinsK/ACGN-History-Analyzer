import { useEffect, useMemo, useState } from 'react'

type YearScrollerProps = {
    years: number[]
    value: number | 'ALL'
    onChange: (value: number | 'ALL') => void
}

const visibleCount = 4

function YearScroller({ years, value, onChange }: YearScrollerProps) {
    const [startIndex, setStartIndex] = useState(0)

    useEffect(() => {
        if (value === 'ALL') {
            return
        }
        const idx = years.indexOf(value)
        if (idx < 0) {
            return
        }
        const maxStart = Math.max(0, years.length - visibleCount)
        const next = Math.min(
            maxStart,
            Math.max(0, idx - Math.floor(visibleCount / 2)),
        )
        setStartIndex(next)
    }, [value, years])

    const visibleYears = useMemo(() => {
        return years.slice(startIndex, startIndex + visibleCount)
    }, [startIndex, years])

    const canMoveLeft = startIndex > 0
    const canMoveRight = startIndex + visibleCount < years.length

    return (
        <div>
            <p className="mb-2 text-xs font-semibold text-slate-400">年份</p>

            <div className="flex items-center gap-2">
                <button
                    type="button"
                    onClick={() => onChange('ALL')}
                    className={[
                        'h-10 rounded-full border px-5 text-sm font-semibold transition-all duration-200',
                        value === 'ALL'
                            ? 'border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                            : 'border-white/75 bg-white/50 text-slate-400 hover:bg-white/80 hover:text-slate-600',
                    ].join(' ')}
                >
                    全部
                </button>

                <button
                    type="button"
                    disabled={!canMoveLeft}
                    onClick={() => setStartIndex((current) => Math.max(0, current - 1))}
                    className={[
                        'h-11 w-11 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        canMoveLeft
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:-translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="向前查看更多年份"
                >
                    &lt;
                </button>

                <div className="flex gap-2 content-enter">
                    {visibleYears.map((year) => (
                        <button
                            key={year}
                            type="button"
                            onClick={() => onChange(year)}
                            className={[
                                'h-10 rounded-full border px-5 text-sm font-semibold transition-all duration-200',
                                value === year
                                    ? 'border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                    : 'border-white/75 bg-white/50 text-slate-400 hover:-translate-y-0.5 hover:bg-white/80 hover:text-slate-600',
                            ].join(' ')}
                        >
                            {year}
                        </button>
                    ))}
                </div>

                <button
                    type="button"
                    disabled={!canMoveRight}
                    onClick={() =>
                        setStartIndex((current) =>
                            Math.min(years.length - visibleCount, current + 1),
                        )
                    }
                    className={[
                        'h-11 w-11 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        canMoveRight
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="向后查看更多年份"
                >
                    &gt;
                </button>
            </div>
        </div>
    )
}

export default YearScroller
