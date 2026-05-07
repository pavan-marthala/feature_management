<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkflowStore } from '@/stores/workflowStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Badge from '@/components/ui/Badge.vue'
import Modal from '@/components/ui/Modal.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import { 
  ArrowLeft, 
  GitBranch, 
  Layers,
  Plus,
  AlertCircle
} from 'lucide-vue-next'
import type { WorkflowStatus, Stage, StageRequest } from '@/types'

import PipelineVisualization from '@/components/pipeline/PipelineVisualization.vue'

const route = useRoute()
const router = useRouter()
const workflowStore = useWorkflowStore()
const environmentStore = useEnvironmentStore()
const workflowId = computed(() => route.params.workflowId as string)

// Stage Modal State
const showStageModal = ref(false)
const editingStage = ref<Stage | null>(null)
const submittingStage = ref(false)
const stageForm = ref<StageRequest>({
  environmentId: '',
  type: 'MANUAL',
  orderIndex: 0,
  scheduleExpression: ''
})

// Reorder State
const localStages = ref<Stage[]>([])
const isDirty = ref(false)
const savingOrder = ref(false)

onMounted(async () => {
  await environmentStore.fetchEnvironments(0, 100)
  await fetchWorkflowData()
})

async function fetchWorkflowData() {
  if (!workflowId.value) return
  const data = await workflowStore.fetchWorkflow(workflowId.value)
  if (data) {
    localStages.value = [...data.stages]
    isDirty.value = false
  }
}

const workflow = computed(() => workflowStore.selectedWorkflow)

function getStatusBadge(status?: WorkflowStatus) {
  if (!status) return { label: 'Unknown', variant: 'default' as const }
  switch (status) {
    case 'ACTIVE': return { label: 'Active', variant: 'success' as const }
    case 'DRAFT': return { label: 'Draft', variant: 'warning' as const }
    case 'ARCHIVED': return { label: 'Archived', variant: 'danger' as const }
    default: return { label: status, variant: 'default' as const }
  }
}

// Stage Actions
function openAddStage() {
  editingStage.value = null
  stageForm.value = {
    environmentId: '',
    type: 'MANUAL',
    orderIndex: localStages.value.length,
    scheduleExpression: ''
  }
  showStageModal.value = true
}

function openEditStage(stage: Stage) {
  editingStage.value = stage
  stageForm.value = {
    environmentId: stage.environmentId,
    type: stage.type,
    orderIndex: stage.orderIndex,
    scheduleExpression: stage.scheduleExpression || ''
  }
  showStageModal.value = true
}

async function handleStageSubmit() {
  if (!stageForm.value.environmentId || !workflowId.value) return
  submittingStage.value = true
  try {
    if (editingStage.value) {
      await workflowStore.updateStage(
        workflowId.value, 
        editingStage.value.id!, 
        stageForm.value, 
        editingStage.value.version || 0
      )
    } else {
      await workflowStore.addStage(workflowId.value, stageForm.value)
    }
    showStageModal.value = false
    await fetchWorkflowData()
  } finally {
    submittingStage.value = false
  }
}

async function deleteStage(stage: Stage) {
  if (!workflowId.value) return
  if (confirm('Are you sure you want to remove this stage?')) {
    await workflowStore.deleteStage(workflowId.value, stage.id!, stage.version || 0)
    await fetchWorkflowData()
  }
}

function handleReorder(newStages: Stage[]) {
  localStages.value = newStages
  isDirty.value = true
}

async function savePipelineOrder() {
  if (!workflowId.value) return
  savingOrder.value = true
  try {
    await workflowStore.reorderStages(workflowId.value, localStages.value)
    isDirty.value = false
  } finally {
    savingOrder.value = false
  }
}
</script>

<template>
  <div class="workflow-detail-page">
    <!-- Header -->
    <div class="header-section">
      <button class="back-btn" @click="router.push('/workflows')">
        <ArrowLeft :size="18" />
        Back to Workflows
      </button>
      
      <div v-if="workflow" class="header-content animate-fadeInUp">
        <div class="header-main">
          <div class="title-area">
            <div class="icon-box">
              <GitBranch :size="24" />
            </div>
            <div>
              <h1 class="page-title">{{ workflow.name }}</h1>
              <div class="metadata">
                <Badge v-bind="getStatusBadge(workflow.status)" />
                <span class="version">Version {{ workflow.version || 1 }}</span>
              </div>
            </div>
          </div>
          <div class="header-actions">
            <button 
              v-if="isDirty" 
              class="btn btn--primary animate-pulse" 
              :disabled="savingOrder"
              @click="savePipelineOrder"
            >
              <Loader2 v-if="savingOrder" :size="16" class="spin" />
              Save Pipeline Changes
            </button>
            <button class="btn btn--secondary" @click="openAddStage">
              <Plus :size="18" />
              Add Stage
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="workflowStore.loading && localStages.length === 0" class="loading-state">
      <LoadingSkeleton variant="card" :rows="3" />
    </div>

    <!-- Content -->
    <div v-else-if="workflow" class="content-grid">
      <!-- Stages Section -->
      <div class="stages-section animate-fadeInUp stagger-1">
        <div class="section-header">
          <div class="section-title-box">
            <Layers :size="20" />
            <h2 class="section-title">Pipeline Orchestration</h2>
          </div>
        </div>

        <GlassCard class="orchestration-card">
          <PipelineVisualization
            :stages="localStages"
            mode="BUILDER"
            @add="openAddStage"
            @edit="openEditStage"
            @delete="deleteStage"
            @reorder="handleReorder"
          />
        </GlassCard>
      </div>

      <!-- Sidebar / Info Section -->
      <div class="info-sidebar animate-fadeInUp stagger-2">
        <GlassCard>
          <h3 class="sidebar-title">Workflow Info</h3>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">ID</span>
              <span class="info-value code">{{ workflow.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Operational Status</span>
              <div class="status-indicator" :class="`status-indicator--${workflow.status?.toLowerCase()}`">
                {{ workflow.status }}
              </div>
            </div>
            <div class="info-item info-item--warning" v-if="workflow.status === 'DRAFT'">
              <AlertCircle :size="16" />
              <span>Draft workflows cannot propagate features to production.</span>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>

    <!-- Stage Modal -->
    <Modal
      :show="showStageModal"
      :title="editingStage ? 'Edit Stage' : 'Add Stage'"
      @close="showStageModal = false"
    >
      <form @submit.prevent="handleStageSubmit" class="stage-form">
        <div class="form-group">
          <label class="form-label">Environment *</label>
          <select v-model="stageForm.environmentId" class="form-input" required>
            <option value="" disabled>Select Environment</option>
            <option 
              v-for="env in environmentStore.environments" 
              :key="env.id" 
              :value="env.id"
            >
              {{ env.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Promotion Strategy *</label>
          <div class="strategy-selector">
            <button 
              type="button" 
              class="strategy-btn"
              :class="{ 'active': stageForm.type === 'MANUAL' }"
              @click="stageForm.type = 'MANUAL'"
            >
              Manual
            </button>
            <button 
              type="button" 
              class="strategy-btn"
              :class="{ 'active': stageForm.type === 'AUTOMATIC' }"
              @click="stageForm.type = 'AUTOMATIC'"
            >
              Automatic
            </button>
            <button 
              type="button" 
              class="strategy-btn"
              :class="{ 'active': stageForm.type === 'SCHEDULED' }"
              @click="stageForm.type = 'SCHEDULED'"
            >
              Scheduled
            </button>
          </div>
        </div>

        <div class="form-group" v-if="stageForm.type === 'SCHEDULED'">
          <label class="form-label">Schedule Expression (Cron)</label>
          <input 
            v-model="stageForm.scheduleExpression" 
            type="text" 
            class="form-input" 
            placeholder="0 0 * * *"
          />
          <p class="form-hint">Standard cron expression for automated promotion.</p>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn--ghost" @click="showStageModal = false">Cancel</button>
          <button type="submit" class="btn btn--primary" :disabled="submittingStage">
            {{ submittingStage ? 'Saving...' : (editingStage ? 'Update Stage' : 'Add Stage') }}
          </button>
        </div>
      </form>
    </Modal>
  </div>
</template>

<style scoped>
.workflow-detail-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.header-section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
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
  transition: color var(--transition-fast);
}

.back-btn:hover { color: var(--text-primary); }

.header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 16px rgba(34, 211, 238, 0.2);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.metadata {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.version {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-family: monospace;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-title-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
}

.orchestration-card {
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

/* Sidebar */
.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item--warning {
  flex-direction: row;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  background: rgba(251, 191, 36, 0.05);
  border-radius: var(--radius-md);
  color: var(--accent-amber);
  font-size: 0.8rem;
  line-height: 1.4;
}

.info-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 0.875rem;
  color: var(--text-primary);
}

.info-value.code {
  font-family: monospace;
  background: rgba(255, 255, 255, 0.05);
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  font-weight: 600;
}

.status-indicator::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator--active::before { background: var(--accent-emerald); box-shadow: 0 0 8px var(--accent-emerald); }
.status-indicator--draft::before { background: var(--accent-amber); box-shadow: 0 0 8px var(--accent-amber); }
.status-indicator--archived::before { background: var(--text-muted); }

/* Modal Form */
.stage-form {
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

.strategy-selector {
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.1);
  padding: 4px;
  border-radius: var(--radius-md);
}

.strategy-btn {
  flex: 1;
  padding: 8px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.strategy-btn.active {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 4px;
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
.btn--secondary { background: var(--glass-bg); color: var(--text-primary); border: 1px solid var(--glass-border); }
.btn--secondary:hover { background: var(--glass-bg-hover); }
.btn--sm { padding: 8px 16px; font-size: 0.8rem; }

@media (max-width: 1024px) {
  .content-grid { grid-template-columns: 1fr 260px; }
}

@media (max-width: 768px) {
  .content-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .header-main { flex-direction: column; align-items: stretch; }
  .header-actions { margin-top: 1rem; }
  .stage-item__actions { opacity: 1; }
  .stage-item__grip { opacity: 0.5; }
}
</style>
