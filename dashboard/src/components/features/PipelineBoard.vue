<script setup lang="ts">
import { computed } from 'vue'
import { 
  CheckCircle2, 
  Circle, 
  Clock, 
  ArrowRight,
  ShieldCheck,
  Zap,
  Calendar,
  AlertCircle
} from 'lucide-vue-next'
import type { Stage, StageType } from '@/types'

const props = defineProps<{
  stages: Stage[]
  currentEnvironmentId?: string
  loading?: boolean
}>()

const emit = defineEmits(['promote'])

function getStageIcon(type: StageType) {
  switch (type) {
    case 'MANUAL': return ShieldCheck
    case 'AUTOMATIC': return Zap
    case 'SCHEDULED': return Calendar
    default: return Circle
  }
}

function getStageStatus(stage: Stage, index: number) {
  if (!props.currentEnvironmentId) return 'pending'
  
  const currentIndex = props.stages.findIndex(s => s.environmentId === props.currentEnvironmentId)
  
  if (currentIndex === -1) return 'pending'
  if (index < currentIndex) return 'completed'
  if (index === currentIndex) return 'current'
  return 'pending'
}

function handlePromote() {
  emit('promote')
}

const progressWidth = computed(() => {
  if (!props.currentEnvironmentId || props.stages.length <= 1) return '0%'
  const currentIndex = props.stages.findIndex(s => s.environmentId === props.currentEnvironmentId)
  if (currentIndex === -1) return '0%'
  return `${(currentIndex / (props.stages.length - 1)) * 100}%`
})
</script>

<template>
  <div class="pipeline-board" :class="{ 'pipeline-board--loading': loading }">
    <div class="pipeline-header">
      <h3 class="pipeline-title">Propagation Pipeline</h3>
      <div v-if="loading" class="pipeline-loader">
        <div class="spinner-sm"></div>
        <span>Propagating...</span>
      </div>
    </div>

    <div class="pipeline-viewport">
      <div class="pipeline-track">
        <!-- Progress Line Background -->
        <div class="pipeline-line"></div>
        <!-- Active Progress Line -->
        <div class="pipeline-line pipeline-line--active" :style="{ width: progressWidth }"></div>

        <div class="pipeline-steps">
          <div 
            v-for="(stage, index) in stages" 
            :key="stage.id || index" 
            class="pipeline-step"
            :class="[`pipeline-step--${getStageStatus(stage, index)}`]"
          >
            <!-- Step Node -->
            <div class="pipeline-node">
              <div class="node-outer">
                <div class="node-inner">
                  <CheckCircle2 v-if="getStageStatus(stage, index) === 'completed'" :size="18" />
                  <component :is="getStageIcon(stage.type)" v-else :size="18" />
                </div>
                
                <!-- Pulsing ring for current stage -->
                <div v-if="getStageStatus(stage, index) === 'current'" class="node-pulse"></div>
              </div>
              
              <div class="node-content">
                <span class="node-env">{{ stage.environmentName || 'Environment' }}</span>
                <span class="node-label">{{ stage.type.toLowerCase() }}</span>
              </div>

              <!-- Action Area -->
              <div class="node-action" v-if="getStageStatus(stage, index) === 'current' && index < stages.length - 1">
                <button class="promote-trigger" @click="handlePromote" :disabled="loading">
                  <span>Promote</span>
                  <ArrowRight :size="14" />
                </button>
              </div>
              
              <!-- Status Tooltip/Indicator -->
              <div class="node-status" v-if="getStageStatus(stage, index) === 'completed'">
                <div class="status-dot status-dot--success"></div>
                Synced
              </div>
              <div class="node-status" v-else-if="getStageStatus(stage, index) === 'current'">
                <div class="status-dot status-dot--active"></div>
                Active
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pipeline-board {
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-xl);
  border: 1px solid var(--glass-border);
  position: relative;
}

.pipeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
}

.pipeline-title {
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--text-muted);
}

.pipeline-loader {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--accent-cyan);
}

.pipeline-viewport {
  padding: 1rem 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.pipeline-viewport::-webkit-scrollbar {
  display: none;
}

.pipeline-track {
  position: relative;
  min-width: 600px;
  padding-bottom: 2rem;
}

.pipeline-line {
  position: absolute;
  top: 24px;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-full);
  z-index: 1;
}

.pipeline-line--active {
  background: var(--gradient-accent);
  width: 0;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 15px rgba(34, 211, 238, 0.3);
}

.pipeline-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.pipeline-step {
  flex: 1;
  display: flex;
  justify-content: center;
}

.pipeline-step:first-child { justify-content: flex-start; }
.pipeline-step:last-child { justify-content: flex-end; }

.pipeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 140px;
  transition: all 0.3s ease;
}

.node-outer {
  position: relative;
  width: 48px;
  height: 48px;
  z-index: 10;
}

.node-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 3px solid var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
}

.node-pulse {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 2px solid var(--accent-cyan);
  opacity: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); opacity: 0.5; }
  100% { transform: scale(1.3); opacity: 0; }
}

.node-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.node-env {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.node-label {
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.node-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  background: rgba(0, 0, 0, 0.2);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  margin-top: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot--success { background: var(--accent-emerald); }
.status-dot--active { background: var(--accent-cyan); box-shadow: 0 0 8px var(--accent-cyan); }

/* Variants */
.pipeline-step--completed .node-inner {
  background: rgba(52, 211, 153, 0.1);
  border-color: var(--accent-emerald);
  color: var(--accent-emerald);
}

.pipeline-step--completed .node-env { color: var(--accent-emerald); }

.pipeline-step--current .node-inner {
  background: rgba(34, 211, 238, 0.1);
  border-color: var(--accent-cyan);
  color: var(--accent-cyan);
  transform: scale(1.15);
}

.pipeline-step--current .node-env { color: var(--accent-cyan); font-size: 1rem; }

/* Action Button */
.node-action {
  position: absolute;
  top: 100px;
  z-index: 20;
}

.promote-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--gradient-accent);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(34, 211, 238, 0.3);
}

.promote-trigger:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(34, 211, 238, 0.4);
}

.promote-trigger:active:not(:disabled) {
  transform: scale(0.95);
}

.promote-trigger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(1);
}

@media (max-width: 640px) {
  .pipeline-track { min-width: 100%; }
  .pipeline-node { width: 120px; }
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(34, 211, 238, 0.2);
  border-top-color: var(--accent-cyan);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
