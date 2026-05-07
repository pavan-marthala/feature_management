<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useUiStore } from '@/stores/uiStore'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import ThemeSelector from '@/components/ui/ThemeSelector.vue'
import WorkspaceDropdown from '@/components/workspace/WorkspaceDropdown.vue'
import WorkspaceFormModal from '@/components/modals/WorkspaceFormModal.vue'
import EnvironmentFormModal from '@/components/modals/EnvironmentFormModal.vue'
import FeatureFormModal from '@/components/modals/FeatureFormModal.vue'
import {
  Flag,
  Layers,
  PanelLeftClose,
  PanelLeftOpen,
  Menu,
  GitBranch,
} from 'lucide-vue-next'

const uiStore = useUiStore()
const workspaceStore = useWorkspaceStore()
const route = useRoute()
const mobileDrawerOpen = ref(false)

function closeMobileDrawer() {
  mobileDrawerOpen.value = false
}

const navItems = [
  { to: '/features', label: 'Features', icon: Flag },
  { to: '/workflows', label: 'Workflows', icon: GitBranch },
  { to: '/environments', label: 'Environments', icon: Layers },
]

function isActive(item: { to: string }) {
  return route.path.startsWith(item.to)
}
</script>

<template>
  <div class="layout"
    :class="{ 'layout--collapsed': uiStore.sidebarCollapsed, 'layout--mobile-open': mobileDrawerOpen }">
    <!-- Ambient Background -->
    <div class="bg-ambient"></div>

    <!-- Mobile Header Bar (always visible on mobile) -->
    <header class="mobile-header">
      <button class="mobile-header__menu" @click="mobileDrawerOpen = true">
        <Menu :size="20" />
      </button>
      <div class="mobile-header__ws">
        <WorkspaceDropdown />
      </div>
    </header>

    <!-- Overlay for mobile drawer -->
    <Transition name="fade">
      <div v-if="mobileDrawerOpen" class="mobile-overlay" @click="closeMobileDrawer"></div>
    </Transition>

    <!-- Sidebar -->
    <aside class="sidebar" id="main-sidebar">
      <div class="sidebar__header">
        <div class="sidebar__logo">
          <div class="sidebar__logo-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24">
              <g fill="#fff">
                <path fill-opacity="0" stroke="#fff" stroke-dasharray="54" stroke-linecap="round"
                  stroke-linejoin="round" stroke-width="2"
                  d="M12 7h5c2.76 0 5 2.24 5 5c0 2.76 -2.24 5 -5 5h-10c-2.76 0 -5 -2.24 -5 -5c0 -2.76 2.24 -5 5 -5Z">
                  <animate fill="freeze" attributeName="stroke-dashoffset" dur="0.6s" values="54;0" />
                  <animate fill="freeze" attributeName="fill-opacity" begin="0.6s" dur="0.15s" to="0.3" />
                </path>
                <circle cx="17" cy="12" r="3" opacity="0">
                  <animate fill="freeze" attributeName="opacity" begin="0.6s" dur="0.2s" to="1" />
                </circle>
              </g>
            </svg>
          </div>
          <Transition name="fade">
            <span v-if="!uiStore.sidebarCollapsed" class="sidebar__logo-text">
              Feature <span class="sidebar__logo-accent">Management</span>
            </span>
          </Transition>
        </div>
      </div>

      <!-- Workspace Dropdown (desktop sidebar only) -->
      <div v-if="!uiStore.sidebarCollapsed" class="sidebar__workspace-dropdown">
        <WorkspaceDropdown />
      </div>

      <nav class="sidebar__nav">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="sidebar__link"
          :class="{ 'sidebar__link--active': isActive(item) }"
          :title="uiStore.sidebarCollapsed ? item.label : undefined" @click="closeMobileDrawer">
          <component :is="item.icon" :size="20" class="sidebar__link-icon" />
          <Transition name="fade">
            <span v-if="!uiStore.sidebarCollapsed" class="sidebar__link-label">
              {{ item.label }}
            </span>
          </Transition>
          <div v-if="isActive(item)" class="sidebar__link-indicator"></div>
        </RouterLink>
      </nav>

      <div class="sidebar__theme-section">
        <ThemeSelector variant="sidebar" :collapsed="uiStore.sidebarCollapsed" />
      </div>

      <div class="sidebar__footer">
        <button class="sidebar__toggle" @click="uiStore.toggleSidebar()"
          :title="uiStore.sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'" id="sidebar-toggle">
          <div class="sidebar__toggle-icon-wrapper">
            <PanelLeftClose v-if="!uiStore.sidebarCollapsed" :size="20" />
            <PanelLeftOpen v-else :size="20" />
          </div>
          <Transition name="fade">
            <span v-if="!uiStore.sidebarCollapsed" class="sidebar__toggle-label">Collapse Menu</span>
          </Transition>
        </button>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="main-area">
      <!-- Page Content -->
      <main class="content" id="main-content">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </main>
    </div>

    <!-- Global Modals -->
    <WorkspaceFormModal />
    <EnvironmentFormModal />
    <FeatureFormModal />
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  position: relative;
}

/* ===== Mobile Header Bar ===== */
.mobile-header {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  z-index: 50;
  background: var(--sidebar-bg);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-bottom: 1px solid var(--glass-border);
  padding: 0 12px;
  align-items: center;
  gap: 8px;
}

.mobile-header__menu {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}

.mobile-header__ws {
  flex: 1;
  min-width: 0;
}

/* ===== Sidebar ===== */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-right: 1px solid var(--glass-border);
  z-index: 100;
  transition: width var(--transition-slow);
  overflow: hidden;
}

.layout--collapsed .sidebar {
  width: var(--sidebar-collapsed-width);
}

.sidebar__header {
  padding: 1.25rem;
  border-bottom: 1px solid var(--glass-border);
}

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar__logo-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--gradient-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.sidebar__logo-text {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.sidebar__logo-accent {
  background: var(--gradient-accent);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Workspace Dropdown in Sidebar */
.sidebar__workspace-dropdown {
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--glass-border);
}

/* Nav */
.sidebar__nav {
  flex: 1;
  padding: 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar__link:hover {
  color: var(--text-primary);
  background: var(--glass-bg-hover);
}

.sidebar__link--active {
  color: var(--text-primary);
  background: rgba(34, 211, 238, 0.08);
}

.sidebar__link-icon {
  flex-shrink: 0;
}

.sidebar__link--active .sidebar__link-icon {
  color: var(--accent-cyan);
}

.sidebar__link-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 4px 4px 0;
  background: var(--gradient-accent);
}

/* Theme Section */
.sidebar__theme-section {
  padding: 0 0.75rem 0.5rem 0.75rem;
  display: flex;
  flex-direction: column;
}

/* Footer */
.sidebar__footer {
  padding: 0.75rem;
  border-top: 1px solid var(--glass-border);
}

.sidebar__toggle {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 10px 12px;
  background: transparent;
  border: none;
  font-family: inherit;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  width: 100%;
  gap: 12px;
}

.sidebar__toggle-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar__toggle-label {
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
}

.sidebar__toggle:hover {
  color: var(--text-primary);
  background: var(--glass-bg-hover);
}

/* ===== Main Area ===== */
.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left var(--transition-slow);
  width: calc(100vw - var(--sidebar-width));
  overflow-x: hidden;
}

.layout--collapsed .main-area {
  margin-left: var(--sidebar-collapsed-width);
  width: calc(100vw - var(--sidebar-collapsed-width));
}

/* ===== Content ===== */
.content {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* ===== Responsive ===== */

/* Overlay */
.mobile-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 90;
}

/* Tablet */
@media (max-width: 1024px) {
  .sidebar {
    width: var(--sidebar-collapsed-width);
  }

  .main-area {
    margin-left: var(--sidebar-collapsed-width);
    width: calc(100vw - var(--sidebar-collapsed-width));
  }

  .sidebar__logo-text,
  .sidebar__link-label {
    display: none;
  }

  .sidebar__toggle {
    display: none;
  }

  .sidebar__workspace-dropdown {
    display: none;
  }
}

/* Mobile */
@media (max-width: 640px) {
  .mobile-header {
    display: flex;
  }

  .mobile-overlay {
    display: block;
  }

  .sidebar {
    width: 280px;
    transform: translateX(-100%);
    transition: transform var(--transition-slow);
  }

  .layout--mobile-open .sidebar {
    transform: translateX(0);
  }

  .sidebar__logo-text,
  .sidebar__link-label {
    display: inline;
  }

  .sidebar__workspace-dropdown {
    display: block;
  }

  /* Reset layout constraints for mobile */
  .layout--collapsed .sidebar {
    width: 280px;
  }

  .main-area,
  .layout--collapsed .main-area {
    margin-left: 0;
    width: 100vw;
  }

  .content {
    padding: 1rem;
    padding-top: 72px;
    /* Make room for fixed mobile header */
  }
}
</style>
