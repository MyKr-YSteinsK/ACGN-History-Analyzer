import type { LlmStatusResponse } from '../../api/llmApi'
import GlassCard from '../common/GlassCard'

export type LlmConfigMode = 'deepseek' | 'preview'

type LlmConfigPanelProps = {
    open: boolean
    status: LlmStatusResponse | null
    mode: LlmConfigMode
    baseUrl: string
    model: string
    apiKey: string
    saving: boolean
    testing: boolean
    message: string | null
    onToggle: () => void
    onModeChange: (mode: LlmConfigMode) => void
    onBaseUrlChange: (value: string) => void
    onModelChange: (value: string) => void
    onApiKeyChange: (value: string) => void
    onSave: () => void
    onTest: () => void
}

function LlmConfigPanel({
    open,
    status,
    mode,
    baseUrl,
    model,
    apiKey,
    saving,
    testing,
    message,
    onToggle,
    onModeChange,
    onBaseUrlChange,
    onModelChange,
    onApiKeyChange,
    onSave,
    onTest,
}: LlmConfigPanelProps) {
    const currentModeLabel = status
        ? status.mockMode ? '预览模式' : 'DeepSeek API'
        : '读取中'

    return (
        <GlassCard className="overflow-hidden">
            <div className="bg-gradient-to-br from-white/75 via-sky-50/45 to-violet-50/55 p-5">
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400">
                            llm settings
                        </p>
                        <h3 className="mt-1 text-lg font-black text-slate-800">
                            大模型配置
                        </h3>
                        <p className="mt-1 text-sm leading-6 text-slate-500">
                            当前：{currentModeLabel}
                            {status && !status.mockMode ? ` · ${status.model}` : ''}
                            {status && !status.mockMode
                                ? status.apiKeyConfigured
                                    ? ' · API Key 已配置'
                                    : ' · API Key 未配置'
                                : ''}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={onToggle}
                        className="rounded-full border border-sky-200 bg-white/75 px-5 py-2.5 text-sm font-black text-sky-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-sky-50"
                    >
                        {open ? '收起设置' : '设置 API'}
                    </button>
                </div>

                {open ? (
                    <div className="mt-5 rounded-[28px] border border-white/75 bg-white/58 p-5 shadow-sm backdrop-blur">
                        <div className="grid gap-3 md:grid-cols-2">
                            <ConfigModeButton
                                active={mode === 'deepseek'}
                                title="DeepSeek API"
                                description="使用 deepseek-v4-flash 生成真实分析结果"
                                onClick={() => onModeChange('deepseek')}
                            />
                            <ConfigModeButton
                                active={mode === 'preview'}
                                title="预览模式"
                                description="不调用外部接口，使用本地统计生成演示结果"
                                onClick={() => onModeChange('preview')}
                            />
                        </div>

                        {mode === 'deepseek' ? (
                            <div className="mt-5 grid gap-4 xl:grid-cols-[1.2fr_1fr_1.4fr]">
                                <ConfigField
                                    label="Base URL"
                                    value={baseUrl}
                                    placeholder="https://api.deepseek.com"
                                    onChange={onBaseUrlChange}
                                />
                                <ConfigField
                                    label="模型"
                                    value={model}
                                    placeholder="deepseek-v4-flash"
                                    onChange={onModelChange}
                                />
                                <ConfigField
                                    label="API Key"
                                    value={apiKey}
                                    placeholder="留空表示沿用后端已配置的 Key"
                                    type="password"
                                    onChange={onApiKeyChange}
                                />
                            </div>
                        ) : null}

                        <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
                            <p className="text-xs leading-5 text-slate-400">
                                页面只更新后端运行时配置，不会把 Key 写入仓库。建议正式运行时用环境变量 DEEPSEEK_API_KEY，临时演示再用这里的表单。
                            </p>
                            <div className="flex flex-wrap gap-2">
                                <button
                                    type="button"
                                    onClick={onTest}
                                    disabled={saving || testing}
                                    className={[
                                        'rounded-full border px-5 py-2.5 text-sm font-black shadow-sm transition-all duration-200',
                                        saving || testing
                                            ? 'cursor-not-allowed border-white/60 bg-white/50 text-slate-300'
                                            : 'border-sky-200 bg-sky-100/90 text-sky-700 hover:-translate-y-0.5 hover:bg-sky-50',
                                    ].join(' ')}
                                >
                                    {testing ? '测试中' : '保存并测试'}
                                </button>
                                <button
                                    type="button"
                                    onClick={onSave}
                                    disabled={saving || testing}
                                    className={[
                                        'rounded-full border px-5 py-2.5 text-sm font-black shadow-sm transition-all duration-200',
                                        saving || testing
                                            ? 'cursor-not-allowed border-white/60 bg-white/50 text-slate-300'
                                            : 'border-emerald-200 bg-emerald-100/90 text-emerald-700 hover:-translate-y-0.5 hover:bg-emerald-50',
                                    ].join(' ')}
                                >
                                    {saving ? '保存中' : '保存配置'}
                                </button>
                            </div>
                        </div>

                        {message ? (
                            <p className="mt-3 rounded-2xl border border-white/75 bg-white/65 px-4 py-3 text-sm leading-6 text-slate-500">
                                {message}
                            </p>
                        ) : null}
                    </div>
                ) : null}
            </div>
        </GlassCard>
    )
}

type ConfigModeButtonProps = {
    active: boolean
    title: string
    description: string
    onClick: () => void
}

function ConfigModeButton({ active, title, description, onClick }: ConfigModeButtonProps) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={[
                'rounded-[24px] border p-4 text-left transition-all duration-200',
                active
                    ? 'border-sky-200 bg-sky-50/90 text-sky-700 shadow-sm'
                    : 'border-white/75 bg-white/55 text-slate-500 hover:-translate-y-0.5 hover:bg-white',
            ].join(' ')}
        >
            <p className="text-sm font-black">{title}</p>
            <p className="mt-1 text-xs leading-5 text-slate-400">{description}</p>
        </button>
    )
}

type ConfigFieldProps = {
    label: string
    value: string
    placeholder: string
    type?: 'text' | 'password'
    onChange: (value: string) => void
}

function ConfigField({
    label,
    value,
    placeholder,
    type = 'text',
    onChange,
}: ConfigFieldProps) {
    return (
        <label className="block">
            <p className="mb-2 text-xs font-semibold text-slate-400">{label}</p>
            <input
                type={type}
                value={value}
                placeholder={placeholder}
                onChange={(event) => onChange(event.target.value)}
                className="h-12 w-full rounded-2xl border border-white/80 bg-white/72 px-4 text-sm font-semibold text-slate-700 outline-none transition-all duration-200 placeholder:text-slate-300 focus:border-sky-200 focus:bg-white focus:ring-4 focus:ring-sky-100/70"
            />
        </label>
    )
}

export default LlmConfigPanel
