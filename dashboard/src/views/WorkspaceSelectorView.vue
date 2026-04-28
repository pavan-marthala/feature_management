<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import SearchInput from '@/components/ui/SearchInput.vue'
import type { WorkspaceSummary } from '@/types'
import { workspaceService } from '@/services/workspaceService'
import {
  FolderKanban,
  Plus,
  Flag,
  GitBranch,
  Layers,
  ArrowRight,
  Zap,
} from 'lucide-vue-next'

const workspaceStore = useWorkspaceStore()
const router = useRouter()
const searchQuery = ref('')
const summaries = ref<Record<string, WorkspaceSummary>>({})

const filteredWorkspaces = computed(() => {
  if (!searchQuery.value) return workspaceStore.workspaces
  const q = searchQuery.value.toLowerCase()
  return workspaceStore.workspaces.filter(
    (w) => w.name.toLowerCase().includes(q) || w.description?.toLowerCase().includes(q)
  )
})

onMounted(async () => {
  await workspaceStore.fetchWorkspaces()
  // Fetch summaries for each workspace
  for (const ws of workspaceStore.workspaces) {
    try {
      summaries.value[ws.id] = await workspaceService.getWorkspaceSummary(ws.id)
    } catch {
      // silently skip
    }
  }
})

function selectWorkspace(workspace: typeof workspaceStore.workspaces[0]) {
  workspaceStore.selectWorkspace(workspace)
  router.push(`/workspaces/${workspace.id}`)
}
</script>

<template>
  <div class="ws-selector">
    <!-- Hero -->
    <div class="ws-selector__hero animate-fadeInUp">
      <div class="ws-selector__hero-content">
        <h1 class="ws-selector__title">
          <Zap :size="28" class="ws-selector__title-icon" />
          Workspaces
        </h1>
        <p class="ws-selector__subtitle">
          Select a workspace to manage its features, workflows, and promotion pipelines.
        </p>
      </div>
      <div class="ws-selector__hero-actions">
        <button class="btn btn--primary" @click="router.push('/workspaces/create')" id="create-workspace-btn">
          <Plus :size="18" />
          New Workspace
        </button>
      </div>
    </div>

    <!-- Search -->
    <div class="ws-selector__search animate-fadeInUp stagger-1">
      <SearchInput v-model="searchQuery" placeholder="Search workspaces..." />
    </div>

    <!-- Loading -->
    <div v-if="workspaceStore.loading" class="ws-selector__grid">
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-2" />
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-3" />
      <LoadingSkeleton variant="card" :rows="2" class="animate-fadeInUp stagger-4" />
    </div>

    <!-- Empty state -->
    <div v-else-if="workspaceStore.workspaces.length === 0" class="ws-selector__empty animate-fadeInUp stagger-2">
      <GlassCard hover>
        <div class="empty-state">
          <FolderKanban :size="48" class="empty-state__icon" />
          <h3>No workspaces yet</h3>
          <p>Create your first workspace to start managing features.</p>
          <button class="btn btn--primary" @click="router.push('/workspaces/create')">
            <Plus :size="18" /> Create Workspace
          </button>
        </div>
      </GlassCard>
    </div>

    <!-- Workspace Grid -->
    <div v-else class="ws-selector__grid">
      <GlassCard
        v-for="(ws, idx) in filteredWorkspaces"
        :key="ws.id"
        hover
        gradient
        class="ws-card animate-fadeInUp"
        :class="`stagger-${Math.min(idx + 2, 6)}`"
        @click="selectWorkspace(ws)"
      >
        <div class="ws-card__header">
          <div class="ws-card__icon">
            <FolderKanban :size="20" />
          </div>
          <ArrowRight :size="16" class="ws-card__arrow" />
        </div>
        <h3 class="ws-card__name">{{ ws.name }}</h3>
        <p class="ws-card__desc">{{ ws.description || 'No description' }}</p>
        <div v-if="summaries[ws.id]" class="ws-card__stats">
          <div class="ws-card__stat">
            <Flag :size="14" />
            <span>{{ summaries[ws.id]?.featureCount ?? 0 }} features</span>
          </div>
          <div class="ws-card__stat">
            <GitBranch :size="14" />
            <span>{{ summaries[ws.id]?.workflowStages ?? 0 }} stages</span>
          </div>
          <div class="ws-card__stat">
            <Layers :size="14" />
            <span>{{ summaries[ws.id]?.environments ?? 0 }} envs</span>
          </div>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<style scoped>
.ws-selector {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.ws-selector__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  flex-wrap: wrap;
}

.ws-selector__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.03em;
}

.ws-selector__title-icon {
  color: var(--accent-cyan);
}

.ws-selector__subtitle {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-top: 6px;
}

.ws-selector__search {
  max-width: 400px;
}

.ws-selector__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

/* Workspace Card */
.ws-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.ws-card:hover {
  transform: translateY(-2px);
}

.ws-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ws-card__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.ws-card__arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast), color var(--transition-fast);
}

.ws-card:hover .ws-card__arrow {
  color: var(--accent-cyan);
  transform: translateX(4px);
}

.ws-card__name {
  font-size: 1.1rem;
  font-weight: 700;
}

.ws-card__desc {
  color: var(--text-muted);
  font-size: 0.8rem;
  line-height: 1.4;
}

.ws-card__stats {
  display: flex;
  gap: 1rem;
  margin-top: 4px;
  flex-wrap: wrap;
}

.ws-card__stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: var(--text-secondary);
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

.btn--primary:hover {
  box-shadow: 0 4px 16px rgba(34, 211, 238, 0.3);
  transform: translateY(-1px);
}

@media (max-width: 640px) {
  .ws-selector__hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .ws-selector__grid {
    grid-template-columns: 1fr;
  }
}
</style>
