import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Feature, FeatureResponse, StrategyInfo, FeatureCreateRequest, FeatureStrategyType, Pagination } from '@/types'
import { featureService } from '@/services/featureService'
import { useUiStore } from './uiStore'

export const useFeatureStore = defineStore('feature', () => {
  // State
  const features = ref<Feature[]>([])
  const selectedFeature = ref<Feature | null>(null)
  const selectedEtag = ref<number>(0)
  const strategies = ref<StrategyInfo[]>([])
  const pagination = ref<Pagination>({ page: 0, size: 25, totalItems: 0, totalPages: 0 })
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Filters
  const searchQuery = ref('')
  const statusFilter = ref<'all' | 'enabled' | 'disabled'>('all')
  const strategyFilter = ref<FeatureStrategyType | 'all'>('all')

  // Getters
  const filteredFeatures = computed(() => {
    let result = features.value

    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(
        (f) =>
          f.name.toLowerCase().includes(query) ||
          f.description?.toLowerCase().includes(query)
      )
    }

    if (statusFilter.value !== 'all') {
      result = result.filter((f) =>
        statusFilter.value === 'enabled' ? f.enabled : !f.enabled
      )
    }

    if (strategyFilter.value !== 'all') {
      result = result.filter(
        (f) => f.configuration?.strategy === strategyFilter.value
      )
    }

    return result
  })

  // Actions
  async function fetchFeatures(page = 0, size = 25) {
    loading.value = true
    error.value = null
    try {
      const response: FeatureResponse = await featureService.getFeatures(page, size)
      features.value = response.items || []
      pagination.value = {
        page: response.page,
        size: response.size,
        totalItems: response.totalItems,
        totalPages: response.totalPages,
      }
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to fetch features'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
    } finally {
      loading.value = false
    }
  }

  async function fetchFeature(id: string, idType: 'ID' | 'NAME' = 'ID') {
    loading.value = true
    error.value = null
    try {
      const { feature, etag } = await featureService.getFeature(id, idType)
      selectedFeature.value = feature
      selectedEtag.value = Number(etag)
      return feature
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to fetch feature'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      return null
    } finally {
      loading.value = false
    }
  }

  async function createFeature(data: FeatureCreateRequest) {
    loading.value = true
    try {
      const result = await featureService.createFeature(data)
      const ui = useUiStore()
      ui.addToast('Feature created successfully', 'success')
      await fetchFeatures(pagination.value.page, pagination.value.size)
      return result
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to create feature'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateFeature(id: string, data: Record<string, unknown>, etag: number) {
    loading.value = true
    try {
      await featureService.updateFeature(id, data, etag)
      const ui = useUiStore()
      ui.addToast('Feature updated successfully', 'success')
      await fetchFeatures(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to update feature'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function toggleFeature(feature: Feature) {
    // Optimistic update
    const idx = features.value.findIndex((f) => f.id === feature.id)
    if (idx !== -1) {
      const prev = features.value[idx]!.enabled
      features.value[idx]!.enabled = !prev
      try {
        await featureService.updateFeatureStatus(feature.id, !prev, feature.etag)
        const ui = useUiStore()
        ui.addToast(`Feature ${!prev ? 'enabled' : 'disabled'}`, 'success')
        // Re-fetch to get updated etag
        await fetchFeatures(pagination.value.page, pagination.value.size)
      } catch {
        // Rollback
        features.value[idx]!.enabled = prev
        const ui = useUiStore()
        ui.addToast('Failed to toggle feature', 'error')
      }
    }
  }

  async function deleteFeature(id: string, etag: number) {
    try {
      await featureService.deleteFeature(id, etag)
      const ui = useUiStore()
      ui.addToast('Feature deleted successfully', 'success')
      await fetchFeatures(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to delete feature'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    }
  }

  async function addOwnerToFeature(id: string, ownerName: string) {
    loading.value = true
    try {
      await featureService.addOwner(id, ownerName)
      const ui = useUiStore()
      ui.addToast('Owner added successfully', 'success')
      await fetchFeature(id)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to add owner'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function removeOwnerFromFeature(id: string, ownerName: string, etag: number) {
    loading.value = true
    try {
      await featureService.removeOwner(id, ownerName, etag)
      const ui = useUiStore()
      ui.addToast('Owner removed successfully', 'success')
      await fetchFeature(id)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to remove owner'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchStrategies() {
    try {
      strategies.value = await featureService.getStrategies()
    } catch {
      // Silent fail — strategies dropdown will be empty
    }
  }

  function clearSelectedFeature() {
    selectedFeature.value = null
    selectedEtag.value = 0
  }

  return {
    // State
    features,
    selectedFeature,
    selectedEtag,
    strategies,
    pagination,
    loading,
    error,
    searchQuery,
    statusFilter,
    strategyFilter,
    // Getters
    filteredFeatures,
    // Actions
    fetchFeatures,
    fetchFeature,
    createFeature,
    updateFeature,
    toggleFeature,
    deleteFeature,
    addOwnerToFeature,
    removeOwnerFromFeature,
    fetchStrategies,
    clearSelectedFeature,
  }
})
