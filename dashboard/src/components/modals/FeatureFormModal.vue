<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useFeatureStore } from '@/stores/featureStore'
import { useWorkflowStore } from '@/stores/workflowStore'
import { useWorkspaceStore } from '@/stores/workspaceStore'
import { useEnvironmentStore } from '@/stores/environmentStore'
import { useUiStore } from '@/stores/uiStore'
import Modal from '@/components/ui/Modal.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import { Plus, Minus, X } from 'lucide-vue-next'
import type { FeatureStrategyType, FeatureCreateRequest, FeatureConfiguration, JWTClaimScope, JWTClaimRole, JWTClaimCustom } from '@/types'

const featureStore = useFeatureStore()
const workflowStore = useWorkflowStore()
const workspaceStore = useWorkspaceStore()
const envStore = useEnvironmentStore()
const uiStore = useUiStore()

const isEdit = computed(() => !!uiStore.editingFeatureId)
const workspaceId = computed(() => workspaceStore.activeWorkspaceId)

// Form state
const name = ref('')
const description = ref('')
const strategy = ref<FeatureStrategyType>('BooleanFeatureStrategy')
const enabled = ref(true)
const selectedWorkflowId = ref('')
const owners = ref<string[]>([])
const newOwner = ref('')

// Boolean strategy
const booleanValue = ref(false)

// JWT Claim strategy
const jwtClaimType = ref<'scopes' | 'roles' | 'custom'>('scopes')
const jwtScopes = ref<string[]>([''])
const jwtRoles = ref<string[]>([''])
const jwtCustomClaims = ref<{ name: string; value: string }[]>([{ name: '', value: '' }])

// HTTP Request strategy
const httpType = ref<'header' | 'requestBody' | 'query'>('header')
const httpName = ref('')
const httpValue = ref('')
const httpPath = ref('')

// Schedule strategy
const cronExpression = ref('')

const submitting = ref(false)
const errors = ref<Record<string, string>>({})

watch(selectedWorkflowId, async (newId) => {
  if (newId && newId !== workflowStore.selectedWorkflow?.id) {
    await workflowStore.fetchWorkflow(newId)
  }
})

const fullSelectedWorkflow = computed(() => {
  if (workflowStore.selectedWorkflow?.id === selectedWorkflowId.value) {
    return workflowStore.selectedWorkflow
  }
  return null
})

const initialEnvironment = computed(() => {
  const wf = fullSelectedWorkflow.value
  if (!wf) return null
  if (!wf.stages || wf.stages.length === 0) return null
  const firstStage = wf.stages.find(s => s.orderIndex === 0) || wf.stages[0]
  if (!firstStage) return null
  const env = envStore.environments.find(e => e.id === firstStage.environmentId)
  return env || { name: firstStage.environmentName || 'Unknown Environment', id: firstStage.environmentId }
})

watch(() => uiStore.featureModalOpen, async (open) => {
  if (open) {
    if (workflowStore.workflows.length === 0) {
      await workflowStore.fetchWorkflows(0, 100)
    }
    if (envStore.environments.length === 0) {
      await envStore.fetchEnvironments(0, 100)
    }
    
    if (isEdit.value && uiStore.editingFeatureId) {
      const feature = await featureStore.fetchFeature(uiStore.editingFeatureId, 'ID')
      if (feature) {
        name.value = feature.name
        description.value = feature.description || ''
        enabled.value = feature.enabled
        owners.value = feature.owners || []

        if (feature.configuration) {
          strategy.value = feature.configuration.strategy
          switch (feature.configuration.strategy) {
            case 'BooleanFeatureStrategy':
              booleanValue.value = feature.configuration.value
              break
            case 'JWTClaimFeatureStrategy': {
              const claims = feature.configuration.claims
              if (claims?.[0]) {
                if ('scopes' in claims[0]) {
                  jwtClaimType.value = 'scopes'
                  jwtScopes.value = claims[0].scopes
                } else if ('roles' in claims[0]) {
                  jwtClaimType.value = 'roles'
                  jwtRoles.value = claims[0].roles
                } else {
                  jwtClaimType.value = 'custom'
                  jwtCustomClaims.value = Array.isArray(claims[0]) ? claims[0] : [claims[0]]
                }
              }
              break
            }
            case 'HTTPRequestFeatureStrategy': {
              const config = feature.configuration
              if (config.header) {
                httpType.value = 'header'
                httpName.value = config.header.name
                httpValue.value = config.header.value
              } else if (config.requestBody) {
                httpType.value = 'requestBody'
                httpPath.value = config.requestBody.path
                httpValue.value = config.requestBody.value
              } else if (config.query) {
                httpType.value = 'query'
                httpName.value = config.query.name
                httpValue.value = config.query.value
              }
              break
            }
            case 'ScheduleFeatureStrategy':
              cronExpression.value = feature.configuration.cron || ''
              break
          }
        }
      }
    } else {
      // Reset form on create
      name.value = ''
      description.value = ''
      strategy.value = 'BooleanFeatureStrategy'
      enabled.value = true
      selectedWorkflowId.value = ''
      owners.value = []
      newOwner.value = ''
      booleanValue.value = false
      jwtScopes.value = ['']
      jwtRoles.value = ['']
      jwtCustomClaims.value = [{ name: '', value: '' }]
      httpType.value = 'header'
      httpName.value = ''
      httpValue.value = ''
      httpPath.value = ''
      cronExpression.value = ''
      errors.value = {}
    }
  }
})

function close() {
  uiStore.closeFeatureModal()
}

function validate(): boolean {
  errors.value = {}

  if (!name.value || name.value.length < 2 || name.value.length > 36) {
    errors.value.name = 'Name must be 2-36 characters'
  }
  if (!isEdit.value && !selectedWorkflowId.value) {
    errors.value.workflowId = 'Workflow is required'
  } else if (!isEdit.value && fullSelectedWorkflow.value) {
    if (!fullSelectedWorkflow.value.stages || fullSelectedWorkflow.value.stages.length === 0) {
      errors.value.workflowId = 'Workflow must contain at least one stage before creating features.'
    }
  }
  if (name.value && !/^[a-zA-Z0-9]+$/.test(name.value)) {
    errors.value.name = 'Name can only contain letters and numbers'
  }
  if (description.value && description.value.length > 255) {
    errors.value.description = 'Description must be 255 characters or less'
  }

  if (strategy.value === 'ScheduleFeatureStrategy' && !cronExpression.value) {
    errors.value.cron = 'Cron expression is required'
  }

  if (strategy.value === 'HTTPRequestFeatureStrategy') {
    if (httpType.value === 'requestBody') {
      if (!httpPath.value) errors.value.httpPath = 'Path is required'
      if (!httpValue.value) errors.value.httpValue = 'Value is required'
    } else {
      if (!httpName.value) errors.value.httpName = 'Name is required'
      if (!httpValue.value) errors.value.httpValue = 'Value is required'
    }
  }

  return Object.keys(errors.value).length === 0
}

function buildConfiguration(): FeatureConfiguration {
  switch (strategy.value) {
    case 'BooleanFeatureStrategy':
      return { strategy: 'BooleanFeatureStrategy', value: booleanValue.value }
    case 'JWTClaimFeatureStrategy': {
      let claims: Array<JWTClaimScope | JWTClaimRole | JWTClaimCustom[]>
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
      const config: {
        strategy: 'HTTPRequestFeatureStrategy'
        header?: { name: string; value: string }
        requestBody?: { path: string; value: string }
        query?: { name: string; value: string }
      } = { strategy: 'HTTPRequestFeatureStrategy' }
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

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true

  try {
    if (isEdit.value && uiStore.editingFeatureId) {
      const data = buildConfiguration() as unknown as Record<string, unknown>
      await featureStore.updateFeature(uiStore.editingFeatureId, data, featureStore.selectedEtag)
      close()
    } else {
      if (!initialEnvironment.value?.id) {
        errors.value.workflowId = 'Workflow must contain at least one stage before creating features.'
        submitting.value = false
        return
      }
      
      const payload: FeatureCreateRequest = {
        name: name.value,
        description: description.value || undefined,
        environmentId: initialEnvironment.value.id,
        workspaceId: workspaceId.value!,
        workflowId: selectedWorkflowId.value,
        configuration: buildConfiguration(),
        owners: owners.value.length ? owners.value : undefined,
        enabled: enabled.value,
      }
      await featureStore.createFeature(payload)
      close()
    }
  } catch (err: unknown) {
    console.error('Submit failed', err)
  } finally {
    submitting.value = false
  }
}

function addOwner() {
  if (newOwner.value.trim() && !owners.value.includes(newOwner.value.trim())) {
    owners.value.push(newOwner.value.trim())
    newOwner.value = ''
  }
}

function removeOwner(idx: number) {
  owners.value.splice(idx, 1)
}

function addJwtScope() { jwtScopes.value.push('') }
function removeJwtScope(idx: number) { jwtScopes.value.splice(idx, 1) }
function addJwtRole() { jwtRoles.value.push('') }
function removeJwtRole(idx: number) { jwtRoles.value.splice(idx, 1) }
function addJwtCustom() { jwtCustomClaims.value.push({ name: '', value: '' }) }
function removeJwtCustom(idx: number) { jwtCustomClaims.value.splice(idx, 1) }
</script>

<template>
  <Modal :show="uiStore.featureModalOpen" :title="isEdit ? 'Edit Feature' : 'Create Feature'" size="lg" @close="close">
    <form @submit.prevent="handleSubmit" class="form">
      <div class="form-section">
        <h3 class="section-title">Basic Information</h3>

        <div class="form-group">
          <label class="form-label" for="feature-name">Feature Name *</label>
          <input
            id="feature-name"
            v-model="name"
            type="text"
            class="form-input"
            :class="{ 'form-input--error': errors.name }"
            placeholder="e.g., DarkMode, BetaAccess"
            :disabled="isEdit"
            maxlength="36"
          />
          <span v-if="errors.name" class="form-error">{{ errors.name }}</span>
        </div>

        <div class="form-group" v-if="!isEdit">
          <label class="form-label" for="workflow-select">Workflow *</label>
          <select
            id="workflow-select"
            v-model="selectedWorkflowId"
            class="form-input form-select"
            :class="{ 'form-input--error': errors.workflowId }"
          >
            <option value="" disabled>Select a workflow...</option>
            <option
              v-for="wf in workflowStore.workflows"
              :key="wf.id"
              :value="wf.id"
            >
              {{ wf.name }}
            </option>
          </select>
          <span v-if="errors.workflowId" class="form-error">{{ errors.workflowId }}</span>
          <span class="form-hint" v-if="!initialEnvironment && !errors.workflowId">The deployment pipeline this feature will follow.</span>
        </div>

        <div class="form-group" v-if="!isEdit && initialEnvironment">
          <label class="form-label">Initial Environment</label>
          <div class="readonly-badge">
            {{ initialEnvironment.name }}
          </div>
          <span class="form-hint">Derived automatically from the selected workflow's first stage.</span>
        </div>

        <div class="form-group" v-if="!isEdit">
          <label class="form-label" for="feature-desc">Description</label>
          <textarea
            id="feature-desc"
            v-model="description"
            class="form-input form-textarea"
            placeholder="Describe what this feature flag controls..."
            rows="3"
            maxlength="255"
          ></textarea>
        </div>

        <div class="form-group form-group--inline" v-if="!isEdit">
          <label class="form-label">Enabled</label>
          <ToggleSwitch v-model="enabled" size="md" />
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">Strategy Configuration</h3>
        
        <div class="form-group">
          <label class="form-label" for="strategy-select">Strategy Type *</label>
          <select id="strategy-select" v-model="strategy" class="form-input form-select">
            <option value="BooleanFeatureStrategy">Boolean Toggle</option>
            <option value="JWTClaimFeatureStrategy">JWT Claim</option>
            <option value="HTTPRequestFeatureStrategy">HTTP Request</option>
            <option value="ScheduleFeatureStrategy">Schedule (Cron)</option>
          </select>
        </div>

        <!-- Strategy Config Blocks -->
        <div v-if="strategy === 'BooleanFeatureStrategy'" class="strategy-config">
          <div class="form-group form-group--inline">
            <label class="form-label">Default Value</label>
            <ToggleSwitch v-model="booleanValue" size="md" />
            <span class="form-hint">{{ booleanValue ? 'True' : 'False' }}</span>
          </div>
        </div>

        <div v-else-if="strategy === 'JWTClaimFeatureStrategy'" class="strategy-config">
          <div class="form-group">
            <label class="form-label">Claim Type</label>
            <select v-model="jwtClaimType" class="form-input form-select">
              <option value="scopes">Scopes</option>
              <option value="roles">Roles</option>
              <option value="custom">Custom Claims</option>
            </select>
          </div>

          <!-- Dynamic Lists -->
          <div v-if="jwtClaimType === 'scopes'" class="dynamic-list">
            <label class="form-label">Scopes</label>
            <div v-for="(_, idx) in jwtScopes" :key="idx" class="dynamic-list__row">
              <input v-model="jwtScopes[idx]" class="form-input" placeholder="e.g., read:features" />
              <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtScope(idx)" v-if="jwtScopes.length > 1">
                <Minus :size="16" />
              </button>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" @click="addJwtScope">
              <Plus :size="16" /> Add Scope
            </button>
          </div>

          <div v-else-if="jwtClaimType === 'roles'" class="dynamic-list">
            <label class="form-label">Roles</label>
            <div v-for="(_, idx) in jwtRoles" :key="idx" class="dynamic-list__row">
              <input v-model="jwtRoles[idx]" class="form-input" placeholder="e.g., admin" />
              <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtRole(idx)" v-if="jwtRoles.length > 1">
                <Minus :size="16" />
              </button>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" @click="addJwtRole">
              <Plus :size="16" /> Add Role
            </button>
          </div>

          <div v-else class="dynamic-list">
            <label class="form-label">Custom Claims</label>
            <div v-for="(claim, idx) in jwtCustomClaims" :key="idx" class="dynamic-list__row">
              <input v-model="claim.name" class="form-input" placeholder="Claim name" />
              <input v-model="claim.value" class="form-input" placeholder="Claim value" />
              <button type="button" class="icon-btn icon-btn--danger" @click="removeJwtCustom(idx)" v-if="jwtCustomClaims.length > 1">
                <Minus :size="16" />
              </button>
            </div>
            <button type="button" class="btn btn--ghost btn--sm" @click="addJwtCustom">
              <Plus :size="16" /> Add Claim
            </button>
          </div>
        </div>

        <div v-else-if="strategy === 'HTTPRequestFeatureStrategy'" class="strategy-config">
          <div class="form-group">
            <label class="form-label">Match Type</label>
            <select v-model="httpType" class="form-input form-select">
              <option value="header">HTTP Header</option>
              <option value="requestBody">Request Body</option>
              <option value="query">Query Parameter</option>
            </select>
          </div>

          <div class="form-group" v-if="httpType !== 'requestBody'">
            <label class="form-label">{{ httpType === 'header' ? 'Header' : 'Parameter' }} Name *</label>
            <input
              v-model="httpName"
              class="form-input"
              :class="{ 'form-input--error': errors.httpName }"
              :placeholder="httpType === 'header' ? 'X-Feature-Flag' : 'feature'"
            />
          </div>
          <div class="form-group" v-if="httpType === 'requestBody'">
            <label class="form-label">JSON Path *</label>
            <input
              v-model="httpPath"
              class="form-input"
              :class="{ 'form-input--error': errors.httpPath }"
              placeholder="$.user.role"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Value *</label>
            <input
              v-model="httpValue"
              class="form-input"
              :class="{ 'form-input--error': errors.httpValue }"
              placeholder="Expected value"
            />
          </div>
        </div>

        <div v-else-if="strategy === 'ScheduleFeatureStrategy'" class="strategy-config">
          <div class="form-group">
            <label class="form-label">Cron Expression *</label>
            <input
              v-model="cronExpression"
              class="form-input"
              :class="{ 'form-input--error': errors.cron }"
              placeholder="0 0 * * MON-FRI"
            />
          </div>
        </div>
      </div>

      <div class="form-section" v-if="!isEdit">
        <h3 class="section-title">Owners</h3>
        <div class="form-group">
          <div class="owners-input">
            <input
              v-model="newOwner"
              class="form-input"
              placeholder="Add owner..."
              @keydown.enter.prevent="addOwner"
            />
            <button type="button" class="btn btn--ghost btn--sm" @click="addOwner">
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
      </div>
    </form>

    <template #footer>
      <button type="button" class="btn btn--ghost" @click="close">Cancel</button>
      <button type="button" class="btn btn--primary" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? 'Saving...' : (isEdit ? 'Update Feature' : 'Create Feature') }}
      </button>
    </template>
  </Modal>
</template>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--glass-border);
}

.form-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.section-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group--inline {
  flex-direction: row;
  align-items: center;
  gap: 1rem;
}

.form-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.form-input {
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.875rem;
  font-family: inherit;
  transition: all var(--transition-fast);
  width: 100%;
}

.form-input:focus {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
  outline: none;
}

.form-input--error { border-color: var(--accent-rose); }
.form-input::placeholder { color: var(--text-muted); }
.form-textarea { resize: vertical; min-height: 80px; }
.form-error { font-size: 0.75rem; color: var(--accent-rose); }
.form-hint { font-size: 0.75rem; color: var(--text-muted); }

.readonly-badge {
  display: inline-block;
  padding: 8px 14px;
  background: var(--glass-bg);
  border: 1px dashed var(--glass-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 0.875rem;
  font-weight: 600;
}

.dynamic-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: var(--bg-secondary);
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
}

.dynamic-list__row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.owners-input {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.owners-input .form-input { flex: 1; }

.owner-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.owner-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  font-size: 0.8rem;
}

.owner-chip__remove {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  padding: 2px;
  border-radius: 50%;
  transition: all var(--transition-fast);
}

.owner-chip__remove:hover { color: var(--accent-rose); }

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  border: none;
  background: var(--glass-bg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.icon-btn--danger:hover {
  background: rgba(244, 63, 94, 0.1);
  color: var(--accent-rose);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn--primary {
  background: var(--gradient-accent);
  color: white;
  box-shadow: 0 2px 8px rgba(34, 211, 238, 0.2);
}

.btn--primary:hover {
  box-shadow: 0 4px 16px rgba(34, 211, 238, 0.3);
  transform: translateY(-1px);
}

.btn--primary:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.btn--sm { padding: 6px 14px; font-size: 0.8rem; }
</style>
