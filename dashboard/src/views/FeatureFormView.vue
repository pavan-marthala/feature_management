<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFeatureStore } from '@/stores/featureStore'
import { useUiStore } from '@/stores/uiStore'
import GlassCard from '@/components/ui/GlassCard.vue'
import ToggleSwitch from '@/components/ui/ToggleSwitch.vue'
import { ArrowLeft, Plus, Minus, Save, X } from 'lucide-vue-next'
import type { FeatureStrategyType, FeatureCreateRequest, FeatureConfiguration } from '@/types'

const route = useRoute()
const router = useRouter()
const featureStore = useFeatureStore()
const uiStore = useUiStore()

const isEdit = computed(() => route.name === 'feature-edit')
const featureId = computed(() => route.params.id as string)

// Form state
const name = ref('')
const description = ref('')
const strategy = ref<FeatureStrategyType>('BooleanFeatureStrategy')
const enabled = ref(true)
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

onMounted(async () => {
  await featureStore.fetchStrategies()

  if (isEdit.value && featureId.value) {
    const feature = await featureStore.fetchFeature(featureId.value, 'ID')
    if (feature) {
      name.value = feature.name
      description.value = feature.description || ''
      enabled.value = feature.enabled
      owners.value = feature.owners || []

      if (feature.configuration) {
        strategy.value = feature.configuration.strategy
        switch (feature.configuration.strategy) {
          case 'BooleanFeatureStrategy':
            booleanValue.value = (feature.configuration as any).value
            break
          case 'JWTClaimFeatureStrategy': {
            const claims = (feature.configuration as any).claims
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
            const config = feature.configuration as any
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
            cronExpression.value = (feature.configuration as any).cron || ''
            break
        }
      }
    }
  }
})

function validate(): boolean {
  errors.value = {}

  if (!name.value || name.value.length < 2 || name.value.length > 36) {
    errors.value.name = 'Name must be 2-36 characters'
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

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true

  try {
    if (isEdit.value) {
      // Send only the edited configuration, not the full object
      const data: Record<string, unknown> = buildConfiguration()
      
      await featureStore.updateFeature(featureId.value, data, featureStore.selectedEtag)
      router.push(`/features/${featureId.value}`)
    } else {
      const payload: FeatureCreateRequest = {
        name: name.value,
        description: description.value || undefined,
        configuration: buildConfiguration(),
        owners: owners.value.length ? owners.value : undefined,
        enabled: enabled.value,
      }
      const result = await featureStore.createFeature(payload)
      // Navigate using the returned id if available, otherwise fall back to list
      if (result?.id) {
        router.push(`/features/${result.id}`)
      } else {
        router.push('/features')
      }
    }
  } catch {
    // Error toast already handled in store
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
  <div class="form-page">
    <button class="back-btn" @click="router.back()">
      <ArrowLeft :size="18" />
      Back
    </button>

    <div class="form-page__header animate-fadeInUp">
      <h1>{{ isEdit ? 'Edit Feature' : 'Create Feature' }}</h1>
      <p class="form-page__subtitle">
        {{ isEdit ? 'Update the feature configuration' : 'Set up a new feature flag with your preferred strategy' }}
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="form animate-fadeInUp stagger-1">
      <!-- Basic Info -->
      <GlassCard>
        <h2 class="section-title">Basic Information</h2>

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
          <span class="form-hint">Alphanumeric, 2-36 characters</span>
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
          <span v-if="errors.description" class="form-error">{{ errors.description }}</span>
        </div>

        <div class="form-group form-group--inline" v-if="!isEdit">
          <label class="form-label">Enabled</label>
          <ToggleSwitch v-model="enabled" size="md" />
        </div>
      </GlassCard>

      <!-- Strategy Selection -->
      <GlassCard class="animate-fadeInUp stagger-2">
        <h2 class="section-title">Strategy Configuration</h2>

        <div class="form-group">
          <label class="form-label" for="strategy-select">Strategy Type *</label>
          <select id="strategy-select" v-model="strategy" class="form-input form-select">
            <option value="BooleanFeatureStrategy">Boolean Toggle</option>
            <option value="JWTClaimFeatureStrategy">JWT Claim</option>
            <option value="HTTPRequestFeatureStrategy">HTTP Request</option>
            <option value="ScheduleFeatureStrategy">Schedule (Cron)</option>
          </select>
        </div>

        <!-- Boolean Strategy Config -->
        <div v-if="strategy === 'BooleanFeatureStrategy'" class="strategy-config">
          <div class="form-group form-group--inline">
            <label class="form-label">Default Value</label>
            <ToggleSwitch v-model="booleanValue" size="md" />
            <span class="form-hint">{{ booleanValue ? 'True' : 'False' }}</span>
          </div>
        </div>

        <!-- JWT Claim Strategy Config -->
        <div v-else-if="strategy === 'JWTClaimFeatureStrategy'" class="strategy-config">
          <div class="form-group">
            <label class="form-label">Claim Type</label>
            <select v-model="jwtClaimType" class="form-input form-select">
              <option value="scopes">Scopes</option>
              <option value="roles">Roles</option>
              <option value="custom">Custom Claims</option>
            </select>
          </div>

          <!-- Scopes -->
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

          <!-- Roles -->
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

          <!-- Custom Claims -->
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

        <!-- HTTP Request Strategy Config -->
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
            <span v-if="errors.httpName" class="form-error">{{ errors.httpName }}</span>
          </div>

          <div class="form-group" v-if="httpType === 'requestBody'">
            <label class="form-label">JSON Path *</label>
            <input
              v-model="httpPath"
              class="form-input"
              :class="{ 'form-input--error': errors.httpPath }"
              placeholder="$.user.role"
            />
            <span v-if="errors.httpPath" class="form-error">{{ errors.httpPath }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Value *</label>
            <input
              v-model="httpValue"
              class="form-input"
              :class="{ 'form-input--error': errors.httpValue }"
              placeholder="Expected value"
            />
            <span v-if="errors.httpValue" class="form-error">{{ errors.httpValue }}</span>
          </div>
        </div>

        <!-- Schedule Strategy Config -->
        <div v-else-if="strategy === 'ScheduleFeatureStrategy'" class="strategy-config">
          <div class="form-group">
            <label class="form-label">Cron Expression *</label>
            <input
              v-model="cronExpression"
              class="form-input"
              :class="{ 'form-input--error': errors.cron }"
              placeholder="0 0 * * MON-FRI"
            />
            <span v-if="errors.cron" class="form-error">{{ errors.cron }}</span>
            <span class="form-hint">Standard cron syntax (minute hour day month weekday)</span>
          </div>
        </div>
      </GlassCard>

      <!-- Owners (Create Only) -->
      <GlassCard class="animate-fadeInUp stagger-3" v-if="!isEdit">
        <h2 class="section-title">Owners</h2>
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
      </GlassCard>

      <!-- Submit -->
      <div class="form-actions animate-fadeInUp stagger-4">
        <button type="button" class="btn btn--ghost" @click="router.back()">Cancel</button>
        <button type="submit" class="btn btn--primary" :disabled="submitting">
          <Save :size="18" />
          {{ submitting ? 'Saving...' : (isEdit ? 'Update Feature' : 'Create Feature') }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 720px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-family: inherit;
  cursor: pointer;
  padding: 6px 0;
  transition: color var(--transition-fast);
  width: fit-content;
}

.back-btn:hover { color: var(--accent-cyan); }

.form-page__header h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.form-page__subtitle {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-top: 4px;
}

/* Form */
.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 1rem;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group--inline {
  flex-direction: row;
  align-items: center;
  gap: 12px;
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
}

.form-input:focus {
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(34, 211, 238, 0.1);
  outline: none;
}

.form-input--error {
  border-color: var(--accent-rose);
}

.form-input--error:focus {
  box-shadow: 0 0 0 3px rgba(251, 113, 133, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
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

.form-error {
  font-size: 0.75rem;
  color: var(--accent-rose);
}

.form-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Strategy Config */
.strategy-config {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--glass-border);
}

/* Dynamic List */
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

.dynamic-list__row .form-input {
  flex: 1;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--glass-border);
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.icon-btn--danger:hover {
  color: var(--accent-rose);
  border-color: var(--accent-rose);
  background: rgba(251, 113, 133, 0.1);
}

/* Owners */
.owners-input {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.owners-input .form-input {
  flex: 1;
}

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

.owner-chip__remove:hover {
  color: var(--accent-rose);
}

/* Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 0.5rem;
}

/* Buttons */
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
  white-space: nowrap;
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

.btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn--ghost {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
}

.btn--ghost:hover {
  background: var(--glass-bg-hover);
  color: var(--text-primary);
}

.btn--sm {
  padding: 6px 14px;
  font-size: 0.8rem;
}
</style>
