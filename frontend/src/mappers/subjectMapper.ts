import type { SubjectDetailResponse, SubjectResponse } from '../api/subjectApi'
import type { SubjectCardView } from '../types/subject'

function splitTags(tags?: string | null) {
    if (!tags) {
        return []
    }

    return tags
        .split(/[,，]/)
        .map((tag) => tag.trim())
        .filter(Boolean)
}

export function mapSubjectResponseToCardView(
    subject: SubjectResponse,
): SubjectCardView {
    return {
        id: subject.id,
        displayTitle: subject.displayTitle,
        titleCn: subject.titleCn,
        subtitle: subject.subtitle,
        category: subject.category,
        releaseYear: subject.releaseYear,
        releaseQuarter: subject.releaseQuarter,
        seasonIndex: subject.seasonIndex,
        partIndex: subject.partIndex,
        coverUrl: subject.coverUrl,
        studioName: subject.studioName,
        platformScore: subject.platformScore,
        personalScore: null,
        personalRecordYear: null,
        personalRecordQuarter: null,
        personalRecordNote: null,
        episodeCount: subject.episodeCount,
        tags: splitTags(subject.tags),
        libraryStatus: subject.libraryStatus,
        libraryStatusLabel: subject.libraryStatusLabel,
        summary: subject.summary,
        platformLink: subject.platformLink,
        sourcePlatform: subject.sourcePlatform,
        sourceId: subject.sourceId,
        franchiseName: null,
        createdAt: subject.createdAt,
        updatedAt: subject.updatedAt,
    }
}

export function mapSubjectDetailToCardView(
    subject: SubjectDetailResponse,
): SubjectCardView {
    const record = subject.record
    const collection = subject.collection
    const franchiseName = subject.franchise?.nameCn || subject.franchise?.name || null

    return {
        ...mapSubjectResponseToCardView(subject),
        recordId: record?.id ?? null,
        collectionId: collection?.id ?? null,
        personalScore: record?.scoreValue ?? null,
        personalRecordYear: record?.recordYear ?? null,
        personalRecordQuarter: record?.recordQuarter ?? null,
        personalRecordNote: record?.note ?? null,
        franchiseName,
    }
}
