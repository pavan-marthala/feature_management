# Phase 1: Infrastructure & State

## Goal
Prepare frontend types, services, and stores for Workflows and Feature Propagation.

## Objectives
- [ ] Define TypeScript types in `@/types/index.ts` for:
  - Workflow, WorkflowStatus, Stage, StageType
  - FeaturePromotionRequest, FeaturePromotionResponse, PropagationHistory
- [ ] Implement `@/services/workflowService.ts` for all workflow CRUD operations.
- [ ] Implement `@/stores/workflowStore.ts` using Pinia for state management.
- [ ] Update `@/services/featureService.ts` to include propagation and history endpoints.

## Boundaries
- No UI components will be built in this phase.
- No changes to existing Feature/Environment stores unless necessary for integration.

## Key Decisions
- **Consistency**: Follow the existing pattern of using Axios-based `api.ts` and Pinia stores with `ref`/`computed`/`async function`.
- **Error Handling**: Use the `useUiStore` toast system for error reporting.

## Verification
- Successful compilation of TypeScript files.
- Manual verification of service methods (can be done in later phases when UI is integrated).
