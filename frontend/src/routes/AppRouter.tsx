import { lazy, Suspense, type ReactNode } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import AppLayout from '../components/layout/AppLayout'

const AnalysisPage = lazy(() => import('../pages/AnalysisPage'))
const CollectionsPage = lazy(() => import('../pages/CollectionsPage'))
const FranchisesPage = lazy(() => import('../pages/FranchisesPage'))
const HomePage = lazy(() => import('../pages/HomePage'))
const ImportPage = lazy(() => import('../pages/ImportPage'))
const LibraryPage = lazy(() => import('../pages/LibraryPage'))
const RecordsPage = lazy(() => import('../pages/RecordsPage'))
const TopPage = lazy(() => import('../pages/TopPage'))

function PageFallback() {
    return (
        <div className="rounded-[28px] border border-white/70 bg-white/58 p-6 shadow-sm backdrop-blur">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-slate-300">
                loading
            </p>
            <div className="mt-4 space-y-3">
                <div className="h-7 w-56 animate-pulse rounded-full bg-slate-200/70" />
                <div className="h-24 animate-pulse rounded-[24px] bg-slate-200/55" />
                <div className="grid gap-3 md:grid-cols-3">
                    <div className="h-28 animate-pulse rounded-[24px] bg-slate-200/45" />
                    <div className="h-28 animate-pulse rounded-[24px] bg-slate-200/45" />
                    <div className="h-28 animate-pulse rounded-[24px] bg-slate-200/45" />
                </div>
            </div>
        </div>
    )
}

function lazyPage(page: ReactNode) {
    return <Suspense fallback={<PageFallback />}>{page}</Suspense>
}

const router = createBrowserRouter([
    {
        path: '/',
        element: <AppLayout />,
        children: [
            {
                index: true,
                element: lazyPage(<HomePage />),
            },
            {
                path: 'records',
                element: lazyPage(<RecordsPage />),
            },
            {
                path: 'library',
                element: lazyPage(<LibraryPage />),
            },
            {
                path: 'top',
                element: lazyPage(<TopPage />),
            },
            {
                path: 'collections',
                element: lazyPage(<CollectionsPage />),
            },
            {
                path: 'import',
                element: lazyPage(<ImportPage />),
            },
            {
                path: 'franchises',
                element: lazyPage(<FranchisesPage />),
            },
            {
                path: 'analysis',
                element: lazyPage(<AnalysisPage />),
            },
        ],
    },
])

function AppRouter() {
    return <RouterProvider router={router} />
}

export default AppRouter
