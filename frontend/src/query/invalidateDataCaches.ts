import type { QueryClient } from '@tanstack/react-query'

/** Invalidate list/read caches after writes to subjects, records, collections, or franchises. */
export function invalidateLibraryRelatedQueries(queryClient: QueryClient) {
    void queryClient.invalidateQueries({ queryKey: ['subjectPage'] })
    void queryClient.invalidateQueries({ queryKey: ['collections'] })
    void queryClient.invalidateQueries({ queryKey: ['records'] })
    void queryClient.invalidateQueries({ queryKey: ['franchises'] })
    void queryClient.invalidateQueries({ queryKey: ['top'] })
    void queryClient.invalidateQueries({ queryKey: ['tags'] })
}
