import request from '@/config/axios/adminService'

export interface DashboardStatsVO {
  todayApiCalls: number
  activeUsers: number
  todayPointsConsumed: number
  totalUsers: number
}

export const getDashboardStats = () => {
  return request.get<DashboardStatsVO>('/system/dashboard/stats')
}
