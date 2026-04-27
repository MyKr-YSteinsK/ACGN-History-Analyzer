import PageHero from '../components/layout/PageHero'

function RecordsPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="MY RECORDS"
                title="我的记录"
                description="这里只展示正式看过、读过或玩过的记录，不展示想看和搁置。后续会加入年份、季节、搜索定位和四类型分区。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                我的记录页待开发
            </section>
        </div>
    )
}

export default RecordsPage