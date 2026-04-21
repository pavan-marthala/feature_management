import api from './api'
import type {
  Feature,
  FeatureResponse,
  FeatureCreateRequest,
  StrategyInfo,
  IdType,
  FeaturePromotionRequest,
  FeaturePromotionResponse,
  PropagationHistory,
} from '@/types'

export const featureService = {
  async getFeatures(page = 0, size = 25): Promise<FeatureResponse> {
    const { data } = await api.get<FeatureResponse>('/features', {
      params: { page, size },
    })
    return data
  },

  async getFeature(id: string, idType: IdType = 'NAME'): Promise<{ feature: Feature; etag: string }> {
    const response = await api.get<Feature>(`/features/${id}`, {
      params: { idType },
    })
    return {
      feature: response.data,
      etag: response.headers['etag'] || String(response.data.etag),
    }
  },

  async createFeature(data: FeatureCreateRequest): Promise<{ id: string }> {
    const response = await api.post<{ id: string } | string>('/features', data)
    // Handle both { id: string } and plain string response shapes
    const id = typeof response.data === 'string' ? response.data : response.data?.id
    return { id }
  },

  async updateFeature(id: string, data: Record<string, unknown>, etag: number): Promise<void> {
    await api.patch(`/features/${id}`, data, {
      headers: { 'If-Match': etag },
    })
  },

  async updateFeatureStatus(id: string, status: boolean, etag: number): Promise<void> {
    await api.patch(`/features/${id}/status`, null, {
      params: { status },
      headers: { 'If-Match': etag },
    })
  },

  async deleteFeature(id: string, etag: number): Promise<void> {
    await api.delete(`/features/${id}`, {
      headers: { 'If-Match': etag },
    })
  },

  async getStrategies(): Promise<StrategyInfo[]> {
    const { data } = await api.get<StrategyInfo[]>('/features/strategies')
    return data
  },

  async addOwner(id: string, owner: string): Promise<void> {
    await api.post(`/features/${id}/owners/${owner}`)
  },

  async removeOwner(id: string, ownerId: string, etag: number): Promise<void> {
    await api.delete(`/features/${id}/owners/${ownerId}`, {
      headers: { 'If-Match': etag },
    })
  },

  async propagateFeature(id: string, data: FeaturePromotionRequest): Promise<FeaturePromotionResponse> {
    const response = await api.post<FeaturePromotionResponse>(`/features/${id}/propagate`, data)
    return response.data
  },

  async getPropagationHistory(id: string, page = 0, size = 25): Promise<PropagationHistory[]> {
    const { data } = await api.get<PropagationHistory[]>(`/features/${id}/propagations`, {
      params: { page, size },
    })
    return data
  },
}
