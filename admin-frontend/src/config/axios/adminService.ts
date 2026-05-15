import axios, { AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'
import { config as axiosConfig } from '@/config/axios/config'
import { getAccessToken, getTenantId, removeToken } from '@/utils/auth'

const adminService: AxiosInstance = axios.create({
  baseURL: axiosConfig.base_url,
  timeout: 30000
})

adminService.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (import.meta.env.VITE_APP_TENANT_ENABLE === 'true') {
    const tenantId = getTenantId()
    if (tenantId) {
      config.headers['tenant-id'] = tenantId
    }
  }
  return config
})

adminService.interceptors.response.use(
  (res) => {
    const result = res.data
    const code = result?.code
    if (code === undefined || code === null || code === 0 || code === 200) {
      return result
    }
    const msg = result?.msg || '请求失败'
    ElMessage.error(msg)
    if (code === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(new Error(msg))
  },
  (err) => {
    const msg = err.response?.data?.msg || err.response?.data?.message || err.message || '请求失败'
    ElMessage.error(msg)
    if (err.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default {
  get: <T = any>(url: string, params?: any) =>
    adminService.get<any, T>(url, { params }),
  post: <T = any>(url: string, data?: any) =>
    adminService.post<any, T>(url, data),
  put: <T = any>(url: string, data?: any) =>
    adminService.put<any, T>(url, data),
  delete: <T = any>(url: string) =>
    adminService.delete<any, T>(url)
}
