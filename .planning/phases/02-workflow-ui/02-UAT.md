---
status: testing
phase: 02-workflow-ui
source: [02-01-SUMMARY.md]
started: $(date -Iseconds)
updated: $(date -Iseconds)
---

## Current Test
<!-- OVERWRITE each test - shows where we are -->

number: 1
name: Workflows Navigation
expected: |
  Sidebar contains a 'Workflows' item with a GitBranch icon. Clicking it navigates to the workflows listing page.
awaiting: user response

## Tests

### 1. Workflows Navigation
expected: Sidebar contains a 'Workflows' item with a GitBranch icon. Clicking it navigates to the workflows listing page.
result: [pending]

### 2. Workflow Listing View
expected: Workflows list displays workflows with status badges (Draft, Active, Archived) and version tracking. Layout responds to screen size (Table/Card views).
result: [pending]

### 3. Create Workflow Flow
expected: Clicking create opens a form with dynamic stage management. Users can add/remove stages mapped to existing environments and save the workflow as Draft or Active.
result: [pending]

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0

## Gaps

