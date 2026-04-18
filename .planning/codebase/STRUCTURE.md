# Directory Structure
Date: 2026-04-18

## High-Level Layout
```
/
├── dashboard/        # Frontend Vue.js SPA
├── docs/             # Extensive project documentation
├── examples/         # Example apps demonstrating SDK usage
├── infrastructure/   # Docker compose & Nginx configuration
├── sdk/              # Client SDKs
│   ├── java/
│   └── javascript/
└── server/           # Spring Boot WebFlux API
```

## Server Structure
```
/server/src/main/java/org/feature/management/
├── config/           # App configuration and beans
├── dashboard/        # Dashboard stats controllers & services
├── environment/      # Environment REST API and business logic
├── feature/          # Feature toggle REST API and business logic
├── shared/           # Cross-cutting concerns (e.g. exceptions)
└── models/           # Generated models from OpenAPI spec (via maven plugin)

/server/src/main/resources/
├── application.yaml  # Spring properties
└── db/changelog/     # Liquibase migrations for Postgres schema
```

## Dashboard Structure
```
/dashboard/
├── src/              # Vue components, Pinia stores, router configuration
├── public/           # Static assets
├── vite.config.ts    # Vite configuration
└── package.json      # NPM scripts and dependencies
```
