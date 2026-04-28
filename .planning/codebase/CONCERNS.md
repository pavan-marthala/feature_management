# Concerns & Areas for Improvement
Date: 2026-04-28

## Technical Debt & Ongoing Work
- **Feature vs Environment Relationship**: There are indications of schema changes to handle relationships between features, environments, and workflows more effectively.
- **Frontend Testing**: There is currently no configured frontend testing framework (like Vitest) or end-to-end framework. All checking is strictly typed/linted (`oxlint`, `eslint`, `vue-tsc`).
- **Feature Evaluation Endpoint**: Dedicated client SDK evaluation endpoints (e.g., bulk evaluate via SSE or robust GET payload) are mixed in with management endpoints or need further separation.

## Scalability Risks
- As the number of connected clients scales, polling for feature configurations may become expensive. Implementing WebSockets or Server-Sent Events (SSE) (which WebFlux excels at) should be prioritized for real-time feature toggling if not already used heavily.

## Security Considerations
- Optimistic locking (ETag) is correctly applied for data consistency, but the SDK should handle conflicts gracefully.
- Authentication/Authorization on the Management APIs (`/features`, `/environments`) needs validation to ensure users cannot maliciously alter features outside their team/role.
