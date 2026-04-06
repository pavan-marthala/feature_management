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
} from 'lucide-vue-next'
import type { Feature } from '@/types'

const route = useRoute()
const router = useRouter()
const featureStore = useFeatureStore()
const showDeleteModal = ref(false)
const showAddOwnerModal = ref(false)
const newOwnerName = ref('')
const copied = ref(false)

const featureId = computed(() => route.params.id as string)

onMounted(async () => {
  await featureStore.fetchFeature(featureId.value, 'ID')
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
          <button class="btn btn--ghost" @click="router.push(`/features/${feature.id}/edit`)">
            <Pencil :size="16" /> Edit
          </button>
          <button class="btn btn--danger" @click="showDeleteModal = true">
            <Trash2 :size="16" /> Delete
          </button>
        </div>
      </div>

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
</style>
