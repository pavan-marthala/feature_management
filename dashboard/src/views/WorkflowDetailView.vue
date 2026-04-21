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
  Pencil, 
  GitBranch, 
  Clock, 
  Layers,
  Settings,
  Plus,
  GripVertical,
  Trash2,
  AlertCircle
} from 'lucide-vue-next'
import type { WorkflowStatus, Stage, StageType, StageRequest } from '@/types'

const route = useRoute()
const router = useRouter()
const workflowStore = useWorkflowStore()
const environmentStore = useEnvironmentStore()
const workflowId = computed(() => route.params.id as string)

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

// Drag and Drop State
const draggingIndex = ref<number | null>(null)
const localStages = ref<Stage[]>([])

onMounted(async () => {
  await fetchWorkflowData()
  await environmentStore.fetchEnvironments(0, 100)
})

async function fetchWorkflowData() {
  const data = await workflowStore.fetchWorkflow(workflowId.value)
  if (data) {
    localStages.value = [...data.stages]
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

function getStageTypeBadge(type: string) {
  switch (type) {
    case 'MANUAL': return { label: 'Manual', variant: 'info' as const }
    case 'AUTOMATIC': return { label: 'Automatic', variant: 'success' as const }
    case 'SCHEDULED': return { label: 'Scheduled', variant: 'warning' as const }
    default: return { label: type, variant: 'default' as const }
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
  if (!stageForm.value.environmentId) return
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
  if (confirm('Are you sure you want to remove this stage?')) {
    await workflowStore.deleteStage(workflowId.value, stage.id!, stage.version || 0)
    await fetchWorkflowData()
  }
}

// Drag and Drop Implementation
function onDragStart(index: number) {
  draggingIndex.value = index
}

function onDragOver(event: DragEvent, index: number) {
  event.preventDefault()
  if (draggingIndex.value === null || draggingIndex.value === index) return
  
  // Reorder locally for visual feedback
  const items = [...localStages.value]
  const item = items.splice(draggingIndex.value, 1)[0]
  if (item) {
    items.splice(index, 0, item)
    localStages.value = items
    draggingIndex.value = index
  }
}

async function onDrop() {
  if (draggingIndex.value === null) return
  
  // Persistence
  await workflowStore.reorderStages(workflowId.value, localStages.value)
  draggingIndex.value = null
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
            <button class="btn btn--secondary" @click="router.push(`/workflows/${workflow.id}/edit`)">
              <Pencil :size="18" />
              Edit Metadata
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
            <h2 class="section-title">Pipeline Stages</h2>
          </div>
          <button class="btn btn--primary btn--sm" @click="openAddStage">
            <Plus :size="16" />
            Add Stage
          </button>
        </div>

        <div v-if="localStages.length === 0" class="empty-stages glass">
          <div class="empty-icon-wrapper">
            <Clock :size="48" class="empty-icon" />
          </div>
          <h3>No stages defined</h3>
          <p>This workflow doesn't have any propagation stages yet.</p>
          <button class="btn btn--secondary btn--sm mt-4" @click="openAddStage">
            Create First Stage
          </button>
        </div>

        <div v-else class="stages-timeline">
          <div 
            v-for="(stage, index) in localStages" 
            :key="stage.id" 
            class="stage-item-wrapper"
            :class="{ 'stage-item-wrapper--dragging': draggingIndex === index }"
            draggable="true"
            @dragstart="onDragStart(index)"
            @dragover="onDragOver($event, index)"
            @drop="onDrop"
          >
            <div class="stage-connector" v-if="index < localStages.length - 1"></div>
            <GlassCard class="stage-item" hover>
              <div class="stage-item__grip">
                <GripVertical :size="20" />
              </div>
              <div class="stage-item__index">{{ index + 1 }}</div>
              <div class="stage-item__content">
                <div class="stage-item__header">
                  <h3 class="stage-env-name">{{ stage.environmentName || 'Unknown Environment' }}</h3>
                  <Badge v-bind="getStageTypeBadge(stage.type)" />
                </div>
                <div class="stage-item__details">
                  <div class="detail-pill" v-if="stage.scheduleExpression">
                    <Clock :size="14" />
                    {{ stage.scheduleExpression }}
                  </div>
                  <div class="detail-pill" v-if="stage.approvalNeeded">
                    <Settings :size="14" />
                    Approval Required
                  </div>
                </div>
              </div>
              <div class="stage-item__actions">
                <button class="action-btn" @click="openEditStage(stage)" title="Edit Stage">
                  <Pencil :size="16" />
                </button>
                <button class="action-btn action-btn--danger" @click="deleteStage(stage)" title="Remove Stage">
                  <Trash2 :size="16" />
                </button>
              </div>
            </GlassCard>
          </div>
        </div>
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

/* Stages Timeline */
.stages-timeline {
  display: flex;
  flex-direction: column;
}

.stage-item-wrapper {
  position: relative;
  padding-bottom: 2rem;
  transition: all var(--transition-normal);
}

.stage-item-wrapper--dragging {
  opacity: 0.5;
  transform: scale(0.98);
}

.stage-connector {
  position: absolute;
  left: 64px;
  top: 60px;
  bottom: 0;
  width: 2px;
  background: var(--glass-border);
  z-index: 0;
  opacity: 0.5;
}

.stage-item {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
}

.stage-item__grip {
  color: var(--text-muted);
  cursor: grab;
  padding: 4px;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.stage-item:hover .stage-item__grip {
  opacity: 0.5;
}

.stage-item__index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  border: 2px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.stage-item__content {
  flex: 1;
}

.stage-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stage-env-name {
  font-size: 1.125rem;
  font-weight: 600;
}

.stage-item__details {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.stage-item__actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.stage-item:hover .stage-item__actions {
  opacity: 1;
}

.action-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.action-btn--danger:hover {
  color: var(--accent-rose);
  background: rgba(251, 113, 133, 0.1);
}

.empty-stages {
  padding: 4rem 2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.empty-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.5;
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
  .content-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .header-main { flex-direction: column; align-items: stretch; }
  .header-actions { margin-top: 1rem; }
  .stage-item__actions { opacity: 1; }
  .stage-item__grip { opacity: 0.5; }
}
</style>
