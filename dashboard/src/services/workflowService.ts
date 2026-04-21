import api from './api'
import type {
  Workflow,
  WorkflowBase,
  WorkflowResponse,
  WorkflowCreateRequest,
  WorkflowUpdateRequest,
  WorkflowStatus,
  Stage,
  StageRequest,
} from '@/types'

export const workflowService = {
  async getWorkflows(page = 0, size = 25): Promise<WorkflowResponse> {
    const { data } = await api.get<WorkflowResponse>('/workflows', {
      params: { page, size },
    })
    return data
  },

  async getWorkflow(id: string): Promise<Workflow> {
    const { data } = await api.get<Workflow>(`/workflows/${id}/stages`)
    return data
  },

  async createWorkflow(data: WorkflowCreateRequest): Promise<{ id: string }> {
    const response = await api.post<{ id: string }>('/workflows', data)
    return response.data
  },

  async updateWorkflow(id: string, data: WorkflowUpdateRequest, etag: number): Promise<void> {
    await api.put(`/workflows/${id}`, data, {
      headers: { 'If-Match': etag },
    })
  },


  async deleteWorkflow(id: string, etag: number): Promise<void> {
    await api.delete(`/workflows/${id}`, {
      headers: { 'If-Match': etag },
    })
  },

  async getStages(workflowId: string): Promise<Stage[]> {
    const { data } = await api.get<Stage[]>(`/workflows/${workflowId}/stages`)
    return data
  },

  async addStage(workflowId: string, data: StageRequest): Promise<{ id: string }> {
    const response = await api.post<{ id: string }>(`/workflows/${workflowId}/stages`, data)
    return response.data
  },

  async getStage(workflowId: string, stageId: string): Promise<Stage> {
    const { data } = await api.get<Stage>(`/workflows/${workflowId}/stages/${stageId}`)
    return data
  },

  async updateStage(workflowId: string, stageId: string, data: Partial<StageRequest>, etag: number): Promise<void> {
    await api.patch(`/workflows/${workflowId}/stages/${stageId}`, data, {
      headers: { 'If-Match': etag },
    })
  },

  async deleteStage(workflowId: string, stageId: string, etag: number): Promise<void> {
    await api.delete(`/workflows/${workflowId}/stages/${stageId}`, {
      headers: { 'If-Match': etag },
    })
  },
}
