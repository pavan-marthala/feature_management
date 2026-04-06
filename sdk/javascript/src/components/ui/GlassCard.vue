<script setup lang="ts">
defineProps<{
  hover?: boolean
  gradient?: boolean
  padding?: string
}>()
</script>

<template>
  <div
    class="glass-card"
    :class="{
      'glass-card--hover': hover,
      'glass-card--gradient': gradient,
    }"
    :style="padding ? { padding } : undefined"
  >
    <slot />
  </div>
</template>

<style scoped>
.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.glass-card--hover:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.glass-card--gradient {
  position: relative;
}

.glass-card--gradient::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 1px;
  background: var(--gradient-accent);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  -webkit-mask-composite: xor;
  pointer-events: none;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.glass-card--gradient:hover::before {
  opacity: 1;
}
</style>
