import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  Workspace,
  WorkspaceRequest,
  WorkspaceResponse,
  WorkspaceSummary,
  Pagination,
} from '@/types'
import { workspaceService } from '@/services/workspaceService'
import { useUiStore } from './uiStore'

const STORAGE_KEY = 'feature-mgmt-workspace'

export const useWorkspaceStore = defineStore('workspace', () => {
  // State
  const workspaces = ref<Workspace[]>([])
  const selectedWorkspace = ref<Workspace | null>(null)
  const workspaceSummary = ref<WorkspaceSummary | null>(null)
  const pagination = ref<Pagination>({ page: 0, size: 25, totalItems: 0, totalPages: 0 })
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Restore persisted workspace on init
  function restoreSelection() {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        selectedWorkspace.value = JSON.parse(saved) as Workspace
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
  }

  // Actions
  async function fetchWorkspaces(page = 0, size = 25) {
    loading.value = true
    error.value = null
    try {
      const response: WorkspaceResponse = await workspaceService.getWorkspaces(page, size)
      workspaces.value = response.items || []
      pagination.value = {
        page: response.page,
        size: response.size,
        totalItems: response.totalItems,
        totalPages: response.totalPages,
      }
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err)
        ? String((err as Record<string, unknown>).errorMessage)
        : 'Failed to fetch workspaces'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
    } finally {
      loading.value = false
    }
  }

  async function fetchWorkspace(id: string) {
    loading.value = true
    error.value = null
    try {
      const { workspace } = await workspaceService.getWorkspace(id)
      selectedWorkspace.value = workspace
      localStorage.setItem(STORAGE_KEY, JSON.stringify(workspace))
      return workspace
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err)
        ? String((err as Record<string, unknown>).errorMessage)
        : 'Failed to fetch workspace'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      return null
    } finally {
      loading.value = false
    }
  }

  function selectWorkspace(workspace: Workspace) {
    selectedWorkspace.value = workspace
    localStorage.setItem(STORAGE_KEY, JSON.stringify(workspace))
  }

  async function createWorkspace(data: WorkspaceRequest) {
    loading.value = true
    try {
      const result = await workspaceService.createWorkspace(data)
      const ui = useUiStore()
      ui.addToast('Workspace created successfully', 'success')
      await fetchWorkspaces(pagination.value.page, pagination.value.size)
      return result
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err)
        ? String((err as Record<string, unknown>).errorMessage)
        : 'Failed to create workspace'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateWorkspace(id: string, data: WorkspaceRequest, etag: number) {
    loading.value = true
    try {
      await workspaceService.updateWorkspace(id, data, etag)
      const ui = useUiStore()
      ui.addToast('Workspace updated successfully', 'success')
      await fetchWorkspaces(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err)
        ? String((err as Record<string, unknown>).errorMessage)
        : 'Failed to update workspace'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteWorkspace(id: string, etag: number) {
    try {
      await workspaceService.deleteWorkspace(id, etag)
      const ui = useUiStore()
      ui.addToast('Workspace deleted successfully', 'success')
      // Clear selection if deleted workspace was selected
      if (selectedWorkspace.value?.id === id) {
        clearSelection()
      }
      await fetchWorkspaces(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err)
        ? String((err as Record<string, unknown>).errorMessage)
        : 'Failed to delete workspace'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    }
  }

  async function fetchSummary(id: string) {
    try {
      workspaceSummary.value = await workspaceService.getWorkspaceSummary(id)
    } catch {
      workspaceSummary.value = null
    }
  }

  function clearSelection() {
    selectedWorkspace.value = null
    workspaceSummary.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    // State
    workspaces,
    selectedWorkspace,
    workspaceSummary,
    pagination,
    loading,
    error,
    // Actions
    restoreSelection,
    fetchWorkspaces,
    fetchWorkspace,
    selectWorkspace,
    createWorkspace,
    updateWorkspace,
    deleteWorkspace,
    fetchSummary,
    clearSelection,
  }
})
