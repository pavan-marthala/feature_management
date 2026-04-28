<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { workflowService } from '@/services/workflowService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  GitBranch, ArrowRight, Plus, X, Loader2, AlertCircle, Sparkles,
} from 'lucide-vue-next'
import type { StageType } from '@/types'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()
const envStore = useEnvironmentStore()

interface PipelineStage {
  environmentId: string
  environmentName: string
  type: StageType
}

const pipeline = ref<PipelineStage[]>([])
const saving = ref(false)
const errorMsg = ref('')

const availableEnvs = computed(() =>
  envStore.environments.filter(
    (env) => !pipeline.value.some((s) => s.environmentId === env.id),
  ),
)

const suggestedOrder = ['dev', 'staging', 'prod', 'qa', 'canary', 'preview']

onMounted(async () => {
  await envStore.fetchEnvironments(0, 100)
})

function addToPipeline(envId: string, envName: string) {
  pipeline.value.push({ environmentId: envId, environmentName: envName, type: 'MANUAL' })
}

function removeFromPipeline(index: number) {
  pipeline.value.splice(index, 1)
}

function applySuggestedOrder() {
  const sorted = [...envStore.environments].sort((a, b) => {
    const ai = suggestedOrder.indexOf(a.name.toLowerCase())
    const bi = suggestedOrder.indexOf(b.name.toLowerCase())
    return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi)
  })
  pipeline.value = sorted.map((env) => ({
    environmentId: env.id,
    environmentName: env.name,
    type: 'MANUAL' as StageType,
  }))
}

function setStageType(index: number, type: StageType) {
  if (pipeline.value[index]) {
    pipeline.value[index].type = type
  }
}

async function handleSave() {
  if (pipeline.value.length < 2) {
    errorMsg.value = 'Add at least 2 stages to your pipeline'
    return
  }
  const wfId = onboarding.createdWorkflowId
  if (!wfId) {
    errorMsg.value = 'No workflow found. Go back and create a workflow first.'
    return
  }

  saving.value = true
  errorMsg.value = ''
  try {
    for (let i = 0; i < pipeline.value.length; i++) {
      const stage = pipeline.value[i]
      if (stage) {
        await workflowService.addStage(wfId, {
          environmentId: stage.environmentId,
          type: stage.type,
          orderIndex: i,
        })
      }
    }
    onboarding.setOnboardingStatus('STAGES_CREATED')
    emit('next')
  } catch {
    errorMsg.value = 'Failed to save pipeline stages. Please try again.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="step-wf animate-fadeInUp">
    <h2 class="step-wf__title">
      <GitBranch :size="24" class="step-wf__title-icon" />
      Design your promotion pipeline stages
    </h2>
    <p class="step-wf__desc">
      Define the order features travel through environments.
      This controls how features get promoted from development to production.
    </p>

    <!-- Suggested order button -->
    <button
      v-if="pipeline.length === 0 && envStore.environments.length >= 2"
      class="suggest-btn animate-fadeInUp stagger-1"
      @click="applySuggestedOrder"
    >
      <Sparkles :size="16" />
      Use suggested order
    </button>

    <div class="step-wf__builder">
      <!-- Available environments -->
      <div class="step-wf__available">
        <p class="step-wf__label">Available Environments</p>
        <div class="env-chips">
          <button
            v-for="env in availableEnvs"
            :key="env.id"
            class="env-chip"
            @click="addToPipeline(env.id, env.name)"
          >
            <Plus :size="14" />
            {{ env.name }}
          </button>
          <p v-if="availableEnvs.length === 0" class="env-chips__empty">All environments added</p>
        </div>
      </div>

      <!-- Pipeline visualization -->
      <div class="step-wf__pipeline">
        <p class="step-wf__label">Pipeline Order</p>
        <div v-if="pipeline.length === 0" class="pipeline-empty">
          <p>Click environments above to build your pipeline</p>
        </div>
        <div v-else class="pipeline-flow">
          <template v-for="(stage, idx) in pipeline" :key="stage.environmentId">
            <GlassCard class="pipeline-card" hover>
              <div class="pipeline-card__header">
                <span class="pipeline-card__num">{{ idx + 1 }}</span>
                <span class="pipeline-card__name">{{ stage.environmentName }}</span>
                <button class="pipeline-card__remove" @click="removeFromPipeline(idx)">
                  <X :size="14" />
                </button>
              </div>
              <div class="pipeline-card__type">
                <button
                  v-for="t in (['MANUAL', 'AUTOMATIC', 'SCHEDULED'] as StageType[])"
                  :key="t"
                  class="type-btn"
                  :class="{ 'type-btn--active': stage.type === t }"
                  @click="setStageType(idx, t)"
                >
                  {{ t === 'MANUAL' ? 'Manual' : t === 'AUTOMATIC' ? 'Auto' : 'Scheduled' }}
                </button>
              </div>
            </GlassCard>
            <div v-if="idx < pipeline.length - 1" class="pipeline-arrow">
              <ArrowRight :size="20" />
            </div>
          </template>
        </div>
      </div>
    </div>

    <div v-if="errorMsg" class="step-wf__error">
      <AlertCircle :size="14" />
      {{ errorMsg }}
    </div>

    <button
      class="btn btn--primary step-wf__cta"
      :disabled="saving || pipeline.length < 2"
      @click="handleSave"
    >
      <Loader2 v-if="saving" :size="16" class="spin" />
      <template v-else>
        Save Pipeline Stages & Continue
        <ArrowRight :size="16" />
      </template>
    </button>
    <p v-if="pipeline.length < 2" class="step-wf__req">Add at least 2 stages to continue</p>
  </div>
</template>

<style scoped>
.step-wf { max-width: 800px; }

.step-wf__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-wf__title-icon { color: var(--accent-indigo); }

.step-wf__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.suggest-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 1.25rem;
  padding: 10px 20px;
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.25);
  border-radius: var(--radius-md);
  color: var(--accent-indigo);
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.suggest-btn:hover {
  background: rgba(99, 102, 241, 0.15);
  transform: translateY(-1px);
}

.step-wf__builder { margin-top: 2rem; }

.step-wf__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  margin-bottom: 10px;
}

.env-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.env-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  color: var(--text-primary);
  font-size: 0.8rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.env-chip:hover {
  background: var(--glass-bg-hover);
  border-color: var(--accent-cyan);
  color: var(--accent-cyan);
}

.env-chips__empty {
  color: var(--text-muted);
  font-size: 0.8rem;
  font-style: italic;
}

/* Pipeline */
.step-wf__pipeline { margin-top: 1.5rem; }

.pipeline-empty {
  padding: 2rem;
  text-align: center;
  background: var(--glass-bg);
  border: 2px dashed var(--glass-border);
  border-radius: var(--radius-lg);
  color: var(--text-muted);
  font-size: 0.85rem;
}

.pipeline-flow {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
  row-gap: 1rem;
}

.pipeline-card {
  min-width: 160px;
  padding: 1rem !important;
}

.pipeline-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.pipeline-card__num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--gradient-accent);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  flex-shrink: 0;
}

.pipeline-card__name {
  font-weight: 700;
  font-size: 0.95rem;
  flex: 1;
}

.pipeline-card__remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all var(--transition-fast);
}

.pipeline-card__remove:hover {
  color: var(--accent-rose);
  background: rgba(251, 113, 133, 0.1);
}

.pipeline-card__type {
  display: flex;
  gap: 4px;
  background: rgba(0, 0, 0, 0.15);
  padding: 3px;
  border-radius: var(--radius-sm);
}

.type-btn {
  flex: 1;
  padding: 5px 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-muted);
  font-size: 0.68rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.type-btn--active {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.pipeline-arrow {
  color: var(--text-muted);
  padding: 0 6px;
  display: flex;
  align-items: center;
}

.step-wf__error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 1rem;
  color: var(--accent-rose);
  font-size: 0.8rem;
}

.step-wf__cta {
  margin-top: 2rem;
  padding: 12px 28px;
  font-size: 0.9rem;
}

.step-wf__req {
  margin-top: 6px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.2);
}

.btn--primary:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(34, 211, 238, 0.3);
  transform: translateY(-1px);
}

.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }

@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }

@media (max-width: 768px) {
  .pipeline-flow { flex-direction: column; align-items: stretch; }
  .pipeline-arrow { transform: rotate(90deg); justify-content: center; }
  .pipeline-card { min-width: auto; }
}
</style>
