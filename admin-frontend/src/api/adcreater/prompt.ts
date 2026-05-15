import request from '@/config/axios/adminService'

export interface PromptVO {
  id?: number
  name: string
  category: string
  modelId: number | null
  templateContent: string
  systemPrompt?: string
  isEnabled: boolean
}

export interface PromptPageParams {
  page: number
  pageSize: number
}

export interface PromptPageResult {
  list: PromptVO[]
  records?: PromptVO[]
  total: number
}

export const getPromptPage = (params: PromptPageParams) => {
  return request.get<PromptPageResult>('/ai/prompts', params)
}

export const createPrompt = (data: PromptVO) => {
  return request.post('/ai/prompts', data)
}

export const updatePrompt = (id: number, data: Partial<PromptVO>) => {
  return request.put(`/ai/prompts/${id}`, data)
}

export const updatePromptStatus = (id: number, isEnabled: boolean) => {
  return request.put(`/ai/prompts/${id}/status`, { isEnabled })
}

export const deletePrompt = (id: number) => {
  return request.delete(`/ai/prompts/${id}`)
}
