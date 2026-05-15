import request from '@/config/axios/adminService'

interface CommonResult<T> {
  code: number
  msg: string
  data: T
}

export interface PromptVO {
  id?: number
  name: string
  category: string
  modelName?: string
  systemPrompt: string
  templateContent: string
  variables?: string
  isEnabled: boolean
}

export interface PromptPageParams {
  page: number
  pageSize: number
  name?: string
  category?: string
  isEnabled?: boolean
}

export interface PromptPageResult {
  list: PromptVO[]
  records?: PromptVO[]
  total: number
}

const unwrap = async <T>(requestPromise: Promise<CommonResult<T>>): Promise<T> => {
  const res = await requestPromise
  return res.data
}

export const getPromptPage = (params: PromptPageParams): Promise<PromptPageResult> => {
  return unwrap(
    request.get<PromptPageResult>('/ai/prompt-template/page', {
      pageNo: params.page,
      pageSize: params.pageSize,
      name: params.name,
      category: params.category,
      isEnabled: params.isEnabled
    })
  )
}

export const createPrompt = (data: PromptVO): Promise<number> => {
  return unwrap(request.post('/ai/prompt-template/create', data))
}

export const updatePrompt = (id: number, data: Partial<PromptVO>): Promise<boolean> => {
  return unwrap(request.put('/ai/prompt-template/update', { ...data, id }))
}

export const updatePromptStatus = (id: number, isEnabled: boolean): Promise<boolean> => {
  return unwrap(request.put('/ai/prompt-template/update-status', { id, isEnabled }))
}

export const deletePrompt = (id: number): Promise<boolean> => {
  return unwrap(request.delete(`/ai/prompt-template/delete?id=${id}`))
}
