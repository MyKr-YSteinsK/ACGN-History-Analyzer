import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import './index.css'
import App from './App.tsx'
import { BackgroundJobsProvider } from './jobs/BackgroundJobsProvider'
import { queryClient } from './query/queryClient'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <BackgroundJobsProvider>
                <App />
            </BackgroundJobsProvider>
        </QueryClientProvider>
    </StrictMode>,
)
