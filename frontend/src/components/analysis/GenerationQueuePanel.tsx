import GlassCard from '../common/GlassCard'
import type { GenerationJobStatus } from '../../types/analysis'

export type GenerationQueueItem = {
    id: string
    modeLabel: string
    scope: string
    status: GenerationJobStatus
    message?: string
}

type GenerationQueuePanelProps = {
    jobs: GenerationQueueItem[]
    onClearFinished: () => void
}

function GenerationQueuePanel({ jobs, onClearFinished }: GenerationQueuePanelProps) {
    const activeCount = jobs.filter((job) => job.status === 'queued' || job.status === 'running').length
    const finishedCount = jobs.length - activeCount

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/75 via-violet-50/45 to-sky-50/55 p-5">
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            generation queue
                        </p>
                        <h3 className="mt-1 text-lg font-black text-slate-800">
                            报告生成队列
                        </h3>
                        <p className="mt-1 text-sm leading-6 text-slate-500">
                            当前还有 {activeCount} 个任务在运行或等待。切换筛选不会打断已提交的任务，完成后会写入本地缓存和历史记录。
                        </p>
                    </div>
                    {finishedCount > 0 ? (
                        <button
                            type="button"
                            onClick={onClearFinished}
                            className="rounded-full border border-slate-200 bg-white/75 px-4 py-2 text-xs font-black text-slate-500 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                        >
                            清理已完成
                        </button>
                    ) : null}
                </div>

                <div className="mt-4 grid gap-3 lg:grid-cols-2">
                    {jobs.slice(-6).map((job) => (
                        <div
                            key={job.id}
                            className={[
                                'rounded-[22px] border bg-white/58 p-4 shadow-sm',
                                job.status === 'running'
                                    ? 'border-sky-200'
                                    : job.status === 'failed'
                                      ? 'border-rose-100'
                                      : 'border-white/75',
                            ].join(' ')}
                        >
                            <div className="flex items-start justify-between gap-3">
                                <div className="min-w-0">
                                    <p className="text-sm font-black text-slate-800">
                                        {job.modeLabel}
                                    </p>
                                    <p className="mt-1 line-clamp-1 text-xs text-slate-400">
                                        {job.scope}
                                    </p>
                                </div>
                                <span
                                    className={[
                                        'shrink-0 rounded-full border px-2.5 py-1 text-[11px] font-black',
                                        getGenerationJobStatusClassName(job.status),
                                    ].join(' ')}
                                >
                                    {getGenerationJobStatusLabel(job.status)}
                                </span>
                            </div>
                            {job.message ? (
                                <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-500">
                                    {job.message}
                                </p>
                            ) : null}
                        </div>
                    ))}
                </div>
            </div>
        </GlassCard>
    )
}

function getGenerationJobStatusLabel(status: GenerationJobStatus) {
    if (status === 'queued') {
        return '排队中'
    }

    if (status === 'running') {
        return '生成中'
    }

    if (status === 'completed') {
        return '已完成'
    }

    return '失败'
}

function getGenerationJobStatusClassName(status: GenerationJobStatus) {
    if (status === 'queued') {
        return 'border-violet-100 bg-violet-50/85 text-violet-600'
    }

    if (status === 'running') {
        return 'border-sky-200 bg-sky-50/90 text-sky-700'
    }

    if (status === 'completed') {
        return 'border-emerald-200 bg-emerald-50/90 text-emerald-700'
    }

    return 'border-rose-200 bg-rose-50/90 text-rose-700'
}

export default GenerationQueuePanel
