import api from './api'
import type {
  Workspace,
  WorkspaceRequest,
  WorkspaceResponse,
  WorkspaceSummary,
  FeatureResponse,
} from '@/types'

export const workspaceService = {
  async getWorkspaces(page = 0, size = 25): Promise<WorkspaceResponse> {
    const { data } = await api.get<WorkspaceResponse>('/workspaces', {
      params: { page, size },
    })
    return data
  },

  async getWorkspace(id: string): Promise<{ workspace: Workspace; etag: string }> {
    const response = await api.get<Workspace>(`/workspaces/${id}`)
    return {
      workspace: response.data,
      etag: response.headers['etag'] || String(response.data.etag),
    }
  },

  async createWorkspace(data: WorkspaceRequest): Promise<{ id: string }> {
    const response = await api.post<string>('/workspaces', data)
    return { id: typeof response.data === 'string' ? response.data : (response.data as unknown as { id: string }).id }
  },

  async updateWorkspace(id: string, data: WorkspaceRequest, etag: number): Promise<void> {
    await api.put(`/workspaces/${id}`, data, {
      headers: { 'If-Match': etag },
    })
  },

  async deleteWorkspace(id: string, etag: number): Promise<void> {
    await api.delete(`/workspaces/${id}`, {
      headers: { 'If-Match': etag },
    })
  },

  async getWorkspaceSummary(id: string): Promise<WorkspaceSummary> {
    const { data } = await api.get<WorkspaceSummary>(`/workspaces/${id}/summary`)
    return data
  },

  async getWorkspaceFeatures(id: string, page = 0, size = 25): Promise<FeatureResponse> {
    const { data } = await api.get<FeatureResponse>(`/workspaces/${id}/features`, {
      params: { page, size },
    })
    return data
  },
}
