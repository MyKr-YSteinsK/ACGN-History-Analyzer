import { useEffect, useState, type Dispatch, type SetStateAction } from 'react'

function resolveInitialValue<T>(initialValue: T | (() => T)) {
    return typeof initialValue === 'function'
        ? (initialValue as () => T)()
        : initialValue
}

export function usePersistentState<T>(
    key: string,
    initialValue: T | (() => T),
): [T, Dispatch<SetStateAction<T>>] {
    const [value, setValue] = useState<T>(() => {
        try {
            const raw = window.localStorage.getItem(key)
            if (raw) {
                return JSON.parse(raw) as T
            }
        } catch {
            // Fall through to initial value when localStorage is unavailable or stale.
        }

        return resolveInitialValue(initialValue)
    })

    useEffect(() => {
        try {
            window.localStorage.setItem(key, JSON.stringify(value))
        } catch {
            // Ignore unavailable localStorage.
        }
    }, [key, value])

    return [value, setValue]
}
