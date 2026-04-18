# Integrations
Date: 2026-04-18

## Internal Services
- **Database**: PostgreSQL (connected via reactive R2DBC drivers).
- **Web Server/Reverse Proxy**: Nginx (used to route traffic between the dashboard UI and backend API).

## External Services
- Currently, the core platform relies solely on its internal PostgreSQL database and does not require third-party API integrations for its primary feature management functionality.

## SDK Integrations
- Provides integration points via an AOP-powered Java SDK for Spring Boot clients.
- Provides an integration point via a Javascript SDK for web clients.
