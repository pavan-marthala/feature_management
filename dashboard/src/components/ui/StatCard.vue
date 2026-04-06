<script setup lang="ts">
import { ref, onMounted, watch, type Component } from 'vue'

const props = defineProps<{
  label: string
  value: number
  icon: Component
  color?: string
  gradient?: string
}>()

const displayValue = ref(0)

function animateCount(target: number) {
  const duration = 800
  const start = displayValue.value
  const diff = target - start
  const startTime = performance.now()

  function step(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    // Ease out cubic
    const eased = 1 - Math.pow(1 - progress, 3)
    displayValue.value = Math.round(start + diff * eased)
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

onMounted(() => {
  animateCount(props.value)
})

watch(() => props.value, (newVal) => {
  animateCount(newVal)
})
</script>

<template>
  <div class="stat-card glass" :class="{ 'stat-card--gradient': gradient }">
    <div class="stat-card__accent" :style="{ background: gradient || color || 'var(--gradient-accent)' }"></div>
    <div class="stat-card__content">
      <div class="stat-card__icon" :style="{ background: (gradient || color || 'var(--gradient-accent)') }">
        <component :is="icon" :size="20" />
      </div>
      <div class="stat-card__info">
        <span class="stat-card__value">{{ displayValue }}</span>
        <span class="stat-card__label">{{ label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  position: relative;
  overflow: hidden;
  padding: 1.25rem 1.5rem;
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: var(--glass-border-hover);
}

.stat-card__accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.stat-card__content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-card__icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  opacity: 0.9;
}

.stat-card__info {
  display: flex;
  flex-direction: column;
}

.stat-card__value {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.2;
  color: var(--text-primary);
}

.stat-card__label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 2px;
}
</style>
