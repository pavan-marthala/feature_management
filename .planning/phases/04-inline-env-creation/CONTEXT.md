# Phase 4: Inline Environment Creation UX

## Goal
Improve the Feature Creation experience by allowing users to create an environment inline if they don't have one yet.

## Objectives
- [ ] Implement an "Inline Creator" mode in `FeatureFormView.vue`.
- [ ] Allow switching between "Select Existing" and "Create New" environment.
- [ ] Ensure the created environment is automatically selected for the new feature.
- [ ] Maintain consistent styling with the existing glassmorphism design.

## Boundaries
- No changes to the main Environment management views.
- Focus exclusively on the feature creation wizard.

## Key Decisions
- **UI Flow**: If no environments are found, default to the "Create New" inline form.
- **Auto-selection**: Once an environment is created inline, it becomes the selected `envId` for the feature form.
- **Validation**: Inline environment creation must be successful before the feature can be created.

## Verification
- Feature form detects empty environment list.
- Inline form appears and allows entering environment details.
- Successful environment creation updates the feature form's selection.
- Feature creation succeeds with the newly created environment.
