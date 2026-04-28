import { apiGet } from './request'

export type ImportEndpointDocResponse = {
    name: string
    method: string
    path: string
    description: string
}

export type ImportFieldDocResponse = {
    fieldName: string
    required: boolean
    description: string
    example: string
    notes: string
}

export type ImportOverviewResponse = {
    moduleName: string
    recommendedOrder: string
    steps: string[]
    notes: string[]
    endpoints: ImportEndpointDocResponse[]
}

export type ImportTemplateResponse = {
    importType: string
    endpoint: string
    recommendedOrder: string
    rules: string[]
    templateBody: Record<string, unknown>
    fields: ImportFieldDocResponse[]
}

export function getImportOverview() {
    return apiGet<ImportOverviewResponse>('/import/overview')
}

export function getSubjectImportTemplate() {
    return apiGet<ImportTemplateResponse>('/import/templates/subject')
}

export function getRecordImportTemplate() {
    return apiGet<ImportTemplateResponse>('/import/templates/record')
}
