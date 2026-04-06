<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUiStore, type ThemeMode } from '@/stores/uiStore'
import { Sun, Moon, Monitor } from 'lucide-vue-next'

const props = defineProps<{
  variant?: 'default' | 'sidebar'
  collapsed?: boolean
}>()

const uiStore = useUiStore()
const isOpen = ref(false)

// Close dropdown when clicking outside
const dropdownRef = ref<HTMLElement | null>(null)

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

function toggleDropdown() {
  isOpen.value = !isOpen.value
}

function selectTheme(theme: ThemeMode) {
  uiStore.setTheme(theme)
  isOpen.value = false
}

const activeIcon = computed(() => {
  if (uiStore.theme === 'light') return Sun
  if (uiStore.theme === 'dark') return Moon
  return Monitor
})

const options: { value: ThemeMode; label: string; icon: any }[] = [
  { value: 'light', label: 'Light', icon: Sun },
  { value: 'dark', label: 'Dark', icon: Moon },
  { value: 'system', label: 'System', icon: Monitor },
]
</script>

<template>
  <div class="theme-selector" :class="{ 'theme-selector--sidebar': variant === 'sidebar' }" ref="dropdownRef">
    <button
      class="theme-selector__trigger"
      :class="[
        `theme-selector__trigger--${variant || 'default'}`,
        { 'theme-selector__trigger--active': isOpen }
      ]"
      title="Select theme"
      @click="toggleDropdown"
    >
      <div class="theme-selector__icon-wrapper">
        <component :is="activeIcon" :size="variant === 'sidebar' ? 20 : 18" />
      </div>
      <Transition name="fade">
        <span v-if="variant === 'sidebar' && !collapsed" class="theme-selector__label">Theme</span>
      </Transition>
    </button>

    <Transition name="fade-slide">
      <div v-if="isOpen" class="theme-selector__menu">
        <button
          v-for="option in options"
          :key="option.value"
          class="theme-selector__option"
          :class="{ 'theme-selector__option--active': uiStore.theme === option.value }"
          @click="selectTheme(option.value)"
        >
          <component :is="option.icon" :size="16" class="option-icon" />
          {{ option.label }}
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.theme-selector {
  position: relative;
  display: inline-block;
}

.theme-selector--sidebar {
  display: block;
  width: 100%;
}

.theme-selector__trigger {
  display: flex;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  background: transparent;
  border: none;
}

.theme-selector__trigger--default {
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  border: 1px solid transparent;
}

.theme-selector__trigger--sidebar {
  justify-content: flex-start;
  width: 100%;
  height: auto;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  gap: 12px;
  color: var(--text-secondary);
}

.theme-selector__icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-selector__label {
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
}

.theme-selector__trigger:hover,
.theme-selector__trigger--active {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.theme-selector__trigger--default:hover,
.theme-selector__trigger--default.theme-selector__trigger--active {
  color: var(--accent-cyan);
  border-color: var(--glass-border);
}

.theme-selector__menu {
  position: absolute;
  bottom: calc(100% + 4px);
  left: 0;
  min-width: 140px;
  padding: 6px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 2px;
  box-shadow: var(--shadow-lg);
  background: var(--glass-menu-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.theme-selector__option {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.85rem;
  text-align: left;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.option-icon {
  color: inherit;
}

.theme-selector__option:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.theme-selector__option--active {
  background: rgba(34, 211, 238, 0.1);
  color: var(--accent-cyan);
  font-weight: 500;
}

/* Custom dropdown transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.95);
  transform-origin: bottom left;
}
</style>
