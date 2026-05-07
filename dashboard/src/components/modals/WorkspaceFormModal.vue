<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import { useUiStore } from '@/stores/uiStore'
import Modal from '@/components/ui/Modal.vue'

const router = useRouter()
const workspaceStore = useWorkspaceStore()
const uiStore = useUiStore()

const name = ref('')
const description = ref('')
const submitting = ref(false)

function close() {
  uiStore.workspaceModalOpen = false
  name.value = ''
  description.value = ''
}

async function handleSubmit() {
  if (!name.value.trim()) return
  submitting.value = true
  try {
    const result = await workspaceStore.createWorkspace({
      name: name.value.trim(),
      description: description.value.trim() || undefined,
    })
    if (result) {
      close()
      // The store handles auto-selecting the newly created workspace.
      router.push(`/features`) // ensure they are on the features view
    }
  } catch {
    // Error handled by store/toast
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <Modal :show="uiStore.workspaceModalOpen" title="Create Workspace" size="md" @close="close">
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
    </form>

    <template #footer>
      <button type="button" class="btn btn--ghost" @click="close">Cancel</button>
      <button type="button" class="btn btn--primary" @click="handleSubmit" :disabled="submitting || !name.trim()">
        {{ submitting ? 'Creating...' : 'Create Workspace' }}
      </button>
    </template>
  </Modal>
</template>

<style scoped>
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

.btn--primary:hover:not(:disabled) {
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
