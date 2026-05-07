import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Environment, EnvironmentRequest, Pagination } from '@/types'
import { environmentService } from '@/services/environmentService'
import { useUiStore } from './uiStore'

const STORAGE_KEY = 'feature-mgmt-environment'

export const useEnvironmentStore = defineStore('environment', () => {
  const environments = ref<Environment[]>([])
  const selectedEnvironment = ref<Environment | null>(null)
  const selectedEtag = ref<number>(0)
  const pagination = ref<Pagination>({ page: 0, size: 25, totalItems: 0, totalPages: 0 })
  const loading = ref(false)
  const error = ref<string | null>(null)
  const initialized = ref(false)

  const activeEnvironmentId = computed(() => selectedEnvironment.value?.id || null)

  function restoreSelection() {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        selectedEnvironment.value = JSON.parse(saved) as Environment
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
  }

  async function initActiveEnvironment() {
    if (initialized.value) return
    initialized.value = true

    restoreSelection()
    await fetchEnvironments(0, 100)

    if (selectedEnvironment.value) {
      const stillExists = environments.value.find(e => e.id === selectedEnvironment.value?.id)
      if (stillExists) {
        selectedEnvironment.value = stillExists
        localStorage.setItem(STORAGE_KEY, JSON.stringify(stillExists))
        return
      }
    }

    if (environments.value.length > 0) {
      const first = environments.value[0]
      if (first) selectEnvironment(first)
    }
  }

  function switchEnvironment(environment: Environment) {
    selectedEnvironment.value = environment
    localStorage.setItem(STORAGE_KEY, JSON.stringify(environment))
  }

  function selectEnvironment(environment: Environment) {
    selectedEnvironment.value = environment
    localStorage.setItem(STORAGE_KEY, JSON.stringify(environment))
  }

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
    activeEnvironmentId,
    selectedEtag,
    pagination,
    loading,
    error,
    initialized,
    fetchEnvironments,
    fetchEnvironment,
    createEnvironment,
    updateEnvironment,
    deleteEnvironment,
    addOwnerToEnvironment,
    removeOwnerFromEnvironment,
    clearSelectedEnvironment,
    restoreSelection,
    initActiveEnvironment,
    switchEnvironment,
    selectEnvironment,
  }
})
