<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  variant?: 'default' | 'success' | 'danger' | 'warning' | 'info' | 'indigo' | 'cyan'
}>()

const colorClass = computed(() => `badge--${props.variant || 'default'}`)
</script>

<template>
  <span class="badge" :class="colorClass">
    {{ label }}
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  font-size: 0.7rem;
  font-weight: 600;
  border-radius: var(--radius-full);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.badge--default {
  background: rgba(148, 163, 184, 0.15);
  color: var(--text-secondary);
}
.badge--success {
  background: rgba(52, 211, 153, 0.15);
  color: var(--accent-emerald);
}
.badge--danger {
  background: rgba(251, 113, 133, 0.15);
  color: var(--accent-rose);
}
.badge--warning {
  background: rgba(251, 191, 36, 0.15);
  color: var(--accent-amber);
}
.badge--info {
  background: rgba(34, 211, 238, 0.15);
  color: var(--accent-cyan);
}
.badge--indigo {
  background: rgba(99, 102, 241, 0.15);
  color: var(--accent-indigo);
}
.badge--cyan {
  background: rgba(34, 211, 238, 0.15);
  color: var(--accent-cyan);
}
</style>
