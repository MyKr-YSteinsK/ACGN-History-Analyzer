import { useMemo, useState } from 'react'
import CategorySelector from '../components/common/CategorySelector'
import FilterPillGroup from '../components/common/FilterPillGroup'
import GlassCard from '../components/common/GlassCard'
import YearScroller from '../components/common/YearScroller'
import PageHero from '../components/layout/PageHero'
import SubjectCard from '../components/subject/SubjectCard'
import SubjectDetailDrawer from '../components/subject/SubjectDetailDrawer'
import {
    neutralPanelGradient,
    quarterTheme,
} from '../theme/acgnTheme'
import AddRecordModal, {
    type AddRecordPayload,
} from '../components/subject/AddRecordModal'
import type { AcgnCategory, LibraryStatus, ReleaseQuarter } from '../types/common'
import type { SubjectCardView } from '../types/subject'

const mockSubjects: SubjectCardView[] = [
    {
        id: 1,
        displayTitle: '葬送的芙莉莲',
        titleCn: '葬送的芙莉莲',
        subtitle: 'Frieren: Beyond Journey’s End',
        category: 'ANIME',
        releaseYear: 2023,
        releaseQuarter: 4,
        seasonIndex: 1,
        partIndex: null,
        coverUrl: null,
        studioName: 'Madhouse',
        platformScore: 8.9,
        personalScore: 48,
        personalRecordYear: 2024,
        personalRecordQuarter: 1,
        personalRecordNote: '非常完整的旅途感和时间感。',
        episodeCount: 28,
        tags: ['奇幻', '治愈', '公路片'],
        libraryStatus: 'WATCHED',
        summary:
            '勇者一行打败魔王后的漫长旅途已经结束，精灵魔法使芙莉莲开始重新理解人类、时间与记忆的意义。',
        platformLink: 'https://bangumi.tv/',
        sourcePlatform: 'BANGUMI',
        sourceId: 'frieren-demo',
        franchiseName: '葬送的芙莉莲',
    },
    {
        id: 2,
        displayTitle: '孤独摇滚！',
        titleCn: '孤独摇滚！',
        subtitle: 'Bocchi the Rock!',
        category: 'ANIME',
        releaseYear: 2022,
        releaseQuarter: 4,
        seasonIndex: 1,
        partIndex: null,
        coverUrl: null,
        studioName: 'CloverWorks',
        platformScore: 8.6,
        episodeCount: 12,
        tags: ['音乐', '日常', '喜剧'],
        libraryStatus: 'WANT',
        summary:
            '极度怕生的少女后藤一里加入乐队，在音乐、社交和舞台中逐渐改变自己的青春喜剧。',
        platformLink: 'https://bangumi.tv/',
        sourcePlatform: 'BANGUMI',
        sourceId: 'bocchi-demo',
        franchiseName: '孤独摇滚！',
    },
    {
        id: 3,
        displayTitle: '跃动青春',
        titleCn: '跃动青春',
        subtitle: 'Skip and Loafer',
        category: 'ANIME',
        releaseYear: 2023,
        releaseQuarter: 2,
        seasonIndex: 1,
        partIndex: null,
        coverUrl: null,
        studioName: 'P.A.WORKS',
        platformScore: 8.1,
        episodeCount: 12,
        tags: ['校园', '青春', '日常'],
        libraryStatus: 'NONE',
        summary:
            '从乡下到东京读书的少女与同学们相遇，在日常校园生活中展开清爽自然的青春故事。',
        platformLink: 'https://bangumi.tv/',
        sourcePlatform: 'BANGUMI',
        sourceId: 'skip-loafer-demo',
        franchiseName: '跃动青春',
    },
    {
        id: 4,
        displayTitle: 'BanG Dream! Ave Mujica',
        titleCn: 'BanG Dream! Ave Mujica',
        subtitle: 'The Die is Cast',
        category: 'ANIME',
        releaseYear: 2025,
        releaseQuarter: 1,
        seasonIndex: 1,
        partIndex: null,
        coverUrl: null,
        studioName: 'SANZIGEN',
        platformScore: 7.8,
        episodeCount: 13,
        tags: ['音乐', '乐队', '原创'],
        libraryStatus: 'PAUSED',
        summary:
            '围绕乐队与少女们的关系展开，带有强烈戏剧张力和舞台表现的音乐企划作品。',
        platformLink: 'https://bangumi.tv/',
        sourcePlatform: 'BANGUMI',
        sourceId: 'ave-mujica-demo',
        franchiseName: 'BanG Dream!',
    },
    {
        id: 5,
        displayTitle: '迷宫饭',
        titleCn: '迷宫饭',
        subtitle: 'Dungeon Meshi',
        category: 'COMIC',
        releaseYear: 2014,
        releaseQuarter: null,
        seasonIndex: null,
        partIndex: null,
        coverUrl: null,
        studioName: '九井谅子',
        platformScore: 8.8,
        episodeCount: null,
        tags: ['奇幻', '美食', '冒险'],
        libraryStatus: 'PAUSED',
    },
    {
        id: 6,
        displayTitle: '赛博朋克 2077',
        titleCn: '赛博朋克 2077',
        subtitle: 'Cyberpunk 2077',
        category: 'GAME',
        releaseYear: 2020,
        releaseQuarter: 4,
        seasonIndex: null,
        partIndex: null,
        coverUrl: null,
        studioName: 'CD Projekt RED',
        platformScore: 8.4,
        episodeCount: null,
        tags: ['开放世界', 'RPG', '科幻'],
        libraryStatus: 'WANT',
    },
    {
        id: 7,
        displayTitle: '三体',
        titleCn: '三体',
        subtitle: 'The Three-Body Problem',
        category: 'NOVEL',
        releaseYear: 2006,
        releaseQuarter: null,
        seasonIndex: null,
        partIndex: null,
        coverUrl: null,
        studioName: '刘慈欣',
        platformScore: 9.0,
        episodeCount: null,
        tags: ['科幻', '宇宙', '文明'],
        libraryStatus: 'WATCHED',
    },
]

const yearOptions = [2026, 2025, 2024, 2023, 2022, 2021, 2020, 2019]

const quarterOptions: Array<{
    value: ReleaseQuarter | 'ALL'
    label: string
    className?: string
}> = [
    { value: 'ALL', label: '全部季度' },
    {
        value: 1,
        label: '春',
        className: 'border-green-200 bg-green-100/85 text-green-700 shadow-sm',
    },
    {
        value: 2,
        label: '夏',
        className: 'border-cyan-200 bg-cyan-100/85 text-cyan-700 shadow-sm',
    },
    {
        value: 3,
        label: '秋',
        className: 'border-orange-200 bg-orange-100/85 text-orange-700 shadow-sm',
    },
    {
        value: 4,
        label: '冬',
        className: 'border-blue-200 bg-blue-100/80 text-blue-700 shadow-sm',
    },
]

const statusOptions: Array<{
    value: LibraryStatus | 'ALL'
    label: string
    className?: string
}> = [
    { value: 'ALL', label: '全部状态' },
    {
        value: 'WATCHED',
        label: '看过',
        className: 'border-sky-200 bg-sky-100/85 text-sky-700 shadow-sm',
    },
    {
        value: 'WANT',
        label: '想看',
        className: 'border-emerald-200 bg-emerald-100/85 text-emerald-700 shadow-sm',
    },
    {
        value: 'PAUSED',
        label: '搁置',
        className: 'border-orange-200 bg-orange-100/85 text-orange-700 shadow-sm',
    },
    {
        value: 'NONE',
        label: '未记录',
        className: 'border-slate-200 bg-slate-100/85 text-slate-600 shadow-sm',
    },
]

function LibraryPage() {
    const [subjects, setSubjects] = useState<SubjectCardView[]>(mockSubjects)
    const [selectedCategory, setSelectedCategory] = useState<AcgnCategory>('ANIME')
    const [keyword, setKeyword] = useState('')
    const [selectedStatus, setSelectedStatus] = useState<LibraryStatus | 'ALL'>('ALL')
    const [selectedYear, setSelectedYear] = useState<number | 'ALL'>('ALL')
    const [selectedQuarter, setSelectedQuarter] = useState<ReleaseQuarter | 'ALL'>('ALL')
    const [selectedSubject, setSelectedSubject] = useState<SubjectCardView | null>(null)
    const [addRecordSubject, setAddRecordSubject] = useState<SubjectCardView | null>(null)

    const selectedQuarterGradient =
        selectedQuarter === 'ALL'
            ? neutralPanelGradient
            : quarterTheme[selectedQuarter].panelGradient

    const filteredSubjects = useMemo(() => {
        return subjects.filter((subject) => {
            const matchCategory = subject.category === selectedCategory

            const normalizedKeyword = keyword.trim().toLowerCase()
            const matchKeyword =
                normalizedKeyword.length === 0 ||
                subject.displayTitle.toLowerCase().includes(normalizedKeyword) ||
                subject.subtitle?.toLowerCase().includes(normalizedKeyword)

            const matchStatus =
                selectedStatus === 'ALL' || subject.libraryStatus === selectedStatus

            const matchYear =
                selectedYear === 'ALL' || subject.releaseYear === selectedYear

            const matchQuarter =
                selectedQuarter === 'ALL' || subject.releaseQuarter === selectedQuarter

            return matchCategory && matchKeyword && matchStatus && matchYear && matchQuarter
        })
    }, [keyword, selectedCategory, selectedQuarter, selectedStatus, selectedYear, subjects])

    const resultAnimationKey = [
        selectedCategory,
        selectedYear,
        selectedQuarter,
        selectedStatus,
        keyword,
    ].join('-')

    function handleSubmitRecord(subject: SubjectCardView, payload: AddRecordPayload) {
        console.log('新增记录：', {
            subjectId: subject.id,
            ...payload,
        })

        const updatedSubject: SubjectCardView = {
            ...subject,
            libraryStatus: 'WATCHED',
            personalScore: payload.scoreValue,
            personalRecordYear: payload.recordYear,
            personalRecordQuarter: payload.recordQuarter,
            personalRecordNote: payload.note,
        }

        setSubjects((current) =>
            current.map((item) => (item.id === subject.id ? updatedSubject : item)),
        )

        setSelectedSubject((current) =>
            current?.id === subject.id ? updatedSubject : current,
        )

        setAddRecordSubject(null)
    }

    return (
        <>
            <div className="space-y-6 content-enter">
                <PageHero
                    eyebrow="SUBJECT LIBRARY"
                    title="作品库"
                    description="作品库是系统的核心入口。这里负责管理作品基础信息，并作为添加已看、加入想看、加入搁置和查看详情的起点。"
                />

                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/65 via-white/40 to-sky-50/45 p-6">
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
                            onChange={setSelectedCategory}
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
                                    onChange={(event) => setKeyword(event.target.value)}
                                    placeholder="输入标题、副标题或别名"
                                    className="h-12 w-full rounded-2xl border border-white/80 bg-white/68 px-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:shadow-sm"
                                />
                            </label>

                            <YearScroller
                                years={yearOptions}
                                value={selectedYear}
                                onChange={setSelectedYear}
                            />

                            <FilterPillGroup
                                label="季度"
                                value={selectedQuarter}
                                options={quarterOptions}
                                onChange={setSelectedQuarter}
                            />

                            <FilterPillGroup
                                label="状态"
                                value={selectedStatus}
                                options={statusOptions}
                                onChange={setSelectedStatus}
                            />
                        </div>
                    </div>
                </GlassCard>

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
                                    当前展示 {filteredSubjects.length} 部作品。后续这里会接入后端分页接口。
                                </p>
                            </div>

                            <div className="flex flex-wrap gap-2 text-xs text-slate-400">
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    评分优先
                                </span>
                                <span className="rounded-full bg-white/65 px-3 py-1.5">
                                    分页待接入
                                </span>
                            </div>
                        </div>

                        <div key={resultAnimationKey} className="content-enter">
                            {filteredSubjects.length > 0 ? (
                                <div className="subject-grid">
                                    {filteredSubjects.map((subject) => (
                                        <SubjectCard
                                            key={subject.id}
                                            subject={subject}
                                            onOpenDetail={setSelectedSubject}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className="rounded-[28px] border border-dashed border-slate-200 bg-white/50 p-12 text-center">
                                    <p className="text-base font-semibold text-slate-500">
                                        没有匹配的作品
                                    </p>
                                    <p className="mt-2 text-sm text-slate-400">
                                        可以尝试切换类型、年份、季度、状态或搜索关键词。
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                </GlassCard>
            </div>

            <SubjectDetailDrawer
                open={selectedSubject !== null}
                subject={selectedSubject}
                onClose={() => setSelectedSubject(null)}
                onAddRecord={setAddRecordSubject}
            />

            <AddRecordModal
                open={addRecordSubject !== null}
                subject={addRecordSubject}
                onClose={() => setAddRecordSubject(null)}
                onSubmit={handleSubmitRecord}
            />
        </>
    )
}

export default LibraryPage