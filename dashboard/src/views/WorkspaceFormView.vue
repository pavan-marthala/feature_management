<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import { FolderKanban, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
const workspaceStore = useWorkspaceStore()

const name = ref('')
const description = ref('')
const submitting = ref(false)

async function handleSubmit() {
  if (!name.value.trim()) return
  submitting.value = true
  try {
    const result = await workspaceStore.createWorkspace({
      name: name.value.trim(),
      description: description.value.trim() || undefined,
    })
    if (result) {
      router.push(`/workspaces/${result.id}`)
    }
  } catch {
    // Error handled by store
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="ws-form">
    <div class="ws-form__header animate-fadeInUp">
      <button class="btn btn--ghost" @click="router.push('/workspaces')">
        <ArrowLeft :size="18" /> Back
      </button>
      <h1 class="ws-form__title">
        <FolderKanban :size="24" class="ws-form__title-icon" />
        Create Workspace
      </h1>
    </div>

    <GlassCard class="ws-form__card animate-fadeInUp stagger-1">
      <form @submit.prevent="handleSubmit" class="ws-form__body">
        <div class="form-group">
          <label for="ws-name" class="form-label">Workspace Name</label>
          <input
            id="ws-name"
            v-model="name"
            type="text"
            class="form-input"
            placeholder="e.g., My Project"
            required
            autofocus
          />
        </div>

        <div class="form-group">
          <label for="ws-desc" class="form-label">Description</label>
          <textarea
            id="ws-desc"
            v-model="description"
            class="form-input form-textarea"
            placeholder="Optional description..."
            rows="3"
          ></textarea>
        </div>

        <div class="ws-form__actions">
          <button type="button" class="btn btn--ghost" @click="router.push('/workspaces')">Cancel</button>
          <button type="submit" class="btn btn--primary" :disabled="submitting || !name.trim()">
            {{ submitting ? 'Creating...' : 'Create Workspace' }}
          </button>
        </div>
      </form>
    </GlassCard>
  </div>
</template>

<style scoped>
.ws-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 640px;
}

.ws-form__header {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.ws-form__title {
  font-size: 1.5rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.03em;
}

.ws-form__title-icon {
  color: var(--accent-cyan);
}

.ws-form__body {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-input {
  padding: 10px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-primary);
  font-size: 0.9rem;
  font-family: inherit;
  transition: border-color var(--transition-fast);
  outline: none;
}

.form-input:focus {
  border-color: var(--accent-cyan);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.ws-form__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 0.5rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.2);
}

.btn--primary:hover {
  box-shadow: 0 4px 16px rgba(34, 211, 238, 0.3);
  transform: translateY(-1px);
}

.btn--primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}
</style>
