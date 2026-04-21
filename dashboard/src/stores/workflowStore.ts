import { defineStore } from 'pinia'
import { ref } from 'vue'
import type {
  Workflow,
  WorkflowBase,
  WorkflowResponse,
  WorkflowCreateRequest,
  WorkflowUpdateRequest,
  WorkflowStatus,
  Stage,
  StageRequest,
  Pagination,
} from '@/types'
import { workflowService } from '@/services/workflowService'
import { useUiStore } from './uiStore'

export const useWorkflowStore = defineStore('workflow', () => {
  // State
  const workflows = ref<WorkflowBase[]>([])
  const selectedWorkflow = ref<Workflow | null>(null)
  const pagination = ref<Pagination>({ page: 0, size: 25, totalItems: 0, totalPages: 0 })
  const loading = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')

  // Actions
  async function fetchWorkflows(page = 0, size = 25) {
    loading.value = true
    error.value = null
    try {
      const response: WorkflowResponse = await workflowService.getWorkflows(page, size)
      workflows.value = response.items || []
      pagination.value = {
        page: response.page,
        size: response.size,
        totalItems: response.totalItems,
        totalPages: response.totalPages,
      }
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to fetch workflows'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
    } finally {
      loading.value = false
    }
  }

  async function fetchWorkflow(id: string) {
    loading.value = true
    error.value = null
    try {
      const workflow = await workflowService.getWorkflow(id)
      selectedWorkflow.value = workflow
      return workflow
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to fetch workflow details'
      error.value = msg
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      return null
    } finally {
      loading.value = false
    }
  }

  async function createWorkflow(data: WorkflowCreateRequest) {
    loading.value = true
    try {
      const result = await workflowService.createWorkflow(data)
      const ui = useUiStore()
      ui.addToast('Workflow created successfully', 'success')
      await fetchWorkflows(pagination.value.page, pagination.value.size)
      return result
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to create workflow'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }


  async function updateWorkflow(id: string, data: WorkflowUpdateRequest, etag: number) {
    loading.value = true
    try {
      await workflowService.updateWorkflow(id, data, etag)
      const ui = useUiStore()
      ui.addToast('Workflow updated successfully', 'success')
      await fetchWorkflows(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to update workflow'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }


  async function deleteWorkflow(id: string, etag: number) {
    try {
      await workflowService.deleteWorkflow(id, etag)
      const ui = useUiStore()
      ui.addToast('Workflow deleted successfully', 'success')
      await fetchWorkflows(pagination.value.page, pagination.value.size)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to delete workflow'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    }
  }

  // Stage Actions
  async function addStage(workflowId: string, data: StageRequest) {
    loading.value = true
    try {
      const result = await workflowService.addStage(workflowId, data)
      const ui = useUiStore()
      ui.addToast('Stage added successfully', 'success')
      await fetchWorkflow(workflowId)
      return result
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to add stage'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateStage(workflowId: string, stageId: string, data: Partial<StageRequest>, etag: number) {
    loading.value = true
    try {
      await workflowService.updateStage(workflowId, stageId, data, etag)
      const ui = useUiStore()
      ui.addToast('Stage updated successfully', 'success')
      await fetchWorkflow(workflowId)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to update stage'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteStage(workflowId: string, stageId: string, etag: number) {
    try {
      await workflowService.deleteStage(workflowId, stageId, etag)
      const ui = useUiStore()
      ui.addToast('Stage removed successfully', 'success')
      await fetchWorkflow(workflowId)
    } catch (err: unknown) {
      const msg = (err && typeof err === 'object' && 'errorMessage' in err) 
        ? String((err as Record<string, unknown>).errorMessage) 
        : 'Failed to remove stage'
      const ui = useUiStore()
      ui.addToast(msg, 'error')
      throw err
    }
  }

  async function reorderStages(workflowId: string, stages: Stage[]) {
    loading.value = true
    try {
      // Update each stage that has a different orderIndex
      const updates = stages.map((s, idx) => {
        if (s.orderIndex !== idx) {
          return workflowService.updateStage(workflowId, s.id!, { orderIndex: idx }, s.version || 0)
        }
        return null
      }).filter(Boolean)

      if (updates.length > 0) {
        await Promise.all(updates)
        const ui = useUiStore()
        ui.addToast('Stages reordered successfully', 'success')
        await fetchWorkflow(workflowId)
      }
    } catch (err: unknown) {
      const ui = useUiStore()
      ui.addToast('Failed to reorder stages', 'error')
      throw err
    } finally {
      loading.value = false
    }
  }

  function clearSelectedWorkflow() {
    selectedWorkflow.value = null
  }

  return {
    // State
    workflows,
    selectedWorkflow,
    pagination,
    loading,
    error,
    searchQuery,
    // Actions
    fetchWorkflows,
    fetchWorkflow,
    createWorkflow,
    updateWorkflow,
    deleteWorkflow,
    addStage,
    updateStage,
    deleteStage,
    reorderStages,
    clearSelectedWorkflow,
  }
})
