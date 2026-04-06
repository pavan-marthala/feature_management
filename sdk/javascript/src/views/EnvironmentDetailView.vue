<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useEnvironmentStore } from '@/stores/environmentStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Modal from '@/components/ui/Modal.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import {
  ArrowLeft,
  Pencil,
  Trash2,
  Layers,
  Copy,
  Check,
  User,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const envStore = useEnvironmentStore()
const showDeleteModal = ref(false)
const showAddOwnerModal = ref(false)
const newOwnerName = ref('')
const copied = ref(false)

const envId = computed(() => route.params.id as string)

onMounted(async () => {
  await envStore.fetchEnvironment(envId.value)
})

const env = computed(() => envStore.selectedEnvironment)

async function handleDelete() {
  if (!env.value) return
  try {
    await envStore.deleteEnvironment(env.value.id, env.value.etag)
    router.push('/environments')
  } catch {
    // Toast already shown
  }
  showDeleteModal.value = false
}

async function handleAddOwner() {
  if (!env.value || !newOwnerName.value.trim()) return
  await envStore.addOwnerToEnvironment(env.value.id, newOwnerName.value.trim())
  newOwnerName.value = ''
  showAddOwnerModal.value = false
}

async function handleRemoveOwner(ownerName: string) {
  if (!env.value) return
  await envStore.removeOwnerFromEnvironment(env.value.id, ownerName, env.value.etag)
}

function copyId() {
  if (env.value) {
    navigator.clipboard.writeText(env.value.id)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
}
</script>

<template>
  <div class="detail-page">
    <button class="back-btn" @click="router.push('/environments')">
      <ArrowLeft :size="18" /> Back to Environments
    </button>

    <LoadingSkeleton v-if="envStore.loading && !env" variant="card" />

    <template v-else-if="env">
      <div class="detail-header animate-fadeInUp">
        <div class="detail-header__info">
          <div class="detail-header__name-row">
            <div class="detail-header__icon">
              <Layers :size="24" />
            </div>
            <h1 class="detail-header__name">{{ env.name }}</h1>
          </div>
          <p class="detail-header__desc">{{ env.description || 'No description provided' }}</p>
        </div>
        <div class="detail-header__actions">
          <button class="btn btn--ghost" @click="router.push(`/environments/${env.id}/edit`)">
            <Pencil :size="16" /> Edit
          </button>
          <button class="btn btn--danger" @click="showDeleteModal = true">
            <Trash2 :size="16" /> Delete
          </button>
        </div>
      </div>

      <div class="detail-grid">
        <GlassCard hover class="animate-fadeInUp stagger-1">
          <h2 class="card-title">Metadata</h2>
          <div class="metadata-list">
            <div class="metadata-item">
              <span class="metadata-label">Environment ID</span>
              <div class="metadata-value metadata-value--copyable" @click="copyId">
                <code>{{ env.id }}</code>
                <Check v-if="copied" :size="14" class="copy-icon copy-icon--success" />
                <Copy v-else :size="14" class="copy-icon" />
              </div>
            </div>
            <div class="metadata-item">
              <span class="metadata-label">Name</span>
              <span class="metadata-value">{{ env.name }}</span>
            </div>
            <div class="metadata-item">
              <span class="metadata-label">Version (ETag)</span>
              <span class="metadata-value">{{ env.etag }}</span>
            </div>
          </div>
        </GlassCard>

        <GlassCard hover class="animate-fadeInUp stagger-2">
          <div class="owners-header">
            <h2 class="card-title">
              <User :size="18" /> Owners
            </h2>
            <button class="btn btn--primary btn--sm" @click="showAddOwnerModal = true">
              Add Owner
            </button>
          </div>
          <div v-if="env.owners?.length" class="owners-list">
            <div v-for="owner in env.owners" :key="owner" class="owner-chip">
              <div class="owner-avatar">{{ owner.charAt(0).toUpperCase() }}</div>
              <span>{{ owner }}</span>
              <button class="owner-remove-btn" @click="handleRemoveOwner(owner)">
                <Trash2 :size="12" />
              </button>
            </div>
          </div>
          <p v-else class="no-data">No owners assigned</p>
        </GlassCard>
      </div>
    </template>

    <Modal :show="showDeleteModal" title="Delete Environment" size="sm" @close="showDeleteModal = false">
      <p style="color: var(--text-secondary); font-size: 0.9rem;">
        Are you sure you want to delete <strong>{{ env?.name }}</strong>? This cannot be undone.
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

.back-btn:hover { color: var(--accent-cyan); }

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
  gap: 12px;
}

.detail-header__icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.detail-header__name {
  font-size: 1.5rem;
  font-weight: 700;
}

.detail-header__desc {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 6px;
}

.detail-header__actions {
  display: flex;
  gap: 10px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 1rem;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

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
}

.metadata-value--copyable:hover {
  color: var(--accent-cyan);
}

.copy-icon { color: var(--text-muted); }
.copy-icon--success { color: var(--accent-emerald); }

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
</style>
