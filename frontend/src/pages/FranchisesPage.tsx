import { useCallback, useEffect, useMemo, useState } from 'react'
import {
    deleteFranchise,
    getFranchiseById,
    getFranchises,
    removeSubjectFromFranchise,
    type FranchiseDetailResponse,
} from '../api/franchiseApi'
import { getApiErrorMessage } from '../api/request'
import { getSubjectById } from '../api/subjectApi'
import ConfirmDialog from '../components/common/ConfirmDialog'
import CoverImage from '../components/common/CoverImage'
import {
    EmptyState,
    ErrorState,
    FeedbackBanner,
} from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import StatusBadge from '../components/common/StatusBadge'
import PageHero from '../components/layout/PageHero'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { mapSubjectDetailToCardView, mapSubjectResponseToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    libraryStatusTheme,
    neutralPanelGradient,
    platformScoreTheme,
    yearChipClassName,
} from '../theme/acgnTheme'
import {
    categoryOrder,
    getReleaseQuarterLabel,
    type AcgnCategory,
    type LibraryStatus,
} from '../types/common'
import type { SubjectCardView } from '../types/subject'

type CategoryScope = AcgnCategory | 'ALL'

type FranchiseStats = {
    total: number
    watched: number
    want: number
    paused: number
    none: number
    platformAverage: number | null
}

type PendingRemoveSubject = {
    franchise: FranchiseDetailResponse
    subject: SubjectCardView
}

const categoryScopeOptions: Array<{
    value: CategoryScope
    label: string
    description: string
}> = [
    {
        value: 'ALL',
        label: '全部',
        description: '展示系列下的全部媒介',
    },
    ...categoryOrder.map((category) => ({
        value: category,
        label: categoryTheme[category].cnName,
        description: categoryTheme[category].enName,
    })),
]

const statusOrder: LibraryStatus[] = ['WATCHED', 'WANT', 'PAUSED', 'NONE']

function getFranchiseTitle(franchise: FranchiseDetailResponse) {
    return franchise.nameCn || franchise.name
}

function getFranchiseSubtitle(franchise: FranchiseDetailResponse) {
    if (franchise.nameCn && franchise.nameCn !== franchise.name) {
        return franchise.name
    }

    return franchise.description || '暂无系列简介'
}

function formatRelease(subject: SubjectCardView) {
    if (!subject.releaseYear) {
        return '未知时间'
    }

    const quarterLabel = subject.releaseQuarter
        ? getReleaseQuarterLabel(subject.releaseQuarter)
        : ''

    return quarterLabel
        ? `${subject.releaseYear}${quarterLabel}`
        : String(subject.releaseYear)
}

function formatSeasonPart(subject: SubjectCardView) {
    const parts: string[] = []

    if (subject.seasonIndex) {
        parts.push(`S${subject.seasonIndex}`)
    }

    if (subject.partIndex) {
        parts.push(`Part ${subject.partIndex}`)
    }

    return parts.join(' · ')
}

function calculateStats(subjects: SubjectCardView[]): FranchiseStats {
    const scoreList = subjects
        .map((subject) => subject.platformScore)
        .filter((score): score is number => typeof score === 'number')

    return {
        total: subjects.length,
        watched: subjects.filter((subject) => subject.libraryStatus === 'WATCHED').length,
        want: subjects.filter((subject) => subject.libraryStatus === 'WANT').length,
        paused: subjects.filter((subject) => subject.libraryStatus === 'PAUSED').length,
        none: subjects.filter((subject) => subject.libraryStatus === 'NONE').length,
        platformAverage:
            scoreList.length > 0
                ? scoreList.reduce((sum, score) => sum + score, 0) / scoreList.length
                : null,
    }
}

function sortSubjectsForTimeline(subjects: SubjectCardView[]) {
    return [...subjects].sort((left, right) => {
        const leftYear = left.releaseYear ?? 9999
        const rightYear = right.releaseYear ?? 9999

        if (leftYear !== rightYear) {
            return leftYear - rightYear
        }

        const leftQuarter = left.releaseQuarter ?? 9
        const rightQuarter = right.releaseQuarter ?? 9

        if (leftQuarter !== rightQuarter) {
            return leftQuarter - rightQuarter
        }

        const leftSeason = left.seasonIndex ?? 999
        const rightSeason = right.seasonIndex ?? 999

        if (leftSeason !== rightSeason) {
            return leftSeason - rightSeason
        }

        return (left.partIndex ?? 999) - (right.partIndex ?? 999)
    })
}

function FranchisesPage() {
    const [franchises, setFranchises] = useState<FranchiseDetailResponse[]>([])
    const [selectedFranchiseId, setSelectedFranchiseId] = useState<number | null>(null)
    const [selectedScope, setSelectedScope] = useState<CategoryScope>('ALL')
    const [keyword, setKeyword] = useState('')
    const [loading, setLoading] = useState(false)
    const [detailLoading, setDetailLoading] = useState(false)
    const [listError, setListError] = useState<string | null>(null)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [pendingDeleteFranchise, setPendingDeleteFranchise] =
        useState<FranchiseDetailResponse | null>(null)
    const [pendingRemoveSubject, setPendingRemoveSubject] =
        useState<PendingRemoveSubject | null>(null)
    const [deleteLoading, setDeleteLoading] = useState(false)

    const selectedFranchise = useMemo(
        () =>
            franchises.find((franchise) => franchise.id === selectedFranchiseId) ??
            franchises[0] ??
            null,
        [franchises, selectedFranchiseId],
    )

    const scopeTheme =
        selectedScope === 'ALL' ? null : categoryTheme[selectedScope]

    const filteredFranchises = useMemo(() => {
        const normalizedKeyword = keyword.trim().toLowerCase()

        return franchises.filter((franchise) => {
            const subjects = franchise.subjects.map(mapSubjectResponseToCardView)
            const matchesScope =
                selectedScope === 'ALL' ||
                subjects.some((subject) => subject.category === selectedScope)

            if (!matchesScope) {
                return false
            }

            if (!normalizedKeyword) {
                return true
            }

            const searchableText = [
                franchise.name,
                franchise.nameCn,
                franchise.description,
                ...subjects.flatMap((subject) => [
                    subject.displayTitle,
                    subject.titleCn,
                    subject.subtitle,
                    subject.studioName,
                ]),
            ]
                .filter(Boolean)
                .join(' ')
                .toLowerCase()

            return searchableText.includes(normalizedKeyword)
        })
    }, [franchises, keyword, selectedScope])

    const selectedSubjects = useMemo(() => {
        if (!selectedFranchise) {
            return []
        }

        const subjects = selectedFranchise.subjects.map(mapSubjectResponseToCardView)

        if (selectedScope === 'ALL') {
            return subjects
        }

        return subjects.filter((subject) => subject.category === selectedScope)
    }, [selectedFranchise, selectedScope])

    const sortedSubjects = useMemo(
        () => sortSubjectsForTimeline(selectedSubjects),
        [selectedSubjects],
    )

    const stats = useMemo(
        () => calculateStats(selectedSubjects),
        [selectedSubjects],
    )

    const categoryCounts = useMemo(() => {
        const subjects = selectedFranchise?.subjects.map(mapSubjectResponseToCardView) ?? []

        return categoryOrder.map((category) => ({
            category,
            count: subjects.filter((subject) => subject.category === category).length,
        }))
    }, [selectedFranchise])

    const loadFranchises = useCallback(async () => {
        setLoading(true)
        setListError(null)

        try {
            const list = await getFranchises()
            const details = await Promise.all(
                list.map((franchise) => getFranchiseById(franchise.id)),
            )

            setFranchises(details)
            setSelectedFranchiseId((currentId) =>
                details.some((franchise) => franchise.id === currentId)
                    ? currentId
                    : details[0]?.id ?? null,
            )
        } catch (error) {
            setFranchises([])
            setSelectedFranchiseId(null)
            setListError(getApiErrorMessage(error))
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        // 系列页进入时需要从后端拉取列表和详情，再把结果写入页面状态。
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadFranchises()
    }, [loadFranchises])

    async function handleOpenSubject(subject: SubjectCardView) {
        setDetailLoading(true)
        setFeedbackMessage(null)

        try {
            const detail = await getSubjectById(subject.id)
            setSelectedSubject(mapSubjectDetailToCardView(detail))
        } catch (error) {
            setListError(getApiErrorMessage(error))
        } finally {
            setDetailLoading(false)
        }
    }

    function handleSelectScope(scope: CategoryScope) {
        setSelectedScope(scope)
    }

    function handleSelectFranchise(franchiseId: number) {
        setSelectedFranchiseId(franchiseId)
    }

    function requestDeleteFranchise(franchise: FranchiseDetailResponse) {
        setPendingDeleteFranchise(franchise)
    }

    function requestRemoveSubject(franchise: FranchiseDetailResponse, subject: SubjectCardView) {
        setPendingRemoveSubject({ franchise, subject })
    }

    function closeDeleteDialog() {
        if (deleteLoading) {
            return
        }

        setPendingDeleteFranchise(null)
        setPendingRemoveSubject(null)
    }

    async function confirmDeleteFranchise() {
        if (!pendingDeleteFranchise) {
            return
        }

        const franchiseToDelete = pendingDeleteFranchise
        setDeleteLoading(true)
        setFeedbackMessage(null)

        try {
            await deleteFranchise(franchiseToDelete.id)
            setPendingDeleteFranchise(null)
            setSelectedSubject(null)
            setFeedbackMessage(`已删除系列《${getFranchiseTitle(franchiseToDelete)}》，作品已保留在作品库`)
            await loadFranchises()
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setDeleteLoading(false)
        }
    }

    async function confirmRemoveSubject() {
        if (!pendingRemoveSubject) {
            return
        }

        const pending = pendingRemoveSubject
        setDeleteLoading(true)
        setFeedbackMessage(null)

        try {
            await removeSubjectFromFranchise(pending.franchise.id, pending.subject.id)
            setPendingRemoveSubject(null)
            setSelectedSubject((current) =>
                current?.id === pending.subject.id ? null : current,
            )
            setFeedbackMessage(
                `已将《${pending.subject.displayTitle}》从《${getFranchiseTitle(pending.franchise)}》中移出`,
            )
            await loadFranchises()
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setDeleteLoading(false)
        }
    }

    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="FRANCHISES"
                title="系列管理"
                description="把多季、Part、外传和跨媒介作品串成一条线，快速查看系列完成度和历史脉络。"
            />

            {feedbackMessage ? (
                <FeedbackBanner>{feedbackMessage}</FeedbackBanner>
            ) : null}

            <section className="grid gap-5 xl:grid-cols-[minmax(300px,340px)_1fr] xl:items-start">
                <GlassCard className="overflow-hidden p-5 xl:sticky xl:top-6 xl:max-h-[calc(100vh-2rem)]">
                    <div>
                        <p className="text-xs font-semibold tracking-[0.22em] text-slate-400">
                            SERIES SEARCH
                        </p>
                        <input
                            value={keyword}
                            onChange={(event) => setKeyword(event.target.value)}
                            placeholder="搜索系列 / 作品 / 制作方"
                            className="mt-3 h-12 w-full rounded-[22px] border border-white/75 bg-white/70 px-4 text-sm font-medium text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:shadow-[0_0_0_4px_rgba(125,211,252,0.18)]"
                        />
                    </div>

                    <div>
                        <div className="flex items-center justify-between gap-3">
                            <p className="text-sm font-bold text-slate-700">类型范围</p>
                            <span className="rounded-full border border-white/70 bg-white/60 px-3 py-1 text-xs font-semibold text-slate-400">
                                {filteredFranchises.length} 个系列
                            </span>
                        </div>

                        <div className="mt-3 grid grid-cols-5 gap-2">
                            {categoryScopeOptions.map((option) => {
                                const active = selectedScope === option.value
                                const theme =
                                    option.value === 'ALL' ? null : categoryTheme[option.value]

                                return (
                                    <button
                                        key={option.value}
                                        type="button"
                                        onClick={() => handleSelectScope(option.value)}
                                        className={[
                                            'group min-h-[64px] rounded-[20px] border px-2 py-2 text-center transition-all duration-200',
                                            active
                                                ? theme
                                                    ? `${theme.border} bg-gradient-to-br ${theme.softGradient} shadow-[0_14px_32px_rgba(120,135,160,0.14)] ring-2 ${theme.ring}`
                                                    : 'border-slate-200 bg-white/85 text-slate-700 shadow-[0_14px_32px_rgba(120,135,160,0.12)] ring-2 ring-slate-200/70'
                                                : 'border-white/70 bg-white/48 text-slate-400 hover:-translate-y-0.5 hover:bg-white/75',
                                        ].join(' ')}
                                    >
                                        <span
                                            className={[
                                                'block text-base font-black leading-none',
                                                theme?.text ?? 'text-slate-600',
                                            ].join(' ')}
                                        >
                                            {option.value === 'ALL'
                                                ? 'ALL'
                                                : categoryTheme[option.value].letter}
                                        </span>
                                        <span className="mt-1 block text-[11px] font-semibold">
                                            {option.label}
                                        </span>
                                    </button>
                                )
                            })}
                        </div>
                    </div>

                    <div className="border-t border-white/65 pt-4">
                        <div className="mb-3 flex items-center justify-between gap-3">
                            <p className="text-sm font-bold text-slate-700">系列列表</p>
                            <span className="max-w-[180px] truncate rounded-full border border-white/70 bg-white/60 px-3 py-1 text-xs font-semibold text-slate-400">
                                {selectedFranchise ? getFranchiseTitle(selectedFranchise) : '未选择'}
                            </span>
                        </div>

                        <div className="space-y-3 xl:max-h-[calc(100vh-340px)] xl:overflow-y-auto xl:pr-1">
                        {loading ? (
                            <FeedbackBanner>正在加载系列数据...</FeedbackBanner>
                        ) : null}

                        {listError ? (
                            <FeedbackBanner tone="error">{listError}</FeedbackBanner>
                        ) : null}

                        {!loading && !listError && filteredFranchises.length === 0 ? (
                            <FeedbackBanner>暂时没有符合条件的系列。</FeedbackBanner>
                        ) : null}

                        {filteredFranchises.map((franchise) => {
                            const subjects = franchise.subjects.map(mapSubjectResponseToCardView)
                            const franchiseStats = calculateStats(subjects)
                            const active = selectedFranchise?.id === franchise.id
                            const title = getFranchiseTitle(franchise)
                            const subtitle = getFranchiseSubtitle(franchise)

                            return (
                                <button
                                    key={franchise.id}
                                    type="button"
                                    onClick={() => handleSelectFranchise(franchise.id)}
                                    className={[
                                        'w-full rounded-[26px] border p-4 text-left transition-all duration-200',
                                        active
                                            ? 'border-sky-200/80 bg-white/86 shadow-[0_18px_44px_rgba(80,110,150,0.14)] ring-2 ring-sky-100/90'
                                            : 'border-white/70 bg-white/46 hover:-translate-y-0.5 hover:bg-white/72',
                                    ].join(' ')}
                                >
                                    <div className="flex items-start justify-between gap-3">
                                        <div className="min-w-0">
                                            <h3 className="line-clamp-1 text-base font-black text-slate-800">
                                                {title}
                                            </h3>
                                            <p className="mt-1 line-clamp-1 text-xs font-medium text-slate-400">
                                                {subtitle}
                                            </p>
                                        </div>

                                        <span className="shrink-0 rounded-full border border-white/70 bg-white/70 px-2.5 py-1 text-xs font-black text-slate-500">
                                            {franchiseStats.total}
                                        </span>
                                    </div>

                                    <div className="mt-3 flex flex-wrap gap-1.5">
                                        {categoryOrder.map((category) => {
                                            const count = subjects.filter(
                                                (subject) => subject.category === category,
                                            ).length

                                            if (count === 0) {
                                                return null
                                            }

                                            const theme = categoryTheme[category]

                                            return (
                                                <span
                                                    key={category}
                                                    className={[
                                                        'rounded-full px-2.5 py-0.5 text-[11px] font-semibold',
                                                        theme.bg,
                                                        theme.text,
                                                    ].join(' ')}
                                                >
                                                    {theme.cnName} {count}
                                                </span>
                                            )
                                        })}
                                    </div>
                                </button>
                            )
                        })}
                        </div>
                    </div>
                </GlassCard>

                <div className="min-w-0 space-y-5">
                    {selectedFranchise ? (
                        <>
                            <section
                                className={[
                                    'overflow-hidden rounded-[32px] border border-white/70 bg-gradient-to-br p-5 shadow-sm backdrop-blur-xl transition-all duration-300',
                                    scopeTheme?.softGradient ?? neutralPanelGradient,
                                ].join(' ')}
                            >
                                <div className="flex flex-wrap items-start justify-between gap-5">
                                    <div className="min-w-0">
                                        <p className="text-xs font-semibold tracking-[0.24em] text-slate-400">
                                            SERIES DETAIL
                                        </p>
                                        <h2 className="mt-2 line-clamp-2 text-3xl font-black leading-tight text-slate-800">
                                            {getFranchiseTitle(selectedFranchise)}
                                        </h2>
                                        <p className="mt-2 max-w-3xl text-sm leading-7 text-slate-500">
                                            {selectedFranchise.description ||
                                                '暂无系列简介。后续可以在后端补充系列简介，用来说明正篇、外传、重制版或跨媒介关系。'}
                                        </p>
                                    </div>

                                    <div className="flex flex-col items-stretch gap-3">
                                        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                                            <StatChip label="作品数" value={stats.total} />
                                            <StatChip label="已看" value={stats.watched} />
                                            <StatChip label="想看" value={stats.want} />
                                            <StatChip
                                                label="平台均分"
                                                value={
                                                    stats.platformAverage === null
                                                        ? '--'
                                                        : stats.platformAverage.toFixed(1)
                                                }
                                            />
                                        </div>

                                        <button
                                            type="button"
                                            onClick={() => requestDeleteFranchise(selectedFranchise)}
                                            className="self-end rounded-2xl border border-rose-100 bg-rose-50/85 px-4 py-2.5 text-sm font-semibold text-rose-700 transition hover:-translate-y-0.5 hover:bg-rose-100"
                                        >
                                            删除系列
                                        </button>
                                    </div>
                                </div>

                                <div className="mt-5 grid gap-3 lg:grid-cols-[1fr_260px]">
                                    <div className="rounded-[28px] border border-white/70 bg-white/48 p-4">
                                        <div className="flex items-center justify-between gap-3">
                                            <h3 className="text-sm font-bold text-slate-700">
                                                跨媒介构成
                                            </h3>
                                            <span className="text-xs font-semibold text-slate-400">
                                                点击切换类型范围
                                            </span>
                                        </div>

                                        <div className="mt-3 grid gap-2 sm:grid-cols-4">
                                            {categoryCounts.map(({ category, count }) => {
                                                const theme = categoryTheme[category]
                                                const active = selectedScope === category

                                                return (
                                                    <button
                                                        key={category}
                                                        type="button"
                                                        onClick={() => handleSelectScope(category)}
                                                        className={[
                                                            'rounded-[22px] border p-3 text-left transition-all duration-200',
                                                            active
                                                                ? `${theme.border} bg-gradient-to-br ${theme.softGradient} shadow-sm ring-2 ${theme.ring}`
                                                                : 'border-white/70 bg-white/58 hover:-translate-y-0.5 hover:bg-white/80',
                                                        ].join(' ')}
                                                    >
                                                        <div className="flex items-baseline gap-1">
                                                            <span
                                                                className={[
                                                                    'text-2xl font-black leading-none',
                                                                    theme.text,
                                                                ].join(' ')}
                                                            >
                                                                {theme.letter}
                                                            </span>
                                                            <span className="text-sm font-black text-slate-700">
                                                                {theme.enName.slice(1)}
                                                            </span>
                                                        </div>
                                                        <p className="mt-2 text-xs font-semibold text-slate-400">
                                                            {theme.cnName}
                                                        </p>
                                                        <p className="mt-2 text-lg font-black text-slate-800">
                                                            {count}
                                                            <span className="ml-1 text-xs font-semibold text-slate-400">
                                                                部
                                                            </span>
                                                        </p>
                                                    </button>
                                                )
                                            })}
                                        </div>
                                    </div>

                                    <div className="rounded-[28px] border border-white/70 bg-white/48 p-4">
                                        <h3 className="text-sm font-bold text-slate-700">
                                            状态统计
                                        </h3>
                                        <div className="mt-4 space-y-2.5">
                                            {statusOrder.map((status) => {
                                                const count =
                                                    status === 'WATCHED'
                                                        ? stats.watched
                                                        : status === 'WANT'
                                                          ? stats.want
                                                          : status === 'PAUSED'
                                                            ? stats.paused
                                                            : stats.none
                                                const percent =
                                                    stats.total > 0
                                                        ? Math.round((count / stats.total) * 100)
                                                        : 0

                                                return (
                                                    <div key={status}>
                                                        <div className="mb-1 flex items-center justify-between text-xs font-semibold">
                                                            <span
                                                                className={[
                                                                    'rounded-full border px-2 py-0.5',
                                                                    libraryStatusTheme[status].className,
                                                                ].join(' ')}
                                                            >
                                                                {libraryStatusTheme[status].label}
                                                            </span>
                                                            <span className="text-slate-400">
                                                                {count} / {stats.total}
                                                            </span>
                                                        </div>
                                                        <div className="h-2 overflow-hidden rounded-full bg-white/70">
                                                            <div
                                                                className="h-full rounded-full bg-slate-400/70 transition-all duration-300"
                                                                style={{ width: `${percent}%` }}
                                                            />
                                                        </div>
                                                    </div>
                                                )
                                            })}
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <section className="rounded-[32px] border border-white/70 bg-white/46 p-5 shadow-sm backdrop-blur-xl">
                                <div className="flex flex-wrap items-center justify-between gap-3">
                                    <div>
                                        <p className="text-xs font-semibold tracking-[0.22em] text-slate-400">
                                            TIMELINE
                                        </p>
                                        <h3 className="mt-1 text-xl font-black text-slate-800">
                                            系列时间线
                                        </h3>
                                    </div>

                                    {selectedScope !== 'ALL' ? (
                                        <button
                                            type="button"
                                            onClick={() => handleSelectScope('ALL')}
                                            className="rounded-full border border-white/70 bg-white/70 px-4 py-2 text-xs font-semibold text-slate-500 transition hover:bg-white"
                                        >
                                            查看全部媒介
                                        </button>
                                    ) : null}
                                </div>

                                {sortedSubjects.length === 0 ? (
                                    <div className="mt-4 rounded-[26px] border border-white/70 bg-white/55 p-6 text-sm text-slate-400">
                                        当前类型范围下没有作品。
                                    </div>
                                ) : (
                                    <div className="mt-5 overflow-x-auto pb-3">
                                        <div className="flex min-w-max gap-4">
                                            {sortedSubjects.map((subject) => (
                                                <TimelineSubjectCard
                                                    key={subject.id}
                                                    subject={subject}
                                                    onOpen={() => void handleOpenSubject(subject)}
                                                    onRemove={() => requestRemoveSubject(selectedFranchise, subject)}
                                                />
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </section>
                        </>
                    ) : (
                        <GlassCard className="p-6">
                            {listError ? (
                                <ErrorState
                                    title="系列数据加载失败"
                                    message={listError}
                                    onRetry={() => void loadFranchises()}
                                />
                            ) : (
                                <EmptyState
                                    title="暂无系列数据"
                                    description="先在作品库里给作品关联系列，再回到这里查看系列时间线。"
                                />
                            )}
                        </GlassCard>
                    )}
                </div>
            </section>

            <SubjectDetailDrawer
                subject={selectedSubject}
                open={selectedSubject !== null}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={() => undefined}
                onAddToWant={() => undefined}
                onAddToPaused={() => undefined}
                onSetToNone={() => undefined}
                detailLoading={detailLoading}
                showActions={false}
                showStatusActions={false}
            />

            <ConfirmDialog
                open={pendingDeleteFranchise !== null}
                title="删除系列？"
                description={
                    pendingDeleteFranchise
                        ? `这会删除系列《${getFranchiseTitle(pendingDeleteFranchise)}》，但不会删除作品库中的作品；该系列下作品会变成未关联系列。`
                        : ''
                }
                confirmText="删除系列"
                loading={deleteLoading}
                tone="danger"
                onCancel={closeDeleteDialog}
                onConfirm={() => void confirmDeleteFranchise()}
            />

            <ConfirmDialog
                open={pendingRemoveSubject !== null}
                title="移出系列？"
                description={
                    pendingRemoveSubject
                        ? `这会把《${pendingRemoveSubject.subject.displayTitle}》从《${getFranchiseTitle(pendingRemoveSubject.franchise)}》中移出，不会删除作品库作品。`
                        : ''
                }
                confirmText="移出系列"
                loading={deleteLoading}
                tone="danger"
                onCancel={closeDeleteDialog}
                onConfirm={() => void confirmRemoveSubject()}
            />
        </div>
    )
}

type StatChipProps = {
    label: string
    value: string | number
}

function StatChip({ label, value }: StatChipProps) {
    return (
        <div className="min-w-[88px] rounded-[22px] border border-white/70 bg-white/62 px-4 py-3 text-center shadow-sm">
            <p className="text-xl font-black text-slate-800">{value}</p>
            <p className="mt-1 text-xs font-semibold text-slate-400">{label}</p>
        </div>
    )
}

type TimelineSubjectCardProps = {
    subject: SubjectCardView
    onOpen: () => void
    onRemove: () => void
}

function TimelineSubjectCard({ subject, onOpen, onRemove }: TimelineSubjectCardProps) {
    const category = categoryTheme[subject.category]
    const seasonPart = formatSeasonPart(subject)

    return (
        <article
            className={[
                'group w-[220px] shrink-0 overflow-hidden rounded-[26px] border border-white/70 bg-gradient-to-br shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-[0_18px_42px_rgba(90,110,140,0.16)]',
                category.softGradient,
            ].join(' ')}
        >
            <div className="relative aspect-[3/4] overflow-hidden bg-slate-100">
                <CoverImage
                    src={subject.coverUrl}
                    alt={subject.displayTitle}
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.04]"
                    fallback={
                    <div
                        className={[
                            'flex h-full w-full items-center justify-center bg-gradient-to-br',
                            category.softGradient,
                        ].join(' ')}
                    >
                        <div className="text-center">
                            <span
                                className={['text-6xl font-black', category.text].join(' ')}
                            >
                                {category.letter}
                            </span>
                            <p className="mt-1 text-xs font-semibold text-slate-400">
                                No Cover
                            </p>
                        </div>
                    </div>
                    }
                />

                <div className="absolute left-3 top-3">
                    <StatusBadge status={subject.libraryStatus} />
                </div>

                {subject.platformScore ? (
                    <div
                        className={[
                            'absolute bottom-3 right-3 rounded-2xl border px-2.5 py-1 text-xs font-black',
                            platformScoreTheme.className,
                        ].join(' ')}
                    >
                        {subject.platformScore.toFixed(1)}
                    </div>
                ) : null}
            </div>

            <div className="m-2.5 rounded-[20px] border border-white/65 bg-white/62 p-3.5 shadow-sm backdrop-blur">
                <div className="flex items-center gap-2 text-[11px] font-semibold">
                    <span
                        className={[
                            'rounded-full px-2.5 py-0.5',
                            category.bg,
                            category.text,
                        ].join(' ')}
                    >
                        {category.cnName}
                    </span>
                    <span
                        className={[
                            'rounded-full border px-2.5 py-0.5',
                            yearChipClassName,
                        ].join(' ')}
                    >
                        {formatRelease(subject)}
                    </span>
                </div>

                <h4 className="mt-3 line-clamp-2 min-h-[40px] text-sm font-black leading-5 text-slate-800">
                    {subject.displayTitle}
                </h4>

                <div className="mt-2 flex min-h-[22px] flex-wrap gap-1.5">
                    {seasonPart ? (
                        <span className="rounded-full bg-white/72 px-2.5 py-0.5 text-[11px] font-semibold text-slate-400">
                            {seasonPart}
                        </span>
                    ) : null}

                </div>

                <div className="mt-3 grid grid-cols-2 gap-2">
                    <button
                        type="button"
                        onClick={onOpen}
                        className="rounded-2xl border border-white/75 bg-white/78 px-3 py-2 text-xs font-semibold text-slate-600 transition hover:-translate-y-0.5 hover:bg-slate-900 hover:text-white"
                    >
                        查看详情
                    </button>

                    <button
                        type="button"
                        onClick={onRemove}
                        className="rounded-2xl border border-rose-100 bg-rose-50/82 px-3 py-2 text-xs font-semibold text-rose-600 transition hover:-translate-y-0.5 hover:bg-rose-100"
                    >
                        移出
                    </button>
                </div>
            </div>
        </article>
    )
}

export default FranchisesPage
