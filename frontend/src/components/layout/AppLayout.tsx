import { Outlet, useLocation } from 'react-router-dom'
import AppNavigation from './AppNavigation'

function AppLayout() {
    const location = useLocation()

    return (
        <div className="min-h-screen px-6 pb-20 pt-6 lg:px-8 lg:pb-28">
            <div className="mx-auto flex max-w-[1500px] items-start gap-6">
                <AppNavigation />

                <main
                    key={location.pathname}
                    className="min-w-0 flex-1 pb-4 lg:pb-8 content-enter"
                >
                    <Outlet />
                </main>
            </div>
        </div>
    )
}

export default AppLayout
