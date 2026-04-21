# Plan: Milestone 1 Rework

## Wave 1: API & Service Layer Correction

### Task 1.1: Fix Workflow Service [MODIFY]
- **Files:** `dashboard/src/services/workflowService.ts`
- **Description:** Ensure service methods match OpenAPI spec exactly. Separate workflow CRUD from stage CRUD.
- **Validation:** Service methods align with `/workflows` and `/workflows/{id}/stages` endpoints.

### Task 1.2: Fix Workflow Store [MODIFY]
- **Files:** `dashboard/src/stores/workflowStore.ts`
- **Description:** Update store actions to handle sequential API calls (create workflow -> create stages). Handle optimistic updates correctly.
- **Validation:** Store actions trigger correct service calls in the right order.

## Wave 2: Feature Creation UX

### Task 2.1: Fix Inline Environment Creation [MODIFY]
- **Files:** `dashboard/src/views/FeatureFormView.vue`
- **Description:** If environment list is empty, show "No environments found" and a clear "Create Environment" button. Implement the flow where creating an environment returns the user to the feature form with the new environment selected.
- **Validation:** Smooth flow from empty state to created feature.

## Wave 3: Premium UI Rebuild

### Task 3.1: Rebuild Workflow Pipeline View [MODIFY]
- **Files:** `dashboard/src/components/features/PipelineBoard.vue`
- **Description:** Redesign the pipeline board to be the primary interaction model. Premium design with `[ Dev ] → [ Stage ] → [ Prod ]` layout.
- **Validation:** High-quality visual representation of the workflow.

### Task 3.2: Rebuild Stage Cards [MODIFY]
- **Files:** `dashboard/src/views/WorkflowFormView.vue`
- **Description:** Replace basic CRUD cards with premium production-quality stage components. Include environment, status, actions, and metadata with smooth transitions.
- **Validation:** Stage management feels premium and responsive.

### Task 3.3: Rebuild Audit Timeline [MODIFY]
- **Files:** `dashboard/src/views/FeatureDetailView.vue`, `dashboard/src/components/ui/SideDrawer.vue`
- **Description:** Redesign the Timeline Details panel. Show audit events, user actions, and timestamps with a modern enterprise SaaS aesthetic.
- **Validation:** Timeline is informative and visually stunning.

## Verification Plan
- Manual audit of all flows.
- Ensure strict adherence to `open-api.yml`.
- Cross-reference UI patterns with Features/Environments.
