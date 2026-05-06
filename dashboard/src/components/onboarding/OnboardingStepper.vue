<script setup lang="ts">
import { computed } from 'vue'
import { Check } from 'lucide-vue-next'

const props = defineProps<{
  currentStep: number
  totalSteps: number
}>()

const steps = [
  { label: 'Welcome', short: 'W' },
  { label: 'Environments', short: 'E' },
  { label: 'Workflow', short: 'Wf' },
  { label: 'Stages', short: 'S' },
  { label: 'Workspace', short: 'Ws' },
  { label: 'Feature', short: 'F' },
  { label: 'Complete', short: '✓' },
]

const progressWidth = computed(() => {
  if (props.currentStep === 0) return '0%'
  return `${(props.currentStep / (props.totalSteps - 1)) * 100}%`
})
</script>

<template>
  <div class="stepper" role="navigation" aria-label="Onboarding progress">
    <!-- Background track -->
    <div class="stepper__track">
      <div class="stepper__track-fill" :style="{ width: progressWidth }" />
    </div>

    <!-- Steps -->
    <div class="stepper__steps">
      <div
        v-for="(step, idx) in steps"
        :key="idx"
        class="stepper__step"
        :class="{
          'stepper__step--completed': idx < currentStep,
          'stepper__step--active': idx === currentStep,
          'stepper__step--upcoming': idx > currentStep,
        }"
      >
        <div class="stepper__dot">
          <Check v-if="idx < currentStep" :size="14" />
          <span v-else>{{ idx + 1 }}</span>
        </div>
        <span class="stepper__label">{{ step.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stepper {
  position: relative;
  padding: 0 2rem;
  width: 100%;
  max-width: 720px;
}

.stepper__track {
  position: absolute;
  top: 16px;
  left: calc(2rem + 16px);
  right: calc(2rem + 16px);
  height: 3px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  z-index: 0;
}

.stepper__track-fill {
  height: 100%;
  background: var(--gradient-accent);
  border-radius: 4px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.stepper__steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.stepper__step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.stepper__dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.stepper__label {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  transition: color 0.3s ease;
  white-space: nowrap;
}

/* Completed */
.stepper__step--completed .stepper__dot {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.25);
}

.stepper__step--completed .stepper__label {
  color: var(--accent-cyan);
}

/* Active */
.stepper__step--active .stepper__dot {
  background: var(--bg-secondary);
  border: 2px solid var(--accent-cyan);
  color: var(--accent-cyan);
  box-shadow: 0 0 0 4px rgba(34, 211, 238, 0.12);
  animation: step-pulse 2s ease-in-out infinite;
}

.stepper__step--active .stepper__label {
  color: var(--text-primary);
}

/* Upcoming */
.stepper__step--upcoming .stepper__dot {
  background: var(--bg-tertiary);
  color: var(--text-muted);
  border: 1px solid var(--glass-border);
}

.stepper__step--upcoming .stepper__label {
  color: var(--text-muted);
}

@keyframes step-pulse {
  0%, 100% {
    box-shadow: 0 0 0 4px rgba(34, 211, 238, 0.12);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(34, 211, 238, 0.06);
  }
}

@media (max-width: 640px) {
  .stepper {
    padding: 0 0.5rem;
  }

  .stepper__label {
    display: none;
  }

  .stepper__dot {
    width: 28px;
    height: 28px;
    font-size: 0.65rem;
  }

  .stepper__track {
    left: calc(0.5rem + 14px);
    right: calc(0.5rem + 14px);
    top: 14px;
  }
}
</style>
