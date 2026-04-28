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
            ? 'bg-rose-500 text-white hover:bg-rose-600'
            : 'bg-slate-900 text-white hover:bg-slate-700'

    return (
        <div className="fixed inset-0 z-[80] flex items-center justify-center p-6">
            <button
                type="button"
                aria-label="关闭确认弹窗"
                disabled={loading}
                onClick={onCancel}
                className="absolute inset-0 bg-slate-900/25 backdrop-blur-[3px]"
            />

            <section className="content-enter relative w-full max-w-[420px] rounded-[30px] border border-white/75 bg-white/88 p-6 shadow-[0_30px_100px_rgba(70,80,110,0.28)] backdrop-blur-2xl">
                <p className="text-xs font-semibold tracking-[0.28em] text-rose-400">
                    CONFIRM
                </p>

                <h2 className="mt-3 text-xl font-black text-slate-800">{title}</h2>

                <p className="mt-3 text-sm leading-7 text-slate-500">{description}</p>

                <div className="mt-6 flex justify-end gap-3">
                    <button
                        type="button"
                        disabled={loading}
                        onClick={onCancel}
                        className="rounded-2xl border border-white/80 bg-white/70 px-5 py-3 text-sm font-semibold text-slate-500 transition hover:bg-white disabled:cursor-not-allowed disabled:text-slate-300"
                    >
                        {cancelText}
                    </button>

                    <button
                        type="button"
                        disabled={loading}
                        onClick={onConfirm}
                        className={[
                            'rounded-2xl px-5 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:bg-slate-400',
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
