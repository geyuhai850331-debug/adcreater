import request from '@/config/axios/adminService'

export interface BillingTransactionVO {
  id: number
  userId: number
  type: 'RECHARGE' | 'CONSUME' | 'REFUND'
  amount: number
  balanceBefore: number
  balanceAfter: number
  status: 'SUCCESS' | 'FAILED' | 'PENDING'
  remark: string
  createdAt: string
}

export interface BillingPageParams {
  page: number
  pageSize: number
  userId?: string
  type?: string
  status?: string
  startDate?: string
  endDate?: string
}

export interface BillingPageResult {
  list: BillingTransactionVO[]
  records?: BillingTransactionVO[]
  total: number
}

export const getTransactionPage = (params: BillingPageParams) => {
  return request.get<BillingPageResult>('/billing/transactions', params)
}

export const recharge = (data: { userId: number; amount: number; remark: string }) => {
  return request.post('/billing/recharge', data)
}
