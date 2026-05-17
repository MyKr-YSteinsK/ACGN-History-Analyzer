type ConfirmDialogProps = {
    open: boolean
    title: string
    description: string
    confirmText: string
    cancelText?: string
    loading?: boolean
    tone?: 'danger' | 'normal'
    onConfirm: () => void
    onCancel: () => void
}

function ConfirmDialog({
                           open,
                           title,
                           description,
                           confirmText,
                           cancelText = '取消',
                           loading = false,
                           tone = 'normal',
                           onConfirm,
                           onCancel,
                       }: ConfirmDialogProps) {
    if (!open) {
        return null
    }

    const confirmClassName =
        tone === 'danger'
            ? 'border-rose-300 bg-rose-500 text-white shadow-[0_12px_30px_rgba(244,63,94,0.24)] hover:bg-rose-600'
            : 'border-slate-800 bg-slate-900 text-white shadow-[0_12px_30px_rgba(15,23,42,0.18)] hover:bg-slate-700'

    return (
        <div className="fixed inset-0 z-[80] flex items-center justify-center p-6">
            <button
                type="button"
                aria-label="关闭确认弹窗"
                disabled={loading}
                onClick={onCancel}
                className="absolute inset-0 bg-slate-900/25 backdrop-blur-[3px]"
            />

            <section className="content-enter relative w-full max-w-[420px] overflow-hidden rounded-[30px] border border-white/75 bg-white/88 p-6 shadow-[0_30px_100px_rgba(70,80,110,0.28)] backdrop-blur-2xl">
                <div
                    className={[
                        'absolute inset-x-0 top-0 h-1',
                        tone === 'danger'
                            ? 'bg-gradient-to-r from-rose-300 via-amber-200 to-white'
                            : 'bg-gradient-to-r from-sky-300 via-emerald-200 to-white',
                    ].join(' ')}
                />

                <p
                    className={[
                        'text-xs font-semibold tracking-[0.28em]',
                        tone === 'danger' ? 'text-rose-400' : 'text-sky-500',
                    ].join(' ')}
                >
                    CONFIRM
                </p>

                <h2 className="mt-3 text-xl font-black text-slate-800">{title}</h2>

                <p className="mt-3 text-sm leading-7 text-slate-500">{description}</p>

                <div className="mt-6 flex justify-end gap-3">
                    <button
                        type="button"
                        disabled={loading}
                        onClick={onCancel}
                        className="rounded-2xl border border-white/80 bg-white/74 px-5 py-3 text-sm font-bold text-slate-500 shadow-sm transition hover:-translate-y-0.5 hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
                    >
                        {cancelText}
                    </button>

                    <button
                        type="button"
                        disabled={loading}
                        onClick={onConfirm}
                        className={[
                            'rounded-2xl border px-5 py-3 text-sm font-bold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:border-slate-300 disabled:bg-slate-400',
                            confirmClassName,
                        ].join(' ')}
                    >
                        {loading ? '处理中' : confirmText}
                    </button>
                </div>
            </section>
        </div>
    )
}

export default ConfirmDialog
