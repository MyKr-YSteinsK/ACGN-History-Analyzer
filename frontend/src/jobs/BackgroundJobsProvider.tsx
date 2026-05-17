import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import {
    isBackgroundJobSuccess,
    subscribeBackgroundJobSettled,
    type BackgroundJobSettledDetail,
} from './backgroundJobEvents'

type ToastItem = {
    id: string
    tone: 'success' | 'error'
    title: string
    message: string
    href?: string
}

export function BackgroundJobsProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([])

    const dismissToast = useCallback((id: string) => {
        setToasts((items) => items.filter((item) => item.id !== id))
    }, [])

    const pushToast = useCallback(
        (item: Omit<ToastItem, 'id'> & { id?: string }) => {
            const id = item.id ?? `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
            setToasts((items) => [...items, { ...item, id }].slice(-5))
            window.setTimeout(() => {
                dismissToast(id)
            }, 8200)
        },
        [dismissToast],
    )

    useEffect(() => {
        function onSettled(detail: BackgroundJobSettledDetail) {
            if (!detail) {
                return
            }

            const ok = isBackgroundJobSuccess(detail.status)
            pushToast({
                tone: ok ? 'success' : 'error',
                title: ok ? '后台任务完成' : '后台任务失败',
                message: ok ? detail.title : (detail.errorMessage || detail.title || '失败'),
                href: ok ? detail.href : undefined,
            })
        }

        return subscribeBackgroundJobSettled(onSettled)
    }, [pushToast])

    const toastStack = (
        <div className="pointer-events-none fixed right-4 top-4 z-[300] flex w-[min(420px,calc(100vw-2rem))] flex-col gap-3">
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    className={[
                        'content-enter pointer-events-auto relative overflow-hidden rounded-[28px] border bg-white/82 p-4 shadow-[0_22px_70px_rgba(72,84,110,0.18)] backdrop-blur-2xl transition',
                        toast.tone === 'success'
                            ? 'border-emerald-200/75 text-emerald-950'
                            : 'border-rose-200/75 text-rose-950',
                    ].join(' ')}
                >
                    <div
                        className={[
                            'absolute inset-y-0 left-0 w-1.5',
                            toast.tone === 'success'
                                ? 'bg-gradient-to-b from-emerald-300 to-sky-300'
                                : 'bg-gradient-to-b from-rose-300 to-amber-300',
                        ].join(' ')}
                    />

                    <div className="flex items-start justify-between gap-3">
                        <div
                            className={[
                                'mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-2xl border text-sm font-black shadow-sm',
                                toast.tone === 'success'
                                    ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                                    : 'border-rose-200 bg-rose-50 text-rose-700',
                            ].join(' ')}
                            aria-hidden="true"
                        >
                            {toast.tone === 'success' ? '✓' : '!'}
                        </div>

                        <div className="min-w-0 flex-1">
                            <div className="flex flex-wrap items-center gap-2">
                                <p className="text-sm font-black leading-snug text-slate-800">
                                    {toast.title}
                                </p>
                                <span
                                    className={[
                                        'rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase tracking-[0.18em]',
                                        toast.tone === 'success'
                                            ? 'border-emerald-200 bg-emerald-50/80 text-emerald-700'
                                            : 'border-rose-200 bg-rose-50/80 text-rose-700',
                                    ].join(' ')}
                                >
                                    {toast.tone === 'success' ? 'done' : 'failed'}
                                </span>
                            </div>

                            <p className="mt-1.5 text-xs font-medium leading-5 text-slate-500">
                                {toast.message}
                            </p>

                            {toast.href ? (
                                <a
                                    href={toast.href}
                                    className="mt-3 inline-flex items-center rounded-full border border-white/80 bg-white/76 px-3 py-1.5 text-xs font-bold text-slate-600 shadow-sm transition hover:-translate-y-0.5 hover:bg-slate-900 hover:text-white"
                                    onClick={() => dismissToast(toast.id)}
                                >
                                    查看结果
                                </a>
                            ) : null}
                        </div>

                        <button
                            type="button"
                            aria-label="关闭通知"
                            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full border border-white/80 bg-white/58 text-sm font-black text-slate-400 shadow-sm transition hover:bg-white hover:text-slate-700"
                            onClick={() => dismissToast(toast.id)}
                        >
                            ×
                        </button>
                    </div>
                </div>
            ))}
        </div>
    )

    return (
        <>
            {children}
            {createPortal(toastStack, document.body)}
        </>
    )
}
