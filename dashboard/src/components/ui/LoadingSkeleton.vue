<script setup lang="ts">
defineProps<{
  rows?: number
  height?: string
  variant?: 'text' | 'card' | 'table-row'
}>()
</script>

<template>
  <div class="skeleton" :class="`skeleton--${variant || 'text'}`">
    <template v-if="variant === 'card'">
      <div class="skeleton__card">
        <div class="skeleton__line skeleton__line--short animate-shimmer"></div>
        <div class="skeleton__line skeleton__line--full animate-shimmer" style="animation-delay: 0.1s"></div>
        <div class="skeleton__line skeleton__line--medium animate-shimmer" style="animation-delay: 0.2s"></div>
      </div>
    </template>
    <template v-else-if="variant === 'table-row'">
      <div
        v-for="i in (rows || 5)"
        :key="i"
        class="skeleton__row animate-shimmer"
        :style="{ animationDelay: `${i * 0.08}s` }"
      >
        <div class="skeleton__cell skeleton__cell--sm"></div>
        <div class="skeleton__cell skeleton__cell--lg"></div>
        <div class="skeleton__cell skeleton__cell--md"></div>
        <div class="skeleton__cell skeleton__cell--sm"></div>
      </div>
    </template>
    <template v-else>
      <div
        v-for="i in (rows || 3)"
        :key="i"
        class="skeleton__line animate-shimmer"
        :class="{
          'skeleton__line--full': i % 3 === 1,
          'skeleton__line--medium': i % 3 === 2,
          'skeleton__line--short': i % 3 === 0,
        }"
        :style="{
          height: height || '14px',
          animationDelay: `${i * 0.1}s`,
        }"
      ></div>
    </template>
  </div>
</template>

<style scoped>
.skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton__line {
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.06);
}

.skeleton__line--full { width: 100%; }
.skeleton__line--medium { width: 70%; }
.skeleton__line--short { width: 40%; }

.skeleton__card {
  padding: 1.5rem;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton__row {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-sm);
}

.skeleton__cell {
  height: 14px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.06);
}

.skeleton__cell--sm { width: 60px; }
.skeleton__cell--md { width: 120px; }
.skeleton__cell--lg { flex: 1; }
</style>
