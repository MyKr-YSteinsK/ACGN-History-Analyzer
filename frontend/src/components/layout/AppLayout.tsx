import { Outlet } from 'react-router-dom'
import AppNavigation from './AppNavigation'

function AppLayout() {
    return (
        <div className="min-h-screen px-6 py-6 lg:px-8">
            <div className="mx-auto flex max-w-[1500px] items-start gap-6">
                <AppNavigation />

                <main className="min-w-0 flex-1">
                    <Outlet />
                </main>
            </div>
        </div>
    )
}

export default AppLayout