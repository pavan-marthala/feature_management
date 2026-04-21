<script setup lang="ts">
import { 
  GripVertical, 
  Trash2, 
  ShieldCheck, 
  Zap, 
  Calendar,
  ChevronDown,
  Info
} from 'lucide-vue-next'
import type { StageType, Environment } from '@/types'
import { computed } from 'vue'

const props = defineProps<{
  index: number
  environmentId: string
  type: StageType
  scheduleExpression?: string
  environments: Environment[]
}>()

const emit = defineEmits(['update', 'remove'])

const currentEnv = computed(() => 
  props.environments.find(e => e.id === props.environmentId)
)

function updateField(field: string, value: any) {
  emit('update', { [field]: value })
}

function getIcon() {
  switch (props.type) {
    case 'MANUAL': return ShieldCheck
    case 'AUTOMATIC': return Zap
    case 'SCHEDULED': return Calendar
    default: return Info
  }
}

const typeLabel = computed(() => {
  switch (props.type) {
    case 'MANUAL': return 'Manual Approval'
    case 'AUTOMATIC': return 'Automatic Promotion'
    case 'SCHEDULED': return 'Scheduled Release'
    default: return props.type
  }
})
</script>

<template>
  <div class="stage-card glass animate-fadeInUp" :class="[`stage-card--${type.toLowerCase()}`]">
    <div class="stage-card__sidebar">
      <GripVertical :size="18" class="drag-handle" />
      <div class="stage-number">{{ index + 1 }}</div>
    </div>

    <div class="stage-card__main">
      <div class="stage-card__header">
        <div class="stage-identity">
          <div class="stage-icon">
            <component :is="getIcon()" :size="20" />
          </div>
          <div class="stage-info">
            <span class="stage-title">{{ currentEnv?.name || 'New Stage' }}</span>
            <span class="stage-subtitle">{{ typeLabel }}</span>
          </div>
        </div>
        
        <button type="button" class="remove-btn" @click="$emit('remove')" title="Remove Stage">
          <Trash2 :size="16" />
        </button>
      </div>

      <div class="stage-card__body">
        <div class="input-grid">
          <div class="input-group">
            <label class="input-label">Environment</label>
            <div class="select-wrapper">
              <select 
                :value="environmentId" 
                @change="updateField('environmentId', ($event.target as HTMLSelectElement).value)"
                class="premium-select"
              >
                <option value="" disabled>Select Environment</option>
                <option v-for="env in environments" :key="env.id" :value="env.id">
                  {{ env.name }}
                </option>
              </select>
              <ChevronDown :size="16" class="select-chevron" />
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">Promotion Strategy</label>
            <div class="select-wrapper">
              <select 
                :value="type" 
                @change="updateField('type', ($event.target as HTMLSelectElement).value)"
                class="premium-select"
              >
                <option value="MANUAL">Manual Approval</option>
                <option value="AUTOMATIC">Automatic</option>
                <option value="SCHEDULED">Scheduled</option>
              </select>
              <ChevronDown :size="16" class="select-chevron" />
            </div>
          </div>
        </div>

        <div v-if="type === 'SCHEDULED'" class="extra-config animate-fadeIn">
          <label class="input-label">Cron Expression</label>
          <input 
            :value="scheduleExpression" 
            @input="updateField('scheduleExpression', ($event.target as HTMLInputElement).value)"
            type="text" 
            class="premium-input" 
            placeholder="0 0 * * *" 
          />
          <p class="config-hint">Standard cron syntax: min hour day month weekday</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stage-card {
  display: flex;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 1rem;
}

.stage-card:hover {
  border-color: var(--accent-cyan);
  transform: translateX(4px);
  background: rgba(255, 255, 255, 0.05);
  box-shadow: var(--shadow-lg);
}

.stage-card__sidebar {
  width: 48px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 0;
  gap: 12px;
  border-right: 1px solid var(--glass-border);
}

.drag-handle {
  color: var(--text-muted);
  cursor: grab;
  transition: color 0.2s;
}

.drag-handle:hover { color: var(--text-primary); }

.stage-number {
  font-size: 0.8rem;
  font-weight: 800;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.05);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--glass-border);
}

.stage-card__main {
  flex: 1;
  padding: 1.25rem;
}

.stage-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.stage-identity {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stage-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: rgba(34, 211, 238, 0.1);
  color: var(--accent-cyan);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(34, 211, 238, 0.2);
}

.stage-card--manual .stage-icon {
  background: rgba(244, 63, 94, 0.1);
  color: var(--accent-rose);
  border-color: rgba(244, 63, 94, 0.2);
}

.stage-card--automatic .stage-icon {
  background: rgba(16, 185, 129, 0.1);
  color: var(--accent-emerald);
  border-color: rgba(16, 185, 129, 0.2);
}

.stage-info {
  display: flex;
  flex-direction: column;
}

.stage-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stage-subtitle {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.remove-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.remove-btn:hover {
  color: var(--accent-rose);
  background: rgba(244, 63, 94, 0.1);
}

.input-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.input-label {
  display: block;
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  margin-bottom: 6px;
  letter-spacing: 0.05em;
}

.select-wrapper {
  position: relative;
}

.premium-select {
  width: 100%;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  color: var(--text-primary);
  font-size: 0.875rem;
  font-family: inherit;
  appearance: none;
  cursor: pointer;
  transition: all 0.2s;
}

.premium-select:focus {
  border-color: var(--accent-cyan);
  outline: none;
  background: rgba(0, 0, 0, 0.4);
}

.select-chevron {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

.extra-config {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--glass-border);
}

.premium-input {
  width: 100%;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  padding: 10px 14px;
  color: var(--text-primary);
  font-size: 0.875rem;
  font-family: inherit;
  transition: all 0.2s;
}

.premium-input:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

.config-hint {
  font-size: 0.7rem;
  color: var(--text-muted);
  margin-top: 6px;
  font-style: italic;
}

@media (max-width: 640px) {
  .input-grid { grid-template-columns: 1fr; }
}
</style>
