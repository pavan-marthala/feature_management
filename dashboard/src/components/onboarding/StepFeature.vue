<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useOnboardingStore } from '@/stores/onboardingStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { workflowService } from '@/services/workflowService'
import { featureService } from '@/services/featureService'
import GlassCard from '@/components/ui/GlassCard.vue'
import {
  Flag, ArrowRight, Loader2, AlertCircle, Layers, Plus, Minus, X
} from 'lucide-vue-next'
import type { FeatureConfiguration, FeatureStrategyType, FeatureCreateRequest } from '@/types'

const emit = defineEmits<{ next: [] }>()
const onboarding = useOnboardingStore()
const envStore = useEnvironmentStore()

const name = ref('')
const description = ref('')
const enabled = ref(true)
const creating = ref(false)
const errorMsg = ref('')
const firstEnvName = ref('first stage')
const firstEnvId = ref('')

const owners = ref<string[]>([])
const newOwner = ref('')

const strategy = ref<FeatureStrategyType>('BooleanFeatureStrategy')
const booleanValue = ref(false)

const jwtClaimType = ref<'scopes' | 'roles' | 'custom'>('scopes')
const jwtScopes = ref<string[]>([''])
const jwtRoles = ref<string[]>([''])
const jwtCustomClaims = ref<{ name: string; value: string }[]>([{ name: '', value: '' }])

const httpType = ref<'header' | 'requestBody' | 'query'>('header')
const httpName = ref('')
const httpValue = ref('')
const httpPath = ref('')

const cronExpression = ref('')

onMounted(async () => {
  await envStore.fetchEnvironments(0, 100)
  const wfId = onboarding.createdWorkflowId
  if (wfId) {
    try {
      const wf = await workflowService.getWorkflow(wfId)
      if (wf?.stages?.length) {
        const sorted = [...wf.stages].sort((a, b) => a.orderIndex - b.orderIndex)
        const firstStage = sorted[0]
        if (firstStage) {
          firstEnvId.value = firstStage.environmentId
          firstEnvName.value = firstStage.environmentName || envStore.environments.find(e => e.id === firstStage.environmentId)?.name || 'first stage'
        }
      }
    } catch {
      // fallback
    }
  }
})

const stageLabel = computed(() => firstEnvName.value)

function addOwner() {
  if (newOwner.value.trim() && !owners.value.includes(newOwner.value.trim())) {
    owners.value.push(newOwner.value.trim())
    newOwner.value = ''
  }
}
function removeOwner(idx: number) { owners.value.splice(idx, 1) }

function addJwtScope() { jwtScopes.value.push('') }
function removeJwtScope(idx: number) { jwtScopes.value.splice(idx, 1) }
function addJwtRole() { jwtRoles.value.push('') }
function removeJwtRole(idx: number) { jwtRoles.value.splice(idx, 1) }
function addJwtCustom() { jwtCustomClaims.value.push({ name: '', value: '' }) }
function removeJwtCustom(idx: number) { jwtCustomClaims.value.splice(idx, 1) }

function buildConfiguration(): FeatureConfiguration {
  switch (strategy.value) {
    case 'BooleanFeatureStrategy':
      return { strategy: 'BooleanFeatureStrategy', value: booleanValue.value }
    case 'JWTClaimFeatureStrategy': {
      let claims: any[]
      if (jwtClaimType.value === 'scopes') {
        claims = [{ scopes: jwtScopes.value.filter(s => s.trim()) }]
      } else if (jwtClaimType.value === 'roles') {
        claims = [{ roles: jwtRoles.value.filter(r => r.trim()) }]
      } else {
        claims = [jwtCustomClaims.value.filter(c => c.name.trim())]
      }
      return { strategy: 'JWTClaimFeatureStrategy', claims }
    }
    case 'HTTPRequestFeatureStrategy': {
      const config: any = { strategy: 'HTTPRequestFeatureStrategy' }
      if (httpType.value === 'header') {
        config.header = { name: httpName.value, value: httpValue.value }
      } else if (httpType.value === 'requestBody') {
        config.requestBody = { path: httpPath.value, value: httpValue.value }
      } else {
        config.query = { name: httpName.value, value: httpValue.value }
      }
      return config
    }
    case 'ScheduleFeatureStrategy':
      return { strategy: 'ScheduleFeatureStrategy', cron: cronExpression.value }
  }
}

async function handleCreate() {
  if (!name.value.trim()) {
    errorMsg.value = 'Feature name is required'
    return
  }
  if (!firstEnvId.value) {
    errorMsg.value = 'Could not determine target environment'
    return
  }
  if (!onboarding.createdWorkspaceId) {
    errorMsg.value = 'No workspace selected'
    return
  }

  creating.value = true
  errorMsg.value = ''
  try {
    const config = buildConfiguration()
    const payload: FeatureCreateRequest = {
      name: name.value.trim(),
      description: description.value.trim() || undefined,
      environmentId: firstEnvId.value,
      workspaceId: onboarding.createdWorkspaceId,
      configuration: config,
      owners: owners.value.length ? owners.value : undefined,
      enabled: enabled.value,
    }
    const result = await featureService.createFeature(payload)
    onboarding.setFeatureId(result.id)
    emit('next')
  } catch {
    errorMsg.value = 'Failed to create feature. Please try again.'
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="step-feat">
    <div class="step-feat__form animate-fadeInUp">
      <h2 class="step-feat__title">
        <Flag :size="24" class="step-feat__title-icon" />
        Create your first feature flag
      </h2>
      <p class="step-feat__desc">
        This feature will be created in your first environment (<strong>{{ stageLabel }}</strong>).
        You'll promote it through your pipeline next.
      </p>

      <!-- Context badge -->
      <GlassCard class="step-feat__context" padding="12px 16px">
        <div class="context-row">
          <Layers :size="16" />
          <span>Creating in: <strong>{{ stageLabel }}</strong> (Stage 1)</span>
        </div>
      </GlassCard>

      <div class="step-feat__field">
        <label class="step-feat__label">Feature Name *</label>
        <input
          v-model="name"
          type="text"
          class="step-feat__input"
          placeholder="e.g., DarkMode"
          :disabled="creating"
        />
      </div>

      <div class="step-feat__field">
        <label class="step-feat__label">Description <span class="optional">(optional)</span></label>
        <input
          v-model="description"
          type="text"
          class="step-feat__input"
          placeholder="Enable dark mode for beta users"
          :disabled="creating"
        />
      </div>

      <div class="step-feat__field">
        <label class="step-feat__label">Enabled</label>
        <label class="toggle">
          <input v-model="enabled" type="checkbox" class="toggle__input" />
          <span class="toggle__slider" />
          <span class="toggle__text">{{ enabled ? 'On' : 'Off' }}</span>
        </label>
      </div>

      <!-- Strategy Configuration -->
      <div class="step-feat__field">
        <label class="step-feat__label">Strategy Type *</label>
        <select v-model="strategy" class="step-feat__input form-select">
          <option value="BooleanFeatureStrategy">Boolean Toggle</option>
          <option value="JWTClaimFeatureStrategy">JWT Claim</option>
          <option value="HTTPRequestFeatureStrategy">HTTP Request</option>
          <option value="ScheduleFeatureStrategy">Schedule (Cron)</option>
        </select>
      </div>

      <!-- Boolean Strategy Config -->
      <div v-if="strategy === 'BooleanFeatureStrategy'" class="strategy-config">
        <div class="step-feat__field">
          <label class="step-feat__label">Default Value</label>
          <label class="toggle">
            <input v-model="booleanValue" type="checkbox" class="toggle__input" />
            <span class="toggle__slider" />
            <span class="toggle__text">{{ booleanValue ? 'True' : 'False' }}</span>
          </label>
        </div>
      </div>

      <!-- JWT Claim Strategy Config -->
      <div v-else-if="strategy === 'JWTClaimFeatureStrategy'" class="strategy-config">
        <div class="step-feat__field">
          <label class="step-feat__label">Claim Type</label>
          <select v-model="jwtClaimType" class="step-feat__input form-select">
            <option value="scopes">Scopes</option>
            <option value="roles">Roles</option>
            <option value="custom">Custom Claims</option>
          </select>
        </div>

        <div v-if="jwtClaimType === 'scopes'" class="dynamic-list">
          <label class="step-feat__label mt-2">Scopes</label>
          <div v-for="(_, idx) in jwtScopes" :key="idx" class="dynamic-list__row">
            <input v-model="jwtScopes[idx]" class="step-feat__input" placeholder="e.g., read:features" />
            <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtScope(idx)" v-if="jwtScopes.length > 1">
              <Minus :size="16" />
            </button>
          </div>
          <button type="button" class="btn btn--ghost btn--sm mt-2" @click="addJwtScope">
            <Plus :size="16" /> Add Scope
          </button>
        </div>

        <div v-else-if="jwtClaimType === 'roles'" class="dynamic-list">
          <label class="step-feat__label mt-2">Roles</label>
          <div v-for="(_, idx) in jwtRoles" :key="idx" class="dynamic-list__row">
            <input v-model="jwtRoles[idx]" class="step-feat__input" placeholder="e.g., admin" />
            <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtRole(idx)" v-if="jwtRoles.length > 1">
              <Minus :size="16" />
            </button>
          </div>
          <button type="button" class="btn btn--ghost btn--sm mt-2" @click="addJwtRole">
            <Plus :size="16" /> Add Role
          </button>
        </div>

        <div v-else class="dynamic-list">
          <label class="step-feat__label mt-2">Custom Claims</label>
          <div v-for="(claim, idx) in jwtCustomClaims" :key="idx" class="dynamic-list__row">
            <input v-model="claim.name" class="step-feat__input" placeholder="Claim name" />
            <input v-model="claim.value" class="step-feat__input" placeholder="Claim value" />
            <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtCustom(idx)" v-if="jwtCustomClaims.length > 1">
              <Minus :size="16" />
            </button>
          </div>
          <button type="button" class="btn btn--ghost btn--sm mt-2" @click="addJwtCustom">
            <Plus :size="16" /> Add Claim
          </button>
        </div>
      </div>

      <!-- HTTP Request Strategy Config -->
      <div v-else-if="strategy === 'HTTPRequestFeatureStrategy'" class="strategy-config">
        <div class="step-feat__field">
          <label class="step-feat__label">Match Type</label>
          <select v-model="httpType" class="step-feat__input form-select">
            <option value="header">HTTP Header</option>
            <option value="requestBody">Request Body</option>
            <option value="query">Query Parameter</option>
          </select>
        </div>

        <div class="step-feat__field" v-if="httpType !== 'requestBody'">
          <label class="step-feat__label">{{ httpType === 'header' ? 'Header' : 'Parameter' }} Name *</label>
          <input
            v-model="httpName"
            class="step-feat__input"
            :placeholder="httpType === 'header' ? 'X-Feature-Flag' : 'feature'"
          />
        </div>

        <div class="step-feat__field" v-if="httpType === 'requestBody'">
          <label class="step-feat__label">JSON Path *</label>
          <input
            v-model="httpPath"
            class="step-feat__input"
            placeholder="$.user.role"
          />
        </div>

        <div class="step-feat__field">
          <label class="step-feat__label">Value *</label>
          <input
            v-model="httpValue"
            class="step-feat__input"
            placeholder="Expected value"
          />
        </div>
      </div>

      <!-- Schedule Strategy Config -->
      <div v-else-if="strategy === 'ScheduleFeatureStrategy'" class="strategy-config">
        <div class="step-feat__field">
          <label class="step-feat__label">Cron Expression *</label>
          <input
            v-model="cronExpression"
            class="step-feat__input"
            placeholder="0 0 * * MON-FRI"
          />
        </div>
      </div>

      <!-- Owners -->
      <div class="step-feat__field">
        <label class="step-feat__label">Owners</label>
        <div class="owners-input">
          <input
            v-model="newOwner"
            class="step-feat__input"
            placeholder="Add owner..."
            @keydown.enter.prevent="addOwner"
          />
          <button type="button" class="btn btn--ghost btn--sm ml-2" @click="addOwner">
            <Plus :size="16" /> Add
          </button>
        </div>
        <div v-if="owners.length" class="owner-chips">
          <div v-for="(owner, idx) in owners" :key="idx" class="owner-chip">
            <span>{{ owner }}</span>
            <button type="button" class="owner-chip__remove" @click="removeOwner(idx)">
              <X :size="14" />
            </button>
          </div>
        </div>
      </div>

      <div v-if="errorMsg" class="step-feat__error">
        <AlertCircle :size="14" />
        {{ errorMsg }}
      </div>

      <button
        class="btn btn--primary step-feat__cta"
        :disabled="creating || !name.trim()"
        @click="handleCreate"
      >
        <Loader2 v-if="creating" :size="16" class="spin" />
        <template v-else>
          Create Feature
          <ArrowRight :size="16" />
        </template>
      </button>
    </div>
  </div>
</template>

<style scoped>
.step-feat { max-width: 560px; }

.step-feat__title {
  font-size: 1.75rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.step-feat__title-icon { color: var(--accent-amber); }

.step-feat__desc {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-top: 8px;
}

.step-feat__context {
  margin-top: 1.25rem;
  border-color: rgba(34, 211, 238, 0.2) !important;
  background: rgba(34, 211, 238, 0.04) !important;
}

.context-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.context-row strong { color: var(--accent-cyan); }

.step-feat__field { margin-top: 1.5rem; }

.step-feat__label {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted);
  display: block;
  margin-bottom: 8px;
}

.mt-2 { margin-top: 0.5rem; }
.ml-2 { margin-left: 0.5rem; }

.optional { font-weight: 400; text-transform: none; }

.step-feat__input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: inherit;
  font-size: 0.9rem;
}

.step-feat__input:focus {
  border-color: var(--accent-cyan);
  outline: none;
}

.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%2394a3b8' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

.form-select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.strategy-config {
  margin-top: 1rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.dynamic-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dynamic-list__row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.owners-input {
  display: flex;
  gap: 8px;
}

.owner-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.owner-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(34, 211, 238, 0.1);
  border: 1px solid rgba(34, 211, 238, 0.2);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  color: var(--text-primary);
}

.owner-chip__remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  display: flex;
  align-items: center;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.owner-chip__remove:hover {
  background: rgba(251, 113, 133, 0.2);
  color: var(--accent-rose);
}

.icon-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.icon-btn--danger:hover {
  background: rgba(251, 113, 133, 0.1);
  color: var(--accent-rose);
}

/* Toggle */
.toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.toggle__input { display: none; }

.toggle__slider {
  width: 44px;
  height: 24px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  position: relative;
  transition: background 0.2s ease;
}

.toggle__slider::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  transition: transform 0.2s ease;
}

.toggle__input:checked + .toggle__slider {
  background: var(--accent-emerald);
}

.toggle__input:checked + .toggle__slider::after {
  transform: translateX(20px);
}

.toggle__text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.step-feat__error {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 1rem;
  color: var(--accent-rose);
  font-size: 0.8rem;
}

.step-feat__cta {
  margin-top: 2rem;
  padding: 12px 28px;
  font-size: 0.9rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.2);
}

.btn--primary:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(34, 211, 238, 0.3);
  transform: translateY(-1px);
}

.btn--primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn--ghost {
  background: transparent;
  color: var(--text-secondary);
}

.btn--ghost:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.btn--sm {
  padding: 6px 12px;
  font-size: 0.75rem;
}

@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin 1s linear infinite; }
</style>
