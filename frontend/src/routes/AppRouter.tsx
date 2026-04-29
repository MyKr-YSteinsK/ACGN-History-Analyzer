import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import AnalysisPage from '../pages/AnalysisPage'
import AppLayout from '../components/layout/AppLayout'
import CollectionsPage from '../pages/CollectionsPage'
import FranchisesPage from '../pages/FranchisesPage'
import HomePage from '../pages/HomePage'
import ImportPage from '../pages/ImportPage'
import LibraryPage from '../pages/LibraryPage'
import RecordsPage from '../pages/RecordsPage'
import TopPage from '../pages/TopPage'

const router = createBrowserRouter([
    {
        path: '/',
        element: <AppLayout />,
        children: [
            {
                index: true,
                element: <HomePage />,
            },
            {
                path: 'records',
                element: <RecordsPage />,
            },
            {
                path: 'library',
                element: <LibraryPage />,
            },
            {
                path: 'top',
                element: <TopPage />,
            },
            {
                path: 'collections',
                element: <CollectionsPage />,
            },
            {
                path: 'import',
                element: <ImportPage />,
            },
            {
                path: 'franchises',
                element: <FranchisesPage />,
            },
            {
                path: 'analysis',
                element: <AnalysisPage />,
            },
        ],
    },
])

function AppRouter() {
    return <RouterProvider router={router} />
}

export default AppRouter
