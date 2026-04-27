export type AcgnCategory = 'ANIME' | 'COMIC' | 'GAME' | 'NOVEL'

export type ReleaseQuarter = 1 | 2 | 3 | 4

export type LibraryStatus = 'WATCHED' | 'WANT' | 'PAUSED' | 'NONE'

export type CollectionType = 'WANT' | 'PAUSED'

export const categoryOrder: AcgnCategory[] = ['ANIME', 'COMIC', 'GAME', 'NOVEL']

export const releaseQuarterOptions: Array<{
    value: ReleaseQuarter
    label: string
}> = [
    { value: 1, label: '春' },
    { value: 2, label: '夏' },
    { value: 3, label: '秋' },
    { value: 4, label: '冬' },
]

export function getReleaseQuarterLabel(quarter?: ReleaseQuarter | null) {
    if (!quarter) {
        return '未知'
    }

    return releaseQuarterOptions.find((item) => item.value === quarter)?.label ?? '未知'
}