import type { ReactNode } from 'react'

type FeedbackTone = 'info' | 'success' | 'warning' | 'error'

const toneClassNames: Record<FeedbackTone, string> = {
    info: 'border-sky-100 bg-sky-50/70 text-sky-700',
    success: 'border-emerald-100 bg-emerald-50/75 text-emerald-700',
    warning: 'border-amber-100 bg-amber-50/75 text-amber-700',
    error: 'border-rose-100 bg-rose-50/75 text-rose-600',
}

type FeedbackBannerProps = {
    children: ReactNode
    tone?: FeedbackTone
    className?: string
}

export function FeedbackBanner({
                                   children,
                                   tone = 'info',
                                   className = '',
                               }: FeedbackBannerProps) {
    return (
        <div
            className={[
                'rounded-[24px] border px-5 py-4 text-sm font-semibold shadow-sm backdrop-blur-xl',
                toneClassNames[tone],
                className,
            ].join(' ')}
        >
            {children}
        </div>
    )
}

type LoadingGridProps = {
    count?: number
    cardClassName?: string
}

export function LoadingGrid({
                                count = 8,
                                cardClassName = 'h-[430px]',
                            }: LoadingGridProps) {
    return (
        <div className="subject-grid">
            {Array.from({ length: count }, (_, index) => (
                <div
                    key={index}
                    className={[
                        'animate-pulse rounded-[24px] border border-white/70 bg-white/50 shadow-sm',
                        cardClassName,
                    ].join(' ')}
                />
            ))}
        </div>
    )
}

type ErrorStateProps = {
    title: string
    message: string
    retryLabel?: string
    onRetry?: () => void
}

export function ErrorState({
                               title,
                               message,
                               retryLabel = '重新加载',
                               onRetry,
                           }: ErrorStateProps) {
    return (
        <div className="rounded-[28px] border border-rose-100 bg-white/58 p-10 text-center">
            <p className="text-base font-semibold text-rose-600">{title}</p>
            <p className="mt-2 text-sm leading-6 text-slate-400">{message}</p>

            {onRetry ? (
                <button
                    type="button"
                    onClick={onRetry}
                    className="mt-5 rounded-2xl bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white transition hover:-translate-y-0.5 hover:bg-slate-700"
                >
                    {retryLabel}
                </button>
            ) : null}
        </div>
    )
}

type EmptyStateProps = {
    title: string
    description?: string
    actionLabel?: string
    onAction?: () => void
}

export function EmptyState({
                               title,
                               description,
                               actionLabel,
                               onAction,
                           }: EmptyStateProps) {
    return (
        <div className="rounded-[28px] border border-dashed border-slate-200 bg-white/50 p-12 text-center">
            <p className="text-base font-semibold text-slate-500">{title}</p>

            {description ? (
                <p className="mt-2 text-sm leading-6 text-slate-400">{description}</p>
            ) : null}

            {actionLabel && onAction ? (
                <button
                    type="button"
                    onClick={onAction}
                    className="mt-5 rounded-2xl border border-white/80 bg-white/75 px-5 py-2.5 text-sm font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-white"
                >
                    {actionLabel}
                </button>
            ) : null}
        </div>
    )
}
