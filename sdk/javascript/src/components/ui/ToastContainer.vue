<script setup lang="ts">
import { useUiStore } from '@/stores/uiStore'
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-vue-next'
import { computed } from 'vue'

const uiStore = useUiStore()

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  info: Info,
  warning: AlertTriangle,
}

function getIcon(type: string) {
  return iconMap[type as keyof typeof iconMap] || Info
}

const toastsReversed = computed(() => [...uiStore.toasts].reverse())
</script>

<template>
  <Teleport to="body">
    <div class="toast-container" id="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toastsReversed"
          :key="toast.id"
          class="toast"
          :class="`toast--${toast.type}`"
        >
          <div class="toast__icon">
            <component :is="getIcon(toast.type)" :size="18" />
          </div>
          <span class="toast__message">{{ toast.message }}</span>
          <button
            class="toast__close"
            @click="uiStore.removeToast(toast.id)"
            aria-label="Close notification"
          >
            <X :size="14" />
          </button>
          <div class="toast__progress" :style="{ animationDuration: `${toast.duration}ms` }"></div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  pointer-events: auto;
  position: relative;
  overflow: hidden;
}

.toast--success {
  border-left: 3px solid var(--accent-emerald);
}
.toast--error {
  border-left: 3px solid var(--accent-rose);
}
.toast--info {
  border-left: 3px solid var(--accent-cyan);
}
.toast--warning {
  border-left: 3px solid var(--accent-amber);
}

.toast__icon {
  flex-shrink: 0;
  display: flex;
}

.toast--success .toast__icon { color: var(--accent-emerald); }
.toast--error .toast__icon { color: var(--accent-rose); }
.toast--info .toast__icon { color: var(--accent-cyan); }
.toast--warning .toast__icon { color: var(--accent-amber); }

.toast__message {
  flex: 1;
  font-size: 0.85rem;
  color: var(--text-primary);
  line-height: 1.4;
}

.toast__close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  transition: all var(--transition-fast);
}

.toast__close:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.1);
}

.toast__progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  width: 100%;
  animation: progress-shrink linear forwards;
}

.toast--success .toast__progress { background: var(--accent-emerald); }
.toast--error .toast__progress { background: var(--accent-rose); }
.toast--info .toast__progress { background: var(--accent-cyan); }
.toast--warning .toast__progress { background: var(--accent-amber); }

@keyframes progress-shrink {
  from { width: 100%; }
  to { width: 0%; }
}

/* Transition */
.toast-enter-active {
  transition: all 0.3s ease-out;
}
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(40px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(40px) scale(0.95);
}
</style>
