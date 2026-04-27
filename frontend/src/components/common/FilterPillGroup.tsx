export type FilterPillOption<T extends string | number> = {
    value: T
    label: string
    className?: string
}

type FilterPillGroupProps<T extends string | number> = {
    label: string
    value: T
    options: Array<FilterPillOption<T>>
    onChange: (value: T) => void
}

function FilterPillGroup<T extends string | number>({
                                                        label,
                                                        value,
                                                        options,
                                                        onChange,
                                                    }: FilterPillGroupProps<T>) {
    return (
        <div>
            <p className="mb-2 text-xs font-semibold text-slate-400">{label}</p>

            <div className="flex flex-wrap gap-2">
                {options.map((option) => {
                    const selected = option.value === value

                    return (
                        <button
                            key={option.value}
                            type="button"
                            onClick={() => onChange(option.value)}
                            className={[
                                'rounded-full border px-4 py-2 text-xs font-semibold transition-all duration-200',
                                selected
                                    ? option.className ??
                                    'border-sky-200 bg-sky-100/85 text-sky-700 shadow-sm'
                                    : 'border-white/75 bg-white/50 text-slate-400 hover:-translate-y-0.5 hover:bg-white/80 hover:text-slate-600',
                            ].join(' ')}
                        >
                            {option.label}
                        </button>
                    )
                })}
            </div>
        </div>
    )
}

export default FilterPillGroup