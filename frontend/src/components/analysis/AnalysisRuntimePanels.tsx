import GlassCard from '../common/GlassCard'
import { formatGeneratedAt } from '../../utils/llmReportFormat'

type CacheInfo = {
    source: 'cache' | 'fresh'
    cachedAt: string
    expiresAt: string
}

type CacheStatusPanelProps = {
    cacheInfo: CacheInfo
    onRegenerate: () => void
    onClearCurrent: () => void
    onClearAll: () => void
}

export function CacheStatusPanel({
    cacheInfo,
    onRegenerate,
    onClearCurrent,
    onClearAll,
}: CacheStatusPanelProps) {
    const isCache = cacheInfo.source === 'cache'

    return (
        <GlassCard className="overflow-hidden">
            <div className="flex flex-wrap items-center justify-between gap-4 bg-gradient-to-br from-white/72 via-emerald-50/45 to-sky-50/45 p-4">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                        local cache
                    </p>
                    <h3 className="mt-1 text-base font-black text-slate-800">
                        {isCache ? '当前显示本地缓存结果' : '当前结果已写入本地缓存'}
                    </h3>
                    <p className="mt-1 text-sm leading-6 text-slate-500">
                        缓存时间：{formatGeneratedAt(cacheInfo.cachedAt)}，有效至 {formatGeneratedAt(cacheInfo.expiresAt)}。切回同一分析条件时，会先把这份结果递给你。
                    </p>
                </div>

                <div className="flex flex-wrap gap-2">
                    <button
                        type="button"
                        onClick={onRegenerate}
                        className="rounded-full border border-emerald-200 bg-white/75 px-5 py-2.5 text-sm font-black text-emerald-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-emerald-50"
                    >
                        重新生成并覆盖缓存
                    </button>
                    <button
                        type="button"
                        onClick={onClearCurrent}
                        className="rounded-full border border-slate-200 bg-white/70 px-5 py-2.5 text-sm font-black text-slate-600 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        清除当前缓存
                    </button>
                    <button
                        type="button"
                        onClick={onClearAll}
                        className="rounded-full border border-rose-200 bg-white/70 px-5 py-2.5 text-sm font-black text-rose-600 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-50"
                    >
                        清除全部 AI 缓存
                    </button>
                </div>
            </div>
        </GlassCard>
    )
}

type GenerationProgress = {
    percent: number
    title: string
    detail: string
}

type GenerationProgressPanelProps = {
    progress: GenerationProgress
    modeLabel: string
    modelLabel: string
}

export function GenerationProgressPanel({
    progress,
    modeLabel,
    modelLabel,
}: GenerationProgressPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/75 via-sky-50/55 to-violet-50/55 p-6">
                <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            generating
                        </p>
                        <h3 className="mt-2 text-2xl font-black text-slate-800">
                            {progress.title}
                        </h3>
                        <p className="mt-2 max-w-3xl text-sm leading-7 text-slate-500">
                            {progress.detail}
                        </p>
                    </div>

                    <div className="flex flex-wrap gap-2">
                        <span className="rounded-full border border-white/75 bg-white/70 px-4 py-2 text-xs font-black text-slate-500">
                            {modeLabel}
                        </span>
                        <span className="rounded-full border border-white/75 bg-white/70 px-4 py-2 text-xs font-black text-slate-500">
                            {modelLabel}
                        </span>
                    </div>
                </div>

                <div className="mt-6 overflow-hidden rounded-full border border-white/80 bg-white/60 p-1 shadow-inner">
                    <div
                        className="h-3 rounded-full bg-gradient-to-r from-sky-300 via-violet-300 to-emerald-300 transition-all duration-500"
                        style={{ width: `${progress.percent}%` }}
                    />
                </div>

                <div className="mt-4 grid gap-3 md:grid-cols-4">
                    {getProgressSteps(progress.percent).map((step) => (
                        <div
                            key={step.label}
                            className={[
                                'rounded-[22px] border px-4 py-3 shadow-sm transition-all duration-200',
                                step.active
                                    ? 'border-sky-200 bg-white/78 text-sky-700'
                                    : 'border-white/70 bg-white/45 text-slate-400',
                            ].join(' ')}
                        >
                            <p className="text-xs font-black">{step.label}</p>
                            <p className="mt-1 text-[11px] leading-5">{step.description}</p>
                        </div>
                    ))}
                </div>

                <p className="mt-4 text-xs leading-5 text-slate-400">
                    真实模型不会暴露内部思考过程，这里展示的是系统根据请求阶段整理出的可解释进度。
                </p>
            </div>
        </GlassCard>
    )
}

type GenerationErrorPanelProps = {
    message: string
    canOpenConfig: boolean
    onRetry: () => void
    onOpenConfig: () => void
}

export function GenerationErrorPanel({
    message,
    canOpenConfig,
    onRetry,
    onOpenConfig,
}: GenerationErrorPanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/80 to-rose-50/75 p-5">
                <p className="text-xs font-semibold uppercase tracking-[0.24em] text-rose-300">
                    generation failed
                </p>
                <h3 className="mt-2 text-lg font-black text-rose-700">
                    智能分析这次没跑顺
                </h3>
                <p className="mt-2 text-sm leading-6 text-rose-600">
                    {message}
                </p>
                <p className="mt-2 text-xs leading-5 text-slate-400">
                    可以先重试一次；如果连续失败，再检查 API Key、Base URL、数据库连接或切换到预览模式确认页面流程。
                </p>
                <div className="mt-4 flex flex-wrap gap-3">
                    <button
                        type="button"
                        onClick={onRetry}
                        className="rounded-full border border-rose-200 bg-white/75 px-5 py-2.5 text-sm font-black text-rose-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-50"
                    >
                        重试生成
                    </button>
                    {canOpenConfig ? (
                        <button
                            type="button"
                            onClick={onOpenConfig}
                            className="rounded-full border border-sky-200 bg-white/75 px-5 py-2.5 text-sm font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                        >
                            检查 API 设置
                        </button>
                    ) : null}
                </div>
            </div>
        </GlassCard>
    )
}

function getProgressSteps(percent: number) {
    return [
        {
            label: '参数校验',
            description: '确认年份、月份、类型和问题内容。',
            active: percent >= 8,
        },
        {
            label: '上下文构建',
            description: '整理记录、标签、收藏和系列关系。',
            active: percent >= 22,
        },
        {
            label: '模型生成',
            description: '调用 DeepSeek 或生成本地预览。',
            active: percent >= 44,
        },
        {
            label: '格式渲染',
            description: '校验 JSON 字段并渲染报告。',
            active: percent >= 78,
        },
    ]
}

