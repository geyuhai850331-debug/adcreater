import request from '@/config/axios/adminService'

export interface AiModelVO {
  id?: number
  modelName: string
  adapterClass: string
  apiKey?: string
  endpointUrl: string
  isEnabled: boolean
  priority: number
  extraConfig?: string
}

export interface AiModelPageParams {
  page: number
  pageSize: number
}

export interface AiModelPageResult {
  list: AiModelVO[]
  records?: AiModelVO[]
  total: number
}

export const getModelPage = (params: AiModelPageParams) => {
  return request.get<AiModelPageResult>('/ai/models', params)
}

export const getModelList = () => {
  return request.get<AiModelVO[]>('/ai/models', { page: 1, pageSize: 200 })
}

export const createModel = (data: AiModelVO) => {
  return request.post('/ai/models', data)
}

export const updateModel = (id: number, data: Partial<AiModelVO>) => {
  return request.put(`/ai/models/${id}`, data)
}

export const updateModelStatus = (id: number, isEnabled: boolean) => {
  return request.put(`/ai/models/${id}/status`, { isEnabled })
}

export const testModelConnection = (id: number) => {
  return request.post(`/ai/models/${id}/test`)
}

export const deleteModel = (id: number) => {
  return request.delete(`/ai/models/${id}`)
}
