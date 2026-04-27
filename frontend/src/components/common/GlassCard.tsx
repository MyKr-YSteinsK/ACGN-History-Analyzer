import type { ReactNode } from 'react'

type GlassCardProps = {
    children: ReactNode
    className?: string
}

function GlassCard({ children, className = '' }: GlassCardProps) {
    return (
        <section
            className={[
                'rounded-[32px] border border-white/70 bg-white/45 shadow-sm backdrop-blur-xl',
                className,
            ].join(' ')}
        >
            {children}
        </section>
    )
}

export default GlassCard