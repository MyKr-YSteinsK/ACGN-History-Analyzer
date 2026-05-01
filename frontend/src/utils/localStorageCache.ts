export function readJsonCache<T>(
    key: string,
    validate: (value: unknown) => value is T,
) {
    try {
        const raw = window.localStorage.getItem(key)
        if (!raw) {
            return null
        }

        const parsed = JSON.parse(raw)
        return validate(parsed) ? parsed : null
    } catch {
        return null
    }
}

export function writeJsonCache<T>(key: string, value: T) {
    try {
        window.localStorage.setItem(key, JSON.stringify(value))
        return true
    } catch {
        return false
    }
}

export function removeCacheItem(key: string) {
    try {
        window.localStorage.removeItem(key)
    } catch {
        // Ignore unavailable localStorage.
    }
}

export function removeCacheItemsByPrefixes(prefixes: string[]) {
    try {
        const keys = Array.from(
            { length: window.localStorage.length },
            (_, index) => window.localStorage.key(index),
        ).filter((key): key is string => Boolean(key))

        keys
            .filter((key) => prefixes.some((prefix) => key.startsWith(prefix)))
            .forEach((key) => window.localStorage.removeItem(key))
    } catch {
        // Ignore unavailable localStorage.
    }
}

export function listCacheKeysByPrefixes(prefixes: string[]) {
    try {
        return Array.from(
            { length: window.localStorage.length },
            (_, index) => window.localStorage.key(index),
        )
            .filter((key): key is string => Boolean(key))
            .filter((key) => prefixes.some((prefix) => key.startsWith(prefix)))
    } catch {
        return []
    }
}
