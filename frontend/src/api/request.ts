import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '../types/api'

export class ApiRequestError extends Error {
    code?: number
    status?: number
    raw?: unknown

    constructor(message: string, code?: number, status?: number, raw?: unknown) {
        super(message)
        this.name = 'ApiRequestError'
        this.code = code
        this.status = status
        this.raw = raw
    }
}

const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
})

function isApiResponse(value: unknown): value is ApiResponse<unknown> {
    return (
        typeof value === 'object' &&
        value !== null &&
        'code' in value &&
        'message' in value &&
        'data' in value
    )
}

request.interceptors.response.use(
    (response) => {
        const body = response.data

        if (!isApiResponse(body)) {
            return body
        }

        if (body.code !== 0) {
            throw new ApiRequestError(body.message || '请求失败', body.code, response.status, body)
        }

        return body.data
    },
    (error: AxiosError<ApiResponse<unknown>>) => {
        const responseBody = error.response?.data
        const message =
            responseBody?.message ||
            error.message ||
            '网络请求失败，请检查后端服务是否正常运行'

        return Promise.reject(
            new ApiRequestError(
                message,
                responseBody?.code,
                error.response?.status,
                responseBody ?? error,
            ),
        )
    },
)

export function apiRequest<T>(config: AxiosRequestConfig) {
    return request.request<unknown, T>(config)
}

export function apiGet<T>(url: string, params?: unknown) {
    return apiRequest<T>({
        method: 'GET',
        url,
        params,
    })
}

export function apiPost<T, B = unknown>(url: string, data?: B) {
    return apiRequest<T>({
        method: 'POST',
        url,
        data,
    })
}

export function apiPut<T, B = unknown>(url: string, data?: B) {
    return apiRequest<T>({
        method: 'PUT',
        url,
        data,
    })
}

export function apiDelete<T>(url: string) {
    return apiRequest<T>({
        method: 'DELETE',
        url,
    })
}
