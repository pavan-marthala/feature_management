# Testing
Date: 2026-04-18

## Backend Testing
- **Framework**: JUnit 5, bundled via `spring-boot-starter-test`.
- **Reactive Testing**: Uses `reactor-test` for validating `Flux`/`Mono` streams (e.g., `StepVerifier`).
- **Web Testing**: `spring-boot-starter-webflux-test` allows for testing reactive REST controllers via `WebTestClient`.
- **Data Testing**: `spring-boot-starter-data-r2dbc-test` for validating database queries against a testing database.
- **Coverage**: Jacoco is configured in the `pom.xml` for generating test coverage reports.

## Frontend Testing
- Currently, the `package.json` relies on type checking via `vue-tsc --build` and robust linting (`oxlint`, `eslint`). A dedicated unit testing framework (e.g., Vitest) or e2e framework (e.g., Cypress/Playwright) is not explicitly configured in the initial structure.
