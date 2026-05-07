<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflowStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Badge from '@/components/ui/Badge.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import SearchInput from '@/components/ui/SearchInput.vue'
import Modal from '@/components/ui/Modal.vue'
import type { WorkflowStatus } from '@/types'
import {
  GitBranch,
  Plus,
  Zap,
  Layers,
  Loader2,
  Trash2,
} from 'lucide-vue-next'

const workflowStore = useWorkflowStore()
const router = useRouter()
const searchQuery = ref('')

// Create workflow modal
const showCreateModal = ref(false)
const newWorkflowName = ref('')
const creatingWorkflow = ref(false)

const filteredWorkflows = computed(() => {
  if (!searchQuery.value) return workflowStore.workflows
  const q = searchQuery.value.toLowerCase()
  return workflowStore.workflows.filter(
    (w) => w.name.toLowerCase().includes(q)
  )
})

onMounted(async () => {
  await workflowStore.fetchWorkflows()
})

function getStatusBadge(status?: WorkflowStatus) {
  switch (status) {
    case 'ACTIVE': return { label: 'Active', variant: 'success' as const }
    case 'DRAFT': return { label: 'Draft', variant: 'warning' as const }
    case 'ARCHIVED': return { label: 'Archived', variant: 'danger' as const }
    default: return { label: status || 'Unknown', variant: 'default' as const }
  }
}

function selectWorkflow(id?: string) {
  if (id) router.push(`/workflows/${id}`)
}

async function handleCreateWorkflow() {
  if (!newWorkflowName.value.trim()) return
  creatingWorkflow.value = true
  try {
    const id = await workflowStore.createWorkflow({
      name: newWorkflowName.value.trim(),
      status: 'DRAFT',
    })
    showCreateModal.value = false
    newWorkflowName.value = ''
    if (id) {
      router.push(`/workflows/${id}`)
    }
  } finally {
    creatingWorkflow.value = false
  }
}

async function handleDeleteWorkflow(id: string, version: number, event: Event) {
  event.stopPropagation()
  if (confirm('Are you sure you want to delete this workflow?')) {
    await workflowStore.deleteWorkflow(id, version)
  }
}
</script>

<template>
  <div class="wf-list">
    <!-- Hero -->
    <div class="wf-list__hero animate-fadeInUp">
      <div class="wf-list__hero-content">
        <h1 class="wf-list__title">
          <Zap :size="28" class="wf-list__title-icon" />
          Workflows
        </h1>
        <p class="wf-list__subtitle">
          Manage reusable deployment strategies that define how features propagate across environments.
        </p>
      </div>
      <div class="wf-list__hero-actions">
        <button class="btn btn--primary" @click="showCreateModal = true" id="create-workflow-btn">
          <Plus :size="18" />
          New Workflow
        </button>
      </div>
    </div>

    <!-- Search -->
    <div class="wf-list__search animate-fadeInUp stagger-1">
      <SearchInput v-model="searchQuery" placeholder="Search workflows..." />
    </div>

    <!-- Loading -->
    <div v-if="workflowStore.loading" class="wf-list__grid">
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-2" />
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-3" />
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-4" />
    </div>

    <!-- Empty state -->
    <div v-else-if="workflowStore.workflows.length === 0" class="wf-list__empty animate-fadeInUp stagger-2">
      <GlassCard hover>
        <div class="empty-state">
          <GitBranch :size="48" class="empty-state__icon" />
          <h3>No workflows yet</h3>
          <p>Create your first workflow to define a deployment pipeline.</p>
          <button class="btn btn--primary" @click="showCreateModal = true">
            <Plus :size="18" /> Create Workflow
          </button>
        </div>
      </GlassCard>
    </div>

    <!-- Workflow Grid -->
    <div v-else class="wf-list__grid">
      <GlassCard
        v-for="(wf, idx) in filteredWorkflows"
        :key="wf.id"
        hover
        gradient
        class="wf-card animate-fadeInUp"
        :class="`stagger-${Math.min(idx + 2, 6)}`"
        @click="selectWorkflow(wf.id)"
      >
        <div class="wf-card__header">
          <div class="wf-card__icon">
            <GitBranch :size="20" />
          </div>
          <div class="wf-card__header-right">
            <Badge v-bind="getStatusBadge(wf.status)" />
            <button
              class="action-btn action-btn--danger"
              title="Delete workflow"
              @click="handleDeleteWorkflow(wf.id!, wf.version || 0, $event)"
            >
              <Trash2 :size="16" />
            </button>
          </div>
        </div>
        <h3 class="wf-card__name">{{ wf.name }}</h3>
        <div class="wf-card__stats">
          <div class="wf-card__stat">
            <Layers :size="14" />
            <span>Version {{ wf.version || 1 }}</span>
          </div>
        </div>
      </GlassCard>
    </div>

    <!-- Create Workflow Modal -->
    <Modal
      :show="showCreateModal"
      title="Create Workflow"
      @close="showCreateModal = false"
    >
      <form @submit.prevent="handleCreateWorkflow" class="create-form">
        <div class="form-group">
          <label class="form-label">Workflow Name *</label>
          <input
            v-model="newWorkflowName"
            type="text"
            class="form-input"
            placeholder="e.g., Standard Release Pipeline"
            autofocus
          />
        </div>
        <div class="modal-actions">
          <button type="button" class="btn btn--ghost" @click="showCreateModal = false">Cancel</button>
          <button type="submit" class="btn btn--primary" :disabled="creatingWorkflow || !newWorkflowName.trim()">
            <Loader2 v-if="creatingWorkflow" :size="16" class="spin" />
            <template v-else>Create Workflow</template>
          </button>
        </div>
      </form>
    </Modal>
  </div>
</template>

<style scoped>
.wf-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.wf-list__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  flex-wrap: wrap;
}

.wf-list__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.03em;
}

.wf-list__title-icon {
  color: var(--accent-cyan);
}

.wf-list__subtitle {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-top: 6px;
}

.wf-list__search {
  max-width: 400px;
}

.wf-list__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

/* Workflow Card */
.wf-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  position: relative;
}

.wf-card:hover {
  transform: translateY(-2px);
}

.wf-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wf-card__header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wf-card__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}


.wf-card__name {
  font-size: 1.1rem;
  font-weight: 700;
}

.wf-card__stats {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.wf-card__stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: var(--text-secondary);
}


.action-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
}

.action-btn--danger:hover {
  color: var(--accent-rose);
  background: rgba(251, 113, 133, 0.1);
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.empty-state__icon {
  color: var(--text-muted);
  margin-bottom: 8px;
}

.empty-state h3 {
  font-size: 1.1rem;
  font-weight: 600;
}

.empty-state p {
  color: var(--text-muted);
  font-size: 0.85rem;
}

/* Modal Form */
.create-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 0.5rem 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
}

.form-input {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.9rem;
}

.form-input:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 1rem;
}

/* Buttons */
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

@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }

@media (max-width: 640px) {
  .wf-list__hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .wf-list__grid {
    grid-template-columns: 1fr;
  }
}
</style>
