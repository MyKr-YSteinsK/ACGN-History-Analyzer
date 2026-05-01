import { useEffect } from 'react'

/**
 * 当 open 为 true 时，按 Escape 调用 onClose。
 */
export function useEscapeToClose(open: boolean, onClose: () => void) {
    useEffect(() => {
        if (!open) {
            return
        }

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose()
            }
        }

        window.addEventListener('keydown', handleKeyDown)
        return () => {
            window.removeEventListener('keydown', handleKeyDown)
        }
    }, [open, onClose])
}
