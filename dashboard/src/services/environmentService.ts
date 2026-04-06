import api from './api'
import type {
  Environment,
  EnvironmentRequest,
  EnvironmentResponse,
} from '@/types'

export const environmentService = {
  async getEnvironments(page = 0, size = 25): Promise<EnvironmentResponse> {
    const { data } = await api.get<EnvironmentResponse>('/environments', {
      params: { page, size },
    })
    return data
  },

  async getEnvironment(id: string): Promise<{ environment: Environment; etag: string }> {
    const response = await api.get<Environment>(`/environments/${id}`)
    return {
      environment: response.data,
      etag: response.headers['etag'] || String(response.data.etag),
    }
  },

  async createEnvironment(data: EnvironmentRequest): Promise<{ id: string }> {
    const response = await api.post<{ id: string } | string>('/environments', data)
    // Handle both { id: string } and plain string response shapes
    const id = typeof response.data === 'string' ? response.data : response.data?.id
    return { id }
  },

  async updateEnvironment(id: string, data: EnvironmentRequest, etag: number): Promise<void> {
    await api.patch(`/environments/${id}`, data, {
      headers: { 'If-Match': etag },
    })
  },

  async deleteEnvironment(id: string, etag: number): Promise<void> {
    await api.delete(`/environments/${id}`, {
      headers: { 'If-Match': etag },
    })
  },

  async addOwner(id: string, owner: string): Promise<void> {
    await api.post(`/environments/${id}/owners/${owner}`)
  },

  async removeOwner(id: string, ownerId: string, etag: number): Promise<void> {
    await api.delete(`/environments/${id}/owners/${ownerId}`, {
      headers: { 'If-Match': etag },
    })
  },
}
