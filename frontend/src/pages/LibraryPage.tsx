import PageHero from '../components/layout/PageHero'

function LibraryPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="SUBJECT LIBRARY"
                title="作品库"
                description="作品库是系统的核心入口。后续这里会实现类型切换、组合筛选、分页、作品卡片、详情抽屉和添加已看。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                作品库页待开发
            </section>
        </div>
    )
}

export default LibraryPage