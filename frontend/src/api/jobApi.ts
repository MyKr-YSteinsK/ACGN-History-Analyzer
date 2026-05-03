import { apiGet, apiPost } from './request'

export type BackgroundJobType =
    | 'BANGUMI_SUBJECT_PREVIEW'
    | 'BANGUMI_SUBJECT_IMPORT'
    | 'COLLECTION_IMPORT_PREVIEW'
    | 'COLLECTION_IMPORT'
    | 'RECORD_IMPORT_PREVIEW'
    | 'RECORD_IMPORT'
    | 'LLM_ANALYSIS'

export type BackgroundJobStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED'

export type BackgroundJobResponse = {
    id: string
    jobType: string
    status: BackgroundJobStatus
    progress: number
    message: string
    errorMessage: string | null
    resultJson: string | null
    createdAt: string
    updatedAt: string
}

export type BackgroundJobStartResponse = {
    jobId: string
}

export type BackgroundJobUiMeta = {
    /** When false, a global toast is shown on settle (via BackgroundJobsProvider). */
    silent?: boolean
    title: string
    href?: string
}

const inflightWaits = new Map<string, Promise<BackgroundJobResponse>>()
const uiMetaByJobId = new Map<string, BackgroundJobUiMeta>()

const POLL_MS = 1500

function sleep(ms: number) {
    return new Promise<void>((resolve) => {
        window.setTimeout(resolve, ms)
    })
}

export function registerBackgroundJobUi(jobId: string, meta: BackgroundJobUiMeta) {
    uiMetaByJobId.set(jobId, meta)
}

export async function enqueueBackgroundJob(body: {
    jobType: BackgroundJobType
    payload: Record<string, unknown>
}): Promise<BackgroundJobStartResponse> {
    return apiPost<BackgroundJobStartResponse, typeof body>('/jobs', body)
}

export async function getBackgroundJob(jobId: string): Promise<BackgroundJobResponse> {
    return apiGet<BackgroundJobResponse>(`/jobs/${jobId}`)
}

function dispatchJobSettled(
    jobId: string,
    status: BackgroundJobStatus,
    errorMessage: string | null,
) {
    const meta = uiMetaByJobId.get(jobId)
    uiMetaByJobId.delete(jobId)

    if (!meta || meta.silent) {
        return
    }

    window.dispatchEvent(
        new CustomEvent('acgn-background-job-settled', {
            detail: {
                jobId,
                status,
                title: meta.title,
                href: meta.href,
                errorMessage,
            },
        }),
    )
}

async function pollUntilTerminal(jobId: string): Promise<BackgroundJobResponse> {
    for (;;) {
        const snapshot = await getBackgroundJob(jobId)
        if (snapshot.status === 'SUCCEEDED' || snapshot.status === 'FAILED') {
            dispatchJobSettled(jobId, snapshot.status, snapshot.errorMessage)
            return snapshot
        }
        await sleep(POLL_MS)
    }
}

/**
 * Shared wait for a job: concurrent callers await the same in-flight poll loop.
 * Call {@link registerBackgroundJobUi} after enqueue so completion can surface as a toast.
 */
export function waitForBackgroundJob(jobId: string): Promise<BackgroundJobResponse> {
    let promise = inflightWaits.get(jobId)
    if (!promise) {
        promise = pollUntilTerminal(jobId).finally(() => {
            inflightWaits.delete(jobId)
        })
        inflightWaits.set(jobId, promise)
    }
    return promise
}
