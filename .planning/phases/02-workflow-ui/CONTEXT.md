# Phase 2: Workflow Management UI

## Goal
Build the listing and CRUD views for Workflows, integrating with the `workflowStore` created in Phase 1.

## Objectives
- [ ] Implement `WorkflowsListView.vue` for listing and managing workflows.
- [ ] Implement `WorkflowFormView.vue` for creating and editing workflows.
- [ ] Register new routes in `@/router/index.ts`.
- [ ] Update Sidebar/Navigation to include a link to Workflows.

## Boundaries
- No pipeline visualization in this phase (Phase 3).
- No promotion logic in this phase (Phase 3).
- Focus on the basic management of the Workflow entity itself.

## Key Decisions
- **Reuse Components**: Use `GlassCard`, `Badge`, `SearchInput`, and `LoadingSkeleton` from the existing UI kit.
- **Route Paths**: `/workflows`, `/workflows/create`, `/workflows/:id/edit`.
- **Form Pattern**: Use a similar structure to `FeatureFormView.vue` with a wizard-like feel for adding stages.

## Verification
- Successful navigation to `/workflows`.
- Ability to create a workflow with a name.
- Ability to list workflows with status badges.
