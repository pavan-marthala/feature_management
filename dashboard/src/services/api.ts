import axios from 'axios'
import type { ApiError } from '@/types'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
})

// Response interceptor for error normalization
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data) {
      const apiError = error.response.data as ApiError
      return Promise.reject(apiError)
    }
    return Promise.reject({
      httpMethod: 'GET',
      requestUri: error.config?.url || '',
      statusCode: error.response?.status || 0,
      errorMessage: error.message || 'Network error',
      correlationIdentifier: '',
      errorTimestamp: new Date().toISOString(),
    } as ApiError)
  }
)

export default api
