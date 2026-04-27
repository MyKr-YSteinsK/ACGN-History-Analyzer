import PageHero from '../components/layout/PageHero'

function FranchisesPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="FRANCHISES"
                title="系列管理"
                description="后续这里会展示跨动画、漫画、游戏、小说的系列归档、横向时间线和系列统计。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                系列页待开发
            </section>
        </div>
    )
}

export default FranchisesPage