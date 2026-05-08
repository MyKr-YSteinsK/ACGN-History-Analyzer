export type BackgroundJobSettledStatus =
    | 'SUCCESS'
    | 'SUCCEEDED'
    | 'FAILED'
    | 'CANCELLED'

export type BackgroundJobSettledDetail = {
    jobId: string
    status: BackgroundJobSettledStatus | string
    title: string
    href?: string
    errorMessage?: string | null
}

export type BackgroundJobSettledInput = Omit<BackgroundJobSettledDetail, 'jobId'> & {
    jobId?: string
}

type BackgroundJobSettledListener = (detail: BackgroundJobSettledDetail) => void

const listeners = new Set<BackgroundJobSettledListener>()
const pendingDetails: BackgroundJobSettledDetail[] = []

function createToastJobId() {
    return `toast-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

function normalizeDetail(detail: BackgroundJobSettledInput): BackgroundJobSettledDetail {
    return {
        jobId: detail.jobId ?? createToastJobId(),
        status: String(detail.status ?? '').trim().toUpperCase(),
        title: detail.title,
        href: detail.href,
        errorMessage: detail.errorMessage ?? null,
    }
}

export function isBackgroundJobSuccess(status: unknown) {
    const normalized = String(status ?? '').trim().toUpperCase()
    return normalized === 'SUCCESS' || normalized === 'SUCCEEDED'
}

export function emitBackgroundJobSettledToast(detail: BackgroundJobSettledInput) {
    const normalizedDetail = normalizeDetail(detail)

    if (listeners.size === 0) {
        pendingDetails.push(normalizedDetail)
        return
    }

    listeners.forEach((listener) => listener(normalizedDetail))
}

export function subscribeBackgroundJobSettled(listener: BackgroundJobSettledListener) {
    listeners.add(listener)

    while (pendingDetails.length > 0) {
        const detail = pendingDetails.shift()
        if (detail) {
            listener(detail)
        }
    }

    return () => {
        listeners.delete(listener)
    }
}
