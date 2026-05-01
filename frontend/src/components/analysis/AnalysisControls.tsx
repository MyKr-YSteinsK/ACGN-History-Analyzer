import GlassCard from '../common/GlassCard'
import { categoryTheme } from '../../theme/acgnTheme'
import { categoryOrder, getReleaseQuarterLabel, type AcgnCategory, type ReleaseQuarter } from '../../types/common'
import type { AnalysisMode, AnalysisModeMeta, CategoryFilter } from '../../types/analysis'

type AnalysisWorkspacePanelProps = {
    panelGradient: string
    activeMode: AnalysisModeMeta
    analysisModes: AnalysisModeMeta[]
    analysisMode: AnalysisMode
    loading: boolean
    reportReady: boolean
    selectedYear: number
    selectedQuarter: ReleaseQuarter
    selectedCategory: CategoryFilter
    usesYearFilter: boolean
    usesQuarterFilter: boolean
    usesQuestionInput: boolean
    question: string
    onModeChange: (mode: AnalysisMode) => void
    onYearChange: (year: number) => void
    onQuarterChange: (quarter: ReleaseQuarter) => void
    onCategoryChange: (category: CategoryFilter) => void
    onQuestionChange: (value: string) => void
    onSubmit: () => void
}

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020]
const quarterOptions: ReleaseQuarter[] = [1, 2, 3, 4]

const questionExamples = [
    '像朋友一样吐槽一下我最近的 ACGN 口味',
    '帮我设计一个适合答辩展示的 AI 分析演示问题',
    '如果今晚只补一部，我应该从想看里挑什么？',
    '把我的高分动画口味写成一段有记忆感的总结',
]

function AnalysisWorkspacePanel({
    panelGradient,
    activeMode,
    analysisModes,
    analysisMode,
    loading,
    reportReady,
    selectedYear,
    selectedQuarter,
    selectedCategory,
    usesYearFilter,
    usesQuarterFilter,
    usesQuestionInput,
    question,
    onModeChange,
    onYearChange,
    onQuarterChange,
    onCategoryChange,
    onQuestionChange,
    onSubmit,
}: AnalysisWorkspacePanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div
                className={[
                    'bg-gradient-to-br p-5 transition-all duration-300 xl:p-6',
                    panelGradient,
                ].join(' ')}
            >
                <div className="flex flex-wrap items-start justify-between gap-5">
                    <div className="max-w-3xl">
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            analysis workspace
                        </p>
                        <h2 className="mt-2 text-2xl font-black text-slate-800">
                            {activeMode.label}
                        </h2>
                        <p className="mt-2 text-sm leading-6 text-slate-500">
                            {activeMode.description}
                        </p>
                    </div>

                    <div className="grid grid-cols-3 gap-2">
                        <CapabilityPill label="报告" value="结构化" />
                        <CapabilityPill label="依据" value="可追溯" />
                        <CapabilityPill label="结果" value="可展示" />
                    </div>
                </div>

                <div className="mt-6 grid gap-3 md:grid-cols-2 xl:grid-cols-4 2xl:grid-cols-6">
                    {analysisModes.map((mode) => (
                        <AnalysisModeButton
                            key={mode.value}
                            mode={mode}
                            selected={analysisMode === mode.value}
                            onClick={() => onModeChange(mode.value)}
                        />
                    ))}
                </div>

                {usesQuestionInput ? (
                    <QuestionAnswerControl
                        question={question}
                        loading={loading}
                        buttonLabel={reportReady ? '重新生成回答' : '生成回答'}
                        onQuestionChange={onQuestionChange}
                        onSubmit={onSubmit}
                    />
                ) : (
                    <AnalysisControlBar
                        loading={loading}
                        selectedYear={selectedYear}
                        selectedQuarter={selectedQuarter}
                        selectedCategory={selectedCategory}
                        usesYearFilter={usesYearFilter}
                        usesQuarterFilter={usesQuarterFilter}
                        analysisMode={analysisMode}
                        buttonLabel={reportReady ? '重新生成' : '生成分析'}
                        onYearChange={onYearChange}
                        onQuarterChange={onQuarterChange}
                        onCategoryChange={onCategoryChange}
                        onSubmit={onSubmit}
                    />
                )}
            </div>
        </GlassCard>
    )
}

type CapabilityPillProps = {
    label: string
    value: string
}

function CapabilityPill({ label, value }: CapabilityPillProps) {
    return (
        <div className="rounded-2xl border border-white/70 bg-white/58 px-4 py-2 text-center shadow-sm backdrop-blur">
            <p className="text-[11px] font-semibold text-slate-400">{label}</p>
            <p className="mt-0.5 text-sm font-black text-slate-700">{value}</p>
        </div>
    )
}

type AnalysisModeButtonProps = {
    mode: AnalysisModeMeta
    selected: boolean
    onClick: () => void
}

function AnalysisModeButton({ mode, selected, onClick }: AnalysisModeButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'group overflow-hidden rounded-[24px] border p-4 text-left transition-all duration-200',
                selected
                    ? 'scale-[1.01] border-sky-200 bg-white/85 text-sky-700 shadow-[0_16px_42px_rgba(125,145,170,0.16)]'
                    : 'border-white/75 bg-white/50 text-slate-500 hover:-translate-y-0.5 hover:bg-white/78',
            ].join(' ')}
        >
            <div
                className={[
                    'mb-3 h-1.5 rounded-full bg-gradient-to-r transition-opacity duration-200',
                    mode.accent,
                    selected ? 'opacity-100' : 'opacity-55 group-hover:opacity-85',
                ].join(' ')}
            />
            <p className="text-sm font-black">{mode.label}</p>
            <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-400">
                {mode.description}
            </p>
        </button>
    )
}

type AnalysisControlBarProps = {
    loading: boolean
    selectedYear: number
    selectedQuarter: ReleaseQuarter
    selectedCategory: CategoryFilter
    usesYearFilter: boolean
    usesQuarterFilter: boolean
    analysisMode: AnalysisMode
    buttonLabel: string
    onYearChange: (year: number) => void
    onQuarterChange: (quarter: ReleaseQuarter) => void
    onCategoryChange: (category: CategoryFilter) => void
    onSubmit: () => void
}

function AnalysisControlBar({
    loading,
    selectedYear,
    selectedQuarter,
    selectedCategory,
    usesYearFilter,
    usesQuarterFilter,
    analysisMode,
    buttonLabel,
    onYearChange,
    onQuarterChange,
    onCategoryChange,
    onSubmit,
}: AnalysisControlBarProps) {
    const gridClassName = usesQuarterFilter
        ? 'mt-6 grid gap-4 xl:grid-cols-[260px_340px_1fr_150px]'
        : usesYearFilter
          ? 'mt-6 grid gap-4 xl:grid-cols-[300px_1fr_150px]'
          : 'mt-6 grid gap-4 xl:grid-cols-[1fr_150px]'

    return (
        <div className={gridClassName}>
            {usesYearFilter ? (
                <div>
                    <ControlLabel>年份</ControlLabel>
                    <div className="flex flex-wrap gap-2">
                        {yearOptions.map((year) => {
                            const selected = selectedYear === year

                            return (
                                <button
                                    key={year}
                                    type="button"
                                    onClick={() => onYearChange(year)}
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-black transition-all duration-200',
                                        selected
                                            ? 'border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                            : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
                                    ].join(' ')}
                                >
                                    {year}
                                </button>
                            )
                        })}
                    </div>
                </div>
            ) : null}

            {usesQuarterFilter ? (
                <div>
                    <ControlLabel>月份</ControlLabel>
                    <div className="grid grid-cols-4 gap-2">
                        {quarterOptions.map((quarter) => {
                            const selected = selectedQuarter === quarter

                            return (
                                <button
                                    key={quarter}
                                    type="button"
                                    onClick={() => onQuarterChange(quarter)}
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-black transition-all duration-200',
                                        selected
                                            ? 'border-cyan-200 bg-cyan-100/85 text-cyan-700 shadow-sm'
                                            : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
                                    ].join(' ')}
                                >
                                    {getReleaseQuarterLabel(quarter)}
                                </button>
                            )
                        })}
                    </div>
                </div>
            ) : null}

            <div>
                <ControlLabel>类型</ControlLabel>
                <div className="grid gap-2 sm:grid-cols-5">
                    {analysisMode !== 'TYPE_PREFERENCE' ? (
                        <CategoryButton
                            label="全部"
                            active={selectedCategory === 'ALL'}
                            onClick={() => onCategoryChange('ALL')}
                        />
                    ) : null}
                    {categoryOrder.map((category) => (
                        <CategoryButton
                            key={category}
                            category={category}
                            label={categoryTheme[category].cnName}
                            active={selectedCategory === category}
                            onClick={() => onCategoryChange(category)}
                        />
                    ))}
                </div>
            </div>

            <div className="flex items-end">
                <GenerateButton
                    loading={loading}
                    disabled={false}
                    label={buttonLabel}
                    onClick={onSubmit}
                />
            </div>
        </div>
    )
}

type ControlLabelProps = {
    children: string
}

function ControlLabel({ children }: ControlLabelProps) {
    return (
        <p className="mb-2 text-xs font-semibold text-slate-400">
            {children}
        </p>
    )
}

type CategoryButtonProps = {
    label: string
    active: boolean
    category?: AcgnCategory
    onClick: () => void
}

function CategoryButton({ label, active, category, onClick }: CategoryButtonProps) {
    const theme = category ? categoryTheme[category] : null

    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-2xl border px-4 py-3 text-sm font-black transition-all duration-200',
                active && theme
                    ? `${theme.border} ${theme.bg} ${theme.text} scale-[1.02] shadow-sm`
                    : active
                      ? 'scale-[1.02] border-slate-200 bg-white/90 text-slate-700 shadow-sm'
                      : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
            ].join(' ')}
        >
            {label}
        </button>
    )
}

type QuestionAnswerControlProps = {
    question: string
    loading: boolean
    buttonLabel: string
    onQuestionChange: (value: string) => void
    onSubmit: () => void
}

function QuestionAnswerControl({
    question,
    loading,
    buttonLabel,
    onQuestionChange,
    onSubmit,
}: QuestionAnswerControlProps) {
    return (
        <div className="mt-6 rounded-[30px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
            <div className="grid gap-4 xl:grid-cols-[1fr_160px]">
                <div>
                    <ControlLabel>提问</ControlLabel>
                    <textarea
                        value={question}
                        onChange={(event) => onQuestionChange(event.target.value)}
                        rows={3}
                        placeholder="可以自由问：查记录、要推荐、让它吐槽口味、写答辩话术，或做一个补番计划。"
                        className="min-h-[112px] w-full resize-none rounded-[26px] border border-white/80 bg-white/72 px-4 py-3 text-sm font-semibold leading-6 text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
                    />
                </div>

                <div className="flex items-end">
                    <GenerateButton
                        loading={loading}
                        disabled={!question.trim()}
                        label={buttonLabel}
                        onClick={onSubmit}
                    />
                </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
                {questionExamples.map((example) => (
                    <button
                        key={example}
                        type="button"
                        onClick={() => onQuestionChange(example)}
                        className="rounded-full border border-white/75 bg-white/68 px-3 py-1.5 text-xs font-semibold text-slate-500 transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        {example}
                    </button>
                ))}
            </div>
        </div>
    )
}

type GenerateButtonProps = {
    loading: boolean
    disabled: boolean
    label: string
    onClick: () => void
}

function GenerateButton({ loading, disabled, label, onClick }: GenerateButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            className={[
                'h-12 w-full rounded-2xl border px-4 text-sm font-black shadow-sm transition-all duration-200',
                disabled
                    ? 'cursor-not-allowed border-white/55 bg-white/45 text-slate-300'
                    : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
            ].join(' ')}
        >
            {loading ? '加入队列' : label}
        </button>
    )
}

type ScopeSummaryProps = {
    scopeText: string
    modeLabel: string
    category: CategoryFilter
}

function ScopeSummary({ scopeText, modeLabel, category }: ScopeSummaryProps) {
    const theme = category === 'ALL' ? null : categoryTheme[category]

    return (
        <GlassCard className="overflow-hidden">
            <div
                className={[
                    'flex flex-wrap items-center justify-between gap-4 bg-gradient-to-br p-4',
                    theme?.softGradient ?? 'from-white/70 via-sky-50/45 to-violet-50/45',
                ].join(' ')}
            >
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        current scope
                    </p>
                    <h3 className="mt-1 text-lg font-black text-slate-800">
                        {scopeText}
                    </h3>
                </div>

                <div className="flex flex-wrap gap-2">
                    <span className="rounded-full border border-white/75 bg-white/65 px-4 py-2 text-xs font-black text-slate-500">
                        {modeLabel}
                    </span>
                    <span className="rounded-full border border-white/75 bg-white/65 px-4 py-2 text-xs font-black text-slate-500">
                        {category === 'ALL' ? '全部类型' : categoryTheme[category].cnName}
                    </span>
                </div>
            </div>
        </GlassCard>
    )
}

export { AnalysisWorkspacePanel, ScopeSummary }
