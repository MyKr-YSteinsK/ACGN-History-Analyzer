import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { getRecordPage, type UserSubjectRecordResponse } from '../api/recordApi'
import { getApiErrorMessage } from '../api/request'
import { getSubjectById } from '../api/subjectApi'
import CategorySwitch from '../components/common/CategorySwitch'
import CoverImage from '../components/common/CoverImage'
import {
    EmptyState,
    FeedbackBanner,
    LoadingGrid,
} from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { mapSubjectDetailToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    personalScoreTheme,
    yearChipClassName,
} from '../theme/acgnTheme'
import type { AcgnCategory } from '../types/common'
import type { SubjectCardView } from '../types/subject'
import { formatRecordRelease, sortRecordsByScoreDesc } from '../utils/recordDisplay'
import { queryKeys } from '../query/queryKeys'
import { usePersistentState } from '../hooks/usePersistentState'

const rankingFetchSize = 200

type ScoreBand = {
    key: string
    label: string
    minScore: number
    maxScore: number
    count: number
    className: string
}

const scoreBandConfig: Array<Omit<ScoreBand, 'count'>> = [
    {
        key: 'masterpiece',
        label: '47-50',
        minScore: 47,
        maxScore: 50,
        className: 'border-rose-200 bg-rose-50/90 text-rose-700',
    },
    {
        key: 'excellent',
        label: '40-46',
        minScore: 40,
        maxScore: 46,
        className: 'border-amber-200 bg-amber-50/90 text-amber-700',
    },
    {
        key: 'normal',
        label: '30-39',
        minScore: 30,
        maxScore: 39,
        className: 'border-sky-200 bg-sky-50/90 text-sky-700',
    },
    {
        key: 'low',
        label: '0-29',
        minScore: 0,
        maxScore: 29,
        className: 'border-slate-200 bg-slate-50/90 text-slate-600',
    },
]

function applyTopRule(records: UserSubjectRecordResponse[]) {
    const sortedRecords = sortRecordsByScoreDesc(
        records.filter((record) => record.scoreValue !== null),
    )
    const tenthScore = sortedRecords[9]?.scoreValue ?? null
    const recordsAbove48 = sortedRecords.filter((record) => (record.scoreValue ?? 0) >= 48)
    const recordsAbove47 = sortedRecords.filter((record) => (record.scoreValue ?? 0) >= 47)

    if (tenthScore !== null && tenthScore >= 48) {
        return recordsAbove48
    }

    if (recordsAbove47.length < 10) {
        return sortedRecords.slice(0, 10)
    }

    return recordsAbove47
}

function TopPage() {
    const [selectedCategory, setSelectedCategory] = usePersistentState<AcgnCategory>(
        'acgn.top.selectedCategory',
        'ANIME',
    )
    const [detailLoading, setDetailLoading] = useState(false)
    const [detailError, setDetailError] = useState<string | null>(null)
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)

    const topQuery = useQuery({
        queryKey: queryKeys.topPage(selectedCategory),
        queryFn: async () => {
            const [rankingResponse, ...bandResponses] = await Promise.all([
                getRecordPage({
                    category: selectedCategory,
                    page: 0,
                    size: rankingFetchSize,
                    sort: 'SCORE_DESC',
                }),
                ...scoreBandConfig.map((band) =>
                    getRecordPage({
                        category: selectedCategory,
                        minScore: band.minScore,
                        maxScore: band.maxScore,
                        page: 0,
                        size: 1,
                        sort: 'SCORE_DESC',
                    }),
                ),
            ])

            const bundle: import('../query/queryKeys').TopPageBundle = {
                rankingRecords: sortRecordsByScoreDesc(rankingResponse.content),
                scoreBands: scoreBandConfig.map((band, index) => ({
                    ...band,
                    count: bandResponses[index]?.totalElements ?? 0,
                })),
            }
            return bundle
        },
        placeholderData: (previousData) => previousData,
    })

    const rankingRecords = useMemo(
        () => topQuery.data?.rankingRecords ?? [],
        [topQuery.data],
    )
    const scoreBands = useMemo(
        () => topQuery.data?.scoreBands ?? scoreBandConfig.map((band) => ({ ...band, count: 0 })),
        [topQuery.data],
    )
    const loading = topQuery.isPending && !topQuery.data
    const rankingLoadError = topQuery.isError ? getApiErrorMessage(topQuery.error) : null

    const selectedTheme = categoryTheme[selectedCategory]
    const displayRecords = useMemo(() => applyTopRule(rankingRecords), [rankingRecords])
    const featuredRecords = displayRecords.slice(0, 3)
    const resultAnimationKey = `${selectedCategory}-${displayRecords.length}`
    const bannerError = detailError ?? rankingLoadError

    async function handleOpenDetail(record: UserSubjectRecordResponse) {
        setDetailLoading(true)
        setDetailError(null)

        try {
            const detail = await getSubjectById(record.subjectId)
            setSelectedSubject(mapSubjectDetailToCardView(detail))
        } catch (openError) {
            setDetailError(getApiErrorMessage(openError))
        } finally {
            setDetailLoading(false)
        }
    }

    return (
        <>
            <div className="space-y-6 content-enter">
                <PageHero
                    eyebrow="TOP RANKING"
                    title="TOP 榜单"
                    description="按 ACGN 类型展示个人高分作品和分数段统计，同分作品按并列感处理。"
                />

                <CategorySwitch
                    value={selectedCategory}
                    onChange={setSelectedCategory}
                />

                {bannerError ? (
                    <FeedbackBanner tone="error">{bannerError}</FeedbackBanner>
                ) : null}

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="grid gap-5 xl:grid-cols-[1.3fr_0.7fr]">
                            <section>
                                <div className="mb-4 flex items-end justify-between gap-4">
                                    <div>
                                        <h3 className="text-lg font-black text-slate-800">
                                            {selectedTheme.cnName}高分精选
                                        </h3>
                                        <p className="mt-1 text-sm text-slate-400">
                                            按评分顺序展示，不显式标注名次。
                                        </p>
                                    </div>

                                    <span className="rounded-full border border-white/70 bg-white/62 px-4 py-2 text-xs font-semibold text-slate-400">
                                        个人评分 / 50
                                    </span>
                                </div>

                                {loading ? (
                                    <LoadingGrid count={3} cardClassName="h-[330px]" />
                                ) : featuredRecords.length > 0 ? (
                                    <div key={resultAnimationKey} className="grid gap-4 md:grid-cols-3 content-enter">
                                        {featuredRecords.map((record) => (
                                            <TopRecordCard
                                                key={record.id}
                                                record={record}
                                                category={selectedCategory}
                                                onOpenDetail={handleOpenDetail}
                                            />
                                        ))}
                                    </div>
                                ) : (
                                    <EmptyState title="暂无高分记录" />
                                )}
                            </section>

                            <section className="rounded-[28px] border border-white/70 bg-white/50 p-5 shadow-sm backdrop-blur">
                                <h3 className="text-lg font-black text-slate-800">
                                    分数段统计
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    仅统计当前类型的正式记录。
                                </p>

                                <div className="mt-5 grid gap-3">
                                    {scoreBands.map((band) => (
                                        <ScoreBandCard
                                            key={band.key}
                                            band={band}
                                            totalCount={Math.max(1, rankingRecords.length)}
                                        />
                                    ))}
                                </div>
                            </section>
                        </div>
                    </div>
                </GlassCard>

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="mb-5 flex flex-wrap items-end justify-between gap-4">
                            <div>
                                <h3 className="text-lg font-black text-slate-800">
                                    高分作品
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    按评分从高到低排列；同分作品不单独标注排名。
                                </p>
                            </div>
                        </div>

                        {loading ? (
                            <LoadingGrid />
                        ) : displayRecords.length > 0 ? (
                            <div className="subject-grid content-enter">
                                {displayRecords.map((record) => (
                                    <TopRecordCard
                                        key={record.id}
                                        record={record}
                                        category={selectedCategory}
                                        onOpenDetail={handleOpenDetail}
                                    />
                                ))}
                            </div>
                        ) : (
                            <EmptyState
                                title="没有可展示的榜单记录"
                                description="先在作品库或我的记录中添加正式记录和个人评分。"
                            />
                        )}
                    </div>
                </GlassCard>
            </div>

            <SubjectDetailDrawer
                open={selectedSubject !== null}
                subject={selectedSubject}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={() => undefined}
                onAddToWant={() => undefined}
                onAddToPaused={() => undefined}
                onSetToNone={() => undefined}
                detailLoading={detailLoading}
                showActions={false}
            />
        </>
    )
}

type TopRecordCardProps = {
    record: UserSubjectRecordResponse
    category: AcgnCategory
    onOpenDetail: (record: UserSubjectRecordResponse) => void
}

function TopRecordCard({ record, category, onOpenDetail }: TopRecordCardProps) {
    const theme = categoryTheme[record.subjectCategory ?? category]
    return (
        <button
            type="button"
            onClick={() => onOpenDetail(record)}
            className={[
                'group overflow-hidden rounded-[26px] border border-white/70 bg-gradient-to-br text-left shadow-sm transition-all duration-200',
                'hover:-translate-y-1 hover:shadow-[0_18px_48px_rgba(120,135,160,0.16)]',
                theme.softGradient,
            ].join(' ')}
        >
            <div className="relative aspect-[3/4] overflow-hidden bg-slate-100">
                <CoverImage
                    src={record.subjectCoverUrl}
                    alt={record.subjectTitle}
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.04]"
                    fallback={
                    <div
                        className={[
                            'flex h-full w-full items-center justify-center bg-gradient-to-br',
                            theme.softGradient,
                        ].join(' ')}
                    >
                        <span className={['text-6xl font-black', theme.text].join(' ')}>
                            {theme.letter}
                        </span>
                    </div>
                    }
                />

                <span
                    className={[
                        'absolute right-3 top-3 rounded-2xl border px-3 py-1 text-sm font-black',
                        personalScoreTheme.className,
                    ].join(' ')}
                >
                    {record.scoreValue}
                </span>
            </div>

            <div className="m-2.5 rounded-[20px] border border-white/65 bg-white/58 p-3.5 shadow-sm backdrop-blur">
                <h3 className="line-clamp-1 text-[14px] font-bold text-slate-800">
                    {record.subjectTitle}
                </h3>

                <div className="mt-3 flex flex-wrap gap-1.5">
                    <span
                        className={[
                            'rounded-full px-2.5 py-0.5 text-[11px] font-semibold',
                            theme.bg,
                            theme.text,
                        ].join(' ')}
                    >
                        {theme.cnName}
                    </span>

                    <span
                        className={[
                            'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold',
                            yearChipClassName,
                        ].join(' ')}
                    >
                        {formatRecordRelease(record)}
                    </span>

                </div>

                <p className="mt-3 min-h-[40px] line-clamp-2 text-xs leading-5 text-slate-400">
                    {record.note ?? '暂无评价备注'}
                </p>
            </div>
        </button>
    )
}

type ScoreBandCardProps = {
    band: ScoreBand
    totalCount: number
}

function ScoreBandCard({ band, totalCount }: ScoreBandCardProps) {
    const percent = Math.min(100, Math.round((band.count / totalCount) * 100))

    return (
        <div className="rounded-[22px] border border-white/70 bg-white/58 p-4">
            <div className="flex items-center justify-between gap-3">
                <span
                    className={[
                        'rounded-full border px-3 py-1 text-xs font-black',
                        band.className,
                    ].join(' ')}
                >
                    {band.label}
                </span>
                <span className="text-xl font-black text-slate-800">{band.count}</span>
            </div>

            <div className="mt-3 h-2 overflow-hidden rounded-full bg-white/75">
                <div
                    className="h-full rounded-full bg-slate-800/75 transition-all duration-300"
                    style={{ width: `${percent}%` }}
                />
            </div>
        </div>
    )
}

export default TopPage
