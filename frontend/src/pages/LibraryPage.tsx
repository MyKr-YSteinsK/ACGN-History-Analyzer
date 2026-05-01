import { useCallback, useEffect, useMemo, useState } from 'react'
import { addToPaused, addToWant, deleteCollection } from '../api/collectionApi'
import { createRecord, deleteRecord, updateRecord } from '../api/recordApi'
import { getApiErrorMessage } from '../api/request'
import {
    deleteSubject,
    getSubjectById,
    getSubjectPage,
    type SubjectPageSort,
} from '../api/subjectApi'
import { getTags, type TagResponse } from '../api/tagApi'
import CategorySelector from '../components/common/CategorySelector'
import ConfirmDialog from '../components/common/ConfirmDialog'
import {
    EmptyState,
    ErrorState,
    FeedbackBanner,
    LoadingGrid,
} from '../components/common/FeedbackState'
import FilterPillGroup from '../components/common/FilterPillGroup'
import GlassCard from '../components/common/GlassCard'
import YearScroller from '../components/common/YearScroller'
import PageHero from '../components/layout/PageHero'
import AddRecordModal, {
    type AddRecordPayload,
} from '../components/subject/AddRecordModal'
import SubjectCard from '../components/subject/SubjectCard'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import { GRID_PAGE_SIZE } from '../constants/pagination'
import {
    categoryTheme,
    neutralPanelGradient,
    quarterTheme,
} from '../theme/acgnTheme'
import {
    mapSubjectDetailToCardView,
    mapSubjectResponseToCardView,
} from '../mappers/subjectMapper'
import type { AcgnCategory, LibraryStatus, ReleaseQuarter } from '../types/common'
import type { SubjectCardView } from '../types/subject'
import {
    quarterOptions,
    sortOptions,
    statusOptions,
    yearOptions,
} from './library/libraryConstants'
import type { PendingStatusChange, SubjectPageState, StatusTarget } from './library/libraryTypes'
import TagFilterBar from './library/TagFilterBar'

const initialPageState: SubjectPageState = {
    page: 0,
    size: GRID_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
}

function LibraryPage() {
    const [subjects, setSubjects] = useState<SubjectCardView[]>([])
    const [pageInfo, setPageInfo] = useState<SubjectPageState>(initialPageState)
    const [selectedCategory, setSelectedCategory] = useState<AcgnCategory>('ANIME')
    const [keyword, setKeyword] = useState('')
    const [selectedStatus, setSelectedStatus] = useState<LibraryStatus | 'ALL'>('ALL')
    const [selectedTagName, setSelectedTagName] = useState<string | 'ALL'>('ALL')
    const [availableTags, setAvailableTags] = useState<TagResponse[]>([])
    const [selectedYear, setSelectedYear] = useState<number | 'ALL'>('ALL')
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter | 'ALL'>('ALL')
    const [sort, setSort] = useState<SubjectPageSort>('TITLE_ASC')
    const [currentPage, setCurrentPage] = useState(0)
    const [listLoading, setListLoading] = useState(false)
    const [listError, setListError] = useState<string | null>(null)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [addRecordSubject, setAddRecordSubject] = useState<SubjectCardView | null>(null)
    const [detailLoading, setDetailLoading] = useState(false)
    const [recordSubmitting, setRecordSubmitting] = useState(false)
    const [statusAction, setStatusAction] = useState<StatusTarget | null>(null)
    const [pendingStatusChange, setPendingStatusChange] =
        useState<PendingStatusChange | null>(null)
    const [pendingSubjectDelete, setPendingSubjectDelete] =
        useState<SubjectCardView | null>(null)
    const [subjectDeleting, setSubjectDeleting] = useState(false)

    const selectedQuarterGradient =
        selectedQuarter === 'ALL'
            ? neutralPanelGradient
            : quarterTheme[selectedQuarter].panelGradient
    const selectedCategoryTheme = categoryTheme[selectedCategory]

    const resultAnimationKey = [
        selectedCategory,
        selectedYear,
        selectedQuarter,
        selectedStatus,
        selectedTagName,
        keyword,
        sort,
        currentPage,
    ].join('-')

    const updateSubjectEverywhere = useCallback((updatedSubject: SubjectCardView) => {
        setSubjects((current) =>
            current.map((item) =>
                item.id === updatedSubject.id ? { ...item, ...updatedSubject } : item,
            ),
        )

        setSelectedSubject((current) =>
            current?.id === updatedSubject.id
                ? { ...current, ...updatedSubject }
                : current,
        )

        setAddRecordSubject((current) =>
            current?.id === updatedSubject.id
                ? { ...current, ...updatedSubject }
                : current,
        )
    }, [])

    const loadSubjects = useCallback(async () => {
        setListLoading(true)
        setListError(null)
        const normalizedKeyword = keyword.trim()
        const useGlobalKeywordSearch = normalizedKeyword.length > 0

        try {
            const response = await getSubjectPage({
                category: useGlobalKeywordSearch ? undefined : selectedCategory,
                keyword: normalizedKeyword || undefined,
                year:
                    useGlobalKeywordSearch || selectedYear === 'ALL'
                        ? undefined
                        : selectedYear,
                quarter:
                    useGlobalKeywordSearch || selectedQuarter === 'ALL'
                        ? undefined
                        : selectedQuarter,
                status: selectedStatus === 'ALL' ? 'NORMAL' : selectedStatus,
                tagName: selectedTagName === 'ALL' ? undefined : selectedTagName,
                page: currentPage,
                size: GRID_PAGE_SIZE,
                sort,
            })

            setSubjects(response.content.map(mapSubjectResponseToCardView))
            setPageInfo({
                page: response.page,
                size: response.size,
                totalElements: response.totalElements,
                totalPages: response.totalPages,
                first: response.first,
                last: response.last,
            })
        } catch (error) {
            setSubjects([])
            setPageInfo(initialPageState)
            setListError(getApiErrorMessage(error))
        } finally {
            setListLoading(false)
        }
    }, [
        currentPage,
        keyword,
        selectedCategory,
        selectedQuarter,
        selectedStatus,
        selectedTagName,
        selectedYear,
        sort,
    ])

    useEffect(() => {
        // 筛选条件或分页变化时，需要同步后端作品分页数据。
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void loadSubjects()
    }, [loadSubjects])

    useEffect(() => {
        async function loadTags() {
            try {
                setAvailableTags(await getTags())
            } catch (error) {
                setFeedbackMessage(getApiErrorMessage(error, '标签列表加载失败'))
            }
        }

        // 页面进入时读取标签库，供作品库筛选使用。
        void loadTags()
    }, [])

    const loadSubjectDetail = useCallback(
        async (subjectId: number, showLoading = true) => {
            if (showLoading) {
                setDetailLoading(true)
            }

            try {
                const detail = await getSubjectById(subjectId)
                const mappedDetail = mapSubjectDetailToCardView(detail)
                updateSubjectEverywhere(mappedDetail)
                return mappedDetail
            } catch (error) {
                setFeedbackMessage(getApiErrorMessage(error))
                return null
            } finally {
                if (showLoading) {
                    setDetailLoading(false)
                }
            }
        },
        [updateSubjectEverywhere],
    )

    function resetPage() {
        setCurrentPage(0)
    }

    function handleCategoryChange(category: AcgnCategory) {
        resetPage()
        setSelectedCategory(category)
    }

    function handleKeywordChange(value: string) {
        resetPage()
        setKeyword(value)
    }

    function handleStatusChange(status: LibraryStatus | 'ALL') {
        resetPage()
        setSelectedStatus(status)
    }

    function handleTagChange(tagName: string | 'ALL') {
        resetPage()
        setSelectedTagName(tagName)
    }

    function handleYearChange(year: number | 'ALL') {
        resetPage()
        setSelectedYear(year)
    }

    function handleQuarterChange(quarter: ReleaseQuarter | 'ALL') {
        resetPage()
        setSelectedQuarter(quarter)
    }

    function handleSortChange(nextSort: SubjectPageSort) {
        resetPage()
        setSort(nextSort)
    }

    function handleOpenDetail(subject: SubjectCardView) {
        setSelectedSubject(subject)
        setFeedbackMessage(null)
        void loadSubjectDetail(subject.id)
    }

    async function handleOpenAddRecord(subject: SubjectCardView) {
        setFeedbackMessage(null)

        if (subject.libraryStatus === 'WATCHED' && !subject.recordId) {
            const detail = await loadSubjectDetail(subject.id, false)
            setAddRecordSubject(detail ?? subject)
            return
        }

        setAddRecordSubject(subject)
    }

    async function handleSubmitRecord(subject: SubjectCardView, payload: AddRecordPayload) {
        setRecordSubmitting(true)
        setFeedbackMessage(null)

        try {
            const requestBody = {
                subjectId: subject.id,
                recordYear: payload.recordYear,
                recordQuarter: payload.recordQuarter,
                scoreValue: payload.scoreValue,
                note: payload.note || null,
            }

            if (subject.recordId) {
                await updateRecord(subject.recordId, requestBody)
            } else {
                await createRecord(requestBody)
            }

            await loadSubjectDetail(subject.id, false)
            await loadSubjects()
            setAddRecordSubject(null)
            setFeedbackMessage(
                subject.recordId ? '已保存记录修改' : '已添加正式记录，收藏状态已自动更新',
            )
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setRecordSubmitting(false)
        }
    }

    async function getFreshSubject(subject: SubjectCardView) {
        if (
            (subject.libraryStatus === 'WATCHED' && !subject.recordId) ||
            (subject.libraryStatus !== 'WATCHED' &&
                subject.libraryStatus !== 'NONE' &&
                !subject.collectionId)
        ) {
            return (await loadSubjectDetail(subject.id, false)) ?? subject
        }

        return subject
    }

    async function removeCurrentStatus(subject: SubjectCardView) {
        const freshSubject = await getFreshSubject(subject)

        if (freshSubject.libraryStatus === 'WATCHED') {
            if (!freshSubject.recordId) {
                throw new Error('未找到可删除的观看记录，请重新打开详情后再试')
            }

            await deleteRecord(freshSubject.recordId)
            return
        }

        if (freshSubject.libraryStatus === 'WANT' || freshSubject.libraryStatus === 'PAUSED') {
            if (!freshSubject.collectionId) {
                throw new Error('未找到可移除的收藏记录，请重新打开详情后再试')
            }

            await deleteCollection(freshSubject.collectionId)
        }
    }

    async function applyStatusChange(subject: SubjectCardView, target: StatusTarget) {
        setStatusAction(target)
        setFeedbackMessage(null)

        try {
            if (target === 'NONE') {
                await removeCurrentStatus(subject)
            } else {
                if (subject.libraryStatus === 'WATCHED') {
                    await removeCurrentStatus(subject)
                }

                if (target === 'WANT') {
                    await addToWant(subject.id)
                } else {
                    await addToPaused(subject.id)
                }
            }

            await loadSubjectDetail(subject.id, false)
            await loadSubjects()

            if (target === 'NONE') {
                setFeedbackMessage(
                    subject.libraryStatus === 'WATCHED'
                        ? '已删除观看记录，作品状态已回到未记录'
                        : '已移出收藏夹，作品状态已回到未记录',
                )
            } else {
                setFeedbackMessage(target === 'WANT' ? '已加入想看' : '已加入搁置')
            }
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setStatusAction(null)
        }
    }

    function requestStatusChange(subject: SubjectCardView, target: StatusTarget) {
        if (subject.libraryStatus === 'WATCHED') {
            setPendingStatusChange({ subject, target })
            return
        }

        void applyStatusChange(subject, target)
    }

    function handleAddToWant(subject: SubjectCardView) {
        requestStatusChange(subject, 'WANT')
    }

    function handleAddToPaused(subject: SubjectCardView) {
        requestStatusChange(subject, 'PAUSED')
    }

    function handleSetToNone(subject: SubjectCardView) {
        requestStatusChange(subject, 'NONE')
    }

    function closeConfirmDialog() {
        if (statusAction !== null) {
            return
        }

        setPendingStatusChange(null)
    }

    async function confirmStatusChange() {
        if (!pendingStatusChange) {
            return
        }

        const currentPending = pendingStatusChange
        await applyStatusChange(currentPending.subject, currentPending.target)
        setPendingStatusChange(null)
    }

    function requestSubjectDelete(subject: SubjectCardView) {
        setPendingSubjectDelete(subject)
    }

    function closeSubjectDeleteDialog() {
        if (subjectDeleting) {
            return
        }

        setPendingSubjectDelete(null)
    }

    async function confirmSubjectDelete() {
        if (!pendingSubjectDelete) {
            return
        }

        const subjectToDelete = pendingSubjectDelete
        setSubjectDeleting(true)
        setFeedbackMessage(null)

        try {
            await deleteSubject(subjectToDelete.id)
            setSubjects((current) =>
                current.filter((subject) => subject.id !== subjectToDelete.id),
            )
            setSelectedSubject((current) =>
                current?.id === subjectToDelete.id ? null : current,
            )
            setAddRecordSubject((current) =>
                current?.id === subjectToDelete.id ? null : current,
            )
            setPendingSubjectDelete(null)
            setFeedbackMessage(`已删除作品《${subjectToDelete.displayTitle}》`)
            await loadSubjects()
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setSubjectDeleting(false)
        }
    }

    const confirmCopy = useMemo(() => {
        if (!pendingStatusChange) {
            return null
        }

        const targetText =
            pendingStatusChange.target === 'WANT'
                ? '想看'
                : pendingStatusChange.target === 'PAUSED'
                  ? '搁置'
                  : '未记录'

        return {
            title:
                pendingStatusChange.target === 'NONE'
                    ? '删除观看记录？'
                    : `改为${targetText}？`,
            description:
                pendingStatusChange.target === 'NONE'
                    ? `这会删除《${pendingStatusChange.subject.displayTitle}》的正式观看记录，个人评分和备注也会从记录表移除。`
                    : `这会先删除《${pendingStatusChange.subject.displayTitle}》的正式观看记录，再把作品加入${targetText}清单。`,
            confirmText:
                pendingStatusChange.target === 'NONE'
                    ? '删除记录'
                    : `删除记录并加入${targetText}`,
        }
    }, [pendingStatusChange])

    const pageText = useMemo(() => {
        if (pageInfo.totalPages === 0) {
            return '第 0 / 0 页'
        }

        return `第 ${pageInfo.page + 1} / ${pageInfo.totalPages} 页`
    }, [pageInfo.page, pageInfo.totalPages])

    return (
        <>
            <div className="space-y-6 content-enter">
                <PageHero
                    eyebrow="SUBJECT LIBRARY"
                    title="作品库"
                    description="作品库是系统的核心入口。这里负责管理作品基础信息，并作为添加已看、加入想看、加入搁置和查看详情的起点。"
                />

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            categoryTheme[selectedCategory].softGradient,
                        ].join(' ')}
                    >
                        <div className="mb-4 flex flex-wrap items-end justify-between gap-4">
                            <div>
                                <h3 className="text-lg font-bold text-slate-800">
                                    选择作品类型
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    当前页面一次只展示一种类型，保持作品库浏览的清晰度。
                                </p>
                            </div>

                            <div className="rounded-full border border-white/70 bg-white/55 px-4 py-2 text-xs font-semibold text-slate-400">
                                默认动画 · 支持切换
                            </div>
                        </div>

                        <CategorySelector
                            value={selectedCategory}
                            onChange={handleCategoryChange}
                        />
                    </div>
                </GlassCard>

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedQuarterGradient,
                        ].join(' ')}
                    >
                        <div className="mx-auto grid max-w-[1180px] gap-x-10 gap-y-5 xl:grid-cols-[520px_560px] xl:justify-center">
                            <label className="block">
                                <span className="mb-2 block text-xs font-semibold text-slate-400">
                                    搜索作品
                                </span>
                                <input
                                    value={keyword}
                                    onChange={(event) => handleKeywordChange(event.target.value)}
                                    placeholder="输入标题、副标题或别名"
                                    className="h-12 w-full rounded-2xl border border-white/80 bg-white/68 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:shadow-sm"
                                />
                            </label>

                            <YearScroller
                                years={yearOptions}
                                value={selectedYear}
                                onChange={handleYearChange}
                            />

                            <FilterPillGroup
                            label="月份"
                                value={selectedQuarter}
                                options={quarterOptions}
                                onChange={handleQuarterChange}
                            />

                            <FilterPillGroup
                                label="状态"
                                value={selectedStatus}
                                options={statusOptions}
                                onChange={handleStatusChange}
                            />
                        </div>

                        <div className="mx-auto mt-5 max-w-[1180px] border-t border-white/65 pt-5">
                            <TagFilterBar
                                tags={availableTags}
                                value={selectedTagName}
                                onChange={handleTagChange}
                            />
                        </div>
                    </div>
                </GlassCard>

                {feedbackMessage ? (
                    <FeedbackBanner>{feedbackMessage}</FeedbackBanner>
                ) : null}

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedQuarterGradient,
                        ].join(' ')}
                    >
                        <div className="mb-5 flex flex-wrap items-end justify-between gap-4">
                            <div>
                                <h3 className="text-lg font-bold text-slate-800">
                                    作品列表
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    当前展示 {subjects.length} 部作品，共 {pageInfo.totalElements} 部。
                                </p>
                            </div>

                            <div className="flex flex-wrap items-center justify-end gap-2">
                                <div className="flex flex-wrap gap-2">
                                    {sortOptions.map((option) => {
                                        const selected = sort === option.value

                                        return (
                                            <button
                                                key={option.value}
                                                type="button"
                                                onClick={() => handleSortChange(option.value)}
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

                                <span className="rounded-full bg-white/65 px-3 py-1.5 text-xs text-slate-400">
                                    {pageText}
                                </span>
                            </div>
                        </div>

                        <div key={resultAnimationKey} className="content-enter">
                            {listLoading ? (
                                <LoadingGrid />
                            ) : listError ? (
                                <ErrorState
                                    title="作品列表加载失败"
                                    message={listError}
                                    onRetry={() => void loadSubjects()}
                                />
                            ) : subjects.length > 0 ? (
                                <div className="subject-grid">
                                    {subjects.map((subject) => (
                                        <SubjectCard
                                            key={subject.id}
                                            subject={subject}
                                            onOpenDetail={handleOpenDetail}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <EmptyState
                                    title="没有匹配的作品"
                        description="可以尝试切换类型、年份、月份、状态或搜索关键词。"
                                />
                            )}
                        </div>

                        <div className="mt-6 flex flex-wrap items-center justify-between gap-3 border-t border-white/65 pt-5">
                            <p className="text-sm font-semibold text-slate-400">{pageText}</p>

                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    disabled={pageInfo.first || listLoading}
                                    onClick={() => setCurrentPage((page) => Math.max(0, page - 1))}
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-semibold transition',
                                        pageInfo.first || listLoading
                                            ? 'cursor-not-allowed border-white/50 bg-white/35 text-slate-300'
                                            : 'border-white/80 bg-white/70 text-slate-600 hover:-translate-y-0.5 hover:bg-white',
                                    ].join(' ')}
                                >
                                    上一页
                                </button>

                                <button
                                    type="button"
                                    disabled={pageInfo.last || listLoading || pageInfo.totalPages === 0}
                                    onClick={() =>
                                        setCurrentPage((page) =>
                                            Math.min(pageInfo.totalPages - 1, page + 1),
                                        )
                                    }
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-semibold transition',
                                        pageInfo.last || listLoading || pageInfo.totalPages === 0
                                            ? 'cursor-not-allowed border-white/50 bg-white/35 text-slate-300'
                                            : 'border-white/80 bg-white/70 text-slate-600 hover:-translate-y-0.5 hover:bg-white',
                                    ].join(' ')}
                                >
                                    下一页
                                </button>
                            </div>
                        </div>
                    </div>
                </GlassCard>
            </div>

            <SubjectDetailDrawer
                open={selectedSubject !== null}
                subject={selectedSubject}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={handleOpenAddRecord}
                onAddToWant={handleAddToWant}
                onAddToPaused={handleAddToPaused}
                onSetToNone={handleSetToNone}
                onDeleteSubject={requestSubjectDelete}
                statusAction={statusAction}
                deleteAction={subjectDeleting ? 'SUBJECT' : null}
                detailLoading={detailLoading}
            />

            <AddRecordModal
                open={addRecordSubject !== null}
                subject={addRecordSubject}
                onClose={() => setAddRecordSubject(null)}
                onSubmit={handleSubmitRecord}
                submitting={recordSubmitting}
            />

            {confirmCopy ? (
                <ConfirmDialog
                    open={pendingStatusChange !== null}
                    title={confirmCopy.title}
                    description={confirmCopy.description}
                    confirmText={confirmCopy.confirmText}
                    loading={statusAction !== null}
                    tone="danger"
                    onCancel={closeConfirmDialog}
                    onConfirm={() => void confirmStatusChange()}
                />
            ) : null}

            <ConfirmDialog
                open={pendingSubjectDelete !== null}
                title="删除作品？"
                description={
                    pendingSubjectDelete
                        ? `这会从作品库删除《${pendingSubjectDelete.displayTitle}》。如果它已有正式记录或收藏，后端会拒绝删除，请先删除对应记录或收藏。`
                        : ''
                }
                confirmText="删除作品"
                loading={subjectDeleting}
                tone="danger"
                onCancel={closeSubjectDeleteDialog}
                onConfirm={() => void confirmSubjectDelete()}
            />
        </>
    )
}

export default LibraryPage
