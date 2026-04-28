import { apiGet } from './request'
import type { SubjectResponse } from './subjectApi'
import type {
    RecordQuarterOverviewResponse,
    UserSubjectRecordResponse,
} from './recordApi'
import type { AcgnCategory, ReleaseQuarter } from '../types/common'

export type HomeRecordSort = 'TITLE_ASC' | 'TITLE_DESC' | 'SCORE_ASC' | 'SCORE_DESC'

export type HomeLibrarySort =
    | 'TITLE_ASC'
    | 'TITLE_DESC'
    | 'RELEASE_NEWEST'
    | 'RELEASE_OLDEST'

export type HomeSearchScope = 'RECORDED' | 'LIBRARY'

export type HomeQuarterDashboardResponse = {
    year: number
    quarter: ReleaseQuarter
    category: AcgnCategory
    keyword: string | null
    recordSort: HomeRecordSort
    librarySort: HomeLibrarySort
    baseQuarterOverview: RecordQuarterOverviewResponse
    viewQuarterOverview: RecordQuarterOverviewResponse
    recordList: UserSubjectRecordResponse[]
    highScoreRecordList: UserSubjectRecordResponse[]
    subjectLibraryList: SubjectResponse[]
}

export type HomeSearchResponse = {
    year: number
    quarter: ReleaseQuarter
    category: AcgnCategory
    keyword: string
    scope: HomeSearchScope
    recordedResults: UserSubjectRecordResponse[]
    libraryResults: SubjectResponse[]
}

export type HomeQuarterDashboardParams = {
    year: number
    quarter: ReleaseQuarter
    category?: AcgnCategory
    status?: string
    keyword?: string
    recordSort?: HomeRecordSort
    librarySort?: HomeLibrarySort
}

export type HomeSearchParams = {
    year: number
    quarter: ReleaseQuarter
    category?: AcgnCategory
    keyword: string
    scope?: HomeSearchScope
}

export function getHomeQuarterDashboard(params: HomeQuarterDashboardParams) {
    return apiGet<HomeQuarterDashboardResponse>('/home/quarter-dashboard', params)
}

export function searchHomeData(params: HomeSearchParams) {
    return apiGet<HomeSearchResponse>('/home/search', params)
}
