<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  disabled?: boolean
  loading?: boolean
  size?: 'sm' | 'md' | 'lg'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const sizeClass = computed(() => `toggle--${props.size || 'md'}`)

function toggle() {
  if (!props.disabled && !props.loading) {
    emit('update:modelValue', !props.modelValue)
  }
}
</script>

<template>
  <button
    type="button"
    class="toggle"
    :class="[
      sizeClass,
      {
        'toggle--active': modelValue,
        'toggle--disabled': disabled,
        'toggle--loading': loading,
      },
    ]"
    :aria-checked="modelValue"
    role="switch"
    @click="toggle"
  >
    <span class="toggle__track">
      <span class="toggle__thumb">
        <svg
          v-if="loading"
          class="toggle__spinner"
          viewBox="0 0 24 24"
          fill="none"
        >
          <circle
            cx="12" cy="12" r="10"
            stroke="currentColor"
            stroke-width="3"
            stroke-linecap="round"
            stroke-dasharray="31.4 31.4"
            stroke-dashoffset="10"
          />
        </svg>
      </span>
    </span>
  </button>
</template>

<style scoped>
.toggle {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px;
  display: inline-flex;
  align-items: center;
  -webkit-tap-highlight-color: transparent;
}

.toggle--disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toggle__track {
  position: relative;
  border-radius: var(--radius-full);
  background: var(--bg-tertiary);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
}

.toggle--active .toggle__track {
  background: linear-gradient(135deg, var(--accent-cyan), var(--accent-indigo));
  box-shadow: 0 0 12px rgba(34, 211, 238, 0.3);
}

.toggle__thumb {
  position: absolute;
  background: white;
  border-radius: 50%;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* Sizes */
.toggle--sm .toggle__track {
  width: 36px;
  height: 20px;
}
.toggle--sm .toggle__thumb {
  width: 16px;
  height: 16px;
  left: 2px;
}
.toggle--sm.toggle--active .toggle__thumb {
  left: 18px;
}

.toggle--md .toggle__track {
  width: 44px;
  height: 24px;
}
.toggle--md .toggle__thumb {
  width: 20px;
  height: 20px;
  left: 2px;
}
.toggle--md.toggle--active .toggle__thumb {
  left: 22px;
}

.toggle--lg .toggle__track {
  width: 52px;
  height: 28px;
}
.toggle--lg .toggle__thumb {
  width: 24px;
  height: 24px;
  left: 2px;
}
.toggle--lg.toggle--active .toggle__thumb {
  left: 26px;
}

/* Spinner */
.toggle__spinner {
  width: 12px;
  height: 12px;
  animation: spin 0.8s linear infinite;
  color: var(--accent-indigo);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
