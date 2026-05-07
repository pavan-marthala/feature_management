<script setup lang="ts">
import { computed } from "vue";
import { Check } from "lucide-vue-next";

const props = defineProps<{
  currentStep: number;
  totalSteps: number;
}>();

const steps = [
  { label: "Welcome", short: "W" },
  { label: "Environments", short: "E" },
  { label: "Workflow", short: "Wf" },
  { label: "Stages", short: "S" },
  { label: "Workspace", short: "Ws" },
  { label: "Feature", short: "F" },
  { label: "Complete", short: "✓" },
];

const progressWidth = computed(() => {
  if (props.currentStep === 0) return "0%";
  return `${(props.currentStep / (props.totalSteps - 1)) * 100}%`;
});
</script>

<template>
  <div class="stepper" role="navigation" aria-label="Onboarding progress">
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
        <!-- Connector Line: Spans from this dot's center to the next dot's center -->
        <div
          v-if="idx < steps.length - 1"
          class="stepper__connector"
          :class="{ 'stepper__connector--filled': idx < currentStep }"
        ></div>

        <!-- Step Indicator: Higher z-index and solid background to cover the connector -->
        <div class="stepper__dot">
          <Check v-if="idx < currentStep" :size="14" />
          <span v-else>{{ idx + 1 }}</span>
        </div>

        <!-- Step Label -->
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
  max-width: 720px; /* Increased to accommodate all labels without overlap */
  margin: 0 auto;
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
  gap: 12px;
  flex: 1;
  min-width: 100px;
  position: relative;
}

/* Connector Logic: Hidden behind the solid dots */
.stepper__connector {
  position: absolute;
  top: 16px;
  left: 50%; /* Start at center of current dot */
  width: 100%; /* Reach to center of next dot */
  height: 4px;
  background: rgba(255, 255, 255, 0.08);
  z-index: 0;
  transition: background 0.4s ease;
}

.stepper__connector--filled {
  background: #22d3ee;
  box-shadow: 0 0 10px rgba(34, 211, 238, 0.3);
}

.stepper__dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 700;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.stepper__label {
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
  line-height: 1.3;
  white-space: nowrap;
}

/* Completed State */
.stepper__step--completed .stepper__dot {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 0 15px rgba(34, 211, 238, 0.3);
}

.stepper__step--completed .stepper__label {
  color: var(--accent-cyan);
}

/* Active State */
.stepper__step--active .stepper__dot {
  background: var(--bg-secondary);
  border: 2px solid var(--accent-cyan);
  color: var(--accent-cyan);
  box-shadow: 0 0 0 4px rgba(34, 211, 238, 0.12);
  transform: scale(1.1);
}

.stepper__step--active .stepper__label {
  color: var(--text-primary);
}
.stepper__step--upcoming .stepper__dot {
  background: var(--bg-tertiary);
  color: var(--text-muted);
  border: 1px solid var(--glass-border);
  /* Responsive Overrides */
  @media (max-width: 1100px) {
    .stepper__step {
      min-width: 120px;
    }
  }
}
.stepper__step--upcoming .stepper__label {
  color: var(--text-muted);
}

@media (max-width: 1000px) {
  .stepper {
    padding: 0 1rem;
  }

  .stepper__step {
    min-width: 0;
  }

  .stepper__connector {
    top: 14px;
    height: 3px;
  }

  .stepper__dot {
    width: 28px;
    height: 28px;
    font-size: 0.75rem;
  }

  .stepper__label {
    position: absolute;
    top: 40px;
    left: 50%;
    transform: translateX(-50%);
    opacity: 0;
    pointer-events: none;
    font-size: 0.9rem;
    font-weight: 600;
  }

  .stepper__step--active .stepper__label {
    opacity: 1;
    position: relative;
    top: 0;
    transform: none;
    left: 0;
    margin-top: 10px;
    font-size: 1rem;
    letter-spacing: 0.01em;
  }

  .stepper__step:not(.stepper__step--active) .stepper__label {
    display: none;
  }
}

@media (max-width: 640px) {
  .stepper {
    padding: 0 0.5rem;
  }
}

/* Animations */
.stepper__step--active .stepper__dot {
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0% {
    box-shadow: 0 0 0 0 rgba(34, 211, 238, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(34, 211, 238, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(34, 211, 238, 0);
  }
}
</style>
