import PageHero from '../components/layout/PageHero'

function ImportPage() {
    return (
        <div className="space-y-6">
            <PageHero
                eyebrow="IMPORT"
                title="导入中心"
                description="后续这里会支持 CSV、Excel、文本粘贴、Bangumi 链接、JSON 备份恢复和手动快速录入。"
            />

            <section className="rounded-[32px] border border-white/70 bg-white/45 p-8 text-sm text-slate-500 shadow-sm backdrop-blur-xl">
                导入页待开发
            </section>
        </div>
    )
}

export default ImportPage