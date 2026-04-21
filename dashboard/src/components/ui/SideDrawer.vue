<script setup lang="ts">
import { X } from 'lucide-vue-next'

defineProps<{
  show: boolean
  title: string
}>()

defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div v-if="show" class="drawer-overlay" @click="$emit('close')">
        <div class="drawer" @click.stop>
          <div class="drawer__header">
            <h2 class="drawer__title">{{ title }}</h2>
            <button class="drawer__close" @click="$emit('close')">
              <X :size="20" />
            </button>
          </div>
          <div class="drawer__content">
            <slot></slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 200;
  display: flex;
  justify-content: flex-end;
}

.drawer {
  width: 100%;
  max-width: 450px;
  height: 100%;
  background: var(--bg-secondary);
  border-left: 1px solid var(--glass-border);
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.drawer__header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--glass-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drawer__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.drawer__close {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.drawer__close:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.drawer__content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

/* Transitions */
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.3s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-active .drawer,
.drawer-leave-active .drawer {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.drawer-enter-from .drawer {
  transform: translateX(100%);
}

.drawer-leave-to .drawer {
  transform: translateX(100%);
}

@media (max-width: 640px) {
  .drawer {
    max-width: 100%;
  }
}
</style>
