import { FolderKanban, Trophy, BookMarked, ChevronRight, Sparkles, Radar, Bookmark, Layers3 } from "lucide-react";
import type { LucideIcon } from "lucide-react";

type PortalCardProps = {
    icon: LucideIcon;
    title: string;
    subtitle: string;
    desc: string;
    badge: string;
};

type MetricCardProps = {
    label: string;
    value: string;
    hint: string;
};

type ShelfCardProps = {
    title: string;
    meta: string;
    tone: string;
};

function PortalCard({ icon: Icon, title, subtitle, desc, badge }: PortalCardProps) {
    return (
        <button className="group relative overflow-hidden rounded-[28px] border border-white/10 bg-white/[0.04] p-5 text-left transition-all duration-300 hover:border-cyan-300/30 hover:bg-white/[0.06] hover:shadow-[0_0_40px_rgba(34,211,238,0.10)]">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(103,232,249,0.10),transparent_35%)]" />
            <div className="relative">
                <div className="mb-4 flex items-center justify-between">
                    <div className="grid h-11 w-11 place-items-center rounded-2xl border border-cyan-300/20 bg-cyan-300/8 text-cyan-200">
                        <Icon className="h-5 w-5" />
                    </div>
                    <div className="rounded-full border border-white/10 px-3 py-1 text-[10px] uppercase tracking-[0.26em] text-zinc-400">
                        {badge}
                    </div>
                </div>
                <div className="text-lg font-semibold text-zinc-50">{title}</div>
                <div className="mt-1 text-sm text-cyan-200/80">{subtitle}</div>
                <p className="mt-4 text-sm leading-7 text-zinc-400">{desc}</p>
                <div className="mt-5 flex items-center gap-2 text-sm text-zinc-300 transition-transform duration-300 group-hover:translate-x-1">
                    进入模块
                    <ChevronRight className="h-4 w-4" />
                </div>
            </div>
        </button>
    );
}

function MetricCard({ label, value, hint }: MetricCardProps) {
    return (
        <div className="rounded-[24px] border border-white/10 bg-white/[0.035] p-4">
            <div className="text-[11px] uppercase tracking-[0.22em] text-zinc-500">{label}</div>
            <div className="mt-3 text-3xl font-black tracking-tight text-zinc-50">{value}</div>
            <div className="mt-2 text-sm text-zinc-400">{hint}</div>
        </div>
    );
}

function ShelfCard({ title, meta, tone }: ShelfCardProps) {
    return (
        <div className="group overflow-hidden rounded-[26px] border border-white/10 bg-white/[0.035]">
            <div className={`aspect-[3/4] w-full ${tone} relative overflow-hidden`}>
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.20),transparent_30%),linear-gradient(135deg,rgba(255,255,255,0.06),transparent_45%),linear-gradient(to_top,rgba(2,6,23,0.55),transparent_55%)]" />
                <div className="absolute left-4 top-4 rounded-full border border-white/15 bg-black/20 px-3 py-1 text-[10px] uppercase tracking-[0.26em] text-white/70 backdrop-blur-md">
                    archive unit
                </div>
                <div className="absolute inset-x-4 bottom-4">
                    <div className="text-xs uppercase tracking-[0.2em] text-white/55">cover placeholder</div>
                    <div className="mt-1 h-px w-full bg-gradient-to-r from-white/40 to-transparent" />
                </div>
            </div>
            <div className="px-4 py-4">
                <div className="text-base font-semibold text-zinc-50">{title}</div>
                <div className="mt-2 text-sm leading-6 text-zinc-400">{meta}</div>
            </div>
        </div>
    );
}

export default function HomepageConceptsPrototype() {
    return (
        <div className="min-h-screen bg-[#07111a] text-zinc-100">
            <div className="fixed inset-0 pointer-events-none">
                <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(34,211,238,0.12),transparent_28%),radial-gradient(circle_at_85%_18%,rgba(125,211,252,0.10),transparent_24%),linear-gradient(to_bottom,rgba(255,255,255,0.03),transparent_18%)]" />
                <div className="absolute inset-0 opacity-[0.06] [background-image:linear-gradient(to_right,white_1px,transparent_1px),linear-gradient(to_bottom,white_1px,transparent_1px)] [background-size:36px_36px]" />
                <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-cyan-200/20 to-transparent" />
            </div>

            <div className="relative mx-auto max-w-[1440px] px-5 pb-20 pt-6 md:px-8 lg:px-10">
                <header className="mb-8 flex items-center justify-between gap-4 rounded-full border border-white/10 bg-white/[0.04] px-4 py-3 backdrop-blur-xl">
                    <div className="flex min-w-0 items-center gap-3">
                        <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full border border-cyan-300/20 bg-cyan-300/8 text-cyan-200">
                            <Radar className="h-4 w-4" />
                        </div>
                        <div className="min-w-0">
                            <div className="truncate text-sm font-semibold tracking-[0.08em] text-zinc-100">ACGN HISTORY ARCHIVE</div>
                            <div className="truncate text-[11px] uppercase tracking-[0.22em] text-zinc-500">black tide archive / winter runtime</div>
                        </div>
                    </div>

                    <nav className="hidden items-center gap-2 md:flex">
                        {[
                            "首页",
                            "我的记录",
                            "作品库",
                            "TOP",
                            "导入"
                        ].map((item, index) => (
                            <button
                                key={item}
                                className={`rounded-full px-4 py-2 text-sm transition-colors ${index === 0 ? "bg-cyan-300/10 text-cyan-200" : "text-zinc-400 hover:text-zinc-200"}`}
                            >
                                {item}
                            </button>
                        ))}
                    </nav>
                </header>

                <section className="grid gap-6 lg:grid-cols-[1.18fr_0.82fr]">
                    <div className="relative overflow-hidden rounded-[36px] border border-white/10 bg-white/[0.045] p-7 md:p-9">
                        <div className="absolute inset-0 bg-[radial-gradient(circle_at_85%_15%,rgba(165,243,252,0.12),transparent_24%)]" />
                        <div className="relative">
                            <div className="mb-5 flex flex-wrap items-center gap-3 text-[11px] uppercase tracking-[0.24em] text-zinc-500">
                <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.03] px-3 py-1.5">
                  <Sparkles className="h-3.5 w-3.5 text-cyan-200" />
                  seasonal runtime / 冬
                </span>
                                <span>personal archive system</span>
                            </div>

                            <div className="max-w-4xl">
                                <h1 className="text-4xl font-black leading-[0.94] tracking-tight text-zinc-50 md:text-6xl xl:text-[72px]">
                                    面向 ACGN 的
                                    <br />
                                    <span className="bg-gradient-to-r from-zinc-50 via-cyan-100 to-blue-200 bg-clip-text text-transparent">
                    私人历史档案馆
                  </span>
                                </h1>
                                <p className="mt-6 max-w-2xl text-sm leading-7 text-zinc-400 md:text-base">
                                    它不是普通后台，也不是泛化的作品库模板。这里更像一座私人档案室：把季度记录、系列归档、偏好波动、个人评价与封面记忆压缩成一个可以长期浏览的系统。
                                </p>
                            </div>

                            <div className="mt-7 flex flex-wrap gap-3">
                                {[
                                    "季度切片",
                                    "系列归档",
                                    "一作品一记录",
                                    "低饱和深海基底",
                                    "封面作为主色源"
                                ].map((item) => (
                                    <div key={item} className="rounded-full border border-white/10 bg-white/[0.03] px-3 py-2 text-xs text-zinc-300">
                                        {item}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="grid gap-4">
                        <div className="rounded-[30px] border border-white/10 bg-white/[0.04] p-5">
                            <div className="mb-4 flex items-center justify-between">
                                <div>
                                    <div className="text-[11px] uppercase tracking-[0.22em] text-zinc-500">Current Readout</div>
                                    <div className="mt-2 text-xl font-semibold text-zinc-50">2026 Q2 / 冬季运行态</div>
                                </div>
                                <div className="rounded-full border border-cyan-300/20 bg-cyan-300/8 px-3 py-1 text-xs text-cyan-200">
                                    stable
                                </div>
                            </div>
                            <div className="grid gap-3 md:grid-cols-3 lg:grid-cols-1 xl:grid-cols-3">
                                <MetricCard label="Library" value="621" hint="作品库存量" />
                                <MetricCard label="Record" value="214" hint="个人记录条数" />
                                <MetricCard label="Peak" value="49" hint="当前最高评分" />
                            </div>
                        </div>

                        <div className="rounded-[30px] border border-white/10 bg-white/[0.04] p-5">
                            <div className="mb-4 text-[11px] uppercase tracking-[0.22em] text-zinc-500">System Signal</div>
                            <div className="space-y-4">
                                {[
                                    { label: "Archive Integrity", value: 92 },
                                    { label: "Season Mood", value: 78 },
                                    { label: "Cover Noise Balance", value: 84 }
                                ].map((item) => (
                                    <div key={item.label}>
                                        <div className="mb-2 flex items-center justify-between text-sm text-zinc-300">
                                            <span>{item.label}</span>
                                            <span className="text-zinc-500">{item.value}%</span>
                                        </div>
                                        <div className="h-2 overflow-hidden rounded-full bg-white/5">
                                            <div
                                                className="h-full rounded-full bg-gradient-to-r from-cyan-200 via-sky-300 to-blue-300"
                                                style={{ width: `${item.value}%` }}
                                            />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </section>

                <section className="mt-7 grid gap-4 lg:grid-cols-3">
                    <PortalCard
                        icon={BookMarked}
                        badge="Q-ENTRY"
                        title="我的记录"
                        subtitle="按季度切开的私人观测面板"
                        desc="适合承载时间性、季度性和记录层级。它不需要太花，但必须有阅读节奏和档案分隔感。"
                    />
                    <PortalCard
                        icon={FolderKanban}
                        badge="ARCHIVE"
                        title="全作品库"
                        subtitle="高密度编目区，秩序必须压住封面噪声"
                        desc="这里是整站最需要稳定网格和高质量留白的区域。设计重点不是炫技，而是让封面与标签共同构成秩序。"
                    />
                    <PortalCard
                        icon={Trophy}
                        badge="RANKING"
                        title="年度 TOP"
                        subtitle="信息密度较低，可承接更强的仪式感与奇特图形语言"
                        desc="这页允许更像年度颁奖台、偏好神殿或私人奖章室，是整站最适合做戏剧化视觉的一页。"
                    />
                </section>

                <section className="mt-8 grid gap-8 lg:grid-cols-[0.95fr_1.05fr]">
                    <div className="rounded-[32px] border border-white/10 bg-white/[0.04] p-6">
                        <div className="mb-5 flex items-center gap-2 text-[11px] uppercase tracking-[0.22em] text-zinc-500">
                            <Layers3 className="h-4 w-4" />
                            季度剖面 / 冬季样式示意
                        </div>
                        <div className="space-y-4">
                            {[
                                {
                                    title: "Q1 · 冬季余波",
                                    desc: "更安静、更收束，边界偏冷白与霜蓝，适合做启动态与低温统计块。"
                                },
                                {
                                    title: "Q2 · 过渡运行",
                                    desc: "延续冬季的冷静底色，但允许局部注入春季湿度感，适合作为主页主状态。"
                                },
                                {
                                    title: "Q4 · 年度回响",
                                    desc: "最适合把 TOP 与年度分析做成仪式页，图标、徽章、称号感都可以更强。"
                                }
                            ].map((item) => (
                                <div key={item.title} className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
                                    <div className="text-base font-semibold text-zinc-50">{item.title}</div>
                                    <div className="mt-2 text-sm leading-7 text-zinc-400">{item.desc}</div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="rounded-[32px] border border-white/10 bg-white/[0.04] p-6">
                        <div className="mb-5 flex items-center gap-2 text-[11px] uppercase tracking-[0.22em] text-zinc-500">
                            <Bookmark className="h-4 w-4" />
                            封面陈列逻辑 / 低干扰示意
                        </div>
                        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                            <ShelfCard
                                title="冬季信号体"
                                meta="适合详情页氛围封面、首页低干扰封面占位，先看骨架和节奏。"
                                tone="bg-[linear-gradient(160deg,#172334,#24344f_45%,#7dd3fc_100%)]"
                            />
                            <ShelfCard
                                title="冷潮档案簇"
                                meta="适合高密度作品库的统一封面承载策略，避免系统自己和彩色海报打架。"
                                tone="bg-[linear-gradient(160deg,#111827,#1f2937_45%,#475569_100%)]"
                            />
                            <ShelfCard
                                title="年度勋章室"
                                meta="适合 TOP 页、年度回顾页，把封面逻辑转译成更偏徽章和勋章式的陈列。"
                                tone="bg-[linear-gradient(160deg,#0f172a,#1e293b_45%,#c4b5fd_100%)]"
                            />
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
}
