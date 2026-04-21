/* ===== Pagination ===== */
export interface Pagination {
  page: number
  size: number
  totalItems: number
  totalPages: number
}

/* ===== Error Types ===== */
export interface ErrorDetails {
  errorCode?: string
  field: string
  value?: string
  message: string
}

export interface ApiError {
  httpMethod: 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'GET'
  requestUri: string
  statusCode: number
  errorMessage?: string
  correlationIdentifier: string
  errorTimestamp: string
  detailedErrors?: ErrorDetails[]
}

/* ===== Strategy Types ===== */
export type FeatureStrategyType =
  | 'BooleanFeatureStrategy'
  | 'JWTClaimFeatureStrategy'
  | 'HTTPRequestFeatureStrategy'
  | 'ScheduleFeatureStrategy'

export interface BooleanFeatureStrategy {
  strategy: 'BooleanFeatureStrategy'
  value: boolean
}

export interface JWTClaimScope {
  scopes: string[]
}

export interface JWTClaimRole {
  roles: string[]
}

export interface JWTClaimCustom {
  name: string
  value: string
}

export interface JWTClaimFeatureStrategy {
  strategy: 'JWTClaimFeatureStrategy'
  claims: Array<JWTClaimScope | JWTClaimRole | JWTClaimCustom[]>
}

export interface HTTPHeaderConfig {
  name: string
  value: string
}

export interface HTTPBodyConfig {
  path: string
  value: string
}

export interface HTTPQueryConfig {
  name: string
  value: string
}

export interface HTTPRequestFeatureStrategy {
  strategy: 'HTTPRequestFeatureStrategy'
  header?: HTTPHeaderConfig
  requestBody?: HTTPBodyConfig
  query?: HTTPQueryConfig
}

export interface ScheduleFeatureStrategy {
  strategy: 'ScheduleFeatureStrategy'
  cron: string
}

export type FeatureConfiguration =
  | BooleanFeatureStrategy
  | JWTClaimFeatureStrategy
  | HTTPRequestFeatureStrategy
  | ScheduleFeatureStrategy

/* ===== Feature ===== */
export interface Feature {
  id: string
  name: string
  description?: string
  configuration: FeatureConfiguration
  owners?: string[]
  enabled: boolean
  envId: string
  etag: number
}

export interface FeatureCreateRequest {
  name: string
  description?: string
  envId: string
  configuration: FeatureConfiguration
  owners?: string[]
  enabled?: boolean
}

export interface FeatureUpdateRequest {
  value?: boolean
  strategy?: string
  [key: string]: unknown
}

export interface FeatureResponse extends Pagination {
  items: Feature[]
}

export interface StrategyInfo {
  name: FeatureStrategyType
  description: string
}

/* ===== Environment ===== */
export interface Environment {
  id: string
  name: string
  description?: string
  owners?: string[]
  etag: number
}

export interface EnvironmentRequest {
  name?: string
  description?: string
  owners?: string[]
}

export interface EnvironmentResponse extends Pagination {
  items: Environment[]
}

/* ===== Dashboard ===== */
export interface DashboardStats {
  totalFeatures: number
  activeFeatures: number
  disabledFeatures: number
  totalEnvironments: number
}

/* ===== UI Types ===== */
export type ToastType = 'success' | 'error' | 'info' | 'warning'

export interface Toast {
  id: string
  message: string
  type: ToastType
  duration: number
}

export type IdType = 'ID' | 'NAME'


/* ===== Workflow Types ===== */
export type WorkflowStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'

export interface WorkflowBase {
  id?: string
  name: string
  status?: WorkflowStatus
  version?: number
}

export interface Stage {
  id?: string
  environmentId: string
  environmentName?: string
  orderIndex: number
  type: StageType
  nextStageId?: string
  scheduleExpression?: string
  version?: number
  approvalNeeded?: boolean
}

export type StageType = 'MANUAL' | 'AUTOMATIC' | 'SCHEDULED'

export interface Workflow extends WorkflowBase {
  stages: Stage[]
}

export interface WorkflowResponse extends Pagination {
  items: WorkflowBase[]
}

export interface WorkflowCreateRequest {
  name: string
  status: WorkflowStatus
}

export interface WorkflowUpdateRequest {
  name: string
  status: WorkflowStatus
}

export interface StageRequest {
  environmentId: string
  orderIndex: number
  type: StageType
  scheduleExpression?: string
}

/* ===== Propagation & Promotion ===== */
export type PromotionStatus = 'SUCCESS' | 'PENDING' | 'FAILED'

export interface FeaturePromotionRequest {
  workflowId?: string
  targetEnvironmentId?: string
  scheduledAt?: string
}

export interface FeaturePromotionResponse {
  id: string
  status: PromotionStatus
}

export interface PropagationHistory {
  id: string
  featureId: string
  sourceEnvironmentId: string
  targetEnvironmentId: string
  promotedBy: string
  status: PromotionStatus
  createdAt: string
  completedAt?: string
}
