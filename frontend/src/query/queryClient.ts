import { QueryClient } from '@tanstack/react-query'

export const defaultStaleTimeMs = 60_000

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: defaultStaleTimeMs,
            gcTime: 5 * 60_000,
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
})
