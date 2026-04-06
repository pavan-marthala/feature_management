import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Environment, EnvironmentRequest, Pagination } from '@/types'
import { environmentService } from '@/services/environmentService'
import { useUiStore } from './uiStore'

export const useEnvironmentStore = defineStore('environment', () => {
  const environments = ref<Environment[]>([])
  const selectedEnvironment = ref<Environment | null>(null)
  const selectedEtag = ref<number>(0)
  const pagination = ref<Pagination>({ page: 0, size: 25, totalItems: 0, totalPages: 0 })
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchEnvironments(page = 0, size = 25) {
    loading.value = true
    error.value = null
    try {
      const response = await environmentService.getEnvironments(page, size)
      environments.value = response.items || []
      pagination.value = {
        page: response.page,
        size: response.size,
        totalItems: response.totalItems,
        totalPages: response.totalPages,
      }
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to fetch environments'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
    } finally {
      loading.value = false
    }
  }

  async function fetchEnvironment(id: string) {
    loading.value = true
    error.value = null
    try {
      const { environment, etag } = await environmentService.getEnvironment(id)
      selectedEnvironment.value = environment
      selectedEtag.value = Number(etag)
      return environment
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to fetch environment'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      return null
    } finally {
      loading.value = false
    }
  }

  async function createEnvironment(data: EnvironmentRequest) {
    loading.value = true
    try {
      const result = await environmentService.createEnvironment(data)
      const ui = useUiStore()
      ui.addToast('Environment created successfully', 'success')
      await fetchEnvironments(pagination.value.page, pagination.value.size)
      return result
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to create environment'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateEnvironment(id: string, data: EnvironmentRequest, etag: number) {
    loading.value = true
    try {
      await environmentService.updateEnvironment(id, data, etag)
      const ui = useUiStore()
      ui.addToast('Environment updated successfully', 'success')
      await fetchEnvironments(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to update environment'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteEnvironment(id: string, etag: number) {
    try {
      await environmentService.deleteEnvironment(id, etag)
      const ui = useUiStore()
      ui.addToast('Environment deleted successfully', 'success')
      await fetchEnvironments(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to delete environment'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    }
  }

  async function addOwnerToEnvironment(id: string, ownerName: string) {
    loading.value = true
    try {
      await environmentService.addOwner(id, ownerName)
      const ui = useUiStore()
      ui.addToast('Owner added successfully', 'success')
      await fetchEnvironment(id)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to add owner'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function removeOwnerFromEnvironment(id: string, ownerName: string, etag: number) {
    loading.value = true
    try {
      await environmentService.removeOwner(id, ownerName, etag)
      const ui = useUiStore()
      ui.addToast('Owner removed successfully', 'success')
      await fetchEnvironment(id)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) ? String((err as Record<string, unknown>).errorMessage) : 'Failed to remove owner'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearSelectedEnvironment() {
    selectedEnvironment.value = null
    selectedEtag.value = 0
  }

  return {
    environments,
    selectedEnvironment,
    selectedEtag,
    pagination,
    loading,
    error,
    fetchEnvironments,
    fetchEnvironment,
    createEnvironment,
    updateEnvironment,
    deleteEnvironment,
    addOwnerToEnvironment,
    removeOwnerFromEnvironment,
    clearSelectedEnvironment,
  }
})
