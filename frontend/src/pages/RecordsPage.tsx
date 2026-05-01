import { useCallback, useEffect, useMemo, useState } from 'react'
import {
    deleteRecord,
    getRecords,
    updateRecord,
    type RecordPageSort,
    type UserSubjectRecordResponse,
} from '../api/recordApi'
import { getApiErrorMessage } from '../api/request'
import ConfirmDialog from '../components/common/ConfirmDialog'
import { getSubjectById } from '../api/subjectApi'
import CategorySwitch from '../components/common/CategorySwitch'
import CoverImage from '../components/common/CoverImage'
import {
    EmptyState,
    ErrorState,
    FeedbackBanner,
    LoadingGrid,
} from '../components/common/FeedbackState'
import GlassCard from '../components/common/GlassCard'
import PageHero from '../components/layout/PageHero'
import AddRecordModal, {
    type AddRecordPayload,
} from '../components/subject/AddRecordModal'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { mapSubjectDetailToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    neutralPanelGradient,
    personalScoreTheme,
    quarterTheme,
} from '../theme/acgnTheme'
import {
    getCurrentReleaseQuarter,
    releaseQuarters,
    type AcgnCategory,
    type ReleaseQuarter,
} from '../types/common'
import type { SubjectCardView } from '../types/subject'

type SortOption = {
    value: RecordPageSort
    label: string
}

const sortOptions: SortOption[] = [
    { value: 'TITLE_ASC', label: '默认' },
    { value: 'SCORE_DESC', label: '评分降序' },
    { value: 'SCORE_ASC', label: '评分升序' },
]

function compareRecordsBySort(
    first: UserSubjectRecordResponse,
    second: UserSubjectRecordResponse,
    sort: RecordPageSort,
) {
    if (sort === 'RECORD_TIME_ASC') {
        return first.id - second.id
    }

    if (sort === 'SCORE_DESC') {
        return (second.scoreValue ?? -1) - (first.scoreValue ?? -1)
    }

    if (sort === 'SCORE_ASC') {
        return (first.scoreValue ?? 51) - (second.scoreValue ?? 51)
    }

    return second.id - first.id
}

function compareRecordTitle(first: UserSubjectRecordResponse, second: UserSubjectRecordResponse) {
    return first.subjectTitle.localeCompare(second.subjectTitle, 'zh-Hans-CN', {
        numeric: true,
        sensitivity: 'base',
    })
}

function compareRecordsForDisplay(
    first: UserSubjectRecordResponse,
    second: UserSubjectRecordResponse,
    sort: RecordPageSort,
) {
    if (sort === 'TITLE_ASC') {
        return compareRecordTitle(first, second) || first.id - second.id
    }

    return compareRecordsBySort(first, second, sort) || compareRecordTitle(first, second)
}

function getReleaseMonthText(record: UserSubjectRecordResponse) {
    if (!record.subjectReleaseYear) {
        return null
    }

    const releaseMonth = record.subjectReleaseQuarter
        ? quarterTheme[record.subjectReleaseQuarter].monthLabel
        : ''

    return releaseMonth
        ? `${record.subjectReleaseYear}${releaseMonth}`
        : String(record.subjectReleaseYear)
}

function RecordsPage() {
    const [records, setRecords] = useState<UserSubjectRecordResponse[]>([])
    const [loading, setLoading] = useState(false)
    const [listError, setListError] = useState<string | null>(null)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [selectedCategory, setSelectedCategory] = useState<AcgnCategory>('ANIME')
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter>(getCurrentReleaseQuarter())
    const [keyword, setKeyword] = useState('')
    const [sort, setSort] = useState<RecordPageSort>('TITLE_ASC')
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [addRecordSubject, setAddRecordSubject] = useState<SubjectCardView | null>(null)
    const [detailLoading, setDetailLoading] = useState(false)
    const [recordSubmitting, setRecordSubmitting] = useState(false)
    const [pendingRecordDelete, setPendingRecordDelete] =
        useState<SubjectCardView | null>(null)
    const [recordDeleting, setRecordDeleting] = useState(false)

    const loadRecords = useCallback(async () => {
        setLoading(true)
        setListError(null)

        try {
            const response = await getRecords()
            const sortedRecords = [...response].sort((first, second) =>
                compareRecordsBySort(first, second, 'RECORD_TIME_DESC'),
            )
            setRecords(sortedRecords)

            const firstAnimeRecord =
                sortedRecords.find((record) => record.subjectCategory === 'ANIME') ??
                sortedRecords[0]

            if (firstAnimeRecord) {
                setSelectedCategory(firstAnimeRecord.subjectCategory ?? 'ANIME')
                setSelectedYear(firstAnimeRecord.recordYear)
                setSelectedQuarter(firstAnimeRecord.recordQuarter)
            }
        } catch (loadError) {
            setRecords([])
            setListError(getApiErrorMessage(loadError))
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        // 页面打开时同步正式记录列表，筛选和排序在前端完成。
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadRecords()
    }, [loadRecords])

    const yearOptions = useMemo(() => {
        return Array.from(new Set(records.map((record) => record.recordYear))).sort(
            (first, second) => first - second,
        )
    }, [records])

    const currentYearIndex = yearOptions.indexOf(selectedYear)
    const previousYear = currentYearIndex > 0 ? yearOptions[currentYearIndex - 1] : null
    const nextYear =
        currentYearIndex >= 0 && currentYearIndex < yearOptions.length - 1
            ? yearOptions[currentYearIndex + 1]
            : null

    const filteredRecords = useMemo(() => {
        const normalizedKeyword = keyword.trim().toLowerCase()

        return records
            .filter((record) => {
                const matchCategory = record.subjectCategory === selectedCategory
                const matchYear = record.recordYear === selectedYear
                const matchQuarter = record.recordQuarter === selectedQuarter
                const matchKeyword =
                    normalizedKeyword.length === 0 ||
                    record.subjectTitle.toLowerCase().includes(normalizedKeyword)

                return matchCategory && matchYear && matchQuarter && matchKeyword
            })
            .sort((first, second) => compareRecordsForDisplay(first, second, sort))
    }, [keyword, records, selectedCategory, selectedQuarter, selectedYear, sort])

    const selectedCategoryTheme = categoryTheme[selectedCategory]
    const selectedQuarterTheme = quarterTheme[selectedQuarter]
    const selectedQuarterGradient =
        selectedQuarterTheme?.panelGradient ?? neutralPanelGradient

    function moveYear(direction: 'previous' | 'next') {
        const targetYear = direction === 'previous' ? previousYear : nextYear

        if (targetYear !== null) {
            setSelectedYear(targetYear)
        }
    }

    async function handleOpenDetail(record: UserSubjectRecordResponse) {
        setDetailLoading(true)
        setFeedbackMessage(null)

        try {
            const detail = await getSubjectById(record.subjectId)
            setSelectedSubject(mapSubjectDetailToCardView(detail))
        } catch (detailError) {
            setFeedbackMessage(getApiErrorMessage(detailError))
        } finally {
            setDetailLoading(false)
        }
    }

    async function handleOpenEditRecord(subject: SubjectCardView) {
        setFeedbackMessage(null)

        if (!subject.recordId) {
            const detail = await getSubjectById(subject.id)
            setAddRecordSubject(mapSubjectDetailToCardView(detail))
            return
        }

        setAddRecordSubject(subject)
    }

    async function handleSubmitRecord(subject: SubjectCardView, payload: AddRecordPayload) {
        if (!subject.recordId) {
            setFeedbackMessage('未找到可编辑的正式记录，请重新打开详情后再试。')
            return
        }

        setRecordSubmitting(true)
        setFeedbackMessage(null)

        try {
            const updatedRecord = await updateRecord(subject.recordId, {
                subjectId: subject.id,
                recordYear: payload.recordYear,
                recordQuarter: payload.recordQuarter,
                scoreValue: payload.scoreValue,
                note: payload.note || null,
            })

            setRecords((current) =>
                current.map((record) =>
                    record.id === updatedRecord.id ? updatedRecord : record,
                ),
            )
            setSelectedCategory(updatedRecord.subjectCategory ?? selectedCategory)
            setSelectedYear(updatedRecord.recordYear)
            setSelectedQuarter(updatedRecord.recordQuarter)

            const detail = await getSubjectById(subject.id)
            const mappedDetail = mapSubjectDetailToCardView(detail)
            setSelectedSubject(mappedDetail)
            setAddRecordSubject(null)
            setFeedbackMessage('已保存记录修改')
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setRecordSubmitting(false)
        }
    }

    function requestRecordDelete(subject: SubjectCardView) {
        if (!subject.recordId) {
            setFeedbackMessage('未找到可删除的正式记录，请重新打开详情后再试。')
            return
        }

        setPendingRecordDelete(subject)
    }

    function closeRecordDeleteDialog() {
        if (recordDeleting) {
            return
        }

        setPendingRecordDelete(null)
    }

    async function confirmRecordDelete() {
        if (!pendingRecordDelete?.recordId) {
            return
        }

        const subjectToDelete = pendingRecordDelete
        const recordId = pendingRecordDelete.recordId
        setRecordDeleting(true)
        setFeedbackMessage(null)

        try {
            await deleteRecord(recordId)
            setRecords((current) =>
                current.filter((record) => record.id !== recordId),
            )
            setSelectedSubject((current) =>
                current?.id === subjectToDelete.id ? null : current,
            )
            setAddRecordSubject((current) =>
                current?.id === subjectToDelete.id ? null : current,
            )
            setPendingRecordDelete(null)
            setFeedbackMessage(`已删除《${subjectToDelete.displayTitle}》的正式记录`)
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setRecordDeleting(false)
        }
    }

    return (
        <>
            <div className="space-y-6 content-enter">
                <PageHero
                    eyebrow="MY RECORDS"
                    title="我的记录"
            description="固定年份与月份查看正式记录，按 ACGN 类型切换浏览。"
                />

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-5 transition-all duration-300',
                            selectedQuarterGradient,
                        ].join(' ')}
                    >
                        <div className="grid items-end gap-4 xl:grid-cols-[1.05fr_330px_360px]">
                            <label className="block">
                                <span className="mb-2 block text-xs font-semibold text-slate-400">
                                    搜索记录
                                </span>
                                <input
                                    value={keyword}
                                    onChange={(event) => setKeyword(event.target.value)}
                                    placeholder="输入作品标题"
                                    className="h-12 w-full rounded-2xl border border-white/80 bg-white/72 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:shadow-sm"
                                />
                            </label>

                            <YearTriplet
                                selectedYear={selectedYear}
                                previousYear={previousYear}
                                nextYear={nextYear}
                                onSelect={setSelectedYear}
                                onMove={moveYear}
                            />

                            <div>
                                <span className="mb-2 block text-xs font-semibold text-slate-400">
                                    月份
                                </span>
                                <div className="grid grid-cols-4 gap-2">
                                    {releaseQuarters.map((quarter) => {
                                        const theme = quarterTheme[quarter]
                                        const selected = selectedQuarter === quarter

                                        return (
                                            <button
                                                key={quarter}
                                                type="button"
                                                onClick={() => setSelectedQuarter(quarter)}
                                                className={[
                                                    'h-12 rounded-2xl border text-sm font-bold transition-all duration-200',
                                                    selected
                                                        ? `${theme.border} ${theme.bg} ${theme.text} scale-[1.03] shadow-sm`
                                                        : 'border-white/80 bg-white/62 text-slate-400 hover:-translate-y-0.5 hover:bg-white',
                                                ].join(' ')}
                                            >
                                                {theme.cnName}
                                            </button>
                                        )
                                    })}
                                </div>
                            </div>
                        </div>
                    </div>
                </GlassCard>

                <CategorySwitch
                    value={selectedCategory}
                    onChange={setSelectedCategory}
                />

                {feedbackMessage ? (
                    <FeedbackBanner>{feedbackMessage}</FeedbackBanner>
                ) : null}

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-5 transition-all duration-300',
                            selectedQuarterGradient,
                        ].join(' ')}
                    >
                        <div className="flex flex-wrap items-center justify-between gap-4">
                            <div>
                                <h3 className="text-lg font-black text-slate-800">
                                    {selectedCategoryTheme.cnName}记录
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                        {selectedYear} · {selectedQuarterTheme.displayName} · {filteredRecords.length} 条
                                </p>
                            </div>

                            <div className="flex flex-wrap gap-2">
                                {sortOptions.map((option) => {
                                    const selected = sort === option.value

                                    return (
                                        <button
                                            key={option.value}
                                            type="button"
                                            onClick={() => setSort(option.value)}
                                            className={[
                                                'rounded-full border px-4 py-2 text-xs font-semibold transition-all duration-200',
                                                selected
                                                    ? `${selectedCategoryTheme.border} ${selectedCategoryTheme.bg} ${selectedCategoryTheme.text} shadow-sm`
                                                    : 'border-white/75 bg-white/55 text-slate-400 hover:-translate-y-0.5 hover:bg-white hover:text-slate-600',
                                            ].join(' ')}
                                        >
                                            {option.label}
                                        </button>
                                    )
                                })}
                            </div>
                        </div>

                        <div className="mt-5">
                            {loading ? (
                                <LoadingGrid />
                            ) : listError ? (
                                <ErrorState
                                    title="记录加载失败"
                                    message={listError}
                                    onRetry={() => void loadRecords()}
                                />
                            ) : filteredRecords.length > 0 ? (
                                <div className="subject-grid">
                                    {filteredRecords.map((record) => (
                                        <RecordCard
                                            key={record.id}
                                            record={record}
                                            category={selectedCategory}
                                            onOpenDetail={handleOpenDetail}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <EmptyState
                                    title="没有匹配的记录"
                                    description="可以切换年份、月份、类型或调整搜索关键词。"
                                />
                            )}
                        </div>
                    </div>
                </GlassCard>
            </div>

            <SubjectDetailDrawer
                open={selectedSubject !== null}
                subject={selectedSubject}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={(subject) => void handleOpenEditRecord(subject)}
                onAddToWant={() => undefined}
                onAddToPaused={() => undefined}
                onSetToNone={() => undefined}
                onDeleteRecord={requestRecordDelete}
                deleteAction={recordDeleting ? 'RECORD' : null}
                detailLoading={detailLoading}
                showActions
                showStatusActions={false}
            />

            <AddRecordModal
                open={addRecordSubject !== null}
                subject={addRecordSubject}
                onClose={() => setAddRecordSubject(null)}
                onSubmit={handleSubmitRecord}
                submitting={recordSubmitting}
            />

            <ConfirmDialog
                open={pendingRecordDelete !== null}
                title="删除正式记录？"
                description={
                    pendingRecordDelete
                        ? `这会删除《${pendingRecordDelete.displayTitle}》的个人评分和备注，作品本体仍会保留在作品库。`
                        : ''
                }
                confirmText="删除记录"
                loading={recordDeleting}
                tone="danger"
                onCancel={closeRecordDeleteDialog}
                onConfirm={() => void confirmRecordDelete()}
            />
        </>
    )
}

type YearTripletProps = {
    selectedYear: number
    previousYear: number | null
    nextYear: number | null
    onSelect: (year: number) => void
    onMove: (direction: 'previous' | 'next') => void
}

function YearTriplet({
                         selectedYear,
                         previousYear,
                         nextYear,
                         onSelect,
                         onMove,
                     }: YearTripletProps) {
    const visibleYears = [previousYear, selectedYear, nextYear]

    return (
        <div>
            <span className="mb-2 block text-xs font-semibold text-slate-400">
                年份
            </span>
            <div className="grid grid-cols-[44px_1fr_44px] gap-2">
                <button
                    type="button"
                    disabled={previousYear === null}
                    onClick={() => onMove('previous')}
                    className={[
                        'h-12 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        previousYear !== null
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:-translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="选择上一年"
                >
                    &lt;
                </button>

                <div className="grid grid-cols-3 gap-2">
                    {visibleYears.map((year, index) => {
                        if (year === null) {
                            return (
                                <span
                                    key={`empty-${index}`}
                                    aria-hidden="true"
                                    className="h-12 rounded-2xl border border-white/35 bg-white/25"
                                />
                            )
                        }

                        const selected = year === selectedYear

                        return (
                            <button
                                key={year}
                                type="button"
                                onClick={() => onSelect(year)}
                                className={[
                                    'h-12 rounded-2xl border px-2 text-sm font-black transition-all duration-200',
                                    selected
                                        ? 'scale-[1.03] border-amber-200 bg-amber-100/90 text-amber-700 shadow-sm'
                                        : 'border-white/75 bg-white/62 text-slate-400 hover:-translate-y-0.5 hover:bg-white hover:text-slate-600',
                                ].join(' ')}
                            >
                                {year}
                            </button>
                        )
                    })}
                </div>

                <button
                    type="button"
                    disabled={nextYear === null}
                    onClick={() => onMove('next')}
                    className={[
                        'h-12 rounded-2xl border text-lg font-black shadow-sm transition-all duration-200',
                        nextYear !== null
                            ? 'border-amber-200/75 bg-white/78 text-amber-600 hover:translate-x-0.5 hover:bg-amber-50'
                            : 'cursor-not-allowed border-white/45 bg-white/35 text-slate-300',
                    ].join(' ')}
                    aria-label="选择下一年"
                >
                    &gt;
                </button>
            </div>
        </div>
    )
}

type RecordCardProps = {
    record: UserSubjectRecordResponse
    category: AcgnCategory
    onOpenDetail: (record: UserSubjectRecordResponse) => void
}

function RecordCard({ record, category, onOpenDetail }: RecordCardProps) {
    const theme = categoryTheme[record.subjectCategory ?? category]
    const releaseQuarter = record.subjectReleaseQuarter
        ? quarterTheme[record.subjectReleaseQuarter]
        : null
    const releaseText = getReleaseMonthText(record)
    const visibleTags = (record.tagList ?? []).slice(0, 4)

    return (
        <button
            type="button"
            onClick={() => onOpenDetail(record)}
            className={[
                'group flex h-full flex-col overflow-hidden rounded-[24px] border border-white/70 bg-gradient-to-br text-left shadow-sm backdrop-blur transition-all duration-200',
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

                <div className="absolute right-3 top-3">
                    {record.scoreValue !== null ? (
                        <span
                            className={[
                                'rounded-2xl border px-3 py-1 text-xs font-black',
                                personalScoreTheme.className,
                            ].join(' ')}
                        >
                            {record.scoreValue}
                        </span>
                    ) : (
                        <span className="rounded-2xl border border-slate-200 bg-slate-50/90 px-3 py-1 text-xs font-semibold text-slate-400">
                            未评分
                        </span>
                    )}
                </div>
            </div>

            <div className="m-2.5 flex flex-1 flex-col rounded-[20px] border border-white/65 bg-white/58 p-3.5 shadow-sm backdrop-blur">
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

                    {releaseText ? (
                        <span
                            className={[
                                'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold',
                                releaseQuarter
                                    ? `${releaseQuarter.bg} ${releaseQuarter.text} ${releaseQuarter.border}`
                                    : 'border-amber-200/75 bg-amber-50/90 text-amber-700',
                            ].join(' ')}
                        >
                            {releaseText}
                        </span>
                    ) : null}
                </div>

                <div className="mt-3 flex h-[24px] min-w-0 flex-nowrap gap-1.5 overflow-hidden">
                    {visibleTags.length > 0 ? (
                        visibleTags.map((tag) => (
                            <span
                                key={tag.id}
                                className="shrink-0 rounded-full bg-white/75 px-2 py-0.5 text-[11px] text-slate-400"
                            >
                                #{tag.name}
                            </span>
                        ))
                    ) : (
                        <span className="text-[11px] text-slate-300">暂无标签</span>
                    )}
                </div>

                <p className="mt-3 min-h-[40px] line-clamp-2 text-xs leading-5 text-slate-400">
                    {record.note ?? '暂无备注'}
                </p>

                <div className="mt-auto flex min-w-0 items-center justify-between gap-3 border-t border-white/70 pt-3">
                    <span className="min-w-0 flex-1 truncate text-[11px] text-slate-400">
                        {record.updatedAt ? '已同步记录' : '正式记录'}
                    </span>

                    <span className="shrink-0 whitespace-nowrap rounded-full border border-slate-200/70 bg-white/75 px-3 py-1.5 text-[11px] font-semibold text-slate-600 transition-all duration-200 group-hover:-translate-y-0.5 group-hover:bg-slate-900 group-hover:text-white">
                        详情
                    </span>
                </div>
            </div>
        </button>
    )
}

export default RecordsPage
