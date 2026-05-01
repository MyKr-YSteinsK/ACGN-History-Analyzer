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

function getResponseMessage(value: unknown) {
    if (isApiResponse(value)) {
        return value.message
    }

    return null
}

function getTransportErrorMessage(error: AxiosError<unknown>) {
    const status = error.response?.status
    const url = error.config?.url ?? ''
    const isLlmRequest = url.includes('/llm/')

    if (status === 502 || status === 503 || status === 504) {
        if (isLlmRequest) {
            return '大模型服务这次没接上。可以检查 API Key、模型名、Base URL，或者先切到预览模式继续演示'
        }

        return '后端服务暂时不可用。请看一下 Spring Boot 控制台，通常是数据库账号、端口或启动配置没有对上'
    }

    if (error.code === 'ECONNABORTED') {
        if (isLlmRequest) {
            return '大模型思考时间太久了。可以重试一次，或先切到预览模式确认页面流程'
        }

        return '请求等得太久了，请确认后端还在运行，数据库连接也没有卡住'
    }

    if (!error.response) {
        return '前端还没连上后端。请先确认 Spring Boot 已启动，并且 Vite 代理指向 8080'
    }

    if (status && status >= 500) {
        return '后端处理时出了异常。控制台日志会给出真正原因，常见是数据库连接或外部 API 调用失败'
    }

    return error.message || '网络请求没有成功，请稍后重试；如果连续失败，先看后端控制台日志'
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
    (error: AxiosError<unknown>) => {
        const responseBody = error.response?.data
        const message =
            getResponseMessage(responseBody) ||
            getTransportErrorMessage(error)

        return Promise.reject(
            new ApiRequestError(
                message,
                isApiResponse(responseBody) ? responseBody.code : undefined,
                error.response?.status,
                responseBody ?? error,
            ),
        )
    },
)

export function getApiErrorMessage(
    error: unknown,
    fallback = '请求失败，请稍后重试',
) {
    if (error instanceof ApiRequestError) {
        return error.message || fallback
    }

    if (error instanceof Error) {
        return error.message || fallback
    }

    return fallback
}

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

export function apiPostWithConfig<T, B = unknown>(
    url: string,
    data?: B,
    config?: Omit<AxiosRequestConfig, 'method' | 'url' | 'data'>,
) {
    return apiRequest<T>({
        ...config,
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
