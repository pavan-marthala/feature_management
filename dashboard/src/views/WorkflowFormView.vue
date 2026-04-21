<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflowStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import { ArrowLeft, Save } from 'lucide-vue-next'
import type { WorkflowStatus, WorkflowCreateRequest, WorkflowUpdateRequest } from '@/types'

const route = useRoute()
const router = useRouter()
const workflowStore = useWorkflowStore()

const isEdit = computed(() => route.name === 'workflow-edit')
const workflowId = computed(() => route.params.id as string)

const name = ref('')
const status = ref<WorkflowStatus>('DRAFT')
const submitting = ref(false)
const errors = ref<Record<string, string>>({})

onMounted(async () => {
  if (isEdit.value && workflowId.value) {
    const workflow = await workflowStore.fetchWorkflow(workflowId.value)
    if (workflow) {
      name.value = workflow.name
      status.value = workflow.status || 'DRAFT'
    }
  }
})

function validate(): boolean {
  errors.value = {}
  if (!name.value || name.value.length < 2) {
    errors.value.name = 'Name must be at least 2 characters'
  }
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  
  try {
    if (isEdit.value) {
      const payload: WorkflowUpdateRequest = {
        name: name.value,
        status: status.value
      }
      await workflowStore.updateWorkflow(workflowId.value, payload, workflowStore.selectedWorkflow?.version || 0)
      router.push(`/workflows/${workflowId.value}`)
    } else {
      const payload: WorkflowCreateRequest = {
        name: name.value,
        status: status.value
      }
      await workflowStore.createWorkflow(payload)
      router.push('/workflows')
    }
  } catch (err) {
    console.error('Submit failed', err)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="workflow-form-page">
    <button class="back-btn" @click="router.back()">
      <ArrowLeft :size="18" />
      Back
    </button>

    <div class="form-header animate-fadeInUp">
      <h1>{{ isEdit ? 'Edit Workflow' : 'Create Workflow' }}</h1>
      <p class="form-subtitle">Configure workflow metadata and operational status</p>
    </div>

    <form @submit.prevent="handleSubmit" class="form animate-fadeInUp stagger-1">
      <GlassCard>
        <h2 class="section-title">Workflow Details</h2>
        <div class="form-group">
          <label class="form-label" for="workflow-name">Workflow Name *</label>
          <input
            id="workflow-name"
            v-model="name"
            type="text"
            class="form-input"
            :class="{ 'form-input--error': errors.name }"
            placeholder="e.g., Production Release Pipeline"
          />
          <span v-if="errors.name" class="form-error">{{ errors.name }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">Workflow Status</label>
          <div class="status-selector">
            <button
              type="button"
              class="status-btn"
              :class="{ 'status-btn--active': status === 'DRAFT' }"
              @click="status = 'DRAFT'"
            >
              Draft
            </button>
            <button
              type="button"
              class="status-btn"
              :class="{ 'status-btn--active': status === 'ACTIVE' }"
              @click="status = 'ACTIVE'"
            >
              Active
            </button>
            <button
              type="button"
              class="status-btn"
              :class="{ 'status-btn--active': status === 'ARCHIVED' }"
              @click="status = 'ARCHIVED'"
            >
              Archived
            </button>
          </div>
          <p class="form-hint">Only Active workflows can be used for feature propagation.</p>
        </div>
      </GlassCard>

      <div class="form-actions animate-fadeInUp stagger-2">
        <button type="button" class="btn btn--ghost" @click="router.back()">Cancel</button>
        <button type="submit" class="btn btn--primary" :disabled="submitting">
          <Save :size="18" />
          {{ submitting ? 'Saving...' : (isEdit ? 'Update Workflow' : 'Create Workflow') }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.workflow-form-page {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  padding: 0;
  width: fit-content;
}

.back-btn:hover { color: var(--text-primary); }

.form-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
}

.form-subtitle {
  color: var(--text-secondary);
  margin-top: 4px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 1.25rem;
}

.form-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-input {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.875rem;
  transition: all var(--transition-fast);
}

.form-input:focus {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 2px rgba(34, 211, 238, 0.1);
}

.form-input--error {
  border-color: var(--accent-rose);
}

.status-selector {
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.1);
  padding: 4px;
  border-radius: var(--radius-md);
  width: fit-content;
}

.status-btn {
  padding: 6px 16px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.status-btn--active {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 4px;
}

.form-error {
  font-size: 0.75rem;
  color: var(--accent-rose);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn--primary { background: var(--gradient-accent); color: white; }
.btn--ghost { background: var(--glass-bg); color: var(--text-secondary); border: 1px solid var(--glass-border); }
</style>
