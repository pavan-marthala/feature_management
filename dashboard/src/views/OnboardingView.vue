<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import OnboardingStepper from '@/components/onboarding/OnboardingStepper.vue'
import StepWelcome from '@/components/onboarding/StepWelcome.vue'
import StepEnvironments from '@/components/onboarding/StepEnvironments.vue'
import StepWorkspace from '@/components/onboarding/StepWorkspace.vue'
import StepWorkflow from '@/components/onboarding/StepWorkflow.vue'
import StepStages from '@/components/onboarding/StepStages.vue'
import StepFeature from '@/components/onboarding/StepFeature.vue'
import StepComplete from '@/components/onboarding/StepComplete.vue'
import { Zap } from 'lucide-vue-next'

const onboarding = useOnboardingStore()
const step = computed(() => onboarding.currentStep)
const stepIndex = computed(() => onboarding.currentStepIndex)

onMounted(() => {
  onboarding.resolveStep()
})

function handleNext() {
  onboarding.nextStep()
}

function handleBack() {
  onboarding.prevStep()
}
</script>

<template>
  <div class="onboarding">
    <!-- Ambient background -->
    <div class="onboarding__ambient" />

    <!-- Top bar -->
    <header class="onboarding__header">
      <div class="onboarding__logo">
        <Zap :size="20" />
        <span>Feature Management</span>
      </div>

      <OnboardingStepper
        v-if="stepIndex > 0"
        :current-step="stepIndex"
        :total-steps="7"
        class="onboarding__stepper"
      />

      <div class="onboarding__header-right">
        <button
          v-if="stepIndex >= 1 && stepIndex < 6"
          class="skip-btn"
          @click="onboarding.goToStep('COMPLETED')"
        >
          Skip setup
        </button>
      </div>
    </header>

    <!-- Content -->
    <main class="onboarding__content">
      <Transition name="step-slide" mode="out-in">
        <StepWelcome v-if="step === 'WELCOME'" :key="0" @next="handleNext" />
        <StepEnvironments v-else-if="step === 'ENV_SETUP'" :key="1" @next="handleNext" />
        <StepWorkspace v-else-if="step === 'WORKSPACE_SETUP'" :key="2" @next="handleNext" />
        <StepWorkflow v-else-if="step === 'WORKFLOW_SETUP'" :key="3" @next="handleNext" />
        <StepStages v-else-if="step === 'STAGES_SETUP'" :key="4" @next="handleNext" />
        <StepFeature v-else-if="step === 'FEATURE_SETUP'" :key="5" @next="handleNext" />
        <StepComplete v-else-if="step === 'COMPLETED'" :key="6" />
      </Transition>
    </main>

    <!-- Footer navigation -->
    <footer v-if="stepIndex > 0 && stepIndex < 6" class="onboarding__footer">
      <button class="nav-btn nav-btn--back" @click="handleBack">
        ← Back
      </button>
    </footer>
  </div>
</template>

<style scoped>
.onboarding {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.onboarding__ambient {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(99, 102, 241, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(34, 211, 238, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 60% 80%, rgba(99, 102, 241, 0.06) 0%, transparent 50%),
    var(--bg-primary);
}

/* Header */
.onboarding__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 2rem;
  gap: 2rem;
}

.onboarding__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--accent-cyan);
  font-weight: 700;
  font-size: 0.9rem;
  white-space: nowrap;
}

.onboarding__stepper {
  flex: 1;
  max-width: 600px;
}

.onboarding__header-right {
  min-width: 100px;
  display: flex;
  justify-content: flex-end;
}

.skip-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 0.8rem;
  font-family: inherit;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.skip-btn:hover {
  color: var(--text-primary);
  background: var(--glass-bg);
}

/* Content */
.onboarding__content {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 2rem 3rem 4rem;
  overflow-y: auto;
}

/* Footer */
.onboarding__footer {
  padding: 1rem 2rem 1.5rem;
  display: flex;
  justify-content: flex-start;
}

.nav-btn {
  padding: 8px 16px;
  border: none;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.nav-btn--back {
  background: none;
  color: var(--text-muted);
}

.nav-btn--back:hover {
  color: var(--text-primary);
  background: var(--glass-bg);
}

/* Step transitions */
.step-slide-enter-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.step-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.step-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.step-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 768px) {
  .onboarding__header {
    flex-wrap: wrap;
    padding: 1rem;
    gap: 1rem;
  }

  .onboarding__stepper {
    order: 3;
    width: 100%;
    max-width: 100%;
  }

  .onboarding__content {
    padding: 1.5rem 1rem;
  }
}
</style>
