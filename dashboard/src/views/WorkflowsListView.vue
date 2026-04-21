<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflowStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Badge from '@/components/ui/Badge.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import PaginationComp from '@/components/ui/Pagination.vue'
import SearchInput from '@/components/ui/SearchInput.vue'
import { Plus, GitBranch, Pencil, Trash2, Settings } from 'lucide-vue-next'
import type { WorkflowStatus } from '@/types'

const workflowStore = useWorkflowStore()
const router = useRouter()

onMounted(async () => {
  await workflowStore.fetchWorkflows()
})

function getStatusBadge(status?: WorkflowStatus) {
  if (!status) return { label: 'Unknown', variant: 'default' as const }
  switch (status) {
    case 'ACTIVE': return { label: 'Active', variant: 'success' as const }
    case 'DRAFT': return { label: 'Draft', variant: 'warning' as const }
    case 'ARCHIVED': return { label: 'Archived', variant: 'danger' as const }
    default: return { label: status, variant: 'default' as const }
  }
}

function onPageChange(page: number) {
  workflowStore.fetchWorkflows(page, workflowStore.pagination.size)
}
</script>

<template>
  <div class="workflows-page">
    <!-- Header -->
    <div class="workflows-page__header animate-fadeInUp">
      <div>
        <h1 class="workflows-page__title">Workflows</h1>
        <p class="workflows-page__subtitle">Define and manage your feature propagation pipelines</p>
      </div>
      <button class="btn btn--primary" @click="router.push('/workflows/create')" id="create-workflow-btn">
        <Plus :size="18" />
        Create Workflow
      </button>
    </div>

    <!-- Filters -->
    <div class="workflows-page__filters animate-fadeInUp stagger-1">
      <SearchInput
        v-model="workflowStore.searchQuery"
        placeholder="Search workflows..."
        class="search-bar"
      />
    </div>

    <!-- Loading -->
    <LoadingSkeleton v-if="workflowStore.loading && workflowStore.workflows.length === 0" variant="table-row" :rows="5" />

    <!-- Empty State -->
    <div v-else-if="workflowStore.workflows.length === 0" class="empty-state animate-fadeInUp">
      <div class="empty-state__card glass">
        <GitBranch :size="56" class="empty-state__icon" />
        <h3>No workflows yet</h3>
        <p>Create your first propagation pipeline to automate feature rollouts.</p>
        <button class="btn btn--primary" @click="router.push('/workflows/create')">
          <Plus :size="18" /> Create Workflow
        </button>
      </div>
    </div>

    <!-- Workflow Table -->
    <div v-else class="workflows-table animate-fadeInUp stagger-2">
      <div class="desktop-view glass">
        <table class="data-table">
          <thead>
            <tr>
              <th>Workflow Name</th>
              <th>Status</th>
              <th>Version</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="workflow in workflowStore.workflows"
              :key="workflow.id"
              class="data-table__row"
              @click="router.push(`/workflows/${workflow.id}`)"
            >
              <td>
                <div class="workflow-name">
                  <GitBranch :size="18" class="workflow-icon" />
                  <span>{{ workflow.name }}</span>
                </div>
              </td>
              <td>
                <Badge v-bind="getStatusBadge(workflow.status)" />
              </td>
              <td>
                <span class="version-tag">v{{ workflow.version || 1 }}</span>
              </td>
              <td class="text-right" @click.stop>
                <div class="actions">
                  <button
                    class="action-btn"
                    title="Edit"
                    @click="router.push(`/workflows/${workflow.id}/edit`)"
                  >
                    <Pencil :size="16" />
                  </button>
                  <button
                    class="action-btn action-btn--danger"
                    title="Delete"
                    @click="workflowStore.deleteWorkflow(workflow.id!, workflow.version || 0)"
                  >
                    <Trash2 :size="16" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile View -->
      <div class="mobile-view">
        <GlassCard
          v-for="workflow in workflowStore.workflows"
          :key="workflow.id"
          class="workflow-card"
          hover
          @click="router.push(`/workflows/${workflow.id}`)"
        >
          <div class="workflow-card__header">
            <h3 class="workflow-card__name">{{ workflow.name }}</h3>
            <Badge v-bind="getStatusBadge(workflow.status)" />
          </div>
          <div class="workflow-card__footer">
            <span class="version-tag">v{{ workflow.version || 1 }}</span>
            <div class="workflow-card__actions" @click.stop>
              <button class="btn btn--ghost btn--sm" @click="router.push(`/workflows/${workflow.id}/edit`)">
                <Pencil :size="14" />
              </button>
            </div>
          </div>
        </GlassCard>
      </div>

      <!-- Pagination -->
      <PaginationComp
        :page="workflowStore.pagination.page"
        :total-pages="workflowStore.pagination.totalPages"
        :total-items="workflowStore.pagination.totalItems"
        :size="workflowStore.pagination.size"
        @update:page="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.workflows-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.workflows-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.workflows-page__title {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.workflows-page__subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 4px;
}

.workflows-page__filters {
  display: flex;
  gap: 12px;
}

.search-bar {
  max-width: 400px;
  width: 100%;
}

/* Table Styles */
.desktop-view {
  display: block;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--glass-border);
}

.data-table td {
  padding: 14px 16px;
  font-size: 0.875rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  vertical-align: middle;
}

.data-table__row {
  transition: all var(--transition-fast);
  cursor: pointer;
}

.data-table__row:hover {
  background: var(--glass-bg-hover);
}

.workflow-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: var(--text-primary);
}

.workflow-icon {
  color: var(--accent-cyan);
}

.version-tag {
  font-family: monospace;
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.text-right { text-align: right; }

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.action-btn--danger:hover {
  color: var(--accent-rose);
  background: rgba(251, 113, 133, 0.1);
}

/* Mobile View */
.mobile-view {
  display: none;
  flex-direction: column;
  gap: 1rem;
}

.workflow-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.workflow-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.workflow-card__name {
  font-size: 1rem;
  font-weight: 600;
}

.workflow-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

/* Empty State */
.empty-state {
  display: flex;
  justify-content: center;
  padding: 4rem 0;
}

.empty-state__card {
  text-align: center;
  padding: 3rem;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.empty-state__icon {
  color: var(--text-muted);
  opacity: 0.5;
}

.empty-state__card h3 {
  font-size: 1.25rem;
  font-weight: 600;
}

.empty-state__card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
}

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--sm {
  padding: 6px 12px;
}

@media (max-width: 768px) {
  .desktop-view { display: none; }
  .mobile-view { display: flex; }
  .workflows-page__header { flex-direction: column; align-items: stretch; }
}
</style>
