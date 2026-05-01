import type {
    UserSubjectCollectionBatchImportRequest,
    UserSubjectCollectionImportPreviewResponse,
    UserSubjectCollectionImportResponse,
} from '../../api/collectionApi'
import type {
    SubjectMatchCandidateResponse,
    UserSubjectRecordBatchImportRequest,
    UserSubjectRecordImportPreviewResponse,
    UserSubjectRecordImportResponse,
} from '../../api/recordApi'
import type { SubjectImportPreviewResponse, SubjectImportResponse } from '../../api/subjectApi'
import type { BangumiSubjectBatchImportRequest } from '../../api/importApi'

export type ImportTarget = 'SUBJECT' | 'COLLECTION' | 'RECORD'

export type ImportMode = 'JSON' | 'CSV' | 'EXCEL' | 'BANGUMI' | 'BACKUP' | 'MANUAL'

export type ParsedPayload =
    | BangumiSubjectBatchImportRequest
    | UserSubjectCollectionBatchImportRequest
    | UserSubjectRecordBatchImportRequest

export type PreviewState =
    | {
          target: 'SUBJECT'
          data: SubjectImportPreviewResponse
      }
    | {
          target: 'RECORD'
          data: UserSubjectRecordImportPreviewResponse
      }
    | {
          target: 'COLLECTION'
          data: UserSubjectCollectionImportPreviewResponse
      }

export type ImportResultState =
    | {
          target: 'SUBJECT'
          data: SubjectImportResponse
      }
    | {
          target: 'RECORD'
          data: UserSubjectRecordImportResponse
      }
    | {
          target: 'COLLECTION'
          data: UserSubjectCollectionImportResponse
      }

/** 与后端 import 预览/结果 item.result 对齐 */
export type ImportPreviewResultCode =
    | 'CAN_IMPORT'
    | 'IMPORTED'
    | 'UPDATED'
    | 'SKIPPED'
    | 'ERROR'
    | 'AMBIGUOUS'

export type PreviewItem = {
    title: string
    result: ImportPreviewResultCode | string
    message: string
    candidates?: SubjectMatchCandidateResponse[]
    sourcePlatform?: string | null
    sourceId?: string | null
    changeSummary?: string | null
}

export type BangumiPreviewProgress = {
    active: boolean
    percent: number
    message: string
}

export type ImportItemFilter = 'ALL' | 'SUCCESS' | 'WARNING' | 'ERROR'
