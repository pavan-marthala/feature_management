# Feature Management Platform

A high-performance, reactive Feature Management platform designed for modern microservices and front-end applications. This monorepo contains the core API, SDKs for Java and Vue.js, and example implementations.

## 🚀 Key Features

- **Multi-Strategy Evaluation**: Support for Boolean, User-based, Role-based, Scheduled, and Request-based toggles.
- **Workflow Management**: Define multi-stage propagation pipelines (Dev → Stage → Prod).
- **Feature Propagation**: Promote features across environments with a single click.
- **Reactive Architecture**: Built with Spring Boot WebFlux and R2DBC for non-blocking I/O.
- **AOP-Powered Java SDK**: Seamlessly toggle features using annotations with environment-aware resolution.
- **Modern Dashboard**: Premium glassmorphism UI built with Vue.js 3 and Vite.
- **Audit History**: Full visibility into promotion timelines and status changes.
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
- **Frontend**: Vue.js 3, Vite, Tailwind CSS.
- **Infrastructure**: Docker, Nginx.

## ⚡ Quick Start

### 1. Run via Docker Compose
The easiest way to get everything running is using Docker Compose. The environment configuration is automatically handled via build arguments.
```bash
cd infrastructure
docker-compose up -d --build
```
- **API**: `http://localhost/api`
- **Dashboard**: `http://localhost`
- **Direct API**: `http://localhost:8080`

### 2. Manual Setup
Refer to the [Getting Started Guide](docs/getting-started/setup.md) or the individual module `README.md` files for manual installation. Note: For the Dashboard UI, you must copy `.env.example` to `.env` and set your `VITE_API_BASE_URL`.

## 📚 Documentation

Explore the full documentation in the `/docs` directory:

- [Architecture Overview](docs/architecture/overview.md)
- [API Reference](docs/api/overview.md)
- [Java SDK Guide](docs/sdk/java/usage.md)
- [Dashboard Setup](docs/dashboard/setup.md)

---
*Built with ❤️ for scalable software delivery.*
