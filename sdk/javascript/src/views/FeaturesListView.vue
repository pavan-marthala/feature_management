<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useFeatureStore } from '@/stores/featureStore'
import SearchInput from '@/components/ui/SearchInput.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import Badge from '@/components/ui/Badge.vue'
import LoadingSkeleton from '@/components/ui/LoadingSkeleton.vue'
import PaginationComp from '@/components/ui/Pagination.vue'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  Plus,
  Flag,
  Eye,
  Pencil,
  Trash2,
  Filter,
} from 'lucide-vue-next'
import type { FeatureStrategyType } from '@/types'

const featureStore = useFeatureStore()
const router = useRouter()

onMounted(async () => {
  if (featureStore.features.length === 0 && !featureStore.loading) {
    await featureStore.fetchFeatures(0, 25)
  }
  if (featureStore.strategies.length === 0) {
    await featureStore.fetchStrategies()
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

function getStatusBadge(enabled: boolean) {
  return enabled
    ? { label: 'Enabled', variant: 'success' as const }
    : { label: 'Disabled', variant: 'danger' as const }
}

function onPageChange(page: number) {
  featureStore.fetchFeatures(page, featureStore.pagination.size)
}

// Watch is needed to handle the filtered list in the client
watch(() => featureStore.searchQuery, () => {
  // Client-side filtering, no API call needed
})
</script>

<template>
  <div class="features-page">
    <!-- Header -->
    <div class="features-page__header animate-fadeInUp">
      <div>
        <h1 class="features-page__title">Features</h1>
        <p class="features-page__subtitle">Manage your feature flags and rollout strategies</p>
      </div>
      <button class="btn btn--primary" @click="router.push('/features/create')" id="create-feature-btn">
        <Plus :size="18" />
        Create Feature
      </button>
    </div>

    <!-- Filters Bar -->
    <div class="features-page__filters animate-fadeInUp stagger-1">
      <SearchInput
        v-model="featureStore.searchQuery"
        placeholder="Search features..."
      />
      <div class="features-page__filter-group">
        <div class="filter-select">
          <Filter :size="16" class="filter-select__icon" />
          <select
            v-model="featureStore.statusFilter"
            class="filter-select__input"
            id="status-filter"
          >
            <option value="all">All Status</option>
            <option value="enabled">Enabled</option>
            <option value="disabled">Disabled</option>
          </select>
        </div>
        <div class="filter-select">
          <Filter :size="16" class="filter-select__icon" />
          <select
            v-model="featureStore.strategyFilter"
            class="filter-select__input"
            id="strategy-filter"
          >
            <option value="all">All Strategies</option>
            <option
              v-for="s in featureStore.strategies"
              :key="s.name"
              :value="s.name"
            >
              {{ s.name.replace('FeatureStrategy', '') }}
            </option>
            <option v-if="featureStore.strategies.length === 0" value="BooleanFeatureStrategy">Boolean</option>
            <option v-if="featureStore.strategies.length === 0" value="JWTClaimFeatureStrategy">JWT Claim</option>
            <option v-if="featureStore.strategies.length === 0" value="HTTPRequestFeatureStrategy">HTTP Request</option>
            <option v-if="featureStore.strategies.length === 0" value="ScheduleFeatureStrategy">Schedule</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <LoadingSkeleton v-if="featureStore.loading && featureStore.features.length === 0" variant="table-row" :rows="8" />

    <!-- Empty State -->
    <div v-else-if="featureStore.filteredFeatures.length === 0" class="empty-state animate-fadeInUp">
      <div class="empty-state__card glass">
        <Flag :size="56" class="empty-state__icon" />
        <h3>{{ featureStore.searchQuery || featureStore.statusFilter !== 'all' || featureStore.strategyFilter !== 'all' ? 'No matching features' : 'No features yet' }}</h3>
        <p>
          {{ featureStore.searchQuery || featureStore.statusFilter !== 'all' || featureStore.strategyFilter !== 'all'
            ? 'Try adjusting your search or filters.'
            : 'Create your first feature flag to get started.' }}
        </p>
        <button
          v-if="!featureStore.searchQuery && featureStore.statusFilter === 'all' && featureStore.strategyFilter === 'all'"
          class="btn btn--primary"
          @click="router.push('/features/create')"
        >
          <Plus :size="18" /> Create Feature
        </button>
      </div>
    </div>

    <!-- Feature Table (Desktop) -->
    <div v-else class="features-table animate-fadeInUp stagger-2">
      <div class="desktop-features glass">
        <table class="features-table__table">
          <thead>
            <tr>
              <th>Feature Name</th>
              <th>Status</th>
              <th>Strategy</th>
              <th>Description</th>
              <th>Toggle</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody class="clickable-tbody">
            <tr
              v-for="feature in featureStore.filteredFeatures"
              :key="feature.id"
              class="features-table__row"
              @click="router.push(`/features/${feature.id}`)"
            >
              <td>
                <span class="feature-name">
                  {{ feature.name }}
                </span>
              </td>
              <td>
                <Badge v-bind="getStatusBadge(feature.enabled)" />
              </td>
              <td>
                <Badge v-bind="getStrategyBadge(feature.configuration?.strategy)" />
              </td>
              <td>
                <span class="feature-desc">{{ feature.description || '—' }}</span>
              </td>
              <td @click.stop>
                <ToggleSwitch
                  :model-value="feature.enabled"
                  size="sm"
                  @update:model-value="featureStore.toggleFeature(feature)"
                />
              </td>
              <td @click.stop>
                <div class="actions">
                  <button
                    class="action-btn"
                    title="Edit"
                    @click="router.push(`/features/${feature.id}/edit`)"
                  >
                    <Pencil :size="16" />
                  </button>
                  <button
                    class="action-btn action-btn--danger"
                    title="Delete"
                    @click="featureStore.deleteFeature(feature.id, feature.etag)"
                  >
                    <Trash2 :size="16" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Feature Cards -->
      <div class="mobile-features">
        <GlassCard
          v-for="feature in featureStore.filteredFeatures"
          :key="feature.id"
          class="feature-card"
          hover
          @click="router.push(`/features/${feature.id}`)"
        >
          <div class="feature-card__header">
            <h3 class="feature-card__name">{{ feature.name }}</h3>
            <div class="feature-card__status" @click.stop>
              <ToggleSwitch
                :model-value="feature.enabled"
                size="sm"
                @update:model-value="featureStore.toggleFeature(feature)"
              />
            </div>
          </div>
          
          <p class="feature-card__desc">{{ feature.description || 'No description' }}</p>
          
          <div class="feature-card__badges">
            <Badge v-bind="getStatusBadge(feature.enabled)" />
            <Badge v-bind="getStrategyBadge(feature.configuration?.strategy)" />
          </div>

          <div class="feature-card__footer" @click.stop>
            <div class="feature-card__actions">
              <button
                class="btn btn--ghost btn--sm"
                @click="router.push(`/features/${feature.id}/edit`)"
              >
                <Pencil :size="14" /> Edit
              </button>
              <button
                class="btn btn--danger btn--sm"
                @click="featureStore.deleteFeature(feature.id, feature.etag)"
              >
                <Trash2 :size="14" /> Delete
              </button>
            </div>
          </div>
        </GlassCard>
      </div>

      <!-- Pagination -->
      <PaginationComp
        :page="featureStore.pagination.page"
        :total-pages="featureStore.pagination.totalPages"
        :total-items="featureStore.pagination.totalItems"
        :size="featureStore.pagination.size"
        @update:page="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.features-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.features-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
}

.features-page__title {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.features-page__subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 4px;
}

/* Filters */
.features-page__filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.features-page__filter-group {
  display: flex;
  gap: 10px;
}

.filter-select {
  position: relative;
  display: flex;
  align-items: center;
}

.filter-select__icon {
  position: absolute;
  left: 10px;
  color: var(--text-muted);
  pointer-events: none;
  z-index: 1;
}

.filter-select__input {
  appearance: none;
  padding: 10px 32px 10px 34px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.85rem;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%2394a3b8' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
}

.filter-select__input:focus {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
}

.filter-select__input option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* Table & Cards Toggle */
.desktop-features {
  display: block;
  overflow-x: auto;
}

.mobile-features {
  display: none;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

/* Table */
.features-table {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.features-table__table {
  width: 100%;
  border-collapse: collapse;
}

.features-table__table th {
  text-align: left;
  padding: 14px 16px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--glass-border);
  white-space: nowrap;
}

.features-table__table td {
  padding: 14px 16px;
  font-size: 0.875rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  vertical-align: middle;
}

.features-table__row {
  transition: all var(--transition-fast);
  cursor: pointer;
}

.features-table__row:hover {
  background: var(--glass-bg-hover);
  box-shadow: inset 0 0 0 1px var(--glass-border-hover), 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-name {
  font-weight: 600;
  color: var(--text-primary);
  transition: color var(--transition-fast);
}

.features-table__row:hover .feature-name {
  color: var(--accent-cyan);
}

.feature-desc {
  color: var(--text-muted);
  font-size: 0.8rem;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

/* Actions */
.actions {
  display: flex;
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

/* Responsive Cards (Mobile Features) */
.feature-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
}

.feature-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feature-card__name {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-all;
}

.feature-card__desc {
  color: var(--text-muted);
  font-size: 0.85rem;
  line-height: 1.4;
}

.feature-card__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.feature-card__footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  justify-content: flex-end;
}

.feature-card__actions {
  display: flex;
  gap: 8px;
}

/* Empty State */
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

.empty-state__icon {
  color: var(--text-muted);
  margin-bottom: 10px;
}

.empty-state__card h3 {
  font-size: 1.1rem;
  font-weight: 600;
}

.empty-state__card p {
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

.btn--ghost {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.btn--danger {
  background: rgba(251, 113, 133, 0.1);
  color: var(--accent-rose);
  border: 1px solid rgba(251, 113, 133, 0.2);
}

.btn--danger:hover {
  background: rgba(251, 113, 133, 0.25);
}

.btn--sm {
  padding: 6px 12px;
  font-size: 0.75rem;
}

@media (max-width: 1024px) {
  .desktop-features {
    display: none;
  }
  
  .mobile-features {
    display: grid;
  }
}

@media (max-width: 768px) {
  .features-page__filters {
    flex-direction: column;
    align-items: stretch;
  }

  .features-page__filter-group {
    flex-direction: column;
  }
}
</style>
