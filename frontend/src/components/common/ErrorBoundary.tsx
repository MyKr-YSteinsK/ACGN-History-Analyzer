import { Component, type ErrorInfo, type ReactNode } from 'react'
import GlassCard from './GlassCard'

type ErrorBoundaryProps = {
    children: ReactNode
}

type ErrorBoundaryState = {
    hasError: boolean
    message: string
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    state: ErrorBoundaryState = {
        hasError: false,
        message: '',
    }

    static getDerivedStateFromError(error: Error) {
        return {
            hasError: true,
            message: error.message || '页面渲染失败',
        }
    }

    componentDidCatch(error: Error, info: ErrorInfo) {
        console.error('Unhandled page error', error, info.componentStack)
    }

    private reloadPage = () => {
        window.location.reload()
    }

    render() {
        if (!this.state.hasError) {
            return this.props.children
        }

        return (
            <div className="mx-auto max-w-3xl p-6">
                <GlassCard className="overflow-hidden">
                    <div className="bg-gradient-to-br from-white/85 to-rose-50/80 p-6">
                        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-rose-300">
                            page error
                        </p>
                        <h1 className="mt-2 text-2xl font-black text-rose-700">
                            页面暂时无法渲染
                        </h1>
                        <p className="mt-3 text-sm leading-7 text-rose-600">
                            {this.state.message}
                        </p>
                        <p className="mt-3 text-sm leading-7 text-slate-500">
                            这通常是单个页面组件运行时异常，不会破坏后端数据。可以刷新页面重新加载。
                        </p>
                        <button
                            type="button"
                            onClick={this.reloadPage}
                            className="mt-5 rounded-full border border-rose-200 bg-white/80 px-5 py-2.5 text-sm font-black text-rose-700 shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:bg-rose-50"
                        >
                            刷新页面
                        </button>
                    </div>
                </GlassCard>
            </div>
        )
    }
}

export default ErrorBoundary
