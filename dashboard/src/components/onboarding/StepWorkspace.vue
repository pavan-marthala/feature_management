<script setup lang="ts">
import { ref } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  FolderKanban, ArrowRight, Loader2, AlertCircle, Flag, Layers,
} from 'lucide-vue-next'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()
const workspaceStore = useWorkspaceStore()

const name = ref('')
const description = ref('')
const creating = ref(false)
const errorMsg = ref('')

const examples = ['Checkout Service', 'Payment API', 'Auth Module', 'Notification Engine']

function fillExample(example: string) {
  name.value = example
}

async function handleCreate() {
  if (!name.value.trim()) {
    errorMsg.value = 'Workspace name is required'
    return
  }
  creating.value = true
  errorMsg.value = ''
  try {
    // 1. Create workspace (store internally fetches workspaces to ensure sync)
    const wsResult = await workspaceStore.createWorkspace({
      name: name.value.trim(),
      description: description.value.trim() || undefined,
    })
    
    // 2. Set active workspace in the store
    const fullWs = workspaceStore.workspaces.find(w => w.id === wsResult.id)
    if (fullWs) {
      workspaceStore.selectWorkspace(fullWs)
    }
    
    // 3. Update local explicit onboarding state
    onboarding.setWorkspaceId(wsResult.id)
    onboarding.setOnboardingStatus('WORKSPACE_CREATED')

    // 4. Move to next step
    emit('next')
  } catch (err: unknown) {
    const msg = (err && typeof err === 'object' && 'errorMessage' in err)
      ? String((err as Record<string, unknown>).errorMessage)
      : 'Failed to create workspace. Please try again.'
    errorMsg.value = msg
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="step-ws">
    <div class="step-ws__form animate-fadeInUp">
      <h2 class="step-ws__title">
        <FolderKanban :size="24" class="step-ws__title-icon" />
        Create your first workspace
      </h2>
      <p class="step-ws__desc">
        A workspace groups related features under one project boundary.
        Think of it as an organizational scope for a microservice or product area.
      </p>

      <div class="step-ws__field">
        <label class="step-ws__label">Workspace Name *</label>
        <input
          v-model="name"
          type="text"
          class="step-ws__input"
          placeholder="e.g., Checkout Service"
          :disabled="creating"
        />
        <div class="step-ws__examples">
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

      <div class="step-ws__field">
        <label class="step-ws__label">Description <span class="optional">(optional)</span></label>
        <textarea
          v-model="description"
          class="step-ws__textarea"
          placeholder="Manages payment and checkout feature flags"
          rows="3"
          :disabled="creating"
        />
      </div>

      <div v-if="errorMsg" class="step-ws__error">
        <AlertCircle :size="14" />
        {{ errorMsg }}
      </div>

      <button
        class="btn btn--primary step-ws__cta"
        :disabled="creating || !name.trim()"
        @click="handleCreate"
      >
        <Loader2 v-if="creating" :size="16" class="spin" />
        <template v-else>
          Create Workspace
          <ArrowRight :size="16" />
        </template>
      </button>
    </div>

    <div class="step-ws__preview animate-fadeInUp stagger-1">
      <GlassCard gradient>
        <div class="preview-header">
          <div class="preview-icon">
            <FolderKanban :size="20" />
          </div>
          <h4>{{ name || 'Your Workspace' }}</h4>
        </div>
        <p class="preview-desc">{{ description || 'Your workspace will contain:' }}</p>
        <div class="preview-items">
          <div class="preview-item">
            <Flag :size="16" />
            <span>Feature Flags</span>
          </div>
          <div class="preview-item">
            <Layers :size="16" />
            <span>Organized Features</span>
          </div>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.step-ws {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 3rem;
  align-items: start;
}

.step-ws__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-ws__title-icon { color: var(--accent-cyan); }

.step-ws__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.step-ws__field { margin-top: 1.5rem; }

.step-ws__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  display: block;
  margin-bottom: 8px;
}

.optional { font-weight: 400; text-transform: none; }

.step-ws__input,
.step-ws__textarea {
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

.step-ws__input:focus,
.step-ws__textarea:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

.step-ws__textarea { resize: vertical; }

.step-ws__examples {
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

.step-ws__error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 1rem;
  color: var(--accent-rose);
  font-size: 0.8rem;
}

.step-ws__cta {
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

/* Shared */
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
  .step-ws { grid-template-columns: 1fr; }
}
</style>
