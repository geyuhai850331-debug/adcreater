import request from '@/config/axios/adminService'

export interface TemplateVO {
  id?: number
  name: string
  category: string
  description: string
  thumbnailUrl?: string
  configData?: string
  currentVersion?: string
  status?: string
  createdAt?: string
}

export interface TemplatePageParams {
  page: number
  pageSize: number
}

export interface TemplatePageResult {
  list: TemplateVO[]
  records?: TemplateVO[]
  total: number
}

export interface TemplateVersionVO {
  version: string
  url: string
  changelog: string
  createdAt: string
}

export const getTemplatePage = (params: TemplatePageParams) => {
  return request.get<TemplatePageResult>('/templates', params)
}

export const createTemplate = (data: FormData) => {
  return request.post('/templates', data)
}

export const updateTemplate = (id: number, data: FormData) => {
  return request.put(`/templates/${id}`, data)
}

export const deleteTemplate = (id: number) => {
  return request.delete(`/templates/${id}`)
}

export const publishTemplateVersion = (
  templateId: number,
  data: { version: string; url: string; changelog: string }
) => {
  return request.post(`/templates/${templateId}/versions`, data)
}

export const getTemplateVersions = (templateId: number) => {
  return request.get<TemplateVersionVO[]>(`/templates/${templateId}/versions`)
}
