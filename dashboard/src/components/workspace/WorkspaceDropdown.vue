<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import {
  ChevronDown,
  FolderKanban,
  Plus,
  Check,
  Search,
} from 'lucide-vue-next'

const workspaceStore = useWorkspaceStore()
const router = useRouter()

const isOpen = ref(false)
const searchQuery = ref('')
const dropdownRef = ref<HTMLElement | null>(null)

const filteredWorkspaces = computed(() => {
  if (!searchQuery.value) return workspaceStore.workspaces
  const q = searchQuery.value.toLowerCase()
  return workspaceStore.workspaces.filter(
    w => w.name.toLowerCase().includes(q) || w.description?.toLowerCase().includes(q)
  )
})

function toggle() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    searchQuery.value = ''
  }
}

function handleSelect(workspace: typeof workspaceStore.workspaces[0]) {
  workspaceStore.switchWorkspace(workspace)
  isOpen.value = false
}

function handleCreate() {
  isOpen.value = false
  router.push('/workspaces/create')
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
  <div class="ws-dropdown" ref="dropdownRef">
    <button class="ws-dropdown__trigger" @click="toggle" :title="workspaceStore.selectedWorkspace?.name || 'Select Workspace'">
      <div class="ws-dropdown__trigger-icon">
        <FolderKanban :size="16" />
      </div>
      <span class="ws-dropdown__trigger-name">
        {{ workspaceStore.selectedWorkspace?.name || 'No Workspace' }}
      </span>
      <ChevronDown :size="14" class="ws-dropdown__chevron" :class="{ 'ws-dropdown__chevron--open': isOpen }" />
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" class="ws-dropdown__popover">
        <div class="ws-dropdown__search">
          <Search :size="14" class="ws-dropdown__search-icon" />
          <input
            v-model="searchQuery"
            type="text"
            class="ws-dropdown__search-input"
            placeholder="Search workspaces..."
            @click.stop
          />
        </div>

        <div class="ws-dropdown__list">
          <button
            v-for="ws in filteredWorkspaces"
            :key="ws.id"
            class="ws-dropdown__item"
            :class="{ 'ws-dropdown__item--active': ws.id === workspaceStore.activeWorkspaceId }"
            @click="handleSelect(ws)"
          >
            <div class="ws-dropdown__item-icon">
              <FolderKanban :size="14" />
            </div>
            <div class="ws-dropdown__item-info">
              <span class="ws-dropdown__item-name">{{ ws.name }}</span>
              <span v-if="ws.description" class="ws-dropdown__item-desc">{{ ws.description }}</span>
            </div>
            <Check v-if="ws.id === workspaceStore.activeWorkspaceId" :size="14" class="ws-dropdown__item-check" />
          </button>

          <div v-if="filteredWorkspaces.length === 0" class="ws-dropdown__empty">
            <p>No workspaces found</p>
          </div>
        </div>

        <div class="ws-dropdown__footer">
          <button class="ws-dropdown__create" @click="handleCreate">
            <Plus :size="14" />
            <span>Create Workspace</span>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.ws-dropdown {
  position: relative;
  width: 100%;
}

.ws-dropdown__trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--glass-border);
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  font-weight: 600;
  transition: all var(--transition-fast);
  overflow: hidden;
  white-space: nowrap;
}

.ws-dropdown__trigger:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--glass-border-hover);
}

.ws-dropdown__trigger-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.ws-dropdown__trigger-name {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ws-dropdown__chevron {
  color: var(--text-muted);
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.ws-dropdown__chevron--open {
  transform: rotate(180deg);
}

/* Popover */
.ws-dropdown__popover {
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
.ws-dropdown__search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--glass-border);
}

.ws-dropdown__search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.ws-dropdown__search-input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.8rem;
}

.ws-dropdown__search-input::placeholder {
  color: var(--text-muted);
}

/* List */
.ws-dropdown__list {
  max-height: 240px;
  overflow-y: auto;
  padding: 4px;
}

.ws-dropdown__item {
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

.ws-dropdown__item:hover {
  background: var(--glass-bg-hover);
}

.ws-dropdown__item--active {
  background: rgba(34, 211, 238, 0.08);
}

.ws-dropdown__item-icon {
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

.ws-dropdown__item--active .ws-dropdown__item-icon {
  background: rgba(34, 211, 238, 0.12);
  border-color: rgba(34, 211, 238, 0.2);
  color: var(--accent-cyan);
}

.ws-dropdown__item-info {
  flex: 1;
  min-width: 0;
}

.ws-dropdown__item-name {
  display: block;
  font-weight: 600;
  font-size: 0.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ws-dropdown__item-desc {
  display: block;
  color: var(--text-muted);
  font-size: 0.7rem;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 1px;
}

.ws-dropdown__item-check {
  color: var(--accent-cyan);
  flex-shrink: 0;
}

.ws-dropdown__empty {
  padding: 16px;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.8rem;
}

/* Footer */
.ws-dropdown__footer {
  border-top: 1px solid var(--glass-border);
  padding: 4px;
}

.ws-dropdown__create {
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

.ws-dropdown__create:hover {
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
