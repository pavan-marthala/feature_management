# Feature Management Platform

A high-performance, reactive Feature Management platform designed for modern microservices and front-end applications. This monorepo contains the core API, SDKs for Java and Vue.js, and example implementations.

## 🚀 Key Features

- **Multi-Strategy Evaluation**: Support for Boolean, User-based, Role-based, Scheduled, and Request-based toggles.
- **Reactive Architecture**: Built with Spring Boot WebFlux and R2DBC for non-blocking I/O.
- **AOP-Powered Java SDK**: Seamlessly toggle features using annotations without cluttering business logic.
- **Modern Dashboard**: Vue.js 3 dashboard for real-time feature control.
- **Optimistic Concurrency**: ETag-based versioning for safe updates.

## 📁 Monorepo Structure

- `server/`: Feature Management API (Spring Boot WebFlux).
- `sdk/`:
  - `java/`: Java Client SDK (AOP + WebFlux).
  - `javascript/`: Vue.js UI & Web Client.
- `examples/`: Example application demonstrating SDK usage.
- `infrastructure/`: Docker & Deployment configurations.
- `docs/`: Extensive documentation system.

## 🛠 Tech Stack

- **Backend**: Java 25, Spring Boot 4.0.5, Project Reactor (WebFlux).
- **Database**: PostgreSQL with R2DBC.
- **Frontend**: Vue.js 3, Vite, Tailwind CSS (Glassmorphism design).
- **Infrastructure**: Docker, Nginx.

## ⚡ Quick Start

### 1. Run via Docker Compose
The easiest way to get everything running is using Docker Compose.
```bash
cd infrastructure
docker-compose up -d
```
- **API**: `http://localhost:8080`
- **Dashboard**: `http://localhost:3000`
- **Example App**: `http://localhost:8081`

### 2. Manual Setup
Refer to the [Getting Started Guide](docs/getting-started/setup.md) for manual installation steps for each component.

## 📚 Documentation

Explore the full documentation in the `/docs` directory:

- [Architecture Overview](docs/architecture/overview.md)
- [API Reference](docs/api/overview.md)
- [Java SDK Guide](docs/sdk/java/usage.md)
- [Vue SDK Guide](docs/sdk/vue/usage.md)
- [Evaluation Strategies](docs/architecture/strategies/boolean.md)

---
*Built with ❤️ for scalable software delivery.*
