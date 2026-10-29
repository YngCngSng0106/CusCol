import axios from 'axios'
import { showFailToast } from 'vant'

export const TOKEN_KEY = 'cc_token'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const body = response.data
    if (body && body.code === 0) {
      return body.data
    }
    if (body && body.code === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('cc_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.replace(`/login?redirect=${encodeURIComponent(window.location.pathname)}`)
      }
      return Promise.reject(new Error(body.message || '请先登录'))
    }
    const message = body?.message || '请求失败'
    showFailToast(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    const message = error.response?.data?.message || (error.code === 'ECONNABORTED' ? '请求超时' : '网络异常，请稍后重试')
    showFailToast(message)
    return Promise.reject(error)
  }
)

export default http
