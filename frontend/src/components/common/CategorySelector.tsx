import { categoryOrder, type AcgnCategory } from '../../types/common'
import { categoryTheme } from '../../theme/acgnTheme'

type CategorySelectorProps = {
    value: AcgnCategory
    onChange: (category: AcgnCategory) => void
}

function CategorySelector({ value, onChange }: CategorySelectorProps) {
    return (
        <div className="grid gap-3 md:grid-cols-4">
            {categoryOrder.map((category) => {
                const theme = categoryTheme[category]
                const selected = value === category

                return (
                    <button
                        key={category}
                        type="button"
                        onClick={() => onChange(category)}
                        className={[
                            'group relative overflow-hidden rounded-[24px] px-4 py-3.5 text-left transition-all duration-200',
                            selected
                                ? `bg-gradient-to-br ${theme.softGradient} shadow-[0_18px_45px_rgba(120,135,160,0.16)]`
                                : 'bg-white/42 shadow-sm hover:-translate-y-0.5 hover:bg-white/70 hover:shadow-[0_14px_34px_rgba(120,135,160,0.12)]',
                        ].join(' ')}
                    >
                        <div
                            className={[
                                'pointer-events-none absolute inset-0 rounded-[24px] transition-opacity duration-200',
                                selected
                                    ? 'opacity-100 ring-1 ring-inset ring-white/80'
                                    : 'opacity-0 group-hover:opacity-100 group-hover:ring-1 group-hover:ring-inset group-hover:ring-white/75',
                            ].join(' ')}
                        />

                        <div
                            className={[
                                'pointer-events-none absolute -right-8 -top-8 h-24 w-24 rounded-full blur-2xl transition-opacity duration-200',
                                selected ? `${theme.bg} opacity-80` : 'bg-white/40 opacity-0',
                            ].join(' ')}
                        />

                        <div className="relative">
                            <div className="flex items-end gap-1.5 leading-none">
                <span
                    className={[
                        'text-[34px] font-black leading-none tracking-tight transition-all duration-200',
                        selected
                            ? theme.text
                            : 'text-slate-300 group-hover:text-slate-400',
                    ].join(' ')}
                >
                  {theme.letter}
                </span>

                                <span
                                    className={[
                                        'pb-[3px] text-[16px] font-bold leading-none',
                                        selected ? 'text-slate-800' : 'text-slate-500',
                                    ].join(' ')}
                                >
                  {theme.enName.slice(1)}
                </span>
                            </div>

                            <div className="mt-2.5 flex items-center justify-between">
                <span className="text-sm font-semibold text-slate-600">
                  {theme.cnName}
                </span>

                                <span
                                    className={[
                                        'h-2.5 w-2.5 rounded-full transition-all duration-200',
                                        selected ? theme.bg : 'bg-slate-200',
                                    ].join(' ')}
                                />
                            </div>
                        </div>
                    </button>
                )
            })}
        </div>
    )
}

export default CategorySelector