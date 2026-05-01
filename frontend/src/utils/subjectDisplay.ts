import type { SubjectCardView } from '../types/subject'

/** 季数 / Part 展示文案（与详情、记录弹窗一致） */
export function buildSeasonPartDisplay(subject: SubjectCardView): string {
    const parts: string[] = []

    if (subject.seasonIndex) {
        parts.push(`第 ${subject.seasonIndex} 季`)
    }

    if (subject.partIndex) {
        parts.push(`Part ${subject.partIndex}`)
    }

    return parts.length > 0 ? parts.join(' · ') : '无季数 / Part 信息'
}
