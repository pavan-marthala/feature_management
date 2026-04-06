<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useEnvironmentStore } from '@/stores/environmentStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import Modal from '@/components/ui/Modal.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import PaginationComp from '@/components/ui/Pagination.vue'
import SearchInput from '@/components/ui/SearchInput.vue'
import {
  Plus,
  Layers,
  Pencil,
  Trash2,
  Eye,
  Users,
} from 'lucide-vue-next'
import type { Environment } from '@/types'

const envStore = useEnvironmentStore()
const router = useRouter()

const searchQuery = ref('')
const showDeleteModal = ref(false)
const envToDelete = ref<Environment | null>(null)

onMounted(async () => {
  if (envStore.environments.length === 0 && !envStore.loading) {
    await envStore.fetchEnvironments(0, 25)
  }
})

function filteredEnvs() {
  if (!searchQuery.value) return envStore.environments
  const q = searchQuery.value.toLowerCase()
  return envStore.environments.filter(
    (e) => e.name.toLowerCase().includes(q) || e.description?.toLowerCase().includes(q)
  )
}

function confirmDelete(env: Environment) {
  envToDelete.value = env
  showDeleteModal.value = true
}

async function handleDelete() {
  if (!envToDelete.value) return
  try {
    await envStore.deleteEnvironment(envToDelete.value.id, envToDelete.value.etag)
  } catch {
    // Toast already shown
  }
  showDeleteModal.value = false
  envToDelete.value = null
}

function onPageChange(page: number) {
  envStore.fetchEnvironments(page, envStore.pagination.size)
}
</script>

<template>
  <div class="envs-page">
    <div class="envs-page__header animate-fadeInUp">
      <div>
        <h1 class="envs-page__title">Environments</h1>
        <p class="envs-page__subtitle">Manage deployment environments for your features</p>
      </div>
      <button class="btn btn--primary" @click="router.push('/environments/create')" id="create-env-btn">
        <Plus :size="18" />
        Create Environment
      </button>
    </div>

    <div class="envs-page__filters animate-fadeInUp stagger-1">
      <SearchInput v-model="searchQuery" placeholder="Search environments..." />
    </div>

    <LoadingSkeleton v-if="envStore.loading && envStore.environments.length === 0" variant="card" />

    <div v-else-if="filteredEnvs().length === 0" class="empty-state animate-fadeInUp">
      <div class="empty-state__card glass">
        <Layers :size="56" class="empty-state__icon" />
        <h3>{{ searchQuery ? 'No matching environments' : 'No environments yet' }}</h3>
        <p>{{ searchQuery ? 'Try adjusting your search.' : 'Create environments like Development, Staging, Production.' }}</p>
        <button v-if="!searchQuery" class="btn btn--primary" @click="router.push('/environments/create')">
          <Plus :size="18" /> Create Environment
        </button>
      </div>
    </div>

    <div v-else class="envs-grid animate-fadeInUp stagger-2">
      <GlassCard
        v-for="env in filteredEnvs()"
        :key="env.id"
        hover
        gradient
        class="env-card"
        @click="router.push(`/environments/${env.id}`)"
      >
        <div class="env-card__header">
          <div class="env-card__icon">
            <Layers :size="20" />
          </div>
          <div class="env-card__actions">
            <button class="action-btn" title="Edit" @click.stop="router.push(`/environments/${env.id}/edit`)">
              <Pencil :size="16" />
            </button>
            <button class="action-btn action-btn--danger" title="Delete" @click.stop="confirmDelete(env)">
              <Trash2 :size="16" />
            </button>
          </div>
        </div>
        <h3 class="env-card__name">{{ env.name }}</h3>
        <p class="env-card__desc">{{ env.description || 'No description' }}</p>
        <div v-if="env.owners?.length" class="env-card__footer">
          <Users :size="14" />
          <span>{{ env.owners.length }} owner(s)</span>
        </div>
      </GlassCard>
    </div>

    <PaginationComp
      :page="envStore.pagination.page"
      :total-pages="envStore.pagination.totalPages"
      :total-items="envStore.pagination.totalItems"
      :size="envStore.pagination.size"
      @update:page="onPageChange"
    />

    <!-- Delete Modal -->
    <Modal :show="showDeleteModal" title="Delete Environment" size="sm" @close="showDeleteModal = false">
      <p style="color: var(--text-secondary); font-size: 0.9rem;">
        Are you sure you want to delete <strong>{{ envToDelete?.name }}</strong>? This cannot be undone.
      </p>
      <template #footer>
        <button class="btn btn--ghost" @click="showDeleteModal = false">Cancel</button>
        <button class="btn btn--danger" @click="handleDelete">Delete</button>
      </template>
    </Modal>
  </div>
</template>

<style scoped>
.envs-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.envs-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.envs-page__title {
  font-size: 1.5rem;
  font-weight: 700;
}

.envs-page__subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 4px;
}

.envs-page__filters {
  max-width: 400px;
}

/* Grid */
.envs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.env-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;
}

.env-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.env-card__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.env-card__actions {
  display: flex;
  gap: 4px;
}

.env-card__name {
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.env-card__name:hover {
  color: var(--accent-cyan);
}

.env-card__desc {
  color: var(--text-muted);
  font-size: 0.8rem;
  flex: 1;
}

.env-card__footer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--glass-border);
}

/* Actions */
.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
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

/* Empty */
.empty-state {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}

.empty-state__card {
  text-align: center;
  padding: 3rem;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.empty-state__icon { color: var(--text-muted); }
.empty-state__card h3 { font-size: 1.1rem; font-weight: 600; }
.empty-state__card p { color: var(--text-muted); font-size: 0.85rem; }

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
</style>
