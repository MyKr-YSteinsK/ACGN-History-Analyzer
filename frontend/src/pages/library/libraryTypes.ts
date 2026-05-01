import type { PageResponse } from '../../types/api'
import type { SubjectCardView } from '../../types/subject'

export type SubjectPageState = Pick<
    PageResponse<unknown>,
    'page' | 'size' | 'totalElements' | 'totalPages' | 'first' | 'last'
>

export type StatusTarget = 'WANT' | 'PAUSED' | 'NONE'

export type PendingStatusChange = {
    subject: SubjectCardView
    target: StatusTarget
}
