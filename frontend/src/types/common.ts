export type AcgnCategory = 'ANIME' | 'COMIC' | 'GAME' | 'NOVEL'

export type ReleaseQuarter = 1 | 2 | 3 | 4

export type LibraryStatus = 'WATCHED' | 'WANT' | 'PAUSED' | 'NONE'

export type CollectionType = 'WANT' | 'PAUSED'

export const categoryOrder: AcgnCategory[] = ['ANIME', 'COMIC', 'GAME', 'NOVEL']

export const releaseQuarters: ReleaseQuarter[] = [1, 2, 3, 4]

export const releaseQuarterOptions: Array<{
    value: ReleaseQuarter
    label: string
    monthLabel: string
    shortLabel: string
    displayLabel: string
}> = [
    {
        value: 1,
        label: '一月',
        monthLabel: '一月',
        shortLabel: '一月',
        displayLabel: '一月',
    },
    {
        value: 2,
        label: '四月',
        monthLabel: '四月',
        shortLabel: '四月',
        displayLabel: '四月',
    },
    {
        value: 3,
        label: '七月',
        monthLabel: '七月',
        shortLabel: '七月',
        displayLabel: '七月',
    },
    {
        value: 4,
        label: '十月',
        monthLabel: '十月',
        shortLabel: '十月',
        displayLabel: '十月',
    },
]

export function getReleaseQuarterLabel(quarter?: ReleaseQuarter | null) {
    if (!quarter) {
        return '未知'
    }

    return releaseQuarterOptions.find((item) => item.value === quarter)?.displayLabel ?? '未知'
}

export function getCurrentReleaseQuarter(): ReleaseQuarter {
    const month = new Date().getMonth() + 1

    if (month <= 3) {
        return 1
    }

    if (month <= 6) {
        return 2
    }

    if (month <= 9) {
        return 3
    }

    return 4
}

export function formatReleaseYearQuarter(
    year?: number | null,
    quarter?: ReleaseQuarter | null,
    fallback = '发行未知',
) {
    if (!year) {
        return fallback
    }

    const quarterLabel = quarter ? getReleaseQuarterLabel(quarter) : ''

    return quarterLabel ? `${year}${quarterLabel}` : String(year)
}
