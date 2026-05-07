<script setup lang="ts">
import { ref } from 'vue'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { useUiStore } from '@/stores/uiStore'
import Modal from '@/components/ui/Modal.vue'
import { Plus, X } from 'lucide-vue-next'

const envStore = useEnvironmentStore()
const uiStore = useUiStore()

const name = ref('')
const description = ref('')
const owners = ref<string[]>([])
const newOwner = ref('')
const submitting = ref(false)
const errors = ref<Record<string, string>>({})

function close() {
  uiStore.environmentModalOpen = false
  name.value = ''
  description.value = ''
  owners.value = []
  newOwner.value = ''
  errors.value = {}
}

function validate(): boolean {
  errors.value = {}
  if (!name.value || name.value.length < 2 || name.value.length > 36) {
    errors.value.name = 'Name must be 2-36 characters'
  }
  if (name.value && !/^[a-zA-Z0-9]+$/.test(name.value)) {
    errors.value.name = 'Name can only contain letters and numbers'
  }
  if (description.value && description.value.length > 255) {
    errors.value.description = 'Description must be 255 characters or less'
  }
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true

  try {
    const result = await envStore.createEnvironment({
      name: name.value,
      description: description.value || undefined,
      owners: owners.value.length ? owners.value : undefined,
    })
    
    if (result) {
      // Re-fetch environments to update globally
      await envStore.fetchEnvironments(0, 100)
      close()
    }
  } catch {
    // Toast already shown via store
  } finally {
    submitting.value = false
  }
}

function addOwner() {
  if (newOwner.value.trim() && !owners.value.includes(newOwner.value.trim())) {
    owners.value.push(newOwner.value.trim())
    newOwner.value = ''
  }
}

function removeOwner(idx: number) {
  owners.value.splice(idx, 1)
}
</script>

<template>
  <Modal :show="uiStore.environmentModalOpen" title="Create Environment" size="md" @close="close">
    <form @submit.prevent="handleSubmit" class="form">
      <div class="form-group">
        <label class="form-label" for="env-name">Environment Name *</label>
        <input
          id="env-name"
          v-model="name"
          type="text"
          class="form-input"
          :class="{ 'form-input--error': errors.name }"
          placeholder="e.g., Development, Staging, Production"
          maxlength="36"
        />
        <span v-if="errors.name" class="form-error">{{ errors.name }}</span>
        <span class="form-hint">Alphanumeric, 2-36 characters</span>
      </div>

      <div class="form-group">
        <label class="form-label" for="env-desc">Description</label>
        <textarea
          id="env-desc"
          v-model="description"
          class="form-input form-textarea"
          placeholder="Describe this environment..."
          rows="3"
          maxlength="255"
        ></textarea>
        <span v-if="errors.description" class="form-error">{{ errors.description }}</span>
      </div>

      <div class="form-group">
        <label class="form-label">Owners</label>
        <div class="owners-input">
          <input
            v-model="newOwner"
            class="form-input"
            placeholder="Add owner..."
            @keydown.enter.prevent="addOwner"
          />
          <button type="button" class="btn btn--ghost btn--sm" @click="addOwner">
            <Plus :size="16" /> Add
          </button>
        </div>
        <div v-if="owners.length" class="owner-chips">
          <div v-for="(owner, idx) in owners" :key="idx" class="owner-chip">
            <span>{{ owner }}</span>
            <button type="button" class="owner-chip__remove" @click="removeOwner(idx)">
              <X :size="14" />
            </button>
          </div>
        </div>
      </div>
    </form>

    <template #footer>
      <button type="button" class="btn btn--ghost" @click="close">Cancel</button>
      <button type="button" class="btn btn--primary" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? 'Creating...' : 'Create Environment' }}
      </button>
    </template>
  </Modal>
</template>

<style scoped>
.form {
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
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.form-input {
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.875rem;
  font-family: inherit;
  transition: all var(--transition-fast);
}

.form-input:focus {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
  outline: none;
}

.form-input--error { border-color: var(--accent-rose); }
.form-input::placeholder { color: var(--text-muted); }
.form-textarea { resize: vertical; min-height: 80px; }
.form-error { font-size: 0.75rem; color: var(--accent-rose); }
.form-hint { font-size: 0.75rem; color: var(--text-muted); }

.owners-input {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.owners-input .form-input { flex: 1; }

.owner-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.owner-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  font-size: 0.8rem;
}

.owner-chip__remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  padding: 2px;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.owner-chip__remove:hover { color: var(--accent-rose); }

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

.btn--primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.btn--sm { padding: 6px 14px; font-size: 0.8rem; }
</style>
