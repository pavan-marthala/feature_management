# Roadmap

## Milestone 1: Workflow UI and UX Polish

### Phase 1: Infrastructure & State
**Goal:** Prepare frontend types, services, and stores for Workflows.
- **Tasks:**
  - Define TypeScript types for Workflow, Stage, and Propagation.
  - Implement `workflowService.ts` to interact with backend APIs.
  - Create `workflowStore.ts` (Pinia) for state management.
- **Success Criteria:** Store can fetch and manage workflow state.
- **Depends on:** Backend APIs (Implemented).

### Phase 2: Workflow Management UI
**Goal:** Implement CRUD views for Workflows.
- **Tasks:**
  - Build `WorkflowsView.vue` (Listing).
  - Build `WorkflowFormView.vue` (Create/Edit).
  - Integrate with `workflowStore`.
- **Success Criteria:** User can list, create, and update workflows.
- **Depends on:** Phase 1.

### Phase 3: Pipeline View & Promotion
**Goal:** Visualize the promotion pipeline and enable interactions.
- **Tasks:**
  - Create `PipelineBoard.vue` component.
  - Implement promotion trigger dialog/drawer.
  - Add `PromotionTimeline.vue` drawer for history.
- **Success Criteria:** Visual representation of stages with promote buttons.
- **Depends on:** Phase 2.

### Phase 4: Feature Creation UX
**Goal:** Improve feature creation flow with inline environment creation.
- **Tasks:**
  - Modify `FeatureFormView.vue` to detect empty environments.
  - Implement inline `EnvironmentForm` modal/section.
  - Auto-select newly created environment.
- **Success Criteria:** "Create Environment" button appears and works within the feature form.
- **Depends on:** Existing Environments module.

### Phase 5: Verification & Polish
**Goal:** Ensure consistency and quality.
- **Tasks:**
  - Audit UI against glassmorphism design system.
  - Verify error handling and loading states.
  - Conduct E2E verification of the promotion flow.
- **Success Criteria:** BUILD SUCCESS and all REQs satisfied.
- **Depends on:** Phase 3, Phase 4.

---

## Backlog
(Empty)
