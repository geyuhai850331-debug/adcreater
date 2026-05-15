import request from '@/config/axios/adminService'

interface CommonResult<T> {
  code: number
  msg: string
  data: T
}

export interface AiModelVO {
  id?: number
  modelName: string
  category: string
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
  category?: string
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
    request.get<CommonResult<AiModelPageResult>>('/ai/model-config/page', {
      pageNo: params.page,
      pageSize: params.pageSize,
      category: params.category
    })
  )
}

export const getModelList = async (): Promise<AiModelVO[]> => {
  const res = await getModelPage({ page: 1, pageSize: 200 })
  return res.list || res.records || []
}

export const getModelDetail = (id: number): Promise<AiModelVO> => {
  return unwrap(request.get<CommonResult<AiModelVO>>(`/ai/model-config/get?id=${id}`))
}

export const createModel = (data: AiModelVO): Promise<number> => {
  return unwrap(request.post<CommonResult<number>>('/ai/model-config/create', data))
}

export const updateModel = (id: number, data: Partial<AiModelVO>): Promise<boolean> => {
  return unwrap(request.put<CommonResult<boolean>>('/ai/model-config/update', { ...data, id }))
}

export const updateModelStatus = (id: number, isEnabled: boolean): Promise<boolean> => {
  return unwrap(request.put<CommonResult<boolean>>('/ai/model-config/update-status', { id, isEnabled }))
}

export const testModelConnection = (data: AiModelVO): Promise<boolean> => {
  return unwrap(request.post<CommonResult<boolean>>('/ai/model-config/test-connection', data))
}

export const deleteModel = (id: number): Promise<boolean> => {
  return unwrap(request.delete<CommonResult<boolean>>(`/ai/model-config/delete?id=${id}`))
}
