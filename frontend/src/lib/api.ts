import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''
const api = axios.create({
  baseURL,
  withCredentials: true,
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      const url = err.config?.url || ''
      if (url.includes('/api/auth/me')) {
        return Promise.resolve({ data: null })
      }
      if (!url.includes('/api/auth/login') && !url.includes('/api/auth/register')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export default api
