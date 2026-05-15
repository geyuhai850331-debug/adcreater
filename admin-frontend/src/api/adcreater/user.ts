import request from '@/config/axios/adminService'

export interface UserVO {
  id?: number
  username: string
  nickname: string
  password?: string
  mobile: string
  email: string
  points: number
  status: number
  createdAt?: string
}

export interface UserPageParams {
  page: number
  pageSize: number
  username?: string
  nickname?: string
  mobile?: string
  status?: number
}

export interface UserPageResult {
  list: UserVO[]
  records?: UserVO[]
  total: number
}

export const getUserPage = (params: UserPageParams) => {
  return request.get<UserPageResult>('/system/users', params)
}

export const createUser = (data: UserVO) => {
  return request.post('/system/users', data)
}

export const updateUser = (id: number, data: Partial<UserVO>) => {
  return request.put(`/system/users/${id}`, data)
}

export const updateUserStatus = (id: number, status: number) => {
  return request.put(`/system/users/${id}/status`, { status })
}

export const rechargeUser = (data: { userId: number; amount: number; remark: string }) => {
  return request.post('/billing/recharge', data)
}
