<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import { useFeatureStore } from '@/stores/featureStore'
import StatCard from '@/components/ui/StatCard.vue'
import GlassCard from '@/components/ui/GlassCard.vue'
import Badge from '@/components/ui/Badge.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import {
  Flag,
  CheckCircle,
  GitBranch,
  Layers,
  Plus,
  ArrowRight,
  Zap,
} from 'lucide-vue-next'

const workspaceStore = useWorkspaceStore()
const featureStore = useFeatureStore()
const router = useRouter()
const route = useRoute()

const workspaceId = route.params.workspaceId as string
const statsLoading = ref(true)

onMounted(async () => {
  statsLoading.value = true
  try {
    const promises: Promise<unknown>[] = [
      workspaceStore.fetchWorkspace(workspaceId),
      workspaceStore.fetchSummary(workspaceId),
      featureStore.fetchWorkspaceFeatures(workspaceId, 0, 25),
    ]
    await Promise.allSettled(promises)
  } catch (error) {
    console.error('Failed to load workspace dashboard', error)
  } finally {
    statsLoading.value = false
  }
})

function getStrategyBadge(strategy?: string) {
  switch (strategy) {
    case 'BooleanFeatureStrategy': return { label: 'Boolean', variant: 'cyan' as const }
    case 'JWTClaimFeatureStrategy': return { label: 'JWT Claim', variant: 'indigo' as const }
    case 'HTTPRequestFeatureStrategy': return { label: 'HTTP Request', variant: 'warning' as const }
    case 'ScheduleFeatureStrategy': return { label: 'Schedule', variant: 'success' as const }
    default: return { label: 'Unknown', variant: 'default' as const }
  }
}

function navigateToFeature(id: string) {
  router.push(`/workspaces/${workspaceId}/features/${id}`)
}
</script>

<template>
  <div class="dashboard">
    <!-- Hero Section -->
    <div class="dashboard__hero animate-fadeInUp">
      <div class="dashboard__hero-content">
        <h1 class="dashboard__title">
          <Zap :size="28" class="dashboard__title-icon" />
          {{ workspaceStore.selectedWorkspace?.name || 'Workspace' }}
        </h1>
        <p class="dashboard__subtitle">
          {{ workspaceStore.selectedWorkspace?.description || 'Manage features, workflows, and promotion pipelines.' }}
        </p>
      </div>
      <div class="dashboard__hero-actions">
        <button class="btn btn--primary" @click="router.push(`/workspaces/${workspaceId}/features/create`)"
          id="create-feature-btn">
          <Plus :size="18" />
          New Feature
        </button>
        <button class="btn btn--ghost" @click="router.push(`/workspaces/${workspaceId}/workflow`)"
          id="view-workflow-btn">
          <GitBranch :size="18" />
          Workflow
        </button>
      </div>
    </div>

    <!-- Stats -->
    <div class="dashboard__stats" v-if="statsLoading">
      <LoadingSkeleton variant="card" :rows="1" class="animate-fadeInUp stagger-1" />
      <LoadingSkeleton variant="card" :rows="1" class="animate-fadeInUp stagger-2" />
      <LoadingSkeleton variant="card" :rows="1" class="animate-fadeInUp stagger-3" />
    </div>
    <div class="dashboard__stats" v-else-if="workspaceStore.workspaceSummary">
      <div class="animate-fadeInUp stagger-1">
        <StatCard
          label="Features"
          :value="workspaceStore.workspaceSummary.featureCount"
          :icon="Flag"
          gradient="var(--gradient-accent)"
        />
      </div>
      <div class="animate-fadeInUp stagger-2">
        <StatCard
          label="Workflow Stages"
          :value="workspaceStore.workspaceSummary.workflowStages"
          :icon="GitBranch"
          gradient="var(--gradient-success)"
        />
      </div>
      <div class="animate-fadeInUp stagger-3">
        <StatCard
          label="Environments"
          :value="workspaceStore.workspaceSummary.environments"
          :icon="Layers"
          gradient="var(--gradient-warning)"
        />
      </div>
    </div>

    <!-- Recent Features -->
    <div class="dashboard__section animate-fadeInUp stagger-4">
      <div class="dashboard__section-header">
        <h2 class="dashboard__section-title">Recent Features</h2>
        <button class="btn btn--ghost btn--sm"
          @click="router.push(`/workspaces/${workspaceId}/features`)">
          View All <ArrowRight :size="16" />
        </button>
      </div>

      <LoadingSkeleton v-if="featureStore.loading" variant="table-row" :rows="5" />

      <div v-else-if="featureStore.features.length === 0" class="dashboard__empty">
        <GlassCard hover>
          <div class="empty-state">
            <Flag :size="48" class="empty-state__icon" />
            <h3>No features yet</h3>
            <p>Create your first feature flag in this workspace.</p>
            <button class="btn btn--primary"
              @click="router.push(`/workspaces/${workspaceId}/features/create`)">
              <Plus :size="18" /> Create Feature
            </button>
          </div>
        </GlassCard>
      </div>

      <div v-else class="feature-list">
        <GlassCard
          v-for="feature in featureStore.features.slice(0, 6)"
          :key="feature.id"
          hover
          class="feature-item"
          @click="navigateToFeature(feature.id)"
        >
          <div class="feature-item__content">
            <div class="feature-item__info">
              <div class="feature-item__name-row">
                <span class="feature-item__name">{{ feature.name }}</span>
                <Badge
                  v-bind="getStrategyBadge(feature.configuration?.strategy)"
                />
              </div>
              <p class="feature-item__desc">{{ feature.description || 'No description' }}</p>
            </div>
            <div class="feature-item__actions" @click.stop>
              <ToggleSwitch
                :model-value="feature.enabled"
                size="sm"
                @update:model-value="featureStore.toggleFeature(feature)"
              />
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.dashboard__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  flex-wrap: wrap;
}

.dashboard__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.03em;
}

.dashboard__title-icon {
  color: var(--accent-cyan);
}

.dashboard__subtitle {
  color: var(--text-secondary);
  font-size: 0.95rem;
  margin-top: 6px;
}

.dashboard__hero-actions {
  display: flex;
  gap: 10px;
}

.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.dashboard__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.dashboard__section-title {
  font-size: 1.15rem;
  font-weight: 600;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 0.75rem;
}

.feature-item {
  cursor: pointer;
}

.feature-item__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.feature-item__name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.feature-item__name {
  font-weight: 600;
  font-size: 0.95rem;
}

.feature-item__desc {
  color: var(--text-muted);
  font-size: 0.8rem;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
}

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

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
  border-color: var(--glass-border-hover);
}

.btn--sm {
  padding: 6px 14px;
  font-size: 0.8rem;
}

@media (max-width: 640px) {
  .dashboard__hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }
}
</style>
