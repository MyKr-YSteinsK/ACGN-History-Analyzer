export type ApiResponse<T> = {
    code: number
    message: string
    data: T
}

export type PageResponse<T> = {
    content: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export type PageQuery = {
    page?: number
    size?: number
}

export type EnumOptionResponse = {
    value: string
    label: string
}
