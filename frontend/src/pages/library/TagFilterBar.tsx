import type { TagResponse } from '../../api/tagApi'
import { tagChipClassNames } from './libraryConstants'

type TagFilterBarProps = {
    tags: TagResponse[]
    value: string | 'ALL'
    onChange: (tagName: string | 'ALL') => void
}

export default function TagFilterBar({ tags, value, onChange }: TagFilterBarProps) {
    const visibleTags = tags.slice(0, 48)

    return (
        <div>
            <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
                <div>
                    <p className="text-xs font-semibold text-slate-400">作品标签</p>
                    <p className="mt-1 text-xs text-slate-300">
                        用 #奇幻 #战斗 这类标签做细分类，也会作为后续 AI 兴趣分析的数据基础。
                    </p>
                </div>

                {value !== 'ALL' ? (
                    <button
                        type="button"
                        onClick={() => onChange('ALL')}
                        className="rounded-full border border-white/80 bg-white/65 px-3 py-1.5 text-xs font-semibold text-slate-500 transition hover:bg-white"
                    >
                        清除标签
                    </button>
                ) : null}
            </div>

            <div className="flex max-h-[116px] flex-wrap gap-2 overflow-y-auto pr-1">
                <button
                    type="button"
                    onClick={() => onChange('ALL')}
                    className={[
                        'rounded-full border px-3.5 py-2 text-xs font-semibold transition-all duration-200',
                        value === 'ALL'
                            ? 'border-slate-200 bg-white/90 text-slate-700 shadow-sm'
                            : 'border-white/75 bg-white/50 text-slate-400 hover:-translate-y-0.5 hover:bg-white/80 hover:text-slate-600',
                    ].join(' ')}
                >
                    全部标签
                </button>

                {visibleTags.map((tag) => {
                    const selected = value === tag.name
                    const colorClassName =
                        tagChipClassNames[tag.colorKey] ?? tagChipClassNames.slate

                    return (
                        <button
                            key={tag.id}
                            type="button"
                            onClick={() => onChange(tag.name)}
                            className={[
                                'rounded-full border px-3.5 py-2 text-xs font-semibold transition-all duration-200',
                                selected
                                    ? `${colorClassName} shadow-sm ring-2 ring-white/70`
                                    : 'border-white/75 bg-white/50 text-slate-400 hover:-translate-y-0.5 hover:bg-white/80 hover:text-slate-600',
                            ].join(' ')}
                        >
                            #{tag.name}
                        </button>
                    )
                })}
            </div>
        </div>
    )
}
