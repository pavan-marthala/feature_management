import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Toast, ToastType } from '@/types'

export type ThemeMode = 'system' | 'light' | 'dark'

export const useUiStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const toasts = ref<Toast[]>([])
  const globalLoading = ref(false)
  const theme = ref<ThemeMode>('system')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function addToast(message: string, type: ToastType = 'info', duration = 4000) {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`
    const toast: Toast = { id, message, type, duration }
    toasts.value.push(toast)

    // Auto-remove after duration
    setTimeout(() => {
      removeToast(id)
    }, duration)

    return id
  }

  function removeToast(id: string) {
    const idx = toasts.value.findIndex((t) => t.id === id)
    if (idx !== -1) {
      toasts.value.splice(idx, 1)
    }
  }

  function applyTheme(mode: ThemeMode) {
    let activeTheme = mode
    if (activeTheme === 'system') {
      activeTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    document.documentElement.setAttribute('data-theme', activeTheme)
  }

  function setTheme(newTheme: ThemeMode) {
    theme.value = newTheme
    localStorage.setItem('feature-mgmt-theme', newTheme)
    applyTheme(newTheme)
  }

  function initTheme() {
    const savedTheme = localStorage.getItem('feature-mgmt-theme') as ThemeMode | null
    if (savedTheme && ['system', 'light', 'dark'].includes(savedTheme)) {
      theme.value = savedTheme
    }
    applyTheme(theme.value)

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (theme.value === 'system') {
        applyTheme('system')
      }
    })
  }

  return {
    sidebarCollapsed,
    toasts,
    globalLoading,
    theme,
    toggleSidebar,
    addToast,
    removeToast,
    setTheme,
    initTheme,
  }
})
