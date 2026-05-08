import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState, type ReactNode } from 'react'
import {
    deleteCollection,
    getCollections,
    updateCollection,
    type UserSubjectCollectionResponse,
} from '../api/collectionApi'
import { getApiErrorMessage } from '../api/request'
import { createRecord } from '../api/recordApi'
import { getSubjectById } from '../api/subjectApi'
import CategorySwitch from '../components/common/CategorySwitch'
import ConfirmDialog from '../components/common/ConfirmDialog'
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
import { GRID_PAGE_SIZE } from '../constants/pagination'
import { mapSubjectDetailToCardView } from '../mappers/subjectMapper'
import {
    categoryTheme,
    libraryStatusTheme,
    quarterTheme,
} from '../theme/acgnTheme'
import {
    getReleaseQuarterLabel,
    type AcgnCategory,
    type CollectionType,
} from '../types/common'
import type { PageResponse } from '../types/api'
import type { SubjectCardView } from '../types/subject'
import { invalidateLibraryRelatedQueries } from '../query/invalidateDataCaches'
import { queryKeys } from '../query/queryKeys'
import { usePersistentState } from '../hooks/usePersistentState'

type CollectionPageState = Pick<
    PageResponse<unknown>,
    'page' | 'size' | 'totalElements' | 'totalPages' | 'first' | 'last'
>

const initialPageState: CollectionPageState = {
    page: 0,
    size: GRID_PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
}

const collectionTypeOptions: Array<{
    value: CollectionType
    label: string
    description: string
}> = [
    {
        value: 'WANT',
        label: '想看',
        description: '之后准备正式补完的作品',
    },
    {
        value: 'PAUSED',
        label: '搁置',
        description: '暂时停下但还想保留的作品',
    },
]

function getReleaseText(collection: UserSubjectCollectionResponse) {
    if (!collection.subjectReleaseYear) {
        return null
    }

    const quarterLabel = collection.subjectReleaseQuarter
        ? getReleaseQuarterLabel(collection.subjectReleaseQuarter)
        : ''

    return quarterLabel
        ? `${collection.subjectReleaseYear}${quarterLabel}`
        : String(collection.subjectReleaseYear)
}

function getReleaseChipClassName(collection: UserSubjectCollectionResponse) {
    if (!collection.subjectReleaseQuarter) {
        return 'border-amber-200/75 bg-amber-50/90 text-amber-700'
    }

    const theme = quarterTheme[collection.subjectReleaseQuarter]
    return `${theme.bg} ${theme.text} ${theme.border}`
}

function formatCollectionDate(value?: string | null) {
    if (!value) {
        return '收藏记录'
    }

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
        return '收藏记录'
    }

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')

    return `${year}.${month}.${day}`
}

function CollectionsPage() {
    const queryClient = useQueryClient()
    const [selectedType, setSelectedType] = usePersistentState<CollectionType>(
        'acgn.collections.selectedType',
        'WANT',
    )
    const [selectedCategory, setSelectedCategory] = usePersistentState<AcgnCategory>(
        'acgn.collections.selectedCategory',
        'ANIME',
    )
    const [keyword, setKeyword] = usePersistentState('acgn.collections.keyword', '')
    const [currentPage, setCurrentPage] = usePersistentState('acgn.collections.currentPage', 0)
    const [actionLoading, setActionLoading] = useState(false)
    const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
    const [openMenuId, setOpenMenuId] = useState<number | null>(null)
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [addRecordSubject, setAddRecordSubject] = useState<SubjectCardView | null>(null)
    const [pendingDelete, setPendingDelete] =
        useState<UserSubjectCollectionResponse | null>(null)
    const [detailLoading, setDetailLoading] = useState(false)
    const [recordSubmitting, setRecordSubmitting] = useState(false)

    const selectedCategoryTheme = categoryTheme[selectedCategory]
    const selectedTypeTheme = libraryStatusTheme[selectedType]
    const resultAnimationKey = [
        selectedType,
        selectedCategory,
        keyword,
        currentPage,
    ].join('-')

    const collectionQueryParams = useMemo(
        () => ({
            type: selectedType,
            category: selectedCategory,
            keyword: keyword.trim() || undefined,
            page: currentPage,
            size: GRID_PAGE_SIZE,
        }),
        [currentPage, keyword, selectedCategory, selectedType],
    )

    const collectionsQuery = useQuery({
        queryKey: queryKeys.collections(collectionQueryParams),
        queryFn: () => getCollections(collectionQueryParams),
        placeholderData: (previousData) => previousData,
    })

    const collections = collectionsQuery.data?.content ?? []

    const pageInfo = useMemo<CollectionPageState>(() => {
        if (!collectionsQuery.data) {
            return initialPageState
        }

        const response = collectionsQuery.data
        return {
            page: response.page,
            size: response.size,
            totalElements: response.totalElements,
            totalPages: response.totalPages,
            first: response.first,
            last: response.last,
        }
    }, [collectionsQuery.data])

    const listError = collectionsQuery.isError
        ? getApiErrorMessage(collectionsQuery.error)
        : null
    const listBlocking = collectionsQuery.isPending && !collectionsQuery.data
    const listFetching = collectionsQuery.isFetching

    function resetPage() {
        setCurrentPage(0)
    }

    function handleTypeChange(type: CollectionType) {
        resetPage()
        setOpenMenuId(null)
        setSelectedType(type)
    }

    function handleCategoryChange(category: AcgnCategory) {
        resetPage()
        setOpenMenuId(null)
        setSelectedCategory(category)
    }

    function handleKeywordChange(value: string) {
        resetPage()
        setKeyword(value)
    }

    async function loadSubjectForAction(subjectId: number) {
        const detail = await getSubjectById(subjectId)
        return mapSubjectDetailToCardView(detail)
    }

    async function handleOpenDetail(collection: UserSubjectCollectionResponse) {
        setDetailLoading(true)
        setFeedbackMessage(null)
        setOpenMenuId(null)

        try {
            setSelectedSubject(await loadSubjectForAction(collection.subjectId))
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setDetailLoading(false)
        }
    }

    async function handleOpenAddRecord(collection: UserSubjectCollectionResponse) {
        setActionLoading(true)
        setFeedbackMessage(null)
        setOpenMenuId(null)

        try {
            setAddRecordSubject(await loadSubjectForAction(collection.subjectId))
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setActionLoading(false)
        }
    }

    async function handleMoveCollection(collection: UserSubjectCollectionResponse) {
        const targetType: CollectionType =
            collection.collectionType === 'WANT' ? 'PAUSED' : 'WANT'

        setActionLoading(true)
        setFeedbackMessage(null)
        setOpenMenuId(null)

        try {
            await updateCollection(collection.id, {
                collectionType: targetType,
                note: collection.note,
            })
            invalidateLibraryRelatedQueries(queryClient)
            setFeedbackMessage(targetType === 'WANT' ? '已移至想看' : '已移至搁置')
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setActionLoading(false)
        }
    }

    async function handleConfirmDelete() {
        if (!pendingDelete) {
            return
        }

        setActionLoading(true)
        setFeedbackMessage(null)

        try {
            await deleteCollection(pendingDelete.id)
            invalidateLibraryRelatedQueries(queryClient)
            setPendingDelete(null)
            setFeedbackMessage('已删除收藏')
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setActionLoading(false)
        }
    }

    async function handleSubmitRecord(subject: SubjectCardView, payload: AddRecordPayload) {
        setRecordSubmitting(true)
        setFeedbackMessage(null)

        try {
            await createRecord({
                subjectId: subject.id,
                recordYear: payload.recordYear,
                recordQuarter: payload.recordQuarter,
                scoreValue: payload.scoreValue,
                note: payload.note || null,
            })
            invalidateLibraryRelatedQueries(queryClient)
            setAddRecordSubject(null)
            setSelectedSubject(await loadSubjectForAction(subject.id))
            setFeedbackMessage('已添加正式记录，作品已从收藏夹移出')
        } catch (error) {
            setFeedbackMessage(getApiErrorMessage(error))
        } finally {
            setRecordSubmitting(false)
        }
    }

    const pageText = useMemo(() => {
        if (pageInfo.totalPages === 0) {
            return '第 0 / 0 页'
        }

        return `第 ${pageInfo.page + 1} / ${pageInfo.totalPages} 页`
    }, [pageInfo.page, pageInfo.totalPages])
    const listLabel = selectedType === 'WANT' ? '想看清单' : '搁置清单'

    return (
        <>
            <div className="space-y-6 content-enter">
                <PageHero
                    eyebrow="COLLECTIONS"
                    title="收藏夹"
                    description="收藏夹只管理想看和搁置，不进入正式记录统计；当作品添加为正式记录后，会自动从收藏夹中移出。"
                />

                <CollectionOverviewStrip
                    totalElements={pageInfo.totalElements}
                    currentPageCount={collections.length}
                    pageText={pageText}
                    listLabel={listLabel}
                    selectedType={selectedType}
                    selectedCategory={selectedCategory}
                    keyword={keyword}
                />

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedCategoryTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="grid gap-5 xl:grid-cols-[1fr_1.25fr]">
                            <div>
                                <p className="mb-2 text-xs font-semibold text-slate-400">
                                    清单
                                </p>
                                <CollectionTypeTabs
                                    value={selectedType}
                                    onChange={handleTypeChange}
                                />
                            </div>

                            <label className="block">
                                <span className="mb-2 block text-xs font-semibold text-slate-400">
                                    搜索收藏
                                </span>
                                <input
                                    value={keyword}
                                    onChange={(event) => handleKeywordChange(event.target.value)}
                                    placeholder="输入作品标题"
                                    className="h-12 w-full rounded-2xl border border-white/80 bg-white/72 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:shadow-sm"
                                />
                            </label>
                        </div>
                    </div>
                </GlassCard>

                <CategorySwitch
                    value={selectedCategory}
                    onChange={handleCategoryChange}
                />

                {feedbackMessage ? (
                    <FeedbackBanner>{feedbackMessage}</FeedbackBanner>
                ) : null}

                <GlassCard className="overflow-hidden">
                    <div
                        className={[
                            'bg-gradient-to-br p-6 transition-all duration-300',
                            selectedCategoryTheme.softGradient,
                        ].join(' ')}
                    >
                        <div className="mb-5 flex flex-wrap items-end justify-between gap-4">
                            <div>
                                <h3 className="text-lg font-black text-slate-800">
                                    {selectedCategoryTheme.cnName} · {selectedTypeTheme.label}
                                </h3>
                                <p className="mt-1 text-sm text-slate-400">
                                    当前展示 {collections.length} 部作品，共 {pageInfo.totalElements} 部。
                                </p>
                            </div>

                            <div className="flex flex-wrap gap-2 text-xs text-slate-400">
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    按加入时间排序
                                </span>
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    {pageText}
                                </span>
                            </div>
                        </div>

                        <div key={resultAnimationKey} className="content-enter">
                            {listBlocking ? (
                                <LoadingGrid cardClassName="h-[410px]" />
                            ) : listError ? (
                                <ErrorState
                                    title="收藏夹加载失败"
                                    message={listError}
                                    onRetry={() => void collectionsQuery.refetch()}
                                />
                            ) : collections.length > 0 ? (
                                <div className="subject-grid">
                                    {collections.map((collection) => (
                                        <CollectionCard
                                            key={collection.id}
                                            collection={collection}
                                            fallbackCategory={selectedCategory}
                                            menuOpen={openMenuId === collection.id}
                                            actionLoading={actionLoading}
                                            onToggleMenu={() =>
                                                setOpenMenuId((current) =>
                                                    current === collection.id ? null : collection.id,
                                                )
                                            }
                                            onOpenDetail={handleOpenDetail}
                                            onMove={handleMoveCollection}
                                            onDelete={(target) => {
                                                setOpenMenuId(null)
                                                setPendingDelete(target)
                                            }}
                                            onAddRecord={handleOpenAddRecord}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <EmptyState
                                    title="没有匹配的收藏"
                                    description="可以切换清单、类型，或调整搜索关键词。"
                                />
                            )}
                        </div>

                        <div className="mt-6 flex flex-wrap items-center justify-between gap-3 border-t border-white/65 pt-5">
                            <p className="text-sm font-semibold text-slate-400">{pageText}</p>

                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    disabled={pageInfo.first || listFetching}
                                    onClick={() => setCurrentPage((page) => Math.max(0, page - 1))}
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-semibold transition',
                                        pageInfo.first || listFetching
                                            ? 'cursor-not-allowed border-white/50 bg-white/35 text-slate-300'
                                            : 'border-white/80 bg-white/70 text-slate-600 hover:-translate-y-0.5 hover:bg-white',
                                    ].join(' ')}
                                >
                                    上一页
                                </button>

                                <button
                                    type="button"
                                    disabled={pageInfo.last || listFetching || pageInfo.totalPages === 0}
                                    onClick={() =>
                                        setCurrentPage((page) =>
                                            Math.min(pageInfo.totalPages - 1, page + 1),
                                        )
                                    }
                                    className={[
                                        'rounded-2xl border px-4 py-2.5 text-sm font-semibold transition',
                                        pageInfo.last || listFetching || pageInfo.totalPages === 0
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
                onAddRecord={(subject) => setAddRecordSubject(subject)}
                onAddToWant={() => undefined}
                onAddToPaused={() => undefined}
                onSetToNone={() => undefined}
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
                open={pendingDelete !== null}
                title="删除收藏？"
                description={
                    pendingDelete
                        ? `这会把《${pendingDelete.subjectTitle}》从${pendingDelete.collectionTypeLabel}清单中移除，不会删除作品库里的作品。`
                        : ''
                }
                confirmText="删除收藏"
                loading={actionLoading}
                tone="danger"
                onCancel={() => {
                    if (!actionLoading) {
                        setPendingDelete(null)
                    }
                }}
                onConfirm={() => void handleConfirmDelete()}
            />
        </>
    )
}

type CollectionTypeTabsProps = {
    value: CollectionType
    onChange: (type: CollectionType) => void
}

type CollectionOverviewStripProps = {
    totalElements: number
    currentPageCount: number
    pageText: string
    listLabel: string
    selectedType: CollectionType
    selectedCategory: AcgnCategory
    keyword: string
}

function CollectionOverviewStrip({
    totalElements,
    currentPageCount,
    pageText,
    listLabel,
    selectedType,
    selectedCategory,
    keyword,
}: CollectionOverviewStripProps) {
    const category = categoryTheme[selectedCategory]
    const typeTheme = libraryStatusTheme[selectedType]
    const searchText = keyword.trim() || '无关键词'

    return (
        <GlassCard className="overflow-hidden">
            <div
                className={[
                    'bg-gradient-to-br p-5 transition-all duration-300',
                    category.softGradient,
                ].join(' ')}
            >
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            collection overview
                        </p>
                        <h3 className="mt-1 text-xl font-black text-slate-800">
                            {category.cnName} · {listLabel}
                        </h3>
                        <p className="mt-1 text-sm text-slate-400">
                            收藏夹不参与年份月份统计，只作为补完队列和暂存队列。
                        </p>
                    </div>

                    <div className="grid w-full gap-3 sm:grid-cols-2 xl:w-auto xl:grid-cols-4">
                        <OverviewMetric label="总量" value={totalElements} />
                        <OverviewMetric label="当前页" value={currentPageCount} />
                        <OverviewMetric label="分页" value={pageText} />
                        <OverviewMetric
                            label="搜索"
                            value={searchText}
                            toneClassName={keyword.trim() ? 'text-sky-700' : 'text-slate-500'}
                        />
                    </div>
                </div>

                <div className="mt-4 flex flex-wrap gap-2">
                    <span
                        className={[
                            'rounded-full border px-3 py-1.5 text-xs font-black',
                            typeTheme.className,
                        ].join(' ')}
                    >
                        {typeTheme.label}
                    </span>
                    <span
                        className={[
                            'rounded-full border px-3 py-1.5 text-xs font-black',
                            category.border,
                            category.bg,
                            category.text,
                        ].join(' ')}
                    >
                        {category.cnName}
                    </span>
                    <span className="rounded-full border border-white/75 bg-white/62 px-3 py-1.5 text-xs font-semibold text-slate-400">
                        添加正式记录后自动离开收藏夹
                    </span>
                </div>
            </div>
        </GlassCard>
    )
}

type OverviewMetricProps = {
    label: string
    value: string | number
    toneClassName?: string
}

function OverviewMetric({
    label,
    value,
    toneClassName = 'text-slate-800',
}: OverviewMetricProps) {
    return (
        <div className="min-w-[112px] rounded-[22px] border border-white/75 bg-white/62 px-4 py-3 text-center shadow-sm backdrop-blur">
            <p className={['truncate text-lg font-black', toneClassName].join(' ')}>
                {value}
            </p>
            <p className="mt-1 text-xs font-semibold text-slate-400">{label}</p>
        </div>
    )
}

function CollectionTypeTabs({ value, onChange }: CollectionTypeTabsProps) {
    return (
        <div className="grid gap-3 sm:grid-cols-2">
            {collectionTypeOptions.map((option) => {
                const selected = value === option.value
                const theme = libraryStatusTheme[option.value]

                return (
                    <button
                        key={option.value}
                        type="button"
                        onClick={() => onChange(option.value)}
                        className={[
                            'rounded-[24px] border p-4 text-left transition-all duration-200',
                            selected
                                ? `${theme.className} shadow-sm`
                                : 'border-white/75 bg-white/50 text-slate-500 hover:-translate-y-0.5 hover:bg-white/75',
                        ].join(' ')}
                    >
                        <p className="text-base font-black">{option.label}</p>
                        <p className="mt-1 text-xs leading-5 opacity-75">
                            {option.description}
                        </p>
                    </button>
                )
            })}
        </div>
    )
}

type CollectionCardProps = {
    collection: UserSubjectCollectionResponse
    fallbackCategory: AcgnCategory
    menuOpen: boolean
    actionLoading: boolean
    onToggleMenu: () => void
    onOpenDetail: (collection: UserSubjectCollectionResponse) => void
    onMove: (collection: UserSubjectCollectionResponse) => void
    onDelete: (collection: UserSubjectCollectionResponse) => void
    onAddRecord: (collection: UserSubjectCollectionResponse) => void
}

function CollectionCard({
                            collection,
                            fallbackCategory,
                            menuOpen,
                            actionLoading,
                            onToggleMenu,
                            onOpenDetail,
                            onMove,
                            onDelete,
                            onAddRecord,
                        }: CollectionCardProps) {
    const category = categoryTheme[collection.subjectCategory ?? fallbackCategory]
    const releaseText = getReleaseText(collection)
    const releaseChipClassName = getReleaseChipClassName(collection)
    const typeTheme = libraryStatusTheme[collection.collectionType]
    const moveTargetText = collection.collectionType === 'WANT' ? '搁置' : '想看'
    const collectionDate = formatCollectionDate(collection.createdAt)

    return (
        <article
            className={[
                'group relative flex h-full flex-col overflow-hidden rounded-[24px] border border-white/70 bg-gradient-to-br text-left shadow-sm backdrop-blur transition-all duration-200',
                'hover:-translate-y-1 hover:shadow-[0_18px_48px_rgba(120,135,160,0.16)]',
                category.softGradient,
            ].join(' ')}
        >
            <button
                type="button"
                onClick={() => onOpenDetail(collection)}
                className="block text-left"
            >
                <div className="relative aspect-[3/4] overflow-hidden bg-slate-100">
                    <CoverImage
                        src={collection.subjectCoverUrl}
                        alt={collection.subjectTitle}
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.04]"
                        fallback={
                        <div
                            className={[
                                'flex h-full w-full items-center justify-center bg-gradient-to-br',
                                category.softGradient,
                            ].join(' ')}
                        >
                            <span className={['text-6xl font-black', category.text].join(' ')}>
                                {category.letter}
                            </span>
                        </div>
                        }
                    />

                    <div className="absolute left-3 top-3">
                        <span
                            className={[
                                'rounded-full border px-3 py-1 text-xs font-semibold shadow-sm backdrop-blur',
                                typeTheme.className,
                            ].join(' ')}
                        >
                            {typeTheme.label}
                        </span>
                    </div>
                </div>
            </button>

            <button
                type="button"
                aria-label="打开收藏操作菜单"
                disabled={actionLoading}
                onClick={onToggleMenu}
                className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full border border-white/75 bg-white/80 text-xl font-black leading-none text-slate-500 shadow-sm transition hover:bg-white hover:text-slate-800 disabled:cursor-not-allowed disabled:text-slate-300"
            >
                ⋯
            </button>

            {menuOpen ? (
                <div className="absolute right-3 top-14 z-10 w-40 overflow-hidden rounded-[18px] border border-white/75 bg-white/92 p-1.5 shadow-[0_16px_45px_rgba(80,90,120,0.18)] backdrop-blur-xl">
                    <MenuButton onClick={() => onAddRecord(collection)}>
                        添加已看
                    </MenuButton>
                    <MenuButton onClick={() => onMove(collection)}>
                        移至{moveTargetText}
                    </MenuButton>
                    <MenuButton tone="danger" onClick={() => onDelete(collection)}>
                        删除收藏
                    </MenuButton>
                </div>
            ) : null}

            <div className="m-2.5 flex flex-1 flex-col rounded-[20px] border border-white/65 bg-white/58 p-3.5 shadow-sm backdrop-blur">
                <div className="min-w-0">
                    <h3 className="line-clamp-1 text-[14px] font-bold text-slate-800">
                        {collection.subjectTitle}
                    </h3>
                    <p className="mt-0.5 text-[11px] font-medium text-slate-400">
                        加入于 {collectionDate}
                    </p>
                </div>

                <div className="mt-3 flex flex-wrap gap-1.5">
                    <span
                        className={[
                            'rounded-full px-2.5 py-0.5 text-[11px] font-semibold',
                            category.bg,
                            category.text,
                        ].join(' ')}
                    >
                        {category.cnName}
                    </span>

                    {releaseText ? (
                        <span
                            className={[
                                'rounded-full border px-2.5 py-0.5 text-[11px] font-semibold',
                                releaseChipClassName,
                            ].join(' ')}
                        >
                            {releaseText}
                        </span>
                    ) : null}

                </div>

                <div className="mt-3 min-h-[48px] rounded-[16px] border border-white/65 bg-white/45 px-3 py-2">
                    <p className="line-clamp-2 text-xs leading-5 text-slate-400">
                        {collection.note ?? '暂无收藏备注'}
                    </p>
                </div>

                <div className="mt-auto grid grid-cols-2 gap-2 border-t border-white/70 pt-3">
                    <button
                        type="button"
                        onClick={() => onOpenDetail(collection)}
                        className="rounded-full border border-slate-200/70 bg-white/75 px-3 py-1.5 text-[11px] font-semibold text-slate-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-slate-900 hover:text-white"
                    >
                        查看详情
                    </button>

                    <button
                        type="button"
                        disabled={actionLoading}
                        onClick={() => onAddRecord(collection)}
                        className="rounded-full border border-rose-100 bg-rose-50/85 px-3 py-1.5 text-[11px] font-semibold text-rose-600 transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-100 disabled:cursor-not-allowed disabled:text-rose-300"
                    >
                        添加已看
                    </button>
                </div>
            </div>
        </article>
    )
}

type MenuButtonProps = {
    children: ReactNode
    tone?: 'normal' | 'danger'
    onClick: () => void
}

function MenuButton({ children, tone = 'normal', onClick }: MenuButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'block w-full rounded-[14px] px-3 py-2 text-left text-xs font-semibold transition',
                tone === 'danger'
                    ? 'text-rose-600 hover:bg-rose-50'
                    : 'text-slate-600 hover:bg-slate-50',
            ].join(' ')}
        >
            {children}
        </button>
    )
}

export default CollectionsPage
