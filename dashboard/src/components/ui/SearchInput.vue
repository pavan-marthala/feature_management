<script setup lang="ts">
import { ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { Search, X } from 'lucide-vue-next'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const localValue = ref(props.modelValue)
const isFocused = ref(false)

const debouncedEmit = useDebounceFn((val: string) => {
  emit('update:modelValue', val)
}, 300)

watch(localValue, (val) => {
  debouncedEmit(val)
})

watch(() => props.modelValue, (val) => {
  if (val !== localValue.value) {
    localValue.value = val
  }
})

function clear() {
  localValue.value = ''
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="search-input" :class="{ 'search-input--focused': isFocused }">
    <Search class="search-input__icon" :size="18" />
    <input
      v-model="localValue"
      type="text"
      :placeholder="placeholder || 'Search...'"
      class="search-input__field"
      @focus="isFocused = true"
      @blur="isFocused = false"
    />
    <button
      v-if="localValue"
      class="search-input__clear"
      @click="clear"
      aria-label="Clear search"
    >
      <X :size="16" />
    </button>
  </div>
</template>

<style scoped>
.search-input {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  overflow: hidden;
}

.search-input--focused {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
  background: var(--glass-bg-hover);
}

.search-input__icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
  pointer-events: none;
  transition: color var(--transition-fast);
  flex-shrink: 0;
}

.search-input--focused .search-input__icon {
  color: var(--accent-cyan);
}

.search-input__field {
  width: 100%;
  padding: 10px 36px 10px 40px;
  background: none;
  border: none;
  color: var(--text-primary);
  font-size: 0.875rem;
  font-family: inherit;
  outline: none;
}

.search-input__field::placeholder {
  color: var(--text-muted);
}

.search-input__clear {
  position: absolute;
  right: 8px;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.search-input__clear:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.1);
}
</style>
