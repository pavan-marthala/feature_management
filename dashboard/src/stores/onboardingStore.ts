import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { environmentService } from '@/services/environmentService'
import { workspaceService } from '@/services/workspaceService'
import { workflowService } from '@/services/workflowService'

export type OnboardingStep = 
  | 'WELCOME'
  | 'ENV_SETUP'
  | 'WORKFLOW_SETUP'
  | 'STAGES_SETUP'
  | 'WORKSPACE_SETUP'
  | 'FEATURE_SETUP'
  | 'COMPLETED'

export type OnboardingStatus = 
  | 'NOT_STARTED'
  | 'ENV_CREATED'
  | 'WORKFLOW_CREATED'
  | 'STAGES_CREATED'
  | 'WORKSPACE_CREATED'
  | 'FEATURE_CREATED'

const STORAGE_KEY_COMPLETED = 'onboarding-completed'
const STORAGE_KEY_STEP = 'onboarding-step'
const STORAGE_KEY_STATUS = 'onboarding-status'
const STORAGE_KEY_STATE = 'onboarding-state'

const STEP_ORDER: OnboardingStep[] = [
  'WELCOME',
  'ENV_SETUP',
  'WORKFLOW_SETUP',
  'STAGES_SETUP',
  'WORKSPACE_SETUP',
  'FEATURE_SETUP',
  'COMPLETED'
]

interface OnboardingPersistedState {
  createdEnvironmentIds: string[]
  createdWorkspaceId: string | null
  createdWorkflowId: string | null
  createdFeatureId: string | null
}

export const useOnboardingStore = defineStore('onboarding', () => {
  // State
  const currentStep = ref<OnboardingStep>('WELCOME')
  const onboardingStatus = ref<OnboardingStatus>('NOT_STARTED')
  const completed = ref(false)
  const loading = ref(false)

  const createdEnvironmentIds = ref<string[]>([])
  const createdWorkspaceId = ref<string | null>(null)
  const createdWorkflowId = ref<string | null>(null)
  const createdFeatureId = ref<string | null>(null)

  const currentStepIndex = computed(() => {
    return Math.max(0, STEP_ORDER.indexOf(currentStep.value))
  })

  // Restore from localStorage
  function restoreState() {
    const completedFlag = localStorage.getItem(STORAGE_KEY_COMPLETED)
    if (completedFlag === 'true') {
      completed.value = true
      return
    }

    const savedStep = localStorage.getItem(STORAGE_KEY_STEP) as OnboardingStep
    if (savedStep && STEP_ORDER.includes(savedStep)) {
      currentStep.value = savedStep
    }

    const savedStatus = localStorage.getItem(STORAGE_KEY_STATUS) as OnboardingStatus
    if (savedStatus) {
      onboardingStatus.value = savedStatus
    }

    const savedState = localStorage.getItem(STORAGE_KEY_STATE)
    if (savedState) {
      try {
        const parsed = JSON.parse(savedState) as OnboardingPersistedState
        createdEnvironmentIds.value = parsed.createdEnvironmentIds || []
        createdWorkspaceId.value = parsed.createdWorkspaceId || null
        createdWorkflowId.value = parsed.createdWorkflowId || null
        createdFeatureId.value = parsed.createdFeatureId || null
      } catch {
        // ignore
      }
    }
  }

  function persistState() {
    localStorage.setItem(STORAGE_KEY_STEP, currentStep.value)
    localStorage.setItem(STORAGE_KEY_STATUS, onboardingStatus.value)
    localStorage.setItem(
      STORAGE_KEY_STATE,
      JSON.stringify({
        createdEnvironmentIds: createdEnvironmentIds.value,
        createdWorkspaceId: createdWorkspaceId.value,
        createdWorkflowId: createdWorkflowId.value,
        createdFeatureId: createdFeatureId.value,
      } satisfies OnboardingPersistedState),
    )
  }

  /**
   * Resolves the current step deterministically.
   * Workflow and Workspace are now independent — neither gates the other.
   * New order: ENV → WORKFLOW → STAGES → WORKSPACE → FEATURE
   */
  async function resolveStep() {
    if (completed.value) return

    // 1. Check workflows (independent of workspace)
    if (createdWorkflowId.value) {
      // Workflow already tracked, skip API check
      const currentIndex = STEP_ORDER.indexOf(currentStep.value)
      const stagesSetupIndex = STEP_ORDER.indexOf('STAGES_SETUP')
      if (currentIndex < stagesSetupIndex) {
        currentStep.value = 'STAGES_SETUP'
        onboardingStatus.value = 'WORKFLOW_CREATED'
        persistState()
      }
      return
    }

    try {
      const wfResponse = await workflowService.getWorkflows(0, 1)
      const hasWorkflows = (wfResponse.totalItems ?? 0) > 0
      
      if (hasWorkflows) {
        const currentIndex = STEP_ORDER.indexOf(currentStep.value)
        const stagesSetupIndex = STEP_ORDER.indexOf('STAGES_SETUP')
        
        if (currentIndex < stagesSetupIndex) {
          currentStep.value = 'STAGES_SETUP'
          onboardingStatus.value = 'WORKFLOW_CREATED'
          persistState()
        }

        const firstWorkflow = wfResponse.items?.[0]
        if (firstWorkflow?.id) {
          createdWorkflowId.value = firstWorkflow.id
          persistState()
        }
      }
    } catch {
      // ignore
    }
  }

  async function checkIfNewUser(): Promise<boolean> {
    if (completed.value) return false

    try {
      const [envResponse, wfResponse] = await Promise.all([
        environmentService.getEnvironments(0, 1),
        workflowService.getWorkflows(0, 1),
      ])
      const hasEnvs = (envResponse.totalItems ?? 0) > 0
      const hasWorkflows = (wfResponse.totalItems ?? 0) > 0
      return !hasEnvs && !hasWorkflows
    } catch {
      return false
    }
  }

  function goToStep(stepIndexOrName: number | OnboardingStep) {
    if (typeof stepIndexOrName === 'number') {
      currentStep.value = STEP_ORDER[stepIndexOrName] || 'WELCOME'
    } else {
      currentStep.value = stepIndexOrName
    }
    persistState()
  }

  function nextStep() {
    const idx = STEP_ORDER.indexOf(currentStep.value)
    if (idx !== -1 && idx < STEP_ORDER.length - 1) {
      currentStep.value = STEP_ORDER[idx + 1] as OnboardingStep
      persistState()
    }
  }

  function prevStep() {
    const idx = STEP_ORDER.indexOf(currentStep.value)
    if (idx > 0) {
      currentStep.value = STEP_ORDER[idx - 1] as OnboardingStep
      persistState()
    }
  }

  function setOnboardingStatus(status: OnboardingStatus) {
    onboardingStatus.value = status
    persistState()
  }

  function addEnvironmentId(id: string) {
    if (!createdEnvironmentIds.value.includes(id)) {
      createdEnvironmentIds.value.push(id)
      persistState()
    }
  }

  function removeEnvironmentId(id: string) {
    createdEnvironmentIds.value = createdEnvironmentIds.value.filter((e) => e !== id)
    persistState()
  }

  function setWorkspaceId(id: string) {
    createdWorkspaceId.value = id
    persistState()
  }

  function setWorkflowId(id: string) {
    if (!id) return // prevent null overwrite
    createdWorkflowId.value = id
    persistState()
  }

  function setFeatureId(id: string) {
    createdFeatureId.value = id
    persistState()
  }

  function completeOnboarding() {
    completed.value = true
    currentStep.value = 'COMPLETED'
    localStorage.setItem(STORAGE_KEY_COMPLETED, 'true')
    // Clean up wizard state
    localStorage.removeItem(STORAGE_KEY_STEP)
    localStorage.removeItem(STORAGE_KEY_STATUS)
    localStorage.removeItem(STORAGE_KEY_STATE)
  }

  function resetOnboarding() {
    currentStep.value = 'WELCOME'
    onboardingStatus.value = 'NOT_STARTED'
    completed.value = false
    createdEnvironmentIds.value = []
    createdWorkspaceId.value = null
    createdWorkflowId.value = null
    createdFeatureId.value = null
    localStorage.removeItem(STORAGE_KEY_COMPLETED)
    localStorage.removeItem(STORAGE_KEY_STEP)
    localStorage.removeItem(STORAGE_KEY_STATUS)
    localStorage.removeItem(STORAGE_KEY_STATE)
  }

  return {
    // State
    currentStep,
    currentStepIndex,
    onboardingStatus,
    completed,
    loading,
    createdEnvironmentIds,
    createdWorkspaceId,
    createdWorkflowId,
    createdFeatureId,
    // Actions
    restoreState,
    persistState,
    resolveStep,
    checkIfNewUser,
    goToStep,
    nextStep,
    prevStep,
    setOnboardingStatus,
    addEnvironmentId,
    removeEnvironmentId,
    setWorkspaceId,
    setWorkflowId,
    setFeatureId,
    completeOnboarding,
    resetOnboarding,
  }
})

