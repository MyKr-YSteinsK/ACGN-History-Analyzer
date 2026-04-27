import PageHero from '../components/layout/PageHero'

function TopPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="TOP RANKING"
                title="TOP 榜单"
                description="后续这里会按照原型设计展示四类型榜单、前三名作品、分数段统计和高分作品列表。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                TOP 榜单页待开发
            </section>
        </div>
    )
}

export default TopPage