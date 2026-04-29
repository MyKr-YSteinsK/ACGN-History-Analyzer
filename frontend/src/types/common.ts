export type AcgnCategory = 'ANIME' | 'COMIC' | 'GAME' | 'NOVEL'

export type ReleaseQuarter = 1 | 2 | 3 | 4

export type LibraryStatus = 'WATCHED' | 'WANT' | 'PAUSED' | 'NONE'

export type CollectionType = 'WANT' | 'PAUSED'

export const categoryOrder: AcgnCategory[] = ['ANIME', 'COMIC', 'GAME', 'NOVEL']

export const releaseQuarterOptions: Array<{
    value: ReleaseQuarter
    label: string
    monthLabel: string
    shortLabel: string
    displayLabel: string
}> = [
    {
        value: 1,
        label: '冬',
        monthLabel: '1月新番',
        shortLabel: '冬 · 1月',
        displayLabel: '冬 · 1月新番',
    },
    {
        value: 2,
        label: '春',
        monthLabel: '4月新番',
        shortLabel: '春 · 4月',
        displayLabel: '春 · 4月新番',
    },
    {
        value: 3,
        label: '夏',
        monthLabel: '7月新番',
        shortLabel: '夏 · 7月',
        displayLabel: '夏 · 7月新番',
    },
    {
        value: 4,
        label: '秋',
        monthLabel: '10月新番',
        shortLabel: '秋 · 10月',
        displayLabel: '秋 · 10月新番',
    },
]

export function getReleaseQuarterLabel(quarter?: ReleaseQuarter | null) {
    if (!quarter) {
        return '未知'
    }

    return releaseQuarterOptions.find((item) => item.value === quarter)?.displayLabel ?? '未知'
}
