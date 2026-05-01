import type { ReportHistoryItem } from '../../types/analysis'
import { formatGeneratedAt } from '../../utils/llmReportFormat'
import SidePanel from './SidePanel'

type ReportHistoryPanelProps = {
    items: ReportHistoryItem[]
    onSelect: (item: ReportHistoryItem) => void
    onDelete: (item: ReportHistoryItem) => void
    onPruneInvalid: () => void
    onPruneExpired: () => void
    onClear: () => void
}

function ReportHistoryPanel({
    items,
    onSelect,
    onDelete,
    onPruneInvalid,
    onPruneExpired,
    onClear,
}: ReportHistoryPanelProps) {
    return (
        <SidePanel
            title="分析记录"
            eyebrow="history"
            gradient="from-white/75 to-violet-50/65"
        >
            {items.length > 0 ? (
                <div className="mb-3 flex flex-wrap justify-end gap-2">
                    <button
                        type="button"
                        onClick={onPruneInvalid}
                        className="rounded-full border border-sky-200 bg-white/70 px-3 py-1.5 text-[11px] font-black text-sky-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                    >
                        清理失效
                    </button>
                    <button
                        type="button"
                        onClick={onPruneExpired}
                        className="rounded-full border border-amber-200 bg-white/70 px-3 py-1.5 text-[11px] font-black text-amber-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-amber-50"
                    >
                        清理过期
                    </button>
                    <button
                        type="button"
                        onClick={onClear}
                        className="rounded-full border border-slate-200 bg-white/70 px-3 py-1.5 text-[11px] font-black text-slate-500 transition-all duration-200 hover:-translate-y-0.5 hover:bg-white"
                    >
                        清空历史
                    </button>
                </div>
            ) : null}
            <div className="space-y-2.5">
                {items.length > 0 ? (
                    items.slice(0, 6).map((item, index) => (
                        <article
                            key={`${item.id}-${index}`}
                            onClick={() => onSelect(item)}
                            onKeyDown={(event) => {
                                if (event.key === 'Enter' || event.key === ' ') {
                                    event.preventDefault()
                                    onSelect(item)
                                }
                            }}
                            role="button"
                            tabIndex={0}
                            className="w-full cursor-pointer rounded-[22px] border border-white/75 bg-white/58 p-4 text-left shadow-sm outline-none transition-all duration-200 hover:-translate-y-0.5 hover:bg-white/75 focus:ring-4 focus:ring-violet-100/80"
                        >
                            <div className="flex items-start justify-between gap-3">
                                <div className="min-w-0">
                                    <h4 className="line-clamp-1 text-sm font-black text-slate-800">
                                        {item.title}
                                    </h4>
                                    <p className="mt-1 line-clamp-1 text-xs text-slate-400">
                                        {item.scope}
                                    </p>
                                </div>
                                <span className="shrink-0 rounded-full border border-white/80 bg-white/70 px-2.5 py-1 text-[11px] font-semibold text-slate-500">
                                    {item.mode}
                                </span>
                            </div>
                            <div className="mt-3 flex items-center justify-between gap-2">
                                <p className="text-[11px] font-semibold text-slate-300">
                                    {formatGeneratedAt(item.generatedAt)}
                                </p>
                                <button
                                    type="button"
                                    onClick={(event) => {
                                        event.stopPropagation()
                                        onDelete(item)
                                    }}
                                    className="rounded-full border border-rose-100 bg-white/65 px-2.5 py-1 text-[11px] font-black text-rose-500 transition-all duration-200 hover:bg-rose-50"
                                >
                                    删除
                                </button>
                            </div>
                        </article>
                    ))
                ) : (
                    <p className="rounded-[22px] border border-white/75 bg-white/58 p-4 text-sm leading-6 text-slate-400">
                        生成第一份报告后，这里会保留最近的本地历史记录。
                    </p>
                )}
            </div>
        </SidePanel>
    )
}

export default ReportHistoryPanel
