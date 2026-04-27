type PageHeroProps = {
    eyebrow: string
    title: string
    description: string
}

function PageHero({ eyebrow, title, description }: PageHeroProps) {
    return (
        <section className="rounded-[36px] border border-white/70 bg-white/50 p-8 shadow-[0_24px_80px_rgba(120,135,160,0.14)] backdrop-blur-xl">
            <p className="text-xs font-semibold tracking-[0.32em] text-sky-500">
                {eyebrow}
            </p>

            <h2 className="mt-3 text-3xl font-bold tracking-tight text-slate-800">
                {title}
            </h2>

            <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-500">
                {description}
            </p>
        </section>
    )
}

export default PageHero