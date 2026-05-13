import axios from 'axios'

const client = axios.create({ baseURL: '/admin-api', timeout: 30000 })

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || err.message || 'Request failed'
    console.error(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
