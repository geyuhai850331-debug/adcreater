import request from '@/config/axios/adminService'

interface CommonResult<T> {
  code: number
  msg: string
  data: T
}

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

const unwrap = async <T>(requestPromise: Promise<CommonResult<T>>): Promise<T> => {
  const res = await requestPromise
  return res.data
}

export const getModelPage = (params: AiModelPageParams): Promise<AiModelPageResult> => {
  return unwrap(
    request.get<AiModelPageResult>('/ai/model-config/page', {
      pageNo: params.page,
      pageSize: params.pageSize
    })
  )
}

export const getModelList = async (): Promise<AiModelVO[]> => {
  const res = await getModelPage({ page: 1, pageSize: 200 })
  return res.list || res.records || []
}

export const createModel = (data: AiModelVO): Promise<number> => {
  return unwrap(request.post('/ai/model-config/create', data))
}

export const updateModel = (id: number, data: Partial<AiModelVO>): Promise<boolean> => {
  return unwrap(request.put('/ai/model-config/update', { ...data, id }))
}

export const updateModelStatus = (id: number, isEnabled: boolean): Promise<boolean> => {
  return unwrap(request.put('/ai/model-config/update-status', { id, isEnabled }))
}

export const testModelConnection = (data: AiModelVO): Promise<boolean> => {
  return unwrap(request.post('/ai/model-config/test-connection', data))
}

export const deleteModel = (id: number): Promise<boolean> => {
  return unwrap(request.delete(`/ai/model-config/delete?id=${id}`))
}
