import { libraryStatusTheme } from '../../theme/acgnTheme'
import type { LibraryStatus } from '../../types/common'

type StatusBadgeProps = {
    status: LibraryStatus
}

function StatusBadge({ status }: StatusBadgeProps) {
    const theme = libraryStatusTheme[status]

    return (
        <span
            className={[
                'inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold shadow-sm backdrop-blur',
                theme.className,
            ].join(' ')}
        >
      {theme.label}
    </span>
    )
}

export default StatusBadge