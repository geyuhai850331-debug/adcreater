import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuthSession } from '@/utils/auth'

const DEFAULT_TENANT_ID = '1'

const client = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - attach Bearer token
client.interceptors.request.use(
  (config) => {
    if (config.url?.startsWith('/app-api')) {
      config.baseURL = ''
    }

    if (!config.headers['tenant-id']) {
      config.headers['tenant-id'] = localStorage.getItem('tenantId') || DEFAULT_TENANT_ID
    }

    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle common errors
client.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.msg || error.response?.data?.message || error.message || '请求失败'

    if (status === 401) {
      clearAuthSession()
      window.location.href = '/#/login'
      ElMessage.error('登录已过期，请重新登录')
    } else if (status === 403) {
      ElMessage.error('无权限执行此操作')
    } else if (status === 500) {
      ElMessage.error('服务器错误: ' + message)
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default client
