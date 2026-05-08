import AppRouter from './routes/AppRouter'
import ErrorBoundary from './components/common/ErrorBoundary'
import { BackgroundJobsProvider } from './jobs/BackgroundJobsProvider'

function App() {
  return (
    <ErrorBoundary>
      <BackgroundJobsProvider>
        <AppRouter />
      </BackgroundJobsProvider>
    </ErrorBoundary>
  )
}

export default App
