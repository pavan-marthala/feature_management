<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { environmentService } from '@/services/environmentService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  Layers, Plus, Check, Trash2, Loader2, AlertCircle,
} from 'lucide-vue-next'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()
const envStore = useEnvironmentStore()

interface PresetEnv {
  name: string
  description: string
  color: string
  creating: boolean
  created: boolean
  id: string | null
}

const presets = ref<PresetEnv[]>([
  { name: 'dev', description: 'Development', color: 'var(--accent-emerald)', creating: false, created: false, id: null },
  { name: 'staging', description: 'Staging', color: 'var(--accent-amber)', creating: false, created: false, id: null },
  { name: 'prod', description: 'Production', color: 'var(--accent-rose)', creating: false, created: false, id: null },
])

const customName = ref('')
const customCreating = ref(false)
const errorMsg = ref('')

const createdEnvs = ref<Array<{ id: string; name: string }>>([])

onMounted(async () => {
  // Restore any already-created environments
  await envStore.fetchEnvironments(0, 100)
  for (const env of envStore.environments) {
    createdEnvs.value.push({ id: env.id, name: env.name })
    // Mark presets as created
    const preset = presets.value.find((p) => p.name === env.name)
    if (preset) {
      preset.created = true
      preset.id = env.id
    }
    if (!onboarding.createdEnvironmentIds.includes(env.id)) {
      onboarding.addEnvironmentId(env.id)
    }
  }
})

async function createPreset(preset: PresetEnv) {
  if (preset.created || preset.creating) return
  preset.creating = true
  errorMsg.value = ''
  try {
    const result = await environmentService.createEnvironment({ name: preset.name, description: preset.description })
    preset.created = true
    preset.id = result.id
    createdEnvs.value.push({ id: result.id, name: preset.name })
    onboarding.addEnvironmentId(result.id)
  } catch {
    errorMsg.value = `Failed to create "${preset.name}"`
  } finally {
    preset.creating = false
  }
}

async function createCustom() {
  const name = customName.value.trim().toLowerCase()
  if (!name) return
  if (createdEnvs.value.some((e) => e.name === name)) {
    errorMsg.value = `"${name}" already exists`
    return
  }
  customCreating.value = true
  errorMsg.value = ''
  try {
    const result = await environmentService.createEnvironment({ name })
    createdEnvs.value.push({ id: result.id, name })
    onboarding.addEnvironmentId(result.id)
    customName.value = ''
  } catch {
    errorMsg.value = `Failed to create "${name}"`
  } finally {
    customCreating.value = false
  }
}

async function removeEnv(env: { id: string; name: string }) {
  try {
    const fetched = await environmentService.getEnvironment(env.id)
    await environmentService.deleteEnvironment(env.id, Number(fetched.etag))
    createdEnvs.value = createdEnvs.value.filter((e) => e.id !== env.id)
    onboarding.removeEnvironmentId(env.id)
    const preset = presets.value.find((p) => p.name === env.name)
    if (preset) {
      preset.created = false
      preset.id = null
    }
  } catch {
    errorMsg.value = `Failed to remove "${env.name}"`
  }
}

function canProceed() {
  return createdEnvs.value.length >= 2
}

function handleNext() {
  if (!canProceed()) {
    errorMsg.value = 'Create at least 2 environments to continue'
    return
  }
  emit('next')
}
</script>

<template>
  <div class="step-env">
    <div class="step-env__left animate-fadeInUp">
      <h2 class="step-env__title">
        <Layers :size="24" class="step-env__title-icon" />
        Set up your environments
      </h2>
      <p class="step-env__desc">
        Environments are deployment targets like dev, staging, and production.
        They're shared across all workspaces.
      </p>

      <!-- Presets -->
      <div class="step-env__presets">
        <p class="step-env__label">Quick create</p>
        <div class="step-env__preset-row">
          <button
            v-for="preset in presets"
            :key="preset.name"
            class="preset-btn"
            :class="{ 'preset-btn--created': preset.created, 'preset-btn--loading': preset.creating }"
            :disabled="preset.created || preset.creating"
            @click="createPreset(preset)"
          >
            <span class="preset-btn__dot" :style="{ background: preset.color }" />
            <span class="preset-btn__name">{{ preset.name }}</span>
            <Loader2 v-if="preset.creating" :size="14" class="preset-btn__spinner" />
            <Check v-else-if="preset.created" :size="14" class="preset-btn__check" />
            <Plus v-else :size="14" />
          </button>
        </div>
      </div>

      <!-- Custom -->
      <div class="step-env__custom">
        <p class="step-env__label">Or add custom</p>
        <form class="step-env__custom-form" @submit.prevent="createCustom">
          <input
            v-model="customName"
            type="text"
            placeholder="e.g., qa, canary, preview"
            class="step-env__input"
            :disabled="customCreating"
          />
          <button type="submit" class="btn btn--sm btn--primary" :disabled="customCreating || !customName.trim()">
            <Loader2 v-if="customCreating" :size="14" class="spin" />
            <Plus v-else :size="14" />
            Add
          </button>
        </form>
      </div>

      <!-- Validation -->
      <div v-if="errorMsg" class="step-env__error">
        <AlertCircle :size="14" />
        {{ errorMsg }}
      </div>

      <!-- Next -->
      <button class="btn btn--primary step-env__next" :disabled="!canProceed()" @click="handleNext">
        Continue — Create Workspace
      </button>
      <p v-if="!canProceed()" class="step-env__req">
        Create at least 2 environments to continue
      </p>
    </div>

    <!-- Right panel: live list -->
    <div class="step-env__right animate-fadeInUp stagger-1">
      <GlassCard>
        <div class="env-list-header">
          <h3>Created Environments</h3>
          <span class="env-count">{{ createdEnvs.length }}</span>
        </div>
        <div v-if="createdEnvs.length === 0" class="env-list-empty">
          <Layers :size="32" class="env-list-empty__icon" />
          <p>No environments yet</p>
        </div>
        <TransitionGroup v-else name="env-item" tag="div" class="env-list">
          <div v-for="env in createdEnvs" :key="env.id" class="env-list__item">
            <div class="env-list__info">
              <span class="env-list__dot" />
              <span class="env-list__name">{{ env.name }}</span>
            </div>
            <button class="env-list__remove" @click="removeEnv(env)" title="Remove">
              <Trash2 :size="14" />
            </button>
          </div>
        </TransitionGroup>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.step-env {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 3rem;
  align-items: start;
}

.step-env__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-env__title-icon { color: var(--accent-emerald); }

.step-env__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.step-env__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.step-env__presets { margin-top: 2rem; }

.step-env__preset-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.preset-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.preset-btn:hover:not(:disabled) {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
}

.preset-btn--created {
  border-color: rgba(34, 211, 238, 0.3);
  background: rgba(34, 211, 238, 0.05);
  cursor: default;
}

.preset-btn__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.preset-btn__check { color: var(--accent-emerald); }

.preset-btn__spinner { animation: spin 1s linear infinite; }

.step-env__custom { margin-top: 1.5rem; }

.step-env__custom-form {
  display: flex;
  gap: 8px;
}

.step-env__input {
  flex: 1;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.85rem;
}

.step-env__input:focus {
  border-color: var(--accent-cyan);
}

.step-env__error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 1rem;
  color: var(--accent-rose);
  font-size: 0.8rem;
}

.step-env__next {
  margin-top: 2rem;
  padding: 12px 28px;
  font-size: 0.9rem;
}

.step-env__req {
  margin-top: 6px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Right panel */
.env-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.env-list-header h3 {
  font-size: 1rem;
  font-weight: 700;
}

.env-count {
  background: var(--gradient-accent);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.env-list-empty {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--text-muted);
}

.env-list-empty__icon { opacity: 0.3; margin-bottom: 8px; }

.env-list { display: flex; flex-direction: column; gap: 6px; }

.env-list__item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.env-list__item:hover { background: rgba(255, 255, 255, 0.06); }

.env-list__info { display: flex; align-items: center; gap: 10px; }

.env-list__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-cyan);
}

.env-list__name {
  font-size: 0.85rem;
  font-weight: 600;
  font-family: monospace;
}

.env-list__remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  opacity: 0;
  transition: all var(--transition-fast);
}

.env-list__item:hover .env-list__remove { opacity: 1; }
.env-list__remove:hover { color: var(--accent-rose); background: rgba(251, 113, 133, 0.1); }

/* Transitions */
.env-item-enter-active { transition: all 0.3s ease-out; }
.env-item-leave-active { transition: all 0.2s ease-in; }
.env-item-enter-from { opacity: 0; transform: translateX(20px); }
.env-item-leave-to { opacity: 0; transform: translateX(-20px); }

/* Shared btn styles */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
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

.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.btn--sm { padding: 10px 16px; font-size: 0.8rem; }

@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }

@media (max-width: 768px) {
  .step-env { grid-template-columns: 1fr; }
  .step-env__title { font-size: 1.3rem; }
}
</style>
