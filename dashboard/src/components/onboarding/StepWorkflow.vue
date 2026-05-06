<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { workflowService } from '@/services/workflowService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  GitBranch, ArrowRight, Loader2, AlertCircle, Layers, Settings, Network,
} from 'lucide-vue-next'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()

const name = ref('')
const creating = ref(false)
const errorMsg = ref('')

const examples = ['Standard Pipeline', 'Fast Track', 'Hotfix Flow', 'Enterprise Delivery']

function fillExample(example: string) {
  name.value = example
}

async function handleCreate() {
  if (!name.value.trim()) {
    errorMsg.value = 'Workflow name is required'
    return
  }
  creating.value = true
  errorMsg.value = ''
  try {
    const wfResultId = await workflowService.createWorkflow({
      name: name.value.trim(),
      status: 'DRAFT',
    })
    
    onboarding.setWorkflowId(wfResultId)
    await nextTick()
    onboarding.setOnboardingStatus('WORKFLOW_CREATED')

    emit('next')
  } catch (err: unknown) {
    const msg = (err && typeof err === 'object' && 'errorMessage' in err)
      ? String((err as Record<string, unknown>).errorMessage)
      : 'Failed to create workflow. Please try again.'
    errorMsg.value = msg
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="step-wf">
    <div class="step-wf__form animate-fadeInUp">
      <h2 class="step-wf__title">
        <GitBranch :size="24" class="step-wf__title-icon" />
        Create your workflow
      </h2>
      <p class="step-wf__desc">
        A workflow is a reusable deployment strategy that defines how features propagate across environments.
        In the next step, you'll map environments to pipeline stages.
      </p>

      <div class="step-wf__field">
        <label class="step-wf__label">Workflow Name *</label>
        <input
          v-model="name"
          type="text"
          class="step-wf__input"
          placeholder="e.g., Standard Pipeline"
          :disabled="creating"
        />
        <div class="step-wf__examples">
          <button
            v-for="ex in examples"
            :key="ex"
            class="example-chip"
            @click="fillExample(ex)"
            type="button"
          >
            {{ ex }}
          </button>
        </div>
      </div>

      <div v-if="errorMsg" class="step-wf__error">
        <AlertCircle :size="14" />
        {{ errorMsg }}
      </div>

      <button
        class="btn btn--primary step-wf__cta"
        :disabled="creating || !name.trim()"
        @click="handleCreate"
      >
        <Loader2 v-if="creating" :size="16" class="spin" />
        <template v-else>
          Create Workflow
          <ArrowRight :size="16" />
        </template>
      </button>
    </div>

    <div class="step-wf__preview animate-fadeInUp stagger-1">
      <GlassCard gradient>
        <div class="preview-header">
          <div class="preview-icon">
            <GitBranch :size="20" />
          </div>
          <h4>{{ name || 'Your Workflow' }}</h4>
        </div>
        <p class="preview-desc">The deployment lifecycle container for your platform.</p>
        <div class="preview-items">
          <div class="preview-item">
            <Network :size="16" />
            <span>Propagation Stages</span>
          </div>
          <div class="preview-item">
            <Layers :size="16" />
            <span>Promotion Rules</span>
          </div>
          <div class="preview-item">
            <Settings :size="16" />
            <span>Environment Mapping</span>
          </div>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.step-wf {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 3rem;
  align-items: start;
}

.step-wf__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-wf__title-icon { color: var(--accent-cyan); }

.step-wf__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.step-wf__field { margin-top: 1.5rem; }

.step-wf__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  display: block;
  margin-bottom: 8px;
}

.step-wf__input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.9rem;
  transition: border-color var(--transition-fast);
}

.step-wf__input:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

.step-wf__examples {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.example-chip {
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  color: var(--text-muted);
  font-size: 0.72rem;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.example-chip:hover {
  background: rgba(34, 211, 238, 0.08);
  border-color: rgba(34, 211, 238, 0.3);
  color: var(--accent-cyan);
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

/* Preview */
.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.preview-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.preview-header h4 {
  font-size: 1.1rem;
  font-weight: 700;
}

.preview-desc {
  color: var(--text-muted);
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

.preview-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 0.85rem;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--radius-sm);
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
  .step-wf { grid-template-columns: 1fr; }
}
</style>
