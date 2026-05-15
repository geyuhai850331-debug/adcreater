import axios, { AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'

const adminService: AxiosInstance = axios.create({
  baseURL: '/admin-api',
  timeout: 30000
})

adminService.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

adminService.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const msg = err.response?.data?.message || err.message || '请求失败'
    ElMessage.error(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
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
