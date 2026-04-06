<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{
  page: number
  totalPages: number
  totalItems: number
  size: number
}>()

const emit = defineEmits<{
  'update:page': [page: number]
}>()

const pages = computed(() => {
  const result: (number | string)[] = []
  const total = props.totalPages
  const current = props.page

  if (total <= 7) {
    for (let i = 1; i <= total; i++) result.push(i)
  } else {
    result.push(1)
    if (current > 3) result.push('...')

    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let i = start; i <= end; i++) result.push(i)

    if (current < total - 2) result.push('...')
    result.push(total)
  }

  return result
})

const showingFrom = computed(() => (props.page - 1) * props.size + 1)
const showingTo = computed(() => Math.min(props.page * props.size, props.totalItems))
</script>

<template>
  <div class="pagination" v-if="totalPages > 1">
    <span class="pagination__info">
      Showing {{ showingFrom }}–{{ showingTo }} of {{ totalItems }}
    </span>
    <div class="pagination__controls">
      <button
        class="pagination__btn"
        :disabled="page <= 1"
        @click="emit('update:page', page - 1)"
        aria-label="Previous page"
      >
        <ChevronLeft :size="16" />
      </button>
      <template v-for="p in pages" :key="p">
        <span v-if="p === '...'" class="pagination__dots">…</span>
        <button
          v-else
          class="pagination__btn"
          :class="{ 'pagination__btn--active': p === page }"
          @click="emit('update:page', p as number)"
        >
          {{ p }}
        </button>
      </template>
      <button
        class="pagination__btn"
        :disabled="page >= totalPages"
        @click="emit('update:page', page + 1)"
        aria-label="Next page"
      >
        <ChevronRight :size="16" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
  gap: 1rem;
  flex-wrap: wrap;
}

.pagination__info {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.pagination__controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
}

.pagination__btn:hover:not(:disabled) {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
  color: var(--text-primary);
}

.pagination__btn--active {
  background: var(--gradient-accent);
  border-color: transparent;
  color: white;
  font-weight: 600;
}

.pagination__btn--active:hover {
  background: var(--gradient-accent) !important;
  color: white !important;
}

.pagination__btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.pagination__dots {
  padding: 0 4px;
  color: var(--text-muted);
  font-size: 0.8rem;
}
</style>
