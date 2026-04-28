import type { AcgnCategory, LibraryStatus, ReleaseQuarter } from './common'

export type SubjectCardView = {
    id: number
    recordId?: number | null
    collectionId?: number | null
    displayTitle: string
    titleCn: string
    subtitle?: string | null
    category: AcgnCategory
    releaseYear?: number | null
    releaseQuarter?: ReleaseQuarter | null
    seasonIndex?: number | null
    partIndex?: number | null
    coverUrl?: string | null
    studioName?: string | null

    // 平台评分，例如 Bangumi 评分，通常是 0–10
    platformScore?: number | null

    // 个人评分，当前项目规则是 0–50
    personalScore?: number | null
    personalRecordYear?: number | null
    personalRecordQuarter?: ReleaseQuarter | null
    personalRecordNote?: string | null

    episodeCount?: number | null
    tags: string[]
    libraryStatus: LibraryStatus
    libraryStatusLabel?: string | null

    summary?: string | null
    platformLink?: string | null
    sourcePlatform?: string | null
    sourceId?: string | null
    franchiseName?: string | null
    createdAt?: string | null
    updatedAt?: string | null
}
