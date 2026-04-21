<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFeatureStore } from '@/stores/featureStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Badge from '@/components/ui/Badge.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import Modal from '@/components/ui/Modal.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import {
  ArrowLeft,
  Pencil,
  Trash2,
  Shield,
  Clock,
  Globe,
  ToggleLeft,
  User,
  Copy,
  Check,
  History,
  GitBranch,
  X,
  Zap,
  ArrowRight,
} from 'lucide-vue-next'
import type { Feature } from '@/types'
import PipelineBoard from '@/components/features/PipelineBoard.vue'
import SideDrawer from '@/components/ui/SideDrawer.vue'
import { useWorkflowStore } from '@/stores/workflowStore'

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  }).format(date)
}

const route = useRoute()
const router = useRouter()
const featureStore = useFeatureStore()
const workflowStore = useWorkflowStore()
const showDeleteModal = ref(false)
const showAddOwnerModal = ref(false)
const showHistoryDrawer = ref(false)
const newOwnerName = ref('')
const copied = ref(false)

const featureId = computed(() => route.params.id as string)

onMounted(async () => {
  await featureStore.fetchFeature(featureId.value, 'ID')
  await featureStore.fetchPropagationHistory(featureId.value)
  await workflowStore.fetchWorkflows()
})

const feature = computed(() => featureStore.selectedFeature)

function getStrategyInfo(strategy?: string) {
  switch (strategy) {
    case 'BooleanFeatureStrategy':
      return { label: 'Boolean Toggle', variant: 'cyan' as const, icon: ToggleLeft, description: 'Simple on/off toggle' }
    case 'JWTClaimFeatureStrategy':
      return { label: 'JWT Claim', variant: 'indigo' as const, icon: Shield, description: 'Based on JWT token claims' }
    case 'HTTPRequestFeatureStrategy':
      return { label: 'HTTP Request', variant: 'warning' as const, icon: Globe, description: 'Based on HTTP request properties' }
    case 'ScheduleFeatureStrategy':
      return { label: 'Schedule', variant: 'success' as const, icon: Clock, description: 'Time-based feature activation' }
    default:
      return { label: 'Unknown', variant: 'default' as const, icon: ToggleLeft, description: 'Unknown strategy' }
  }
}

async function handleDelete() {
  if (!feature.value) return
  try {
    await featureStore.deleteFeature(feature.value.id, feature.value.etag)
    router.push('/features')
  } catch {
    // Toast already shown
  }
  showDeleteModal.value = false
}

async function handleAddOwner() {
  if (!feature.value || !newOwnerName.value.trim()) return
  await featureStore.addOwnerToFeature(feature.value.id, newOwnerName.value.trim())
  newOwnerName.value = ''
  showAddOwnerModal.value = false
}

async function handleRemoveOwner(ownerName: string) {
  if (!feature.value) return
  await featureStore.removeOwnerFromFeature(feature.value.id, ownerName, feature.value.etag)
}

async function handleToggle(f: Feature) {
  await featureStore.toggleFeature(f)
  // Re-fetch to update etag
  await featureStore.fetchFeature(featureId.value, 'ID')
}

function copyId() {
  if (feature.value) {
    navigator.clipboard.writeText(feature.value.id)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
}

async function handlePromote() {
  if (!feature.value) return
  try {
    // For now, we use a default workflow if available
    const workflowId = workflowStore.workflows[0]?.id
    await featureStore.propagateFeature(feature.value.id, { workflowId })
  } catch (err) {
    console.error('Promotion failed', err)
  }
}

const currentWorkflow = computed(() => {
  // Mocking: finding a workflow that might fit. In a real app, this would be linked to the feature or environment.
  return workflowStore.selectedWorkflow || workflowStore.workflows[0]
})

async function openHistory() {
  await featureStore.fetchPropagationHistory(featureId.value)
  showHistoryDrawer.value = true
}
</script>

<template>
  <div class="detail-page">
    <!-- Back -->
    <button class="back-btn" @click="router.push('/features')">
      <ArrowLeft :size="18" />
      Back to Features
    </button>

    <LoadingSkeleton v-if="featureStore.loading && !feature" variant="card" />

    <template v-else-if="feature">
      <!-- Header -->
      <div class="detail-header animate-fadeInUp">
        <div class="detail-header__info">
          <div class="detail-header__name-row">
            <h1 class="detail-header__name">{{ feature.name }}</h1>
            <Badge v-bind="getStrategyInfo(feature.configuration?.strategy)" />
            <Badge
              :label="feature.enabled ? 'Enabled' : 'Disabled'"
              :variant="feature.enabled ? 'success' : 'danger'"
            />
          </div>
          <p class="detail-header__desc">{{ feature.description || 'No description provided' }}</p>
        </div>
        <div class="detail-header__actions">
          <ToggleSwitch
            :model-value="feature.enabled"
            size="lg"
            @update:model-value="handleToggle(feature)"
          />
          <button class="btn btn--ghost" @click="openHistory">
            <History :size="16" /> History
          </button>
          <button class="btn btn--ghost" @click="router.push(`/features/${feature.id}/edit`)">
            <Pencil :size="16" /> Edit
          </button>
          <button class="btn btn--danger" @click="showDeleteModal = true">
            <Trash2 :size="16" /> Delete
          </button>
        </div>
      </div>

      <!-- Pipeline Visualization -->
      <GlassCard class="animate-fadeInUp stagger-1 pipeline-section">
        <div class="pipeline-header">
          <h2 class="card-title">
            <GitBranch :size="18" />
            Propagation Pipeline
          </h2>
          <span v-if="currentWorkflow" class="workflow-badge">
            Using: {{ currentWorkflow.name }}
          </span>
        </div>
        
        <PipelineBoard
          v-if="currentWorkflow && 'stages' in currentWorkflow"
          :stages="(currentWorkflow as any).stages"
          :current-environment-id="feature.envId"
          :loading="featureStore.loading"
          @promote="handlePromote"
        />
        <div v-else class="no-workflow glass">
          <p>No propagation workflow assigned to this feature.</p>
          <button class="btn btn--primary btn--sm" @click="router.push('/workflows')">Configure Workflows</button>
        </div>
      </GlassCard>

      <!-- Content Grid -->
      <div class="detail-grid">
        <!-- Configuration Card -->
        <GlassCard hover class="animate-fadeInUp stagger-1">
          <h2 class="card-title">Configuration</h2>
          <div class="config-section">
            <div class="config-strategy">
              <component :is="getStrategyInfo(feature.configuration?.strategy).icon" :size="24" class="config-strategy__icon" />
              <div>
                <h3 class="config-strategy__name">{{ getStrategyInfo(feature.configuration?.strategy).label }}</h3>
                <p class="config-strategy__desc">{{ getStrategyInfo(feature.configuration?.strategy).description }}</p>
              </div>
            </div>

            <!-- Boolean Strategy -->
            <div v-if="feature.configuration?.strategy === 'BooleanFeatureStrategy'" class="config-detail">
              <div class="config-row">
                <span class="config-label">Value</span>
                <Badge
                  :label="(feature.configuration as any).value ? 'True' : 'False'"
                  :variant="(feature.configuration as any).value ? 'success' : 'danger'"
                />
              </div>
            </div>

            <!-- JWT Claim Strategy -->
            <div v-else-if="feature.configuration?.strategy === 'JWTClaimFeatureStrategy'" class="config-detail">
              <div class="config-row">
                <span class="config-label">Claims</span>
                <pre class="config-json">{{ JSON.stringify((feature.configuration as any).claims, null, 2) }}</pre>
              </div>
            </div>

            <!-- HTTP Request Strategy -->
            <div v-else-if="feature.configuration?.strategy === 'HTTPRequestFeatureStrategy'" class="config-detail">
              <div v-if="(feature.configuration as any).header" class="config-row">
                <span class="config-label">Header</span>
                <div class="config-kv">
                  <span class="config-kv__key">{{ (feature.configuration as any).header.name }}</span>
                  <span class="config-kv__sep">:</span>
                  <span class="config-kv__val">{{ (feature.configuration as any).header.value }}</span>
                </div>
              </div>
              <div v-if="(feature.configuration as any).requestBody" class="config-row">
                <span class="config-label">Request Body</span>
                <div class="config-kv">
                  <span class="config-kv__key">{{ (feature.configuration as any).requestBody.path }}</span>
                  <span class="config-kv__sep">=</span>
                  <span class="config-kv__val">{{ (feature.configuration as any).requestBody.value }}</span>
                </div>
              </div>
              <div v-if="(feature.configuration as any).query" class="config-row">
                <span class="config-label">Query</span>
                <div class="config-kv">
                  <span class="config-kv__key">{{ (feature.configuration as any).query.name }}</span>
                  <span class="config-kv__sep">=</span>
                  <span class="config-kv__val">{{ (feature.configuration as any).query.value }}</span>
                </div>
              </div>
            </div>

            <!-- Schedule Strategy -->
            <div v-else-if="feature.configuration?.strategy === 'ScheduleFeatureStrategy'" class="config-detail">
              <div class="config-row">
                <span class="config-label">Cron Expression</span>
                <code class="config-code">{{ (feature.configuration as any).cron }}</code>
              </div>
            </div>
          </div>
        </GlassCard>

        <!-- Metadata Card -->
        <GlassCard hover class="animate-fadeInUp stagger-2">
          <h2 class="card-title">Metadata</h2>
          <div class="metadata-list">
            <div class="metadata-item">
              <span class="metadata-label">Feature ID</span>
              <div class="metadata-value metadata-value--copyable" @click="copyId">
                <code>{{ feature.id }}</code>
                <Check v-if="copied" :size="14" class="copy-icon copy-icon--success" />
                <Copy v-else :size="14" class="copy-icon" />
              </div>
            </div>
            <div class="metadata-item">
              <span class="metadata-label">Version (ETag)</span>
              <span class="metadata-value">{{ feature.etag }}</span>
            </div>
            <div class="metadata-item">
              <span class="metadata-label">Strategy</span>
              <span class="metadata-value">{{ feature.configuration?.strategy || 'None' }}</span>
            </div>
          </div>
        </GlassCard>

        <!-- Owners Card -->
        <GlassCard hover class="animate-fadeInUp stagger-3">
          <div class="owners-header">
            <h2 class="card-title">
              <User :size="18" />
              Owners
            </h2>
            <button class="btn btn--primary btn--sm" @click="showAddOwnerModal = true">
              Add Owner
            </button>
          </div>
          <div v-if="feature.owners?.length" class="owners-list">
            <div
              v-for="owner in feature.owners"
              :key="owner"
              class="owner-chip"
            >
              <div class="owner-avatar">{{ owner.charAt(0).toUpperCase() }}</div>
              <span>{{ owner }}</span>
              <button class="owner-remove-btn" @click="handleRemoveOwner(owner)">
                <Trash2 :size="12" />
              </button>
            </div>
          </div>
          <p v-else class="no-data">No owners assigned</p>
        </GlassCard>

        <!-- Raw JSON Card -->
        <GlassCard class="animate-fadeInUp stagger-4 detail-grid__full">
          <h2 class="card-title">Raw Configuration (JSON)</h2>
          <pre class="config-json config-json--full">{{ JSON.stringify(feature, null, 2) }}</pre>
        </GlassCard>
      </div>
    </template>

    <!-- Delete Modal -->
    <Modal :show="showDeleteModal" title="Delete Feature" size="sm" @close="showDeleteModal = false">
      <p style="color: var(--text-secondary); font-size: 0.9rem;">
        Are you sure you want to delete <strong>{{ feature?.name }}</strong>? This action cannot be undone.
      </p>
      <template #footer>
        <button class="btn btn--ghost" @click="showDeleteModal = false">Cancel</button>
        <button class="btn btn--danger" @click="handleDelete">Delete</button>
      </template>
    </Modal>

    <Modal :show="showAddOwnerModal" title="Add Owner" size="sm" @close="showAddOwnerModal = false">
      <div class="form-group">
        <label class="form-label">Owner Name or ID</label>
        <input
          v-model="newOwnerName"
          type="text"
          class="form-input"
          placeholder="e.g. john.doe"
          @keyup.enter="handleAddOwner"
        />
      </div>
      <template #footer>
        <button class="btn btn--ghost" @click="showAddOwnerModal = false">Cancel</button>
        <button class="btn btn--primary" @click="handleAddOwner" :disabled="!newOwnerName.trim()">Add</button>
      </template>
    </Modal>

    <!-- History Drawer -->
    <SideDrawer :show="showHistoryDrawer" title="Propagation History" @close="showHistoryDrawer = false">
      <div v-if="featureStore.propagationHistory.length === 0" class="history-empty">
        <div class="empty-state-icon">
          <Clock :size="48" />
        </div>
        <p>No promotion history found for this feature.</p>
      </div>
      <div v-else class="premium-timeline">
        <div v-for="log in featureStore.propagationHistory" :key="log.id" class="timeline-event">
          <div class="timeline-event__sidebar">
            <div class="timeline-event__dot" :class="`timeline-event__dot--${log.status.toLowerCase()}`">
              <Check v-if="log.status === 'SUCCESS'" :size="12" />
              <Clock v-else-if="log.status === 'PENDING'" :size="12" />
              <X v-else :size="12" />
            </div>
            <div class="timeline-event__line"></div>
          </div>
          
          <div class="timeline-event__card">
            <div class="event-header">
              <div class="event-user">
                <div class="user-avatar">{{ log.promotedBy.charAt(0).toUpperCase() }}</div>
                <span class="user-name">{{ log.promotedBy }}</span>
              </div>
              <span class="event-time">{{ formatDate(log.createdAt) }}</span>
            </div>
            
            <div class="event-details">
              <div class="propagation-path">
                <div class="env-node">
                  <Globe :size="12" />
                  {{ log.sourceEnvironmentId.split('-')[0] }}
                </div>
                <ArrowRight :size="14" class="path-arrow" />
                <div class="env-node env-node--target">
                  <Zap :size="12" />
                  {{ log.targetEnvironmentId.split('-')[0] }}
                </div>
              </div>
              
              <Badge 
                :label="log.status" 
                :variant="log.status === 'SUCCESS' ? 'success' : log.status === 'PENDING' ? 'warning' : 'danger'" 
                size="sm" 
              />
            </div>
          </div>
        </div>
      </div>
    </SideDrawer>
  </div>
</template>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-family: inherit;
  cursor: pointer;
  padding: 6px 0;
  transition: color var(--transition-fast);
  width: fit-content;
}

.back-btn:hover {
  color: var(--accent-cyan);
}

/* Header */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.detail-header__name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-header__name {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.detail-header__desc {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 6px;
}

.detail-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Grid */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 1rem;
}

.detail-grid__full {
  grid-column: 1 / -1;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Config */
.config-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.config-strategy {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--glass-bg);
  border-radius: var(--radius-md);
}

.config-strategy__icon {
  color: var(--accent-cyan);
  flex-shrink: 0;
}

.config-strategy__name {
  font-size: 0.95rem;
  font-weight: 600;
}

.config-strategy__desc {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-top: 2px;
}

.config-detail {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.config-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.config-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.config-json {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  padding: 12px;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  color: var(--text-primary);
  overflow-x: auto;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.config-json--full {
  max-height: 300px;
  overflow-y: auto;
}

.config-code {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  color: var(--text-primary);
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.config-kv {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.config-kv__key { color: var(--accent-cyan); }
.config-kv__sep { color: var(--text-muted); }
.config-kv__val { color: var(--accent-emerald); }

/* Metadata */
.metadata-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.metadata-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metadata-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metadata-value {
  font-size: 0.875rem;
  color: var(--text-primary);
}

.metadata-value code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.8rem;
}

.metadata-value--copyable {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.metadata-value--copyable:hover {
  color: var(--accent-cyan);
}

.copy-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.copy-icon--success {
  color: var(--accent-emerald);
}

/* Owners */
.owners-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.owner-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  font-size: 0.8rem;
}

.owner-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.65rem;
  font-weight: 600;
  color: white;
}

.owner-remove-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-left: 2px;
}

.owner-remove-btn:hover {
  background: rgba(251, 113, 133, 0.15);
  color: var(--accent-rose);
}

.owners-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.owners-header .card-title {
  margin-bottom: 0;
}

.no-data {
  color: var(--text-muted);
  font-size: 0.85rem;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
  white-space: nowrap;
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

.btn--danger {
  background: rgba(251, 113, 133, 0.15);
  color: var(--accent-rose);
  border: 1px solid rgba(251, 113, 133, 0.2);
}

.btn--danger:hover {
  background: rgba(251, 113, 133, 0.25);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.btn--primary:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
  transform: translateY(-1px);
}

.btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn--sm {
  padding: 4px 10px;
  font-size: 0.75rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.form-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-input {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.95rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.form-input:focus {
  outline: none;
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 2px rgba(6, 182, 212, 0.15);
}

@media (max-width: 640px) {
  .detail-header {
    flex-direction: column;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

/* Pipeline Section */
.pipeline-section {
  margin-bottom: 1.5rem;
}

.pipeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.pipeline-header .card-title {
  margin-bottom: 0;
}

.workflow-badge {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.05);
  padding: 4px 10px;
  border-radius: var(--radius-full);
}

.no-workflow {
  padding: 2rem;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--text-secondary);
}

/* Premium Timeline Styles */
.premium-timeline {
  display: flex;
  flex-direction: column;
  padding: 1rem 0.5rem;
}

.timeline-event {
  display: flex;
  gap: 1.25rem;
}

.timeline-event:last-child .timeline-event__line {
  display: none;
}

.timeline-event__sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
}

.timeline-event__dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  border: 2px solid var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.timeline-event__dot--success { background: var(--accent-emerald); color: white; }
.timeline-event__dot--pending { background: var(--accent-cyan); color: white; }
.timeline-event__dot--failed { background: var(--accent-rose); color: white; }

.timeline-event__line {
  flex: 1;
  width: 2px;
  background: var(--glass-border);
  margin: 4px 0;
}

.timeline-event__card {
  flex: 1;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 1rem;
  margin-bottom: 1.5rem;
  transition: all 0.2s;
}

.timeline-event__card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--accent-cyan);
  transform: translateX(2px);
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.event-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--gradient-accent);
  color: white;
  font-size: 0.6rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
}

.event-time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.event-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.propagation-path {
  display: flex;
  align-items: center;
  gap: 10px;
}

.env-node {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.2);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  text-transform: uppercase;
}

.env-node--target {
  color: var(--accent-cyan);
  background: rgba(34, 211, 238, 0.05);
}

.path-arrow {
  color: var(--text-muted);
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--text-muted);
}

.empty-state-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  color: rgba(255, 255, 255, 0.1);
}

.history-empty p {
  font-size: 0.95rem;
  max-width: 200px;
}
</style>
