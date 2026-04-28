<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { workflowService } from '@/services/workflowService'
import { featureService } from '@/services/featureService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  Flag, ArrowRight, Loader2, AlertCircle, Layers,
} from 'lucide-vue-next'
import type { FeatureConfiguration } from '@/types'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()
const envStore = useEnvironmentStore()

const name = ref('')
const description = ref('')
const enabled = ref(true)
const creating = ref(false)
const errorMsg = ref('')
const firstEnvName = ref('first stage')
const firstEnvId = ref('')

onMounted(async () => {
  await envStore.fetchEnvironments(0, 100)
  // Determine the first stage environment from the workflow
  const wfId = onboarding.createdWorkflowId
  if (wfId) {
    try {
      const wf = await workflowService.getWorkflow(wfId)
      if (wf?.stages?.length) {
        const sorted = [...wf.stages].sort((a, b) => a.orderIndex - b.orderIndex)
        const firstStage = sorted[0]
        if (firstStage) {
          firstEnvId.value = firstStage.environmentId
          firstEnvName.value = firstStage.environmentName || envStore.environments.find(e => e.id === firstStage.environmentId)?.name || 'first stage'
        }
      }
    } catch {
      // fallback
    }
  }
})

const stageLabel = computed(() => firstEnvName.value)

async function handleCreate() {
  if (!name.value.trim()) {
    errorMsg.value = 'Feature name is required'
    return
  }
  if (!firstEnvId.value) {
    errorMsg.value = 'Could not determine target environment'
    return
  }
  creating.value = true
  errorMsg.value = ''
  try {
    const config: FeatureConfiguration = { strategy: 'BooleanFeatureStrategy', value: enabled.value }
    const result = await featureService.createFeature({
      name: name.value.trim(),
      description: description.value.trim() || undefined,
      envId: firstEnvId.value,
      configuration: config,
      enabled: enabled.value,
    })
    onboarding.setFeatureId(result.id)
    emit('next')
  } catch {
    errorMsg.value = 'Failed to create feature. Please try again.'
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="step-feat">
    <div class="step-feat__form animate-fadeInUp">
      <h2 class="step-feat__title">
        <Flag :size="24" class="step-feat__title-icon" />
        Create your first feature flag
      </h2>
      <p class="step-feat__desc">
        This feature will be created in your first environment (<strong>{{ stageLabel }}</strong>).
        You'll promote it through your pipeline next.
      </p>

      <!-- Context badge -->
      <GlassCard class="step-feat__context" padding="12px 16px">
        <div class="context-row">
          <Layers :size="16" />
          <span>Creating in: <strong>{{ stageLabel }}</strong> (Stage 1)</span>
        </div>
      </GlassCard>

      <div class="step-feat__field">
        <label class="step-feat__label">Feature Name *</label>
        <input
          v-model="name"
          type="text"
          class="step-feat__input"
          placeholder="e.g., DarkMode"
          :disabled="creating"
        />
      </div>

      <div class="step-feat__field">
        <label class="step-feat__label">Description <span class="optional">(optional)</span></label>
        <input
          v-model="description"
          type="text"
          class="step-feat__input"
          placeholder="Enable dark mode for beta users"
          :disabled="creating"
        />
      </div>

      <div class="step-feat__field">
        <label class="step-feat__label">Enabled</label>
        <label class="toggle">
          <input v-model="enabled" type="checkbox" class="toggle__input" />
          <span class="toggle__slider" />
          <span class="toggle__text">{{ enabled ? 'On' : 'Off' }}</span>
        </label>
      </div>

      <div v-if="errorMsg" class="step-feat__error">
        <AlertCircle :size="14" />
        {{ errorMsg }}
      </div>

      <button
        class="btn btn--primary step-feat__cta"
        :disabled="creating || !name.trim()"
        @click="handleCreate"
      >
        <Loader2 v-if="creating" :size="16" class="spin" />
        <template v-else>
          Create Feature
          <ArrowRight :size="16" />
        </template>
      </button>
    </div>
  </div>
</template>

<style scoped>
.step-feat { max-width: 560px; }

.step-feat__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-feat__title-icon { color: var(--accent-amber); }

.step-feat__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.step-feat__context {
  margin-top: 1.25rem;
  border-color: rgba(34, 211, 238, 0.2) !important;
  background: rgba(34, 211, 238, 0.04) !important;
}

.context-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.context-row strong { color: var(--accent-cyan); }

.step-feat__field { margin-top: 1.5rem; }

.step-feat__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  display: block;
  margin-bottom: 8px;
}

.optional { font-weight: 400; text-transform: none; }

.step-feat__input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.9rem;
}

.step-feat__input:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

/* Toggle */
.toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.toggle__input { display: none; }

.toggle__slider {
  width: 44px;
  height: 24px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  position: relative;
  transition: background 0.2s ease;
}

.toggle__slider::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  transition: transform 0.2s ease;
}

.toggle__input:checked + .toggle__slider {
  background: var(--accent-emerald);
}

.toggle__input:checked + .toggle__slider::after {
  transform: translateX(20px);
}

.toggle__text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.step-feat__error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 1rem;
  color: var(--accent-rose);
  font-size: 0.8rem;
}

.step-feat__cta {
  margin-top: 2rem;
  padding: 12px 28px;
  font-size: 0.9rem;
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
</style>
