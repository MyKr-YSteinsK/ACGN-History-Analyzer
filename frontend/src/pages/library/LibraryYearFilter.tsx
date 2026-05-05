import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'

export type LibraryYearFilterValue = 'ALL' | 'BEFORE_2000' | number

type DecadeGroup = {
    decade: number
    years: number[]
}

const visibleStripCount = 4
const MENU_PANEL_MIN_WIDTH = 320

function buildDecadeGroups(years: number[]): DecadeGroup[] {
    const bucket = new Map<number, number[]>()
    for (const year of years) {
        const decade = Math.floor(year / 10) * 10
        if (!bucket.has(decade)) {
            bucket.set(decade, [])
        }
        bucket.get(decade)!.push(year)
    }
    return [...bucket.entries()]
        .sort((left, right) => right[0] - left[0])
        .map(([decade, list]) => ({
            decade,
            years: [...list].sort((a, b) => b - a),
        }))
}

type MenuRect = {
    top: number
    left: number
    width: number
}

type LibraryYearFilterProps = {
    years: number[]
    value: LibraryYearFilterValue
    onChange: (value: LibraryYearFilterValue) => void
    beforeBoundYear: number
    beforeLabel: string
}

function LibraryYearFilter({
    years,
    value,
    onChange,
    beforeBoundYear,
    beforeLabel,
}: LibraryYearFilterProps) {
    const menuButtonRef = useRef<HTMLButtonElement>(null)
    const panelRef = useRef<HTMLDivElement>(null)
    const [startIndex, setStartIndex] = useState(0)
    const [menuOpen, setMenuOpen] = useState(false)
    const [menuRect, setMenuRect] = useState<MenuRect | null>(null)

    const decadeGroups = useMemo(() => buildDecadeGroups(years), [years])

    const computeMenuRect = useCallback((): MenuRect | null => {
        const btn = menuButtonRef.current
        if (!btn) {
            return null
        }
        const r = btn.getBoundingClientRect()
        const width = Math.min(
            window.innerWidth - 24,
            Math.max(MENU_PANEL_MIN_WIDTH, Math.ceil(r.width)),
        )
        const maxLeft = window.innerWidth - width - 12
        const left = Math.min(maxLeft, Math.max(12, r.left))
        const maxPanelH = Math.min(window.innerHeight * 0.72, 540)
        let top = r.bottom - 1
        if (top + maxPanelH > window.innerHeight - 10) {
            top = Math.max(12, window.innerHeight - maxPanelH - 10)
        }
        return { top, left, width }
    }, [])

    useEffect(() => {
        if (!menuOpen) {
            return undefined
        }

        const scrollY = window.scrollY
        const prevBody = {
            position: document.body.style.position,
            top: document.body.style.top,
            left: document.body.style.left,
            right: document.body.style.right,
            width: document.body.style.width,
            overflow: document.body.style.overflow,
        }
        document.body.style.position = 'fixed'
        document.body.style.top = `-${scrollY}px`
        document.body.style.left = '0'
        document.body.style.right = '0'
        document.body.style.width = '100%'
        document.body.style.overflow = 'hidden'

        function onKey(event: KeyboardEvent) {
            if (event.key === 'Escape') {
                setMenuOpen(false)
            }
        }

        function onResize() {
            setMenuRect(computeMenuRect())
        }

        function onPointerDownCapture(event: PointerEvent) {
            const target = event.target as Node | null
            if (!target) {
                return
            }
            if (menuButtonRef.current?.contains(target)) {
                return
            }
            if (panelRef.current?.contains(target)) {
                return
            }
            setMenuOpen(false)
        }

        function onWheelCapture(event: WheelEvent) {
            const target = event.target as Node | null
            if (panelRef.current?.contains(target)) {
                return
            }
            event.preventDefault()
        }

        function onTouchMoveCapture(event: TouchEvent) {
            const target = event.target as Node | null
            if (panelRef.current?.contains(target)) {
                return
            }
            event.preventDefault()
        }

        const wheelOpts: AddEventListenerOptions = { capture: true, passive: false }
        const touchOpts: AddEventListenerOptions = { capture: true, passive: false }

        window.addEventListener('keydown', onKey)
        window.addEventListener('resize', onResize)
        document.addEventListener('pointerdown', onPointerDownCapture, true)
        document.addEventListener('wheel', onWheelCapture, wheelOpts)
        document.addEventListener('touchmove', onTouchMoveCapture, touchOpts)

        return () => {
            window.removeEventListener('keydown', onKey)
            window.removeEventListener('resize', onResize)
            document.removeEventListener('pointerdown', onPointerDownCapture, true)
            document.removeEventListener('wheel', onWheelCapture, wheelOpts)
            document.removeEventListener('touchmove', onTouchMoveCapture, touchOpts)

            document.body.style.position = prevBody.position
            document.body.style.top = prevBody.top
            document.body.style.left = prevBody.left
            document.body.style.right = prevBody.right
            document.body.style.width = prevBody.width
            document.body.style.overflow = prevBody.overflow
            window.scrollTo(0, scrollY)
        }
    }, [computeMenuRect, menuOpen])

    const syncStripToYear = useCallback(
        (year: number) => {
            const idx = years.indexOf(year)
            if (idx < 0) {
                return
            }
            const maxStart = Math.max(0, years.length - visibleStripCount)
            const next = Math.min(
                maxStart,
                Math.max(0, idx - Math.floor(visibleStripCount / 2)),
            )
            setStartIndex(next)
        },
        [years],
    )

    useEffect(() => {
        if (value === 'ALL' || value === 'BEFORE_2000') {
            return
        }
        syncStripToYear(value)
    }, [syncStripToYear, value])

    const visibleYears = useMemo(
        () => years.slice(startIndex, startIndex + visibleStripCount),
        [startIndex, years],
    )

    const canMoveLeft = startIndex > 0
    const canMoveRight = startIndex + visibleStripCount < years.length

    function handlePickYear(year: number) {
        onChange(year)
        syncStripToYear(year)
        setMenuOpen(false)
    }

    function handlePickBefore2000() {
        onChange('BEFORE_2000')
        setMenuOpen(false)
    }

    function toggleMenu() {
        if (menuOpen) {
            setMenuOpen(false)
            return
        }
        const next = computeMenuRect()
        if (next) {
            setMenuRect(next)
        }
        setMenuOpen(true)
    }

    const activeChip =
        'border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm ring-1 ring-amber-200/60'
    const idleChip =
        'border-white/75 bg-white/50 text-slate-400 hover:-translate-y-0.5 hover:bg-white/80 hover:text-slate-600'

    const menuHighlight =
        'border-violet-200 bg-gradient-to-br from-violet-50/95 via-white/90 to-sky-50/80 text-violet-900 shadow-[0_12px_36px_rgba(100,90,160,0.12)]'
    const menuIdle =
        'border-white/75 bg-white/70 text-slate-600 hover:border-violet-100 hover:bg-white'

    const triggerOpenClasses =
        'relative z-[125] rounded-t-[14px] rounded-b-none border-b-0 border-violet-200 bg-violet-50/95 text-violet-900 shadow-[0_1px_0_0_rgba(255,255,255,0.65)_inset] ring-1 ring-violet-200/80'

    const floatingMenu =
        menuOpen && menuRect
            ? createPortal(
                  <div
                      ref={panelRef}
                      className={[
                          'fixed z-[110] max-h-[min(72vh,540px)] overflow-y-auto rounded-b-[26px] rounded-t-none px-3 py-3',
                          'border border-t-0 border-violet-200 bg-gradient-to-b from-violet-50/95 via-white/92 to-slate-50/92',
                          'shadow-[0_20px_50px_rgba(55,70,100,0.22)]',
                      ].join(' ')}
                      style={{
                          top: menuRect.top,
                          left: menuRect.left,
                          width: menuRect.width,
                      }}
                      role="dialog"
                      aria-modal="true"
                      aria-label="选年菜单"
                  >
                      <div className="space-y-4 pr-1">
                          {decadeGroups.map((group) => (
                              <div key={group.decade}>
                                  <p className="mb-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
                                      {group.decade} 年代
                                  </p>
                                  <div className="flex flex-wrap gap-2">
                                      {group.years.map((year) => (
                                          <button
                                              key={year}
                                              type="button"
                                              onClick={() => handlePickYear(year)}
                                              className={[
                                                  'rounded-2xl border px-3 py-2 text-xs font-bold transition-all duration-200',
                                                  value === year
                                                      ? 'border-amber-200 bg-amber-100/90 text-amber-900 shadow-sm'
                                                      : 'border-white/70 bg-white/60 text-slate-500 hover:-translate-y-0.5 hover:border-amber-100 hover:bg-white',
                                              ].join(' ')}
                                          >
                                              {year}
                                          </button>
                                      ))}
                                  </div>
                              </div>
                          ))}

                          <div className="border-t border-white/70 pt-4">
                              <button
                                  type="button"
                                  onClick={handlePickBefore2000}
                                  className={[
                                      'w-full rounded-[22px] border px-4 py-3 text-left transition-all duration-200',
                                      value === 'BEFORE_2000' ? menuHighlight : menuIdle,
                                  ].join(' ')}
                              >
                                  <span className="block text-sm font-black">{beforeLabel}</span>
                                  <span className="mt-1 block text-[11px] font-medium text-slate-500">
                                      发行年份早于 {beforeBoundYear} 的作品（老番 / 导入旧数据）
                                  </span>
                              </button>
                          </div>
                      </div>
                  </div>,
                  document.body,
              )
            : null

    return (
        <div className="min-w-0">
            <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                <p className="text-xs font-semibold text-slate-400">年份</p>
                <button
                    ref={menuButtonRef}
                    type="button"
                    onClick={toggleMenu}
                    className={[
                        'inline-flex items-center gap-1.5 border px-3 py-1.5 text-[11px] font-black tracking-wide transition-all duration-200',
                        menuOpen
                            ? triggerOpenClasses
                            : value === 'BEFORE_2000'
                              ? 'relative z-[1] rounded-full border-violet-200/80 bg-violet-50/75 text-violet-800'
                              : 'relative z-[1] rounded-full border-white/80 bg-white/65 text-slate-500 hover:border-violet-100 hover:bg-white hover:text-violet-900',
                    ].join(' ')}
                    aria-expanded={menuOpen}
                    aria-haspopup="dialog"
                >
                    {value === 'BEFORE_2000' ? '已选 · ' : ''}
                    选年菜单
                    <span
                        className={[
                            'inline-block text-[10px] transition-transform duration-200',
                            menuOpen ? 'rotate-180' : '',
                        ].join(' ')}
                        aria-hidden
                    >
                        ▾
                    </span>
                </button>
            </div>

            <div className="flex flex-wrap items-center gap-2">
                <button
                    type="button"
                    onClick={() => onChange('ALL')}
                    className={[
                        'h-10 rounded-full border px-4 text-sm font-semibold transition-all duration-200',
                        value === 'ALL' ? activeChip : idleChip,
                    ].join(' ')}
                >
                    全部
                </button>

                <button
                    type="button"
                    disabled={!canMoveLeft}
                    onClick={() => setStartIndex((current) => Math.max(0, current - 1))}
                    className={[
                        'h-11 w-11 shrink-0 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        canMoveLeft
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:-translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="向前查看更多年份"
                >
                    &lt;
                </button>

                <div className="flex flex-wrap gap-2">
                    {visibleYears.map((year) => (
                        <button
                            key={year}
                            type="button"
                            onClick={() => handlePickYear(year)}
                            className={[
                                'h-10 min-w-[3.25rem] rounded-full border px-4 text-sm font-semibold transition-all duration-200',
                                value === year ? activeChip : idleChip,
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
                            Math.min(years.length - visibleStripCount, current + 1),
                        )
                    }
                    className={[
                        'h-11 w-11 shrink-0 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        canMoveRight
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="向后查看更多年份"
                >
                    &gt;
                </button>
            </div>

            {floatingMenu}
        </div>
    )
}

export default LibraryYearFilter
