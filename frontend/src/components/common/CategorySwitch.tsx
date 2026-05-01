import { categoryTheme } from '../../theme/acgnTheme'
import { categoryOrder, type AcgnCategory } from '../../types/common'

type CategorySwitchProps = {
    value: AcgnCategory
    onChange: (category: AcgnCategory) => void
}

function CategorySwitch({ value, onChange }: CategorySwitchProps) {
    const activeIndex = categoryOrder.indexOf(value)
    const activeTheme = categoryTheme[value]

    return (
        <section
            className={[
                'relative overflow-hidden rounded-[30px] border border-white/70 bg-gradient-to-br p-2 shadow-sm backdrop-blur-xl transition-all duration-300',
                activeTheme.softGradient,
            ].join(' ')}
        >
            <div
                className="absolute bottom-2 top-2 rounded-[24px] border border-white/85 bg-white/78 shadow-[0_16px_40px_rgba(120,135,160,0.14)] transition-transform duration-300 ease-out"
                style={{
                    width: '25%',
                    transform: `translateX(${activeIndex * 100}%)`,
                }}
            />

            <div className="relative grid grid-cols-4">
                {categoryOrder.map((category) => {
                    const theme = categoryTheme[category]
                    const selected = value === category
                    const upperName = theme.enName.toUpperCase()

                    return (
                        <button
                            key={category}
                            type="button"
                            onClick={() => onChange(category)}
                            className="relative flex h-24 items-center justify-center rounded-[24px] px-2 transition-all duration-200"
                        >
                            <span className="flex flex-col items-center gap-2">
                                <span className="flex items-end gap-1 leading-none">
                                    <span
                                        className={[
                                            'text-[36px] font-black leading-none transition-colors',
                                            selected ? theme.text : 'text-slate-300',
                                        ].join(' ')}
                                    >
                                        {upperName[0]}
                                    </span>
                                    <span
                                        className={[
                                            'pb-[4px] text-[15px] font-black leading-none transition-colors',
                                            selected ? 'text-slate-800' : 'text-slate-400',
                                        ].join(' ')}
                                    >
                                        {upperName.slice(1)}
                                    </span>
                                </span>

                                <span
                                    className={[
                                        'rounded-full border px-3 py-1 text-[11px] font-semibold transition-colors',
                                        selected
                                            ? `${theme.border} ${theme.bg} ${theme.text}`
                                            : 'border-white/70 bg-white/45 text-slate-400',
                                    ].join(' ')}
                                >
                                    {theme.cnName}
                                </span>
                            </span>
                        </button>
                    )
                })}
            </div>
        </section>
    )
}

export default CategorySwitch
