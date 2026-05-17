type PageHeroProps = {
    eyebrow: string
    title: string
    description: string
}

function PageHero({ eyebrow, title, description }: PageHeroProps) {
    return (
        <section className="overflow-hidden rounded-[30px] border border-white/70 bg-white/54 p-6 shadow-[0_20px_70px_rgba(120,135,160,0.13)] backdrop-blur-xl sm:p-8">
            <p className="text-xs font-semibold tracking-[0.32em] text-sky-500">
                {eyebrow}
            </p>

            <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-800 sm:text-3xl">
                {title}
            </h2>

            <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-500">
                {description}
            </p>
        </section>
    )
}

export default PageHero
