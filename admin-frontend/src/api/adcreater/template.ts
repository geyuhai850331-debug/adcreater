import request from '@/config/axios/adminService'

interface CommonResult<T> {
  code: number
  msg: string
  data: T
}

export interface TemplateVO {
  id?: number
  name: string
  category: string
  description: string
  thumbnailUrl?: string
  status?: string
  createTime?: string
  updateTime?: string
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
  id: number
  templateId: number
  version: number
  fileUrl: string
  changelog: string
  createTime: string
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const unwrap = async <T>(requestPromise: Promise<any>): Promise<T> => {
  const res = await requestPromise
  return res.data
}

export const getTemplatePage = (params: TemplatePageParams): Promise<TemplatePageResult> => {
  return unwrap(
    request.get('/template/page', {
      pageNo: params.page,
      pageSize: params.pageSize
    })
  )
}

export const createTemplate = (data: TemplateVO): Promise<number> => {
  return unwrap(request.post('/template/create', data))
}

export const updateTemplate = (id: number, data: TemplateVO): Promise<boolean> => {
  return unwrap(request.put('/template/update', { ...data, id }))
}

export const deleteTemplate = (id: number): Promise<boolean> => {
  return unwrap(request.delete(`/template/delete?id=${id}`))
}

export const publishTemplateVersion = (
  templateId: number,
  data: { version: string; fileUrl: string; changelog: string }
): Promise<number> => {
  const params = new URLSearchParams({
    templateId: String(templateId),
    fileUrl: data.fileUrl,
    changelog: data.changelog
  })
  return unwrap(request.post(`/template/publish-version?${params.toString()}`))
}

export const getTemplateVersions = (templateId: number): Promise<TemplateVersionVO[]> => {
  return unwrap(
    request.get('/template/versions', { templateId })
  )
}
