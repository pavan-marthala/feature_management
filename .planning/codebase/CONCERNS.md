# Concerns & Areas for Improvement
Date: 2026-04-18

## Technical Debt & Ongoing Work
- **Feature vs Environment Relationship**: The database changelog has a pending migration (`update-table-feature-for-workflow.yaml`) that is currently commented out. This migration attempts to drop the `feature_environment_mapping` table and instead bind a `feature` strictly to one `environment_id`. This implies a transition in how feature rollouts and environments are isolated.
- **Frontend Testing**: There is currently no configured frontend testing framework (like Vitest) or end-to-end framework. All checking is strictly typed/linted (`oxlint`, `eslint`, `vue-tsc`).
- **Feature Evaluation Endpoint**: The `FeatureController` handles CRUD well, but dedicated client SDK evaluation endpoints (e.g. bulk evaluate via SSE or robust GET payload) are either missing or mixed in with management endpoints.

## Scalability Risks
- As the number of connected clients scales, polling for feature configurations may become expensive. Implementing WebSockets or Server-Sent Events (SSE) (which WebFlux excels at) should be prioritized for real-time feature toggling if not already used heavily.

## Security Considerations
- Optimistic locking (ETag) is correctly applied for data consistency, but the SDK should handle conflicts gracefully.
- Authentication/Authorization on the Management APIs (`/features`, `/environments`) needs validation to ensure users cannot maliciously alter features outside their team/role.
