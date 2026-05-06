import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
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
  const initialized = ref(false)

  // Computed
  const activeWorkspaceId = computed(() => selectedWorkspace.value?.id || null)

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

  /**
   * Initialize active workspace on app startup.
   * 1. Restore last persisted workspace
   * 2. Fetch all workspaces
   * 3. If persisted workspace exists in list → keep it
   * 4. Otherwise → select first available
   */
  async function initActiveWorkspace() {
    if (initialized.value) return
    initialized.value = true

    restoreSelection()
    await fetchWorkspaces(0, 100)

    if (selectedWorkspace.value) {
      // Validate persisted workspace still exists
      const stillExists = workspaces.value.find(w => w.id === selectedWorkspace.value?.id)
      if (stillExists) {
        // Update with fresh data
        selectedWorkspace.value = stillExists
        localStorage.setItem(STORAGE_KEY, JSON.stringify(stillExists))
        return
      }
    }

    // Auto-select first workspace
    if (workspaces.value.length > 0) {
      const first = workspaces.value[0]
      if (first) selectWorkspace(first)
    }
  }

  /**
   * Lightweight workspace switch — no navigation, just context change.
   * Views watch selectedWorkspace to auto-refresh.
   */
  function switchWorkspace(workspace: Workspace) {
    selectedWorkspace.value = workspace
    localStorage.setItem(STORAGE_KEY, JSON.stringify(workspace))
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
      await fetchWorkspaces(0, 100)
      // Auto-select newly created workspace
      const created = workspaces.value.find(w => w.id === result.id)
      if (created) {
        selectWorkspace(created)
      }
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
      await fetchWorkspaces(0, 100)
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
      await fetchWorkspaces(0, 100)
      // Auto-select first remaining workspace
      if (!selectedWorkspace.value && workspaces.value.length > 0) {
        const first = workspaces.value[0]
        if (first) selectWorkspace(first)
      }
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
    activeWorkspaceId,
    workspaceSummary,
    pagination,
    loading,
    error,
    initialized,
    // Actions
    restoreSelection,
    initActiveWorkspace,
    switchWorkspace,
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

