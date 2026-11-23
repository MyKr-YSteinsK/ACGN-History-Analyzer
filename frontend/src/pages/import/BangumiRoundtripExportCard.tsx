import { useState } from 'react'
import { getBangumiRoundtripExport, type BackupExportResponse } from '../../api/exportApi'
import { getApiErrorMessage } from '../../api/request'
import GlassCard from '../../components/common/GlassCard'
import { emitBackgroundJobSettledToast } from '../../jobs/backgroundJobEvents'

export default function BangumiRoundtripExportCard() {
    const [exportData, setExportData] = useState<BackupExportResponse | null>(null)
    const [exportLoading, setExportLoading] = useState(false)
    const [exportError, setExportError] = useState<string | null>(null)
    const [copyTip, setCopyTip] = useState<string | null>(null)

    async function handleLoadExport() {
        setExportLoading(true)
        setExportError(null)
        setCopyTip(null)
        try {
            const data = await getBangumiRoundtripExport()
            setExportData(data)
            emitBackgroundJobSettledToast({
                status: 'SUCCEEDED',
                title: 'Bangumi 往返 JSON 已就绪，可复制或下载',
                href: '/import',
            })
        } catch (error) {
            const message = getApiErrorMessage(error)
            setExportData(null)
            setExportError(message)
            emitBackgroundJobSettledToast({
                status: 'FAILED',
                title: 'Bangumi 往返导出失败',
                href: '/import',
                errorMessage: message,
            })
        } finally {
            setExportLoading(false)
        }
    }

    async function copyBlob(label: string, text: string) {
        try {
            await navigator.clipboard.writeText(text)
            setCopyTip(`已复制：${label}`)
            window.setTimeout(() => setCopyTip(null), 2400)
        } catch {
            setCopyTip('复制失败，请手动全选下方文本')
        }
    }

    function downloadJson(filename: string, text: string) {
        const blob = new Blob([text], { type: 'application/json;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        const anchor = document.createElement('a')
        anchor.href = url
        anchor.download = filename
        anchor.click()
        URL.revokeObjectURL(url)
    }

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/62 via-emerald-50/35 to-sky-50/45 p-6">
                <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-slate-400">
                            round-trip export
                        </p>
                        <h3 className="mt-1 text-xl font-black text-slate-800">
                            Bangumi 往返导出（JSON）
                        </h3>
                        <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-500">
                            生成两段合法 JSON：个人记录为根字段 <code className="rounded bg-white/70 px-1">records</code>
                            ；若有 Bangumi 作品则额外提供 <code className="rounded bg-white/70 px-1">subjects</code>
                            。用于在本站导入中心按顺序还原。
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={() => void handleLoadExport()}
                        disabled={exportLoading}
                        className="shrink-0 rounded-2xl border border-emerald-200/90 bg-white/80 px-5 py-2.5 text-sm font-black text-emerald-800 shadow-sm transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        {exportLoading ? '生成中…' : '拉取导出 JSON'}
                    </button>
                </div>

                {exportError ? (
                    <div className="mt-4 rounded-[22px] border border-rose-100 bg-rose-50/70 px-4 py-3 text-sm text-rose-700">
                        {exportError}
                    </div>
                ) : null}

                {exportData ? (
                    <div className="mt-4 space-y-4">
                        <div className="rounded-[22px] border border-white/75 bg-white/60 p-4 text-sm leading-6 text-slate-600">
                            <p className="font-semibold text-slate-700">导入顺序</p>
                            <p className="mt-1">{exportData.usageHint}</p>
                        </div>

                        <div className="grid gap-4 lg:grid-cols-2">
                            <div className="rounded-[22px] border border-white/75 bg-white/55 p-4">
                                <div className="flex flex-wrap items-center justify-between gap-2">
                                    <p className="text-sm font-black text-slate-800">记录 JSON</p>
                                    <div className="flex flex-wrap gap-2">
                                        <button
                                            type="button"
                                            onClick={() => void copyBlob('记录 JSON', exportData.recordJsonText)}
                                            className="rounded-xl border border-white/80 bg-white/75 px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-white"
                                        >
                                            复制
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() =>
                                                downloadJson(
                                                    'acgn-records-roundtrip.json',
                                                    exportData.recordJsonText,
                                                )
                                            }
                                            className="rounded-xl border border-sky-100 bg-sky-50/90 px-3 py-1.5 text-xs font-semibold text-sky-800 hover:bg-sky-100"
                                        >
                                            下载
                                        </button>
                                    </div>
                                </div>
                                <pre className="mt-3 max-h-40 overflow-auto rounded-xl border border-white/70 bg-slate-900/5 p-3 text-[11px] leading-relaxed text-slate-700">
                                    {exportData.recordJsonText}
                                </pre>
                            </div>

                            <div className="rounded-[22px] border border-white/75 bg-white/55 p-4">
                                <div className="flex flex-wrap items-center justify-between gap-2">
                                    <p className="text-sm font-black text-slate-800">作品 JSON（Bangumi）</p>
                                    {exportData.subjectJsonText ? (
                                        <div className="flex flex-wrap gap-2">
                                            <button
                                                type="button"
                                                onClick={() =>
                                                    void copyBlob('作品 JSON', exportData.subjectJsonText!)
                                                }
                                                className="rounded-xl border border-white/80 bg-white/75 px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-white"
                                            >
                                                复制
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() =>
                                                    downloadJson(
                                                        'acgn-subjects-bangumi-roundtrip.json',
                                                        exportData.subjectJsonText!,
                                                    )
                                                }
                                                className="rounded-xl border border-sky-100 bg-sky-50/90 px-3 py-1.5 text-xs font-semibold text-sky-800 hover:bg-sky-100"
                                            >
                                                下载
                                            </button>
                                        </div>
                                    ) : (
                                        <span className="text-xs font-semibold text-slate-400">无（可只导入记录）</span>
                                    )}
                                </div>
                                {exportData.subjectJsonText ? (
                                    <pre className="mt-3 max-h-40 overflow-auto rounded-xl border border-white/70 bg-slate-900/5 p-3 text-[11px] leading-relaxed text-slate-700">
                                        {exportData.subjectJsonText}
                                    </pre>
                                ) : (
                                    <p className="mt-3 text-xs leading-5 text-slate-400">
                                        当前库中没有可随记录一并导出的 Bangumi 作品行；记录里非 Bangumi 条目使用 subjectId。
                                    </p>
                                )}
                            </div>
                        </div>

                        {copyTip ? (
                            <p className="text-center text-xs font-semibold text-emerald-700">{copyTip}</p>
                        ) : null}
                    </div>
                ) : null}
            </div>
        </GlassCard>
    )
}
