import type { ReactNode } from 'react'
import GlassCard from '../common/GlassCard'

type SidePanelProps = {
    eyebrow: string
    title: string
    gradient: string
    children: ReactNode
}

function SidePanel({ eyebrow, title, gradient, children }: SidePanelProps) {
    return (
        <GlassCard className="overflow-hidden">
            <div className={['bg-gradient-to-br p-5', gradient].join(' ')}>
                <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                    {eyebrow}
                </p>
                <h3 className="mt-2 text-lg font-black text-slate-800">
                    {title}
                </h3>
                <div className="mt-4">{children}</div>
            </div>
        </GlassCard>
    )
}

export default SidePanel
