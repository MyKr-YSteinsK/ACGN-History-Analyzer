import {
    formatReleaseYearQuarter,
    type ReleaseQuarter,
} from '../types/common'

type ScoreRecordLike = {
    id: number
    scoreValue?: number | null
}

type SubjectReleaseLike = {
    subjectReleaseYear?: number | null
    subjectReleaseQuarter?: ReleaseQuarter | null
}

export function sortRecordsByScoreDesc<T extends ScoreRecordLike>(records: T[]) {
    return [...records].sort((first, second) => {
        const scoreCompare = (second.scoreValue ?? -1) - (first.scoreValue ?? -1)

        if (scoreCompare !== 0) {
            return scoreCompare
        }

        return second.id - first.id
    })
}

export function formatRecordRelease(record: SubjectReleaseLike) {
    return formatReleaseYearQuarter(
        record.subjectReleaseYear,
        record.subjectReleaseQuarter,
    )
}
