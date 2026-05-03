import { apiGet } from './request'

export type BackupExportResponse = {
    recordJsonText: string
    subjectJsonText: string | null
    usageHint: string
}

export function getBangumiRoundtripExport() {
    return apiGet<BackupExportResponse>('/export/bangumi-roundtrip')
}
