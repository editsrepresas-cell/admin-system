import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const res = response.data

    if (res.code === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')

      ElMessage.error(res.message || '登录已失效，请重新登录')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }

      return Promise.reject(res)
    }

    if (res.code === 403) {
      ElMessage.error(res.message || '没有访问权限')
      return Promise.reject(res)
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(res)
    }

    return res
  },
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')

      ElMessage.error('登录已失效，请重新登录')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }

      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error(error.response?.data?.message || '没有访问权限')
      return Promise.reject(error)
    }

    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  },
)

export default request
