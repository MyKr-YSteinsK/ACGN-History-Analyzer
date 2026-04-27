import PageHero from '../components/layout/PageHero'

function CollectionsPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="COLLECTIONS"
                title="收藏夹"
                description="收藏夹只管理想看和搁置，不按年份季度展示。后续会加入类型筛选、想看/搁置切换、分页和更多操作菜单。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                收藏夹页待开发
            </section>
        </div>
    )
}

export default CollectionsPage