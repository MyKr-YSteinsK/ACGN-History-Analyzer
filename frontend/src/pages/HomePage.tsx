import PageHero from '../components/layout/PageHero'

function HomePage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="DASHBOARD"
                title="首页统计门面"
                description="后续这里会按照原型设计放置年度类型总览、四季分布、评分前三、季度类型构成和相对往年变化。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                首页统计区域待开发
            </section>
        </div>
    )
}

export default HomePage