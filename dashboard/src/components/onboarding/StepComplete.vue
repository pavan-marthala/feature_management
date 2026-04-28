<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { workflowService } from '@/services/workflowService'
import { featureService } from '@/services/featureService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  PartyPopper, Layers, FolderKanban, Flag, ArrowRight, Loader2, Check,
} from 'lucide-vue-next'

const router = useRouter()
const onboarding = useOnboardingStore()
const envStore = useEnvironmentStore()

interface StageInfo { name: string; promoted: boolean }

const stages = ref<StageInfo[]>([])
const promoting = ref(false)
const promoted = ref(false)
const featureName = ref('')
const workspaceName = ref('Your Workspace')

onMounted(async () => {
  await envStore.fetchEnvironments(0, 100)
  const wfId = onboarding.createdWorkflowId
  if (wfId) {
    try {
      const wf = await workflowService.getWorkflow(wfId)
      if (wf?.stages?.length) {
        const sorted = [...wf.stages].sort((a, b) => a.orderIndex - b.orderIndex)
        stages.value = sorted.map((s, i) => ({
          name: s.environmentName || envStore.environments.find(e => e.id === s.environmentId)?.name || `Stage ${i + 1}`,
          promoted: i === 0, // first stage already has the feature
        }))
      }
      workspaceName.value = wf?.name?.replace(' Workflow', '') || 'Your Workspace'
    } catch { /* ignore */ }
  }
  // Get feature name
  const fId = onboarding.createdFeatureId
  if (fId) {
    try {
      const { feature } = await featureService.getFeature(fId, 'ID')
      featureName.value = feature.name
    } catch { featureName.value = 'Your Feature' }
  }
})

async function handlePromote() {
  const fId = onboarding.createdFeatureId
  if (!fId) return
  promoting.value = true
  try {
    await featureService.propagateFeature(fId)
    promoted.value = true
    if (stages.value.length >= 2 && stages.value[1]) {
      stages.value[1].promoted = true
    }
  } catch { /* ignore */ }
  finally { promoting.value = false }
}

function goToDashboard() {
  onboarding.completeOnboarding()
  const wsId = onboarding.createdWorkspaceId
  router.push(wsId ? `/workspaces/${wsId}` : '/workspaces')
}
</script>

<template>
  <div class="step-done">
    <div class="step-done__hero animate-fadeInUp">
      <!-- Confetti particles (CSS) -->
      <div class="confetti-wrapper">
        <div v-for="i in 20" :key="i" class="confetti-piece" :style="{ '--i': i }" />
      </div>

      <PartyPopper :size="48" class="step-done__icon" />
      <h2 class="step-done__title">You're all set!</h2>
      <p class="step-done__subtitle">Your workspace is ready. Here's what you've built:</p>
    </div>

    <!-- Summary cards -->
    <div class="step-done__cards animate-fadeInUp stagger-1">
      <GlassCard class="summary-card">
        <Layers :size="20" class="summary-card__icon summary-card__icon--green" />
        <h4>Environments</h4>
        <p>{{ onboarding.createdEnvironmentIds.length }} created</p>
      </GlassCard>
      <GlassCard class="summary-card">
        <FolderKanban :size="20" class="summary-card__icon summary-card__icon--cyan" />
        <h4>{{ workspaceName }}</h4>
        <p>Workspace ready</p>
      </GlassCard>
      <GlassCard class="summary-card">
        <Flag :size="20" class="summary-card__icon summary-card__icon--amber" />
        <h4>{{ featureName || 'Feature' }}</h4>
        <p>Created in {{ stages[0]?.name || 'dev' }}</p>
      </GlassCard>
    </div>

    <!-- Promotion CTA -->
    <GlassCard v-if="stages.length >= 2" class="promo-card animate-fadeInUp stagger-2" gradient>
      <h3 class="promo-card__title">Ready to promote your feature?</h3>

      <!-- Mini pipeline -->
      <div class="mini-pipeline">
        <div
          v-for="(stage, idx) in stages"
          :key="idx"
          class="mini-pipeline__stage"
          :class="{ 'mini-pipeline__stage--active': stage.promoted }"
        >
          <div class="mini-pipeline__dot">
            <Check v-if="stage.promoted" :size="12" />
          </div>
          <span class="mini-pipeline__name">{{ stage.name }}</span>
          <div v-if="idx < stages.length - 1" class="mini-pipeline__line" :class="{ 'mini-pipeline__line--filled': stages[idx + 1]?.promoted }" />
        </div>
      </div>

      <button
        v-if="!promoted"
        class="btn btn--primary promo-card__cta"
        :disabled="promoting"
        @click="handlePromote"
      >
        <Loader2 v-if="promoting" :size="16" class="spin" />
        <template v-else>
          Promote to {{ stages[1]?.name || 'next stage' }}
          <ArrowRight :size="16" />
        </template>
      </button>
      <div v-else class="promo-card__success">
        <Check :size="18" />
        Successfully promoted to {{ stages[1]?.name }}!
      </div>
    </GlassCard>

    <!-- Go to dashboard -->
    <button class="btn btn--primary step-done__dashboard animate-fadeInUp stagger-3" @click="goToDashboard">
      Go to Dashboard
      <ArrowRight :size="16" />
    </button>
  </div>
</template>

<style scoped>
.step-done {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 2rem;
}

.step-done__hero { position: relative; }

.step-done__icon { color: var(--accent-amber); margin-bottom: 0.5rem; }

.step-done__title {
  font-size: 2.25rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.step-done__subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
  margin-top: 8px;
}

/* Summary cards */
.step-done__cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  width: 100%;
  max-width: 640px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.summary-card h4 { font-size: 0.95rem; font-weight: 700; }
.summary-card p { font-size: 0.75rem; color: var(--text-muted); }

.summary-card__icon--green { color: var(--accent-emerald); }
.summary-card__icon--cyan { color: var(--accent-cyan); }
.summary-card__icon--amber { color: var(--accent-amber); }

/* Promotion card */
.promo-card {
  width: 100%;
  max-width: 640px;
  padding: 2rem !important;
}

.promo-card__title {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
}

/* Mini pipeline */
.mini-pipeline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 1.5rem;
}

.mini-pipeline__stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
}

.mini-pipeline__dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  transition: all 0.3s ease;
}

.mini-pipeline__stage--active .mini-pipeline__dot {
  background: var(--gradient-accent);
  border-color: var(--accent-cyan);
  color: white;
  box-shadow: 0 0 12px rgba(34, 211, 238, 0.3);
}

.mini-pipeline__name {
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--text-muted);
}

.mini-pipeline__stage--active .mini-pipeline__name {
  color: var(--accent-cyan);
}

.mini-pipeline__line {
  position: absolute;
  top: 14px;
  left: 100%;
  width: 60px;
  height: 2px;
  background: var(--glass-border);
  z-index: -1;
}

.mini-pipeline__line--filled {
  background: var(--gradient-accent);
}

.promo-card__cta { margin: 0 auto; }

.promo-card__success {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--accent-emerald);
  font-weight: 600;
  font-size: 0.9rem;
}

.step-done__dashboard {
  padding: 14px 32px;
  font-size: 1rem;
}

/* Confetti */
.confetti-wrapper {
  position: absolute;
  inset: -40px;
  pointer-events: none;
  overflow: hidden;
}

.confetti-piece {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  top: 50%;
  left: 50%;
  animation: confetti-burst 1.5s ease-out forwards;
  animation-delay: calc(var(--i) * 0.05s);
  opacity: 0;
}

.confetti-piece:nth-child(3n) { background: var(--accent-cyan); }
.confetti-piece:nth-child(3n+1) { background: var(--accent-indigo); }
.confetti-piece:nth-child(3n+2) { background: var(--accent-amber); }

@keyframes confetti-burst {
  0% {
    opacity: 1;
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(
      calc((var(--i) - 10) * 15px),
      calc(-80px + var(--i) * 8px)
    ) rotate(calc(var(--i) * 45deg)) scale(0.5);
  }
}

/* Shared */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  padding: 12px 28px;
  font-size: 0.9rem;
  box-shadow: 0 4px 20px rgba(34, 211, 238, 0.25);
}

.btn--primary:hover:not(:disabled) {
  box-shadow: 0 8px 30px rgba(34, 211, 238, 0.35);
  transform: translateY(-2px);
}

.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }

@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }

@media (max-width: 640px) {
  .step-done__cards { grid-template-columns: 1fr; }
  .mini-pipeline__line { width: 30px; }
}
</style>
