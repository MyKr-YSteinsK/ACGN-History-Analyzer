import {
    getBangumiSeasonImportJobStatus,
    type BangumiImportJobStatusResponse,
} from '../api/importApi'
import { emitBackgroundJobSettledToast } from './backgroundJobEvents'

type BangumiSeasonJobUiMeta = {
    silent?: boolean
    title: string
    href?: string
}

type BangumiSeasonJobListener = (snapshot: BangumiImportJobStatusResponse) => void

const POLL_MS = 1000
const MAX_ATTEMPTS = 300

const inflightWaits = new Map<string, Promise<BangumiImportJobStatusResponse>>()
const uiMetaByJobId = new Map<string, BangumiSeasonJobUiMeta>()
const listenersByJobId = new Map<string, Set<BangumiSeasonJobListener>>()
const settledJobIds = new Set<string>()

function sleep(ms: number) {
    return new Promise<void>((resolve) => {
        window.setTimeout(resolve, ms)
    })
}

function isTerminal(snapshot: BangumiImportJobStatusResponse) {
    return snapshot.status === 'SUCCESS' || snapshot.status === 'FAILED'
}

function notify(jobId: string, snapshot: BangumiImportJobStatusResponse) {
    listenersByJobId.get(jobId)?.forEach((listener) => listener(snapshot))
}

function dispatchSettledToast(jobId: string, snapshot: BangumiImportJobStatusResponse) {
    if (settledJobIds.has(jobId)) {
        uiMetaByJobId.delete(jobId)
        return
    }

    settledJobIds.add(jobId)
    const meta = uiMetaByJobId.get(jobId)
    uiMetaByJobId.delete(jobId)

    if (!meta || meta.silent) {
        return
    }

    const title =
        snapshot.status === 'SUCCESS' && snapshot.result
            ? `已从 Bangumi 写入 ${snapshot.result.importedCount} 条作品`
            : meta.title

    emitBackgroundJobSettledToast({
        jobId,
        status: snapshot.status === 'SUCCESS' ? 'SUCCEEDED' : 'FAILED',
        title,
        href: meta.href,
        errorMessage: snapshot.errorMessage,
    })
}

async function pollUntilTerminal(jobId: string): Promise<BangumiImportJobStatusResponse> {
    for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
        const snapshot = await getBangumiSeasonImportJobStatus(jobId)
        notify(jobId, snapshot)

        if (isTerminal(snapshot)) {
            dispatchSettledToast(jobId, snapshot)
            return snapshot
        }

        await sleep(POLL_MS)
    }

    const timeoutSnapshot: BangumiImportJobStatusResponse = {
        jobId,
        status: 'FAILED',
        stage: 'TIMEOUT',
        message: 'Bangumi 导入任务等待超时',
        percent: 100,
        result: null,
        errorMessage: '任务等待超时，请稍后刷新作品库确认结果',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
    }
    notify(jobId, timeoutSnapshot)
    dispatchSettledToast(jobId, timeoutSnapshot)
    return timeoutSnapshot
}

export function registerBangumiSeasonImportJob(jobId: string, meta: BangumiSeasonJobUiMeta) {
    uiMetaByJobId.set(jobId, meta)
    void waitForBangumiSeasonImportJob(jobId)
}

export function waitForBangumiSeasonImportJob(jobId: string) {
    let promise = inflightWaits.get(jobId)
    if (!promise) {
        promise = pollUntilTerminal(jobId).finally(() => {
            inflightWaits.delete(jobId)
        })
        inflightWaits.set(jobId, promise)
    }
    return promise
}

export function subscribeBangumiSeasonImportJob(
    jobId: string,
    listener: BangumiSeasonJobListener,
) {
    const listeners = listenersByJobId.get(jobId) ?? new Set<BangumiSeasonJobListener>()
    listeners.add(listener)
    listenersByJobId.set(jobId, listeners)
    void waitForBangumiSeasonImportJob(jobId)

    return () => {
        listeners.delete(listener)
        if (listeners.size === 0) {
            listenersByJobId.delete(jobId)
        }
    }
}
