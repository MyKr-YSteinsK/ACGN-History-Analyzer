import { useState, type ReactNode } from 'react'

type CoverImageProps = {
    src?: string | null
    alt: string
    className: string
    fallback: ReactNode
}

function CoverImage({ src, alt, className, fallback }: CoverImageProps) {
    const [failedSrc, setFailedSrc] = useState<string | null>(null)

    if (!src || failedSrc === src) {
        return <>{fallback}</>
    }

    return (
        <img
            src={src}
            alt={alt}
            className={className}
            onError={() => setFailedSrc(src)}
        />
    )
}

export default CoverImage
