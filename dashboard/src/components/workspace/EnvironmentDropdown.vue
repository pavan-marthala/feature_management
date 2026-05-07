<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { useUiStore } from '@/stores/uiStore'
import {
  ChevronDown,
  Layers,
  Plus,
  Check,
  Search,
} from 'lucide-vue-next'

const environmentStore = useEnvironmentStore()
const uiStore = useUiStore()
const router = useRouter()

const isOpen = ref(false)
const searchQuery = ref('')
const dropdownRef = ref<HTMLElement | null>(null)

const filteredEnvironments = computed(() => {
  if (!searchQuery.value) return environmentStore.environments
  const q = searchQuery.value.toLowerCase()
  return environmentStore.environments.filter(
    e => e.name.toLowerCase().includes(q) || e.description?.toLowerCase().includes(q)
  )
})

function toggle() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    searchQuery.value = ''
  }
}

function handleSelect(environment: typeof environmentStore.environments[0]) {
  environmentStore.switchEnvironment(environment)
  isOpen.value = false
}

function handleCreate() {
  isOpen.value = false
  uiStore.environmentModalOpen = true
}

function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="env-dropdown" ref="dropdownRef">
    <button class="env-dropdown__trigger" @click="toggle" :title="environmentStore.selectedEnvironment?.name || 'Select Environment'">
      <div class="env-dropdown__trigger-icon">
        <Layers :size="16" />
      </div>
      <span class="env-dropdown__trigger-name">
        {{ environmentStore.selectedEnvironment?.name || 'No Environment' }}
      </span>
      <ChevronDown :size="14" class="env-dropdown__chevron" :class="{ 'env-dropdown__chevron--open': isOpen }" />
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="env-dropdown__popover">
        <div class="env-dropdown__search">
          <Search :size="14" class="env-dropdown__search-icon" />
          <input
            v-model="searchQuery"
            type="text"
            class="env-dropdown__search-input"
            placeholder="Search environments..."
            @click.stop
          />
        </div>

        <div class="env-dropdown__list">
          <button
            v-for="env in filteredEnvironments"
            :key="env.id"
            class="env-dropdown__item"
            :class="{ 'env-dropdown__item--active': env.id === environmentStore.activeEnvironmentId }"
            @click="handleSelect(env)"
          >
            <div class="env-dropdown__item-icon">
              <Layers :size="14" />
            </div>
            <div class="env-dropdown__item-info">
              <span class="env-dropdown__item-name">{{ env.name }}</span>
              <span v-if="env.description" class="env-dropdown__item-desc">{{ env.description }}</span>
            </div>
            <Check v-if="env.id === environmentStore.activeEnvironmentId" :size="14" class="env-dropdown__item-check" />
          </button>

          <div v-if="filteredEnvironments.length === 0" class="env-dropdown__empty">
            <p>No environments found</p>
          </div>
        </div>

        <div class="env-dropdown__footer">
          <button class="env-dropdown__create" @click="handleCreate">
            <Plus :size="14" />
            <span>Create Environment</span>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.env-dropdown {
  position: relative;
  width: 100%;
}

.env-dropdown__trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.85rem;
  transition: all var(--transition-fast);
  overflow: hidden;
  white-space: nowrap;
}

.env-dropdown__trigger:hover {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
}

.env-dropdown__trigger-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  flex-shrink: 0;
}

.env-dropdown__trigger-name {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
}

.env-dropdown__chevron {
  color: var(--text-muted);
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.env-dropdown__chevron--open {
  transform: rotate(180deg);
}

/* Popover */
.env-dropdown__popover {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  min-width: 240px;
  background: var(--bg-secondary);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.05);
  z-index: 200;
  overflow: hidden;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

/* Search */
.env-dropdown__search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--glass-border);
}

.env-dropdown__search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.env-dropdown__search-input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.8rem;
}

.env-dropdown__search-input::placeholder {
  color: var(--text-muted);
}

/* List */
.env-dropdown__list {
  max-height: 240px;
  overflow-y: auto;
  padding: 4px;
}

.env-dropdown__item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: none;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  text-align: left;
  transition: all var(--transition-fast);
}

.env-dropdown__item:hover {
  background: var(--glass-bg-hover);
}

.env-dropdown__item--active {
  background: rgba(34, 211, 238, 0.08);
}

.env-dropdown__item-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.env-dropdown__item--active .env-dropdown__item-icon {
  background: rgba(34, 211, 238, 0.12);
  border-color: rgba(34, 211, 238, 0.2);
  color: var(--accent-cyan);
}

.env-dropdown__item-info {
  flex: 1;
  min-width: 0;
}

.env-dropdown__item-name {
  display: block;
  font-weight: 600;
  font-size: 0.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.env-dropdown__item-desc {
  display: block;
  color: var(--text-muted);
  font-size: 0.7rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 1px;
}

.env-dropdown__item-check {
  color: var(--accent-cyan);
  flex-shrink: 0;
}

.env-dropdown__empty {
  padding: 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.8rem;
}

/* Footer */
.env-dropdown__footer {
  border-top: 1px solid var(--glass-border);
  padding: 4px;
}

.env-dropdown__create {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.env-dropdown__create:hover {
  background: var(--glass-bg-hover);
  color: var(--accent-cyan);
}

/* Transitions */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
