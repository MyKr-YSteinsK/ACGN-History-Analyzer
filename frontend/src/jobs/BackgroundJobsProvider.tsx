import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { Link } from 'react-router-dom'

type JobSettledDetail = {
    jobId: string
    status: string
    title: string
    href?: string
    errorMessage?: string | null
}

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
        function onSettled(event: Event) {
            const custom = event as CustomEvent<JobSettledDetail>
            const detail = custom.detail
            if (!detail) {
                return
            }

            const status = String(detail.status ?? '')
                .trim()
                .toUpperCase()
            const ok = status === 'SUCCEEDED'
            pushToast({
                tone: ok ? 'success' : 'error',
                title: ok ? '后台任务完成' : '后台任务失败',
                message: ok ? detail.title : (detail.errorMessage || detail.title || '失败'),
                href: ok ? detail.href : undefined,
            })
        }

        window.addEventListener('acgn-background-job-settled', onSettled as EventListener)
        return () => {
            window.removeEventListener('acgn-background-job-settled', onSettled as EventListener)
        }
    }, [pushToast])

    const toastStack = (
        <div className="pointer-events-none fixed right-4 top-4 z-[300] flex w-[min(360px,calc(100vw-2rem))] flex-col gap-2">
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    className={[
                        'pointer-events-auto rounded-2xl border px-4 py-3 shadow-lg backdrop-blur-md transition',
                        toast.tone === 'success'
                            ? 'border-emerald-200/80 bg-emerald-50/92 text-emerald-900'
                            : 'border-rose-200/80 bg-rose-50/92 text-rose-900',
                    ].join(' ')}
                >
                    <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                            <p className="text-xs font-semibold uppercase tracking-[0.16em] opacity-70">
                                {toast.tone === 'success' ? 'done' : 'error'}
                            </p>
                            <p className="mt-1 text-sm font-bold leading-snug">{toast.title}</p>
                            <p className="mt-1 text-xs font-medium leading-snug opacity-90">
                                {toast.message}
                            </p>
                            {toast.href ? (
                                <Link
                                    to={toast.href}
                                    className="mt-2 inline-flex text-xs font-semibold underline decoration-dotted underline-offset-2"
                                    onClick={() => dismissToast(toast.id)}
                                >
                                    打开相关页面
                                </Link>
                            ) : null}
                        </div>
                        <button
                            type="button"
                            className="shrink-0 rounded-full border border-black/10 bg-white/55 px-2 py-0.5 text-[11px] font-semibold text-slate-600 hover:bg-white"
                            onClick={() => dismissToast(toast.id)}
                        >
                            关闭
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
