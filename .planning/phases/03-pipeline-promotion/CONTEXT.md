# Phase 3: Pipeline Visualization & Promotion

## Goal
Implement the interactive Pipeline View and Promotion logic to allow features to be propagated across environments.

## Objectives
- [ ] Implement `SideDrawer.vue` for the audit timeline.
- [ ] Implement `PipelineBoard.vue` for visual stage representation.
- [ ] Update `featureStore.ts` with propagation and history actions.
- [ ] Integrate `PipelineBoard` into `FeatureDetailView.vue`.
- [ ] Build the "Timeline Audit" drawer content.

## Boundaries
- No changes to Workflow/Environment management views.
- Focus on the Feature Detail experience.

## Key Decisions
- **Visualization**: Use a step-based horizontal layout for the pipeline.
- **Promotion Flow**: A single "Promote" button that triggers the API call and refreshes the pipeline status.
- **Audit Logs**: Display a chronological list of promotion events in the side drawer.

## Verification
- Pipeline correctly displays stages for the feature's environment.
- "Promote" button triggers a successful API call.
- Timeline drawer correctly displays past promotions.
